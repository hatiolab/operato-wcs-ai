/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.base.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import xyz.elidom.base.BaseConstants;
import xyz.elidom.base.entity.MenuDetail;
import xyz.elidom.base.entity.MenuDetailButton;
import xyz.elidom.base.entity.MenuDetailColumn;
import xyz.elidom.base.entity.Resource;
import xyz.elidom.base.entity.ResourceColumn;
import xyz.elidom.core.entity.Code;
import xyz.elidom.core.rest.CodeController;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/menu_details")
@ServiceDesc(description = "MenuDetail Service API")
public class MenuDetailController extends AbstractRestService {
	@Override
	protected Class<?> entityClass() {
		return MenuDetail.class;
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

	@RequestMapping(value = "/{menu_id}/meta", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find Menu Detail Meta by Menu ID")
	public List<MenuDetail> findMenuDetailMeta(@PathVariable("menu_id") String menuId, @RequestParam(name = "no_trans_term", required = false) Boolean noTransTerm) {
		noTransTerm = noTransTerm == null ? false : noTransTerm;
		Query query = new Query();
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_DOMAIN_ID, Domain.currentDomain().getId()));
		query.addFilter(new Filter(BaseConstants.FIELD_NAME_MENU_ID, menuId));
		
		List<MenuDetail> menuDetails = this.queryManager.selectList(MenuDetail.class, query);
		for(MenuDetail menuDetail : menuDetails) {
			List<MenuDetailColumn> menuDetailColumns = this.menuDetailColumns(menuDetail.getId());
			
			if(!noTransTerm && !ValueUtil.isEmpty(menuDetailColumns)) {
				String locale = User.currentUser().getLocale();
				for (MenuDetailColumn column : menuDetailColumns) {
					String termKey = (column.getTerm() == null) ? SysConstants.TERM_LABELS + column.getName() : column.getTerm(); 
					column.setTerm(MessageUtil.getLocaleTerm(locale, termKey, termKey));
					
					if(ValueUtil.isEqual(BaseConstants.REF_TYPE_COMMON_CODE, column.getRefType()) && ValueUtil.isNotEmpty(column.getRefName())) {
						Code code = BeanUtil.get(CodeController.class).findOne(SysConstants.SHOW_BY_NAME_METHOD, column.getRefName());
						column.setCodeList(code.getItems());
					}
				}
			}
			
			menuDetail.setMenuDetailColumns(menuDetailColumns);
			menuDetail.setMenuDetailButtons(this.menuDetailButtons(menuDetail.getId()));
		}
		
		return menuDetails;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public MenuDetail findOne(@PathVariable("id") String id) {
		MenuDetail menuDetail = this.getOne(true, this.entityClass(), id);
		menuDetail.setMenuDetailColumns(this.menuDetailColumns(id));
		menuDetail.setMenuDetailButtons(this.menuDetailButtons(id));
		return menuDetail;
	}

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public MenuDetail create(@RequestBody MenuDetail input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public MenuDetail update(@PathVariable("id") String id, @RequestBody MenuDetail input) {
		return this.updateOne(input);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}	

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<MenuDetail> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}
	
	@RequestMapping(value="/search_with_details", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search all data with details")
	public List<Map<String, Object>> indexWithDetails(
			@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query,
			@RequestParam(name = "include_default_fields", required = false) boolean includeDefaultFields) {
		
		Page<?> pageResult = this.search(this.entityClass(), page, limit, OrmConstants.ENTITY_FIELD_ID, sort, query);
		List<?> list = pageResult.getList();
		
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		for(Object data : list) {
			String id = ((MenuDetail)data).getId();
			results.add(this.findDetails(id, includeDefaultFields));
		}
		
		return results;
	}	

	@RequestMapping(value = "/{id}/include_details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find One included all details by ID")
	public Map<String, Object> findDetails(@PathVariable("id") String id, @RequestParam(name = "include_default_fields", required = false) boolean includeDefaultFields) {
		return this.findOneIncludedDetails(id, includeDefaultFields);
	}

	@RequestMapping(value = "/{id}/menu_detail_columns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search MenuColumn List of MenuDetail")
	public List<MenuDetailColumn> menuDetailColumns(@PathVariable("id") String id) {
		Query query = new Query();
		query.addFilter(new Filter(BaseConstants.FIELD_NAME_MENU_DETAIL_ID, id));
		query.addOrder(BaseConstants.FIELD_NAME_RANK, true);
		return this.queryManager.selectList(MenuDetailColumn.class, query);
	}

	@RequestMapping(value = "/{id}/menu_detail_buttons", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search MenuButton List of MenuDetail")
	public List<MenuDetailButton> menuDetailButtons(@PathVariable("id") String id) {
		return this.queryManager.selectList(MenuDetailButton.class, new MenuDetailButton(id));
	}

	@RequestMapping(value = "/{id}/menu_detail_columns/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update Multiple Menu Detail Columns")
	public List<MenuDetailColumn> updateMenuDetailColumns(@PathVariable("id") String id, @RequestBody List<MenuDetailColumn> menuDetailColumns) {
		this.cudMultipleData(MenuDetailColumn.class, menuDetailColumns);
		return this.menuDetailColumns(id);
	}

	@RequestMapping(value = "/{id}/menu_detail_buttons/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update Multiple Menu Detail Buttons")
	public List<MenuDetailButton> updateMenuDetailButtons(@PathVariable("id") String id, @RequestBody List<MenuDetailButton> menuDetailButtons) {
		this.cudMultipleData(MenuDetailButton.class, menuDetailButtons);
		return this.menuDetailButtons(id);
	}
	
	@RequestMapping(value = "/{id}/menu_detail_columns/sync_with_entity_columns", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Synchronize Menu Detail Columns With Entity Detail Columns")
	public List<MenuDetailColumn> syncWithEntityColumns(@PathVariable("id") String id) {
		MenuDetail menuDetail = this.getOne(this.entityClass(), id);
		Resource entity = new Resource();
		entity.setId(menuDetail.getEntityId());
		entity = this.queryManager.selectByCondition(Resource.class, entity);
		List<ResourceColumn> entityColumns = this.queryManager.selectList(ResourceColumn.class, new ResourceColumn(entity.getId()));
		List<MenuDetailColumn> menuColumns = this.copyResourceColumn(id, entityColumns);
		super.multipleCud(menuColumns, null, null);		
		return this.menuDetailColumns(id);
	}
	
	/**
	 * copy resource columns
	 * 
	 * @param menuDetailId
	 * @param entityColumns
	 * @return
	 */
	private List<MenuDetailColumn> copyResourceColumn(String menuDetailId, List<ResourceColumn> entityColumns) {
		List<MenuDetailColumn> menuDetailColumns = new ArrayList<MenuDetailColumn>();
		for(ResourceColumn entityColumn : entityColumns) {
			MenuDetailColumn menuColumn = new MenuDetailColumn(menuDetailId);
			menuColumn = ValueUtil.populate(entityColumn, menuColumn);
			// FIXED 하나의 리소스에 대해서 여러 개의 마스터 - 디테일 메뉴를 생성할 때 오류  
			menuColumn.setId(null);
			menuDetailColumns.add(menuColumn);
		}
		
		return menuDetailColumns;
	}
	
}