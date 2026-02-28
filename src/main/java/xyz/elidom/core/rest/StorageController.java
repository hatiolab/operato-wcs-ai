/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.core.rest;

import java.util.List;

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
import xyz.elidom.core.entity.Storage;
import xyz.elidom.core.util.AttachmentUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.system.service.params.BasicOutput;
import xyz.elidom.sys.util.AssertUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/storage_infos")
@ServiceDesc(description = "Storage Service API")
public class StorageController extends AbstractRestService {

	/**
	 * 기본 소트 조건 - '[{\"field\": \"name\", \"ascending\": true}]'
	 */
	private static String DEFAULT_SORT = "[{\"field\": \"name\", \"ascending\": true}]";

	@Override
	protected Class<?> entityClass() {
		return Storage.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Storage (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
		if (ValueUtil.isEmpty(sort)) {
			sort = DEFAULT_SORT;
		}

		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one Storage by ID")
	public Storage findOne(@PathVariable("id") String id, @RequestParam(name = "name", required = false) String name) {
		Storage storage = null;

		if (SysConstants.SHOW_BY_NAME_METHOD.equalsIgnoreCase(id)) {
			AssertUtil.assertNotEmpty(SysConstants.TERM_LABEL_NAME, name);
			storage = this.selectByCondition(Storage.class, new Storage(Domain.currentDomainId(), name));

		} else {
			storage = this.getOne(true, this.entityClass(), id);
			// storage 상세 파라미터가 있다면 Attachment 리스트 조회
			Attachment attachment = new Attachment();
			attachment.setStorageInfoId(storage.getId());
			List<Attachment> attachmentList = this.queryManager.selectList(Attachment.class, attachment);
			storage.setItems(attachmentList);
		}

		return storage;
	}

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check if Storage exists by ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/check_import", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check Before Import")
	public List<Storage> checkImport(@RequestBody List<Storage> list) {
		for (Storage item : list) {
			this.checkForImport(Storage.class, item);
		}

		return list;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create Storage")
	public Storage create(@RequestBody Storage storageInfo) {
		return this.createOne(storageInfo);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update Storage")
	public Storage update(@PathVariable("id") String id, @RequestBody Storage storageInfo) {
		return this.updateOne(storageInfo);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete Storage")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple Storage at one time")
	public Boolean multipleUpdate(@RequestBody List<Storage> storageInfoList) {
		return this.cudMultipleData(this.entityClass(), storageInfoList);
	}

	@RequestMapping(value = "/list_by_tag/{tag}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Show list by Tag")
	public List<Attachment> showList(@PathVariable("tag") String tag) {
		AssertUtil.assertNotEmpty("terms.label.tag", tag);
		Query query = new Query();
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_DOMAIN_ID, Domain.currentDomain().getId()));
		query.addFilter(new Filter("tag", tag));
		return this.queryManager.selectList(Attachment.class, query);
	}

	@RequestMapping(value = "/path/{path}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Show file path by tag")
	public String findPath(@PathVariable("path") String pathName) {
		AssertUtil.assertNotEmpty("terms.label.path", pathName);
		String storagePath = AttachmentUtil.getStoragePath(pathName);
		if (ValueUtil.isEmpty(storagePath)) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.Storage", pathName);
		}

		return storagePath;
	}

	@RequestMapping(value = "/delete_only_file/{id}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete Only attachment file")
	public BasicOutput deleteFile(@PathVariable("id") String id) {
		AttachmentUtil.deleteFile(id);
		return new BasicOutput();
	}
	
}