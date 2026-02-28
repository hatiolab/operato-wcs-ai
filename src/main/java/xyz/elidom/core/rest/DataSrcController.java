/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.core.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import xyz.elidom.core.entity.DataSrc;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.IDataSourceManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/data_srcs")
@ServiceDesc(description = "DataSrc Service API")
public class DataSrcController extends AbstractRestService {
	
	/**
	 * 기본 소트 조건 - '[{\"field\": \"name\", \"ascending\": true}]'
	 */
	private static String DEFAULT_SORT = "[{\"field\": \"name\", \"ascending\": true}]";

	@Autowired
	protected IDataSourceManager dataSourceManager;
	
	@Override
	protected Class<?> entityClass() {
		return DataSrc.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
		if(ValueUtil.isEmpty(sort)) {
			sort = DEFAULT_SORT;
		}
		
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public DataSrc findOne(@PathVariable("id") String id) {
		return this.getOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}
	
	@RequestMapping(value = "/check_import", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check Before Import")
	public List<DataSrc> checkImport(@RequestBody List<DataSrc> list) {
		for (DataSrc item : list) {
			this.checkForImport(DataSrc.class, item);
		}
		
		return list;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public DataSrc create(@RequestBody DataSrc input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public DataSrc update(@PathVariable("id") String id, @RequestBody DataSrc input) {
		return this.updateOne(input);
	}

	@RequestMapping(value="/{id}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}
	
	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple data at one time")
	public Boolean multipleUpdate(@RequestBody List<DataSrc> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}
	
	@RequestMapping(value = "/{id}/init_pool", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Initialize datasource pool")	
	public Boolean initPool(@PathVariable("id") String id) {
		DataSrc datasource = this.getOne(false, false, this.entityClass(), id);
		
		if(datasource == null) {
			return false;
			
		} else {
			// 1. 이전 데이터 소스가 있다면 destroy
			String dsName = datasource.getName();
			if(this.dataSourceManager.isExistDataSource(dsName)) {
				this.dataSourceManager.destroyDataSource(dsName);
			}
			
			// 2. 새로운 데이터 소스 initialize
			this.dataSourceManager.initializeDataSource(dsName, datasource.getClassName(), datasource.getUrl(), datasource.getDomain(), datasource.getUserid(), datasource.getPassword(), datasource.getMinIdle(), datasource.getMaxIdle(), datasource.getMaxActive(), datasource.getMaxWait(), datasource.getEvictTime());
			datasource.setStatus(DataSrc.STATUS_CONNECTED);
			this.queryManager.update(datasource, OrmConstants.ENTITY_FIELD_STATUS, OrmConstants.ENTITY_FIELD_UPDATED_AT);
			return true;
		}
	}

	@RequestMapping(value = "/{id}/destroy_pool", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Destroy datasource pool")	
	public Boolean destroyPool(@PathVariable("id") String id) {
		DataSrc datasource = this.getOne(false, false, this.entityClass(), id);
		dataSourceManager.destroyDataSource(datasource.getName());
		datasource.setStatus(DataSrc.STATUS_CLOSED);
		this.queryManager.update(datasource, OrmConstants.ENTITY_FIELD_STATUS, OrmConstants.ENTITY_FIELD_UPDATED_AT);
		return true;
	}
	
}