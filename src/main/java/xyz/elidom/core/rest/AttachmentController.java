/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.core.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.elidom.core.entity.Attachment;
import xyz.elidom.core.entity.Storage;
import xyz.elidom.core.system.controller.handler.file.download.IPathBasedDownloadHandler;
import xyz.elidom.core.util.AttachmentUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Order;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.system.service.params.BasicOutput;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/attachments")
@ServiceDesc(description="Attachment Service API")
public class AttachmentController extends AbstractRestService {

	@Autowired
	private IPathBasedDownloadHandler fileDownloadHandler;
	
	/**
	 * 기본 소트 조건 - '[{\"field\": \"name\", \"ascending\": true}]'
	 */
	private static String DEFAULT_SORT = "[{\"field\": \"name\", \"ascending\": true}]";
	/**
	 * 스토리지 검색 쿼리 - 'select id from storage_infos where domain_id = :domainId and category = :category'
	 */
	private static String STORAGE_SQL = "select id from storage_infos where domain_id = :domainId and category = :category";
	
	@Override
	protected Class<?> entityClass() {
		return Attachment.class;
	}	
	
	/**
	 * pagination 검색
	 * 
	 * @param page
	 * @param limit
	 * @param select
	 * @param sort
	 * @param query
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Attachments (Pagination) by Search Conditions")
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit, 
			@RequestParam(name = "select", required = false) String select, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
		if(ValueUtil.isEmpty(sort)) {
			sort = DEFAULT_SORT;
		}
		
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}
	
	/**
	 * image pagination 검색
	 * 
	 * @param page
	 * @param limit
	 * @param select
	 * @param sort
	 * @param query
	 * @return
	 */
	@RequestMapping(value="/list/image", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Image Attachments (Pagination) by Search Conditions")	
	public Page<?> searchImages(			
			@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit, 
			@RequestParam(name = "select", required = false) String select, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
		return this.list(page, limit, select, sort, query, "image");
	}
	
	/**
	 * video pagination 검색
	 * 
	 * @param page
	 * @param limit
	 * @param select
	 * @param sort
	 * @param query
	 * @return
	 */
	@RequestMapping(value="/list/video", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Image Attachments (Pagination) by Search Conditions")	
	public Page<?> searchMovies(			
			@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit, 
			@RequestParam(name = "select", required = false) String select, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
		return this.list(page, limit, select, sort, query, "video");
	}
	
	/**
	 * search attachment list 
	 * 
	 * @param page
	 * @param limit
	 * @param select
	 * @param sort
	 * @param query
	 * @param category
	 * @return
	 */
	private Page<?> list(Integer page, Integer limit, String select, String sort, String query, String category) {
		Query input = new Query();
		input.setPageIndex(page == null ? 1 : page.intValue());
		input.setPageSize(limit == null ? 0 : limit.intValue());
		String[] selectFields = ValueUtil.isEmpty(select) ? null : select.split(OrmConstants.COMMA);
		
		if(!ValueUtil.isEmpty(selectFields)) {
			List<String> selectColumns = new ArrayList<String>();
			for(int i = 0 ; i < selectFields.length ; i++) {
				selectColumns.add(selectFields[i]);
			}
			input.setSelect(selectColumns);
		}
		
		if(ValueUtil.isNotEmpty(sort)) {
			Order[] orders = FormatUtil.jsonToObject(sort, Order[].class);
			input.addOrder(orders);
		}
		
		// Domain ID를 기본 검색 조건으로 지정.
		Long domainId = Domain.currentDomain().getId();
		input.addFilter(new Filter(SysConstants.ENTITY_FIELD_DOMAIN_ID, domainId));
		
		if(ValueUtil.isNotEmpty(query)) {
			Filter[] filters = FormatUtil.jsonToObject(query, Filter[].class);
			input.addFilter(filters);
		}
		
		List<String> storageIds = this.queryManager.selectListBySql(STORAGE_SQL, ValueUtil.newMap("domainId,category", domainId, category), String.class, 0, 0);
		input.addFilter(new Filter("storageInfoId", OrmConstants.IN, storageIds));
		return ValueUtil.isEmpty(storageIds) ? new Page<Attachment>() : this.search(this.entityClass(), input);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find one Attachment by ID")
	public Attachment findOne(@PathVariable("id") String id) {
		return this.getOne(true, this.entityClass(), id);
	}
	
	@RequestMapping(method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description="Create Attachment")
	public Attachment create(@RequestBody Attachment attachment) {
		return this.createOne(attachment);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.PUT, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update Attachment")
	public Attachment update(@PathVariable("id") String id, @RequestBody Attachment attachment) {
		return this.updateOne(attachment);
	}

	@RequestMapping(value="/{id}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete Attachment by ID")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}
	
	@RequestMapping(value="/update_multiple", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple attachments at one time")
	public Boolean multipleUpdate(@RequestBody List<Attachment> attachmentList) {
		return this.cudMultipleData(this.entityClass(), attachmentList);
	}

	@RequestMapping(value="/{id}/download", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="File Download")
	public @ResponseBody Object fileDownload(HttpServletRequest req, HttpServletResponse res, @PathVariable("id") String id) {
		Attachment attachment = BeanUtil.get(IQueryManager.class).select(Attachment.class, id);
		String filePath = AttachmentUtil.getAttachmentFileFullPath(attachment);
		this.fileDownloadHandler.handleRequest(req, res, filePath, attachment.getName());
		return new BasicOutput();
	}
	
	@RequestMapping(value="/attach-path/{storage}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Attachment path by storage and filename")
	public Attachment attachmentPath(@PathVariable("storage") String storageName, @RequestParam(name = "filename") String filename) {
		Storage storage = AttachmentUtil.getStorageByName(storageName);
		
		if(storage == null) {
			throw ThrowUtil.newNotFoundRecord("menu.Storage", storageName);
		}
		
		String path = storage.getPath() + OrmConstants.SLASH + filename;
		Map<String, Object> paramMap = ValueUtil.newMap("domainId,storageInfoId,path", storage.getDomainId(), storage.getId(), path);
		Attachment attach = this.queryManager.selectByCondition(true, Attachment.class, paramMap);		
		attach.setDescription(AttachmentUtil.getAttachmentFileFullPath(attach));
		return attach;
	}
	
}