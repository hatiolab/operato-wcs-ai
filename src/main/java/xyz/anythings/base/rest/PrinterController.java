package xyz.anythings.base.rest;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import xyz.anythings.base.entity.Printer;
import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.anythings.sys.model.BaseResponse;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dev.entity.DiyTemplate;
import xyz.elidom.dev.rest.DiyTemplateController;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/printers")
@ServiceDesc(description = "Printer Service API")
public class PrinterController extends AbstractRestService {

	/**
	 * Event Publisher
	 */
	@Autowired
	protected EventPublisher eventPublisher;
	/**
	 * DiyTemplate Controller
	 */
	@Autowired
	private DiyTemplateController templateCtrl;
	
	@Override
	protected Class<?> entityClass() {
		return Printer.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public Printer findOne(@PathVariable("id") String id) {
		return this.getOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public Printer create(@RequestBody Printer input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Printer update(@PathVariable("id") String id, @RequestBody Printer input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Printer> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}
	
	/**
	 * 라벨 인쇄 요청 처리
	 * 
	 * @param printEvent - printType : barcode (바코드) / normal (일반), printerId : 프린터 ID, printTemplate : 인쇄할 커스텀 템플릿 명, templateParams : 인쇄 템플릿을 실행시킬 파라미터
	 * @return
	 */
	@RequestMapping(value = "/print_label", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Print Label")
	public BaseResponse printLabel(@RequestBody PrintEvent printEvent) {
		
		// 인쇄 이벤트를 퍼블리시한다. anythings-printing 모듈을 import하면 그 쪽에서 처리할 수 있고 그렇지 않으면 PrintEvent를 받아서 자체 처리한다.
		this.eventPublisher.publishEvent(printEvent);
		return new BaseResponse(true);
	}
	
	/**
	 * 라벨 템플릿으로 라벨 인쇄 
	 * 
	 * @param printAgentUrl
	 * @param printerName
	 * @param templateName
	 * @param labelData
	 */
	public void printLabelByLabelTemplate(String printAgentUrl, String printerName, String templateName, Map<String, Object> labelData) {
		// 변수를 넣어서 템플릿 엔진을 돌리고 커맨드를 생성 
		DiyTemplate template = this.templateCtrl.dynamicTemplate(templateName, labelData);
		String command = template.getTemplate();
		
		// 송장 라벨 인쇄
		this.printLabelByLabelCommand(printAgentUrl, printerName, command);
	}
	
	/**
	 * 라벨 command으로 라벨 인쇄, 라벨코맨드를 직접 제공하는 고객을 위한 API
	 * 
	 * @param printAgentUrl
	 * @param printerName
	 * @param command
	 */
	public void printLabelByLabelCommand(String printAgentUrl, String printerName, String command) {
		if(ValueUtil.isNotEmpty(printerName)) {
			RestTemplate rest = new RestTemplate();
			rest.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName(SysConstants.CHAR_SET_UTF8)));
			printAgentUrl = printAgentUrl + "/barcode?printer=" + printerName;
			rest.postForEntity(printAgentUrl, command, Boolean.class);
		}
	}
	
	/**
	 * 라벨 command으로 라벨 인쇄, 라벨코맨드를 직접 제공하는 고객을 위한 API
	 * 
	 * @param printAgentUrl
	 * @param printerName
	 * @param labelKey
	 * @param baseCommand
	 */
	public void printLabelImageByGetImage(String printAgentUrl, String printerName, String labelKey, String baseCommand) {
		if(ValueUtil.isNotEmpty(printerName)) {
			RestTemplate rest = new RestTemplate();
			rest.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName(SysConstants.CHAR_SET_UTF8)));
			printAgentUrl = printAgentUrl + "/barcode/by_get_image?printer=" + printerName + "&label_key=" + labelKey;
			rest.postForEntity(printAgentUrl, baseCommand, Boolean.class);
		}
	}

}