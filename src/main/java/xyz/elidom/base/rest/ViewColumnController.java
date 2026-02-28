/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.base.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import xyz.elidom.base.BaseConfigConstants;
import xyz.elidom.base.BaseConstants;
import xyz.elidom.base.entity.Resource;
import xyz.elidom.base.entity.ResourceColumn;
import xyz.elidom.base.entity.ViewColumn;
import xyz.elidom.core.entity.Code;
import xyz.elidom.core.rest.CodeController;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Order;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.system.config.module.IModuleProperties;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ClassUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/view_columns")
@ServiceDesc(description = "ViewColumn Service API")
public class ViewColumnController extends AbstractRestService {
	
	@Override
	protected Class<?> entityClass() {
		return ViewColumn.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public ViewColumn findOne(@PathVariable("id") String id) {
		return this.getOne(true, this.entityClass(), id);
	}

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public ViewColumn create(@RequestBody ViewColumn input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public ViewColumn update(@PathVariable("id") String id, @RequestBody ViewColumn input) {
		return this.updateOne(input);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}	

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<ViewColumn> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	@RequestMapping(value="/update_multiple/{on_type}/{on_id}", method=RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search View Columns By Entity Name & Resource ID")
	public List<ViewColumn> updateViewColumns(@PathVariable("on_type") String onType, @PathVariable("on_id") String onId, @RequestBody List<ViewColumn> list) {
		this.cudMultipleData(this.entityClass(), list);
		return this.viewColumns(onType, onId);
	}
	
	@RequestMapping(value="/{on_type}/{on_id}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search View Columns By Entity Name & Resource ID")
	public List<ViewColumn> viewColumns(@PathVariable("on_type") String onType, @PathVariable("on_id") String onId) {
		Query query = new Query();
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_DOMAIN_ID, Domain.currentDomain().getId()));
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_ON_TYPE, onType));
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_ON_ID, onId));
		query.addOrder(new Order(BaseConstants.FIELD_NAME_RANK, true));
		return this.queryManager.selectList(ViewColumn.class, query);
	}
	
	@RequestMapping(value="/{on_type}/{on_id}/meta", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search View Columns By Entity Name & Resource ID")
	public Map<String, Object> meta(@PathVariable("on_type") String onType, @PathVariable("on_id") String onId) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		if(ValueUtil.isEqual(ENTITY_DIY_GRID, onType)) {
			this.addDiyGridConfig(result, onId);
		}
		
		Query query = new Query();
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_DOMAIN_ID, Domain.currentDomain().getId()));
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_ON_TYPE, onType));
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_ON_ID, onId));
		query.addOrder(new Order(BaseConstants.FIELD_NAME_RANK, true));
		List<ViewColumn> columns = this.queryManager.selectList(ViewColumn.class, query);
		String locale = User.currentUser().getLocale();
		
		if (ValueUtil.isNotEmpty(columns)) {			
			this.translateViewColumnNames(locale, columns);
			if (ValueUtil.isEqual(ENTITY_DIY_GRID, onType)) {
				this.fillCodeData(columns);
			}
		}
		
		result.put(BaseConstants.MENU_OBJECT_COLUMNS_NAME, columns);
		return result;
	}	
	
	@RequestMapping(value="/{on_type}/{on_id}/sync_with_entity_columns/{src_on_type}", method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Synchronize View Columns By Entity Name")
	public List<ViewColumn> syncWithEntityColumns(@PathVariable("on_type") String onType, @PathVariable("on_id") String onId, @PathVariable("src_on_type") String srcOnType) {
		Resource srcRsc = this.selectByCondition(Resource.class, new Resource(Domain.currentDomainId(), srcOnType));
		List<ResourceColumn> srcColumns = srcRsc.resourceColumns();
		
		List<ViewColumn> targetColumns = new ArrayList<ViewColumn>();
		for(ResourceColumn entityColumn : srcColumns) {
			ViewColumn viewColumn = new ViewColumn(srcRsc.getDomainId(), onType, onId);
			viewColumn = ValueUtil.populate(entityColumn, viewColumn);
			viewColumn.setId(null);
			targetColumns.add(viewColumn);
		}
		
		this.queryManager.insertBatch(targetColumns);
		return this.viewColumns(onType, onId);
	}
	
	/**
	 * DiyGrid
	 */
	private static final String ENTITY_DIY_GRID = "DiyGrid";
	
	/**
	 * screen module 명 
	 */
	private static final String MODULE_SCREEN = "screen";
	
	/**
	 * config field 명 
	 */
	private static final String FIELD_CONFIG = "config";
	
	/**
	 * DiyGrid Class Name
	 */
	private static final String ClASS_NAME_DIY_GRID = "xyz.elidom.screen.entity.DiyGrid";	
	
	@Autowired
	private ModuleConfigSet configSet;
	
	/**
	 * diyGrid 설정을 추가 
	 * 
	 * @param result
	 * @param onId
	 */
	private void addDiyGridConfig(Map<String, Object> result, String onId) {
		IModuleProperties devModule = this.configSet.getConfig(MODULE_SCREEN);
		if(devModule != null) {
			Class<?> diyGridClazz = ClassUtil.forName(ClASS_NAME_DIY_GRID);
			Object diyGrid = this.queryManager.select(diyGridClazz, onId);
			String configVal = (String)ClassUtil.getFieldValue(diyGrid, FIELD_CONFIG);
			if(ValueUtil.isNotEmpty(configVal) && ValueUtil.isNotEmpty(configVal.trim())) {
				result.put(OrmConstants.DATA_TYPE_TEXT, configVal.trim());
			}			
		}		
	}
	
	/**
	 * viewColumns 컬럼들의 컬럼명을 번역한다.
	 * 
	 * @param locale
	 * @param viewColumns
	 */	
	private void translateViewColumnNames(String locale, List<ViewColumn> viewColumns) {
		for (ViewColumn column : viewColumns) {
			String termKey = (column.getTerm() == null) ? SysConstants.TERM_LABELS + column.getName() : column.getTerm();
			column.setTerm(MessageUtil.getLocaleTerm(locale, termKey, termKey));
		}		
	}
	
	/**
	 * viewColumns 컬럼들 중 Grid 편집기가 CodeCombo인 경우 해당 컬럼에 코드 데이터를 추가한다. 
	 * 
	 * @param entityColumns
	 */
	private void fillCodeData(List<ViewColumn> viewColumns) {
		if(ValueUtil.toBoolean(SettingUtil.getValue(BaseConfigConstants.CODE_COMBO_DATA_FILL_AT_SERVER, SysConstants.TRUE_STRING))) {
			CodeController codeCtrl = BeanUtil.get(CodeController.class);
		
			for (ViewColumn column : viewColumns) {
				if (ValueUtil.isEqual(BaseConstants.REF_TYPE_COMMON_CODE, column.getRefType()) && ValueUtil.isNotEmpty(column.getRefName()) && ValueUtil.isNotEmpty(column.getGridEditor()) && column.getGridEditor().startsWith(BaseConstants.GRID_CODE_EDITOR_PREFIX)) {
					Code code = codeCtrl.findOne(SysConstants.SHOW_BY_NAME_METHOD, column.getRefName());
					column.setCodeList(code.getItems());
				}
			}
		}
	}
	
}