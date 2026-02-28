/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.dev.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.elidom.core.entity.Attachment;
import xyz.elidom.core.util.AttachmentUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dev.entity.DiyTemplate;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.engine.IScriptEngine;
import xyz.elidom.sys.system.engine.ITemplateEngine;
import xyz.elidom.sys.system.engine.TemplateEngineManager;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.system.service.params.BasicOutput;
import xyz.elidom.sys.util.AssertUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/diy_templates")
@ServiceDesc(description = "Dynamic Template Service API")
public class DiyTemplateController extends AbstractRestService {

	@Autowired
	private TemplateEngineManager templateEngineMgr;
	
	@Autowired
	@Qualifier("basic")
    private ITemplateEngine templateEngine;
	
	@Autowired
	@Qualifier("excel")
    private ITemplateEngine xlsTemplateEngine;
		
	@Override
	protected Class<?> entityClass() {
		return DiyTemplate.class;
	}
	
	@RequestMapping(method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Dynamic Templates (Pagination) By Search Conditions")	
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit, 
			@RequestParam(name = "select", required = false) String select, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Dynamic Template By ID")
	public DiyTemplate findOne(@PathVariable("id") String id, @RequestParam(name = "name", required = false) String name) {
		boolean byName = SysConstants.SHOW_BY_NAME_METHOD.equalsIgnoreCase(id);
		return (byName ? this.findByName(name) : this.getOne(true, this.entityClass(), id));
	}
	
	@RequestMapping(value="/{id}/exist", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Check if Dynamic Templates exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.getClass(), id);
	}
	
	@RequestMapping(value = "/check_import", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check Before Import")
	public List<DiyTemplate> checkImport(@RequestBody List<DiyTemplate> list) {
		for (DiyTemplate item : list) {
			this.checkForImport(DiyTemplate.class, item);
		}
		
		return list;
	}
	
	@RequestMapping(method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description="Create Dynamic Template")
	public DiyTemplate create(@RequestBody DiyTemplate diyTemlate) {
		return this.createOne(diyTemlate);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.PUT, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update Dynamic Template")
	public DiyTemplate update(@PathVariable("id") String id, @RequestBody DiyTemplate diyTemlate) {
		return this.updateOne(diyTemlate);
	}

	@RequestMapping(value="/{id}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete Dynamic Template By ID")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.getClass(), id);
	}
	
	@RequestMapping(value="/update_multiple", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple Dynamic Templates at one time")
	public Boolean multipleUpdate(@RequestBody List<DiyTemplate> diyServiceList) {
		return this.cudMultipleData(this.entityClass(), diyServiceList);
	}
	
	@SuppressWarnings("unchecked")
	@ApiDesc(description = "Execute Dynamic Logic by Id (or Name) and return data")
	@RequestMapping(value = "/{id}/query", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Object query(@PathVariable("id") String id, 
			@RequestParam(name = "input", required = false) String input,
			@RequestParam(name = "query", required = false) String query) {
		Map<String, Object> inputMap = new HashMap<String, Object>();
		if(input != null)
			inputMap = (Map<String, Object>)super.getJsonParser().parse(input, Map.class);
		
		if(query != null)
			inputMap.putAll(ValueUtil.queryToParamMap(query));
		
		DiyTemplate template = null;
		try {
			template = this.getOne(true, this.entityClass(), id);
		} catch (NumberFormatException e) {
			template = (DiyTemplate)this.selectByCondition(true, this.entityClass(), new DiyTemplate(Domain.currentDomain().getId(), id));
		}
		
		AssertUtil.assertNotEmpty("terms.label.logic", template.getLogic());
		IScriptEngine scriptEngine = BeanUtil.get(IScriptEngine.class);
		return scriptEngine.runScript("groovy", template.getLogic(), inputMap);
	}
	
	@SuppressWarnings("unchecked")
	private String executeDynamicTemplate(DiyTemplate template, Map<String, Object> variables) {
		AssertUtil.assertNotEmpty("terms.label.logic", template.getLogic());
		IScriptEngine scriptEngine = BeanUtil.get(IScriptEngine.class);
		Object logicOutput = scriptEngine.runScript("groovy", template.getLogic(), variables);
		@SuppressWarnings("rawtypes")
		Map templateData = (logicOutput != null && logicOutput instanceof Map) ? (Map)logicOutput : variables;
		StringWriter writer = new StringWriter();
		this.templateEngine.processTemplate(template.getTemplate(), writer, templateData, null);
		return writer.toString();
	}
	
	@RequestMapping(value="/{id}/dynamic_template", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Get Content After Processing Template")
	public DiyTemplate dynamicTemplate(@PathVariable("id") String id, @RequestBody Map<String, Object> variables) {
		DiyTemplate template = this.getOne(false, this.entityClass(), id);
		
		if(template == null) {
			template = this.findByName(id);
		}
		
		template.setTemplate(this.executeDynamicTemplate(template, variables));
		return template;
	}
	
	@RequestMapping(value="/{name}/template/basic", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Get Content After Processing Template")
	public String content(@PathVariable("name") String name, @RequestBody Map<String, Object> variables) {
		DiyTemplate template = this.findByName(name);
		StringWriter writer = new StringWriter();
		this.templateEngine.processTemplate(template.getTemplate(), writer, variables, null);
		return writer.toString();
	}
		
	@RequestMapping(value="/{name}/template/excel", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Download Excel After Processing Excel Template")
	public BasicOutput downloadExcel(HttpServletRequest req, HttpServletResponse res, @PathVariable("name") String name, @RequestBody Map<String, Object> variables) {
		String filePath = this.getTemplateFilePath(name, ".xlsx");
		
		try {
			this.xlsTemplateEngine.processTemplateByFile(filePath, res.getOutputStream(), variables, null);
			this.setResponseHeaderForDownload(res, name + ".xlsx");
		} catch (IOException e) {
			throw ThrowUtil.newFailToProcessTemplate("Excel", e);
		}
		
		return new BasicOutput();
	}
	
	@RequestMapping(value="/{name}/template/pdf", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Download PDF After Processing Jasper Template")
	public BasicOutput downloadPdf(HttpServletRequest req, HttpServletResponse res, @PathVariable("name") String name, @RequestParam Map<String, Object> variables) {
		ITemplateEngine pdfTemplateEngine = this.templateEngineMgr.getTemplateEngine("pdf");
		if(pdfTemplateEngine == null) {
			// 템플릿 엔진에 등록되어 있지 않으면 예외 발생 
			throw ThrowUtil.newNotSupportedMethodYet();
		}
		
		String filePath = this.getTemplateFilePath(name, ".jasper");
		try {
			pdfTemplateEngine.processTemplate(filePath, res.getOutputStream(), variables, null);
			this.setResponseHeaderForDownload(res, name + ".pdf");
		} catch (IOException e) {
			throw ThrowUtil.newFailToProcessTemplate("PDF", e);
		}
		
		return new BasicOutput();
	}
	
	/**
	 * templateName으로 Template을 찾은 후 첨부된 Attachment를 찾아 템플릿 파일의 실제 Full Path를 찾아 리턴  
	 * 
	 * @param templateName
	 * @param checkExtension
	 * @return
	 */
	@ApiDesc(description="Get full file path")
	private String getTemplateFilePath(String templateName, String checkExtension) {
		DiyTemplate template = this.findByName(templateName);
		Attachment attachment = this.findAttachment(template.getId()).get(0);
		String filePath = AttachmentUtil.getAttachmentFileFullPath(attachment);
		
		if(!filePath.endsWith(checkExtension)) {
			filePath += checkExtension;
		}
		
		return filePath;
	}
	
	/**
	 * Response Header For Download
	 * 
	 * @param res
	 */
	@ApiDesc(description="Response Header for download")
	private void setResponseHeaderForDownload(HttpServletResponse res, String fileName) throws UnsupportedEncodingException {
		res.setCharacterEncoding("UTF-8");
		res.setContentType("text/plain;charset=UTF-8");
		res.addHeader("Content-Type", "application/octet-stream");
		res.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
		res.addHeader("Content-Transfer-Encoding", "binary;");
		res.addHeader("Pragma", "no-cache;");
		res.addHeader("Expires", "-1;");		
	}
	
	/**
	 * find by name
	 * 
	 * @param name
	 * @return
	 */
	@ApiDesc(description="Find Diy Template by Name")
	private DiyTemplate findByName(String name) {
		/**
		 * Validation
		 */
		AssertUtil.assertNotEmpty(SysConstants.TERM_LABEL_NAME, name);
		return this.selectByCondition(true, DiyTemplate.class, new DiyTemplate(Domain.currentDomainId(), name));
	}
	
	/**
	 * DiyTemplate에 첨부한 attachment를 조회
	 * 
	 * @param templateId
	 * @return
	 */
	@ApiDesc(description="Find Attachment from Diy Template")
	private List<Attachment> findAttachment(String templateId) {
		Attachment conds = new Attachment();
		conds.setOnType(this.entityClass().getSimpleName());
		conds.setOnId(templateId);
		return this.queryManager.selectList(true, Attachment.class, conds);
	}
}