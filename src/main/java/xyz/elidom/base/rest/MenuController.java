/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.base.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import xyz.anythings.sys.model.BaseResponse;
import xyz.elidom.base.BaseConstants;
import xyz.elidom.base.entity.Menu;
import xyz.elidom.base.entity.MenuButton;
import xyz.elidom.base.entity.MenuColumn;
import xyz.elidom.base.entity.MenuDetail;
import xyz.elidom.base.entity.MenuParam;
import xyz.elidom.base.system.meta.MenuMetaService;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.msg.rest.TerminologyController;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.util.AssertUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/menus")
@ServiceDesc(description="Menu Service API")
public class MenuController extends MenuMetaService {
	/**
	 * 태블릿 용 메뉴 카테고리
	 */
	private static final String CATEGORY_TABLET = "TABLET";
	
	/**
	 * PDA 용 메뉴 카테고리
	 */	
	private static final String CATEGORY_PDA = "PDA";
	
	/**
	 * KIOSK 용 메뉴 카테고리
	 */
	private static final String CATEGORY_KIOSK = "KIOSK";
	
	/**
	 * Auth Query
	 */
	private String AUTH_QUERY = new StringBuffer("")
			.append(" SELECT")
			.append("		m.id, m.domain_id, m.name, m.description, m.routing_type, m.routing, m.category, m.parent_id, m.template, m.menu_type, m.detail_form_id, m.resource_type, m.resource_name, m.resource_url, m.grid_save_url, m.id_field, m.title_field, m.pagination, m.items_prop, m.total_prop, m.fixed_columns, m.detail_layout, m.icon_path, p.action_name auth, :rank") 
			.append(" FROM")
			.append("		menus m, permissions p")
			.append(" WHERE")
			.append("		m.domain_id = :domainId and m.id = p.resource_id and m.hidden_flag != :hiddenFlag and m.category = :category")
			.append("		and p.resource_type = 'Menu'")
			.append("		and p.role_id in (select role_id from users_roles where user_id = :userId)")
			.append(" ORDER BY")
			.append("		:rank, m.id, p.action_name").toString();
	
	/**
	 * Auth Query Parameters - 'domainId,userId,category,hiddenFlag'
	 */
	private static final String AUTH_QUERY_PARAMS = "domainId,userId,category,hiddenFlag";	
	
	/**
	 * Index - Default Select Fields - 'id,name,routing_type,routing,category,parent_id,menu_type,rank,template,detail_form_id,resource_type,resource_name,resource_url,grid_save_url,id_field,title_field,pagination,items_prop,total_prop,fixed_columns,detail_layout,icon_path'
	 */
	private static final String INDEX_DEFAULT_SELECT_FIELDS = "id,name,routing_type,routing,category,parent_id,menu_type,rank,template,detail_form_id,resource_type,resource_name,resource_url,grid_save_url,id_field,title_field,pagination,items_prop,total_prop,fixed_columns,detail_layout,icon_path,description";
	
	/**
	 * Index - Default Query - '[{"name" : "hidden_flag", "operator": "is_not_true"}]'
	 */
	private static final String INDEX_DEFAULT_QUERY = "[{\"name\" : \"hidden_flag\", \"operator\": \"is_not_true\"}]";
	
	/**
	 * Index - Category Default Query - '[{"name" : "hidden_flag", "operator": "is_not_true"}, {"name" : "category", "value": "${category}"}]'
	 */
	private static final String INDEX_CATEGORY_DEFAULT_QUERY = "[{\"name\" : \"category\", \"value\": \"$category\"},{\"name\" : \"hidden_flag\", \"operator\": \"is_not_true\"}]";
	
	/**
	 * Index - Default Sort - '[{"field" : "parentId", "ascending": true}, {"field" : "rank", "ascending": true}]'
	 */
	private static final String INDEX_DEFAULT_SORT = "[{\"field\" : \"parentId\", \"ascending\": true},{\"field\" : \"rank\", \"ascending\": true}]";
	
	/**
	 * topMenus 메소드 Default Sort : '[{"field" : "rank", "ascending": true}]'
	 */
	private static final String TOP_MENUS_DEFAULT_SORT = "[{\"field\" : \"rank\", \"ascending\": true}]";

	/**
	 * topMenus 메소드 Default Query Prefix : '[{"name" : "parentId", "operator": "is_null"}, {"name" : "category", "value": "$category"}]'
	 */
	private static final String TOP_MENUS_DEFAULT_QUERY_TEMPLATE = "[{\"name\" : \"parentId\", \"operator\": \"is_null\"}, {\"name\" : \"category\", \"value\": \"$category\"}]";

	/**
	 * Mobile Menus 메소드 Default Query Prefix : '[{"name" : "parentId", "operator": "is_not_null"}, {"name" : "category", "value": "$category"}]'
	 */
	private static final String MOBILE_MENUS_DEFAULT_QUERY_TEMPLATE = "[{\"name\" : \"parentId\", \"operator\": \"is_not_null\"}, {\"name\" : \"category\", \"value\": \"$category\"},{\"name\" : \"hidden_flag\", \"operator\": \"is_not_true\"}]" ;

	/**
	 * subMenus 메소드 Default Sort : '[{"field" : "rank", "ascending": true}]'
	 */
	private static final String SUB_MENUS_DEFAULT_SORT = "[{\"field\" : \"rank\", \"ascending\": true}]";
	
	/**
	 * subMenus 메소드 Default Query Prefix : '[{\"name\" : \"parentId\", \"value\": \"$id\"}'
	 */
	private static final String SUB_MENUS_DEFAULT_QUERY_PREFIX = "[{\"name\" : \"parentId\", \"value\": \"$id\"}";
	
	/**
	 * subMenus 메소드 Default Query Suffix : ', {"name" : "hiddenFlag", "operator" : "noteq", "value": true}]'
	 */
	private static final String SUB_MENUS_DEFAULT_QUERY_SUFFIX = ", {\"name\" : \"hiddenFlag\", \"operator\" : \"noteq\", \"value\": true}]";
	
	/**
	 * QUERY - Get Menu Id by Menu Name : 'SELECT ID FROM MENUS WHERE DOMAIN_ID = :domainId AND NAME = :name'
	 */
	private static final String QUERY_MENU_ID_BY_NAME = "SELECT ID FROM MENUS WHERE DOMAIN_ID = :domainId AND NAME = :name";
	
	/**
	 * QUERY - Get Menu Id by Routing : SELECT ID FROM MENUS WHERE DOMAIN_ID = :domainId AND ROUTING = :routing
	 */
	private static final String QUERY_MENU_ID_BY_ROUTING = "SELECT ID FROM MENUS WHERE DOMAIN_ID = :domainId AND ROUTING = :routing";
	
	/**
	 * indexWithDetails 메소드의 Default Sort : '[{"field" : "parentId", "ascending": false}, {"field" : "rank", "ascending": true}]'
	 */
	private static final String INDEX_WITH_DETAILS_DEFAULT_SORT = "[{\"field\" : \"parentId\", \"ascending\": false}, {\"field\" : \"rank\", \"ascending\": true}]";

	public void setAuthQuery(String sql) {
		AUTH_QUERY = sql;
	}
	
	public String getAuthQuery() {
		return AUTH_QUERY.replaceAll(":rank", ValueUtil.isEqualIgnoreCase(this.queryManager.getDbType(), "mysql")?"m.`rank`":"rank");
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Menu (Pagination) By Search Conditions")
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "mode", required = false) String mode,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query,
			@RequestParam(name = "is_trans_term", required = false) boolean isTransTerm) {
		
		if(ValueUtil.isEmpty(sort)) {
			sort = INDEX_DEFAULT_SORT;
		}

		if(ValueUtil.isEmpty(query)) {
			query = INDEX_DEFAULT_QUERY;
		}

		if(ValueUtil.isEmpty(select)) {
			select = INDEX_DEFAULT_SELECT_FIELDS;
		}
		
		Page<?> result = this.search(Menu.class, page, limit, select, sort, query);
		List<Menu> menuList = (List<Menu>) result.getList();
		this.applyMenuTitle(isTransTerm, menuList);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value="/role_menus", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Domain Set Role Menus List")
	public Page<?> domainParentMenus(
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "mode", required = false) String mode,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query,
			@RequestParam(name = "is_trans_term", required = false) boolean isTransTerm) {
		
		User currentUser = User.currentUser();
		
		if(currentUser.getAdminFlag() == true && currentUser.getSuperUser() == true) {
			return this.index(page, limit, mode, select, sort, query, isTransTerm);
		} else {
			if(ValueUtil.isEqualIgnoreCase(this.queryManager.getDbType(), "mysql")) {
				select = select.replace("rank", "`rank`");
			}
			
			String sql = "select " + select + " from menus where domain_id = :domainId and hidden_flag = false and parent_id is null order by " + (ValueUtil.isEqualIgnoreCase(this.queryManager.getDbType(), "mysql")?"`rank`":"rank") + " asc";
			Page<?> result = this.queryManager.selectPageBySql(sql, ValueUtil.newMap("domainId", Domain.currentDomainId()), Menu.class, 0, 0);
			List<Menu> menuList = (List<Menu>) result.getList();
			this.applyMenuTitle(isTransTerm, menuList);
			return result;
		}
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value="/user_menus/{category}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search user menus by user authorization")
	public List<Menu> userMenus(
			@PathVariable("category") String category, 
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query,
			@RequestParam(name = "is_trans_term", required = false) boolean isTransTerm) {
		
		// 1. 사용자가 superuser이거나 해당 도메인 사용자이면서 admin이면 모든 화면 공개
		if(User.isCurrentUserAdmin()) {
			if(ValueUtil.isEqualIgnoreCase(category, CATEGORY_TABLET) || ValueUtil.isEqualIgnoreCase(category, CATEGORY_PDA) || ValueUtil.isEqualIgnoreCase(category, CATEGORY_KIOSK)) {
				query = this.convertTemplate(MOBILE_MENUS_DEFAULT_QUERY_TEMPLATE, ValueUtil.newMap(BaseConstants.FIELD_NAME_CATEGORY, category));
			} else {
				query = this.convertTemplate(INDEX_CATEGORY_DEFAULT_QUERY, ValueUtil.newMap(BaseConstants.FIELD_NAME_CATEGORY, category));
			}
			
			Page<?> page = this.index(0, 0, BaseConstants.MENU_QUERY_AUTH_MODE, select, sort, query, isTransTerm);
			List<Menu> list = (List<Menu>)page.getList();
			return list;
		}
		
		// 2. 일반 사용자라면 사용자 역할의 권한에 따라 메뉴를 리턴한다. 
		Map<String, Object> paramMap = ValueUtil.newMap(AUTH_QUERY_PARAMS, Domain.currentDomain().getId(), User.currentUser().getId(), category, true);
		String authQuery = this.getAuthQuery();
		List<Menu> items = (List<Menu>) super.queryManager.selectListBySql(authQuery, paramMap, Menu.class, 0, 0);
		List<Menu> authItems = new ArrayList<Menu>();

		for(Menu menu : items) {
			Menu foundMenu = null;

			for(Menu m : authItems) {
				if(ValueUtil.isEqual(menu.getId(), m.getId())) {
					foundMenu = m;
					break;
				}
			}

			boolean isFirst = false;

			if(foundMenu == null) {
				foundMenu = menu;
				isFirst = true;
			}

			String newAuth = foundMenu.getAuth();
			String auth = isFirst ? foundMenu.getAuth() : menu.getAuth();

			if(isFirst) {
				if(!ValueUtil.isEmpty(auth)) {
					if(ValueUtil.isEqual(auth, BaseConstants.MENU_PERMISSION_CREATE)) {
						newAuth = BaseConstants.MENU_PERMISSION_CREATE_VALUE;
					} else if(ValueUtil.isEqual(auth, BaseConstants.MENU_PERMISSION_UPDATE)) {
						newAuth = BaseConstants.MENU_PERMISSION_UPDATE_VALUE;
					} else if(ValueUtil.isEqual(auth, BaseConstants.MENU_PERMISSION_DELETE)) {
						newAuth = BaseConstants.MENU_PERMISSION_DELETE_VALUE;
					} else if(ValueUtil.isEqual(auth, BaseConstants.MENU_PERMISION_SHOW)) {
						newAuth = BaseConstants.MENU_PERMISION_SHOW_VALUE;
					}
				}
			} else {
				if(!ValueUtil.isEmpty(auth)) {
					if(ValueUtil.isEqual(auth, BaseConstants.MENU_PERMISSION_CREATE)) {
						newAuth += BaseConstants.MENU_PERMISSION_COMMA_CREATE_VALUE;
					} else if(ValueUtil.isEqual(auth, BaseConstants.MENU_PERMISSION_UPDATE)) {
						newAuth += BaseConstants.MENU_PERMISSION_COMMA_UPDATE_VALUE;
					} else if(ValueUtil.isEqual(auth, BaseConstants.MENU_PERMISSION_DELETE)) {
						newAuth += BaseConstants.MENU_PERMISSION_COMMA_DELETE_VALUE;
					} else if(ValueUtil.isEqual(auth, BaseConstants.MENU_PERMISION_SHOW)) {
						newAuth += BaseConstants.MENU_PERMISION_COMMA_SHOW_VALUE;
					}
				}
			}

			foundMenu.setAuth(newAuth);

			if(isFirst) {
				authItems.add(foundMenu);
			}
		}

		this.applyMenuTitle(isTransTerm, authItems);
		return authItems;
	}
			
	@SuppressWarnings("unchecked")
	@GetMapping(value="/all/top_menus", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Top Menus Only")
	public List<Menu> topMenus(
			@RequestParam(name = "category", required = false) String category, 
			@RequestParam(name = "is_trans_term", required = false) boolean isTransTerm) {
		
		if(ValueUtil.isEmpty(category)) {
			category = BaseConstants.MENU_CATEGORY_STANDARD;
		}
		
		String sort = TOP_MENUS_DEFAULT_SORT;
		String query = this.convertTemplate(TOP_MENUS_DEFAULT_QUERY_TEMPLATE, ValueUtil.newMap(BaseConstants.FIELD_NAME_CATEGORY, category));
		Page<?> output = this.search(this.entityClass(), 1, 10000, BaseConstants.STAR, sort, query);
		List<Menu> menuList = (List<Menu>) output.getList();
		this.applyMenuTitle(isTransTerm, menuList);
		return menuList;
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value="/{id}/sub_menus", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Sub Menus of Selected Menu")
	public List<Menu> subMenus(
			@PathVariable("id") String id, 
			@RequestParam(name = "select", required = false) String select, 
			@RequestParam(name = "showall", required = false, defaultValue = "true") Boolean showall,
			@RequestParam(name = "is_trans_term", required = false) boolean isTransTerm) {
		
		String sort = SUB_MENUS_DEFAULT_SORT;
		String query = this.convertTemplate(SUB_MENUS_DEFAULT_QUERY_PREFIX, ValueUtil.newMap(OrmConstants.ENTITY_FIELD_ID, id));
		query += !showall ? SUB_MENUS_DEFAULT_QUERY_SUFFIX : "]";
		Page<?> output = this.search(this.entityClass(), 1, 1000, select, sort, query);
		List<Menu> menuList = (List<Menu>) output.getList();
		this.applyMenuTitle(isTransTerm, menuList);
		return menuList;
	}
	
	@Transactional(readOnly=true, propagation=Propagation.NEVER)
	@GetMapping(value="/{id}/menu_meta", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find all meta data of the menu")
	public Map<String, Object> menuMeta(
			@PathVariable("id") String id, 
			@RequestParam(name = "is_trans_term", required = false) boolean isTransTerm, 
			@RequestParam(name = "ignore_on_save", required = false) boolean ignoreOnSave,
			@RequestParam(name = "codes_on_search_form", required = false) boolean codeOnSearchForm, 
			@RequestParam(name = "columns_off", required = false) boolean columnsOff, 
			@RequestParam(name = "buttons_off", required = false) boolean buttonsOff, 
			@RequestParam(name = "params_off", required = false) boolean paramsOff) {

		return this.findMenuMeta(id, isTransTerm, ignoreOnSave, codeOnSearchForm, columnsOff, buttonsOff, paramsOff);
	}
	
	@GetMapping(value="/{name}/named_meta", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find all meta data of the menu by menuName")
	public Map<String, Object> namedMenuMeta(
			@PathVariable("name") String name,
			@RequestParam(name = "is_trans_term", required = false) boolean isTransTerm,
			@RequestParam(name = "columns_off", required = false) boolean columnsOff, 
			@RequestParam(name = "buttons_off", required = false) boolean buttonsOff, 
			@RequestParam(name = "params_off", required = false) boolean paramsOff) {

		String id = this.queryManager.selectBySql(QUERY_MENU_ID_BY_NAME, ValueUtil.newMap("domainId,name", Domain.currentDomain().getId(), name), String.class);
		return this.menuMeta(id, isTransTerm, false, false, columnsOff, buttonsOff, paramsOff);
	}
	
	@Transactional(readOnly=true, propagation=Propagation.NEVER)
	@GetMapping(value="/{menu_name}/screen_menu_meta", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find operato all meta data of the menu")
	public Map<String, Object> screenMenuMeta(
			@PathVariable("menu_name") String menuName, 
			@RequestParam(name = "is_trans_term", required = false) boolean isTransTerm, 
			@RequestParam(name = "ignore_on_save", required = false) boolean ignoreOnSave,
			@RequestParam(name = "codes_on_search_form", required = false) boolean codeOnSearchForm,
			@RequestParam(name = "columns_off", required = false) boolean columnsOff, 
			@RequestParam(name = "buttons_off", required = false) boolean buttonsOff, 
			@RequestParam(name = "params_off", required = false) boolean paramsOff) {
		
		// 1. 메뉴 메타 조회 - 메뉴 라우팅으로 조회했다가 없다면 메뉴 명으로 조회
	    Long domainId = Domain.currentDomainId();
	    Map<String, Object> findParams = ValueUtil.newMap("domainId,name", domainId, menuName);
		String id = this.queryManager.selectBySql("SELECT ID FROM MENUS WHERE DOMAIN_ID = :domainId AND NAME = :name", findParams, String.class);
		
		if(id == null) {
	        String msg = MessageUtil.getTerm(domainId, "text.VALUE_IS_NOT_EXIST", "Not found menu by menu name [" + menuName + "]", ValueUtil.toList(MessageUtil.getTerm(domainId, "label.menu") + " [" + menuName + "]"));
	        throw ThrowUtil.newValidationErrorWithNoLog(msg);
		}
		
		// 2. 메뉴 메타 데이터 조회
		return this.findScreenMenuMeta(id, isTransTerm, ignoreOnSave, codeOnSearchForm, columnsOff, buttonsOff, paramsOff);
	}
	
	@Transactional(readOnly=true, propagation=Propagation.NEVER)
	@GetMapping(value="/{routing}/operato_menu_meta", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find operato all meta data of the menu")
	public Map<String, Object> operatoMenuMeta(
			@PathVariable("routing") String routing, 
			@RequestParam(name = "is_trans_term", required = false) boolean isTransTerm, 
			@RequestParam(name = "ignore_on_save", required = false) boolean ignoreOnSave,
			@RequestParam(name = "codes_on_search_form", required = false) boolean codeOnSearchForm,
			@RequestParam(name = "columns_off", required = false) boolean columnsOff, 
			@RequestParam(name = "buttons_off", required = false) boolean buttonsOff, 
			@RequestParam(name = "params_off", required = false) boolean paramsOff) {
		
		// 1. 메뉴 메타 조회 - 메뉴 라우팅으로 조회했다가 없다면 메뉴 명으로 조회
	    Long domainId = Domain.currentDomainId();
	    Map<String, Object> findParams = ValueUtil.newMap("domainId,routing", domainId, routing);
		String id = this.queryManager.selectBySql(QUERY_MENU_ID_BY_ROUTING, findParams, String.class);
		
		if(id == null) {
		    findParams.put("name", routing);
		    id = this.queryManager.selectBySql(QUERY_MENU_ID_BY_NAME, findParams, String.class);
		    
		    if(id == null) {
		        String msg = MessageUtil.getTerm(domainId, "text.VALUE_IS_NOT_EXIST", "Not found menu by routing [" + routing + "]", ValueUtil.toList(MessageUtil.getTerm(domainId, "label.menu") + " [" + routing + "]"));
		        throw ThrowUtil.newValidationErrorWithNoLog(msg);
		    }
		}
		
		// 2. 메뉴 메타 데이터 조회
		return this.findScreenMenuMeta(id, isTransTerm, ignoreOnSave, codeOnSearchForm, columnsOff, buttonsOff, paramsOff);
	}
	
	@PostMapping(value="/{id}/sync_menu_columns", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Synchroinze menu meta with Entity ID")
	public Map<String, Object> syncMenuMetaWithEntity(@PathVariable("id") String id) {
		MenuController menuCtrl = BeanUtil.get(MenuController.class);
		Menu menu = menuCtrl.findOne(id, null);
		return this.syncEntityByMenu(menu);
	}
		
	@GetMapping(value="/{parent_id}/export", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Export Menus && Menus Columns")
	public Object export(HttpServletRequest request, HttpServletResponse response, @PathVariable("parent_id") String parentId) {
		Workbook workbook = this.exportExcel(parentId);
		return this.excelDownloader.handleRequest(request, response, "menus", workbook);
	}
	
	@GetMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find One By Menu ID")
	@Cacheable(cacheNames="Menu", condition="#p1 == null", key="'Menu-' + #p0")
	public Menu findOne(@PathVariable("id") String id, @RequestParam(name = "name", required = false) String name) {
		Menu menu = null;
		if(SysConstants.SHOW_BY_NAME_METHOD.equalsIgnoreCase(id)) {
			AssertUtil.assertNotEmpty(SysConstants.TERM_LABEL_NAME, name);
			menu = this.selectByCondition(true, Menu.class, new Menu(Domain.currentDomain().getId(), name));
		} else {
			menu = this.getOne(true, this.entityClass(), id);
		}
		return menu;
	}
	
	@GetMapping(value="/{id}/exist", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Check If Menu Exist By Menu ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}
	
	@PostMapping( consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description="Create Menu")
	public Menu create(@RequestBody Menu menu) {
		return this.createOne(menu);
	}
	
	@PutMapping(value="/{id}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update Menu")
	@CachePut(cacheNames="Menu", key="'Menu-' + #p0")
	public Menu update(@PathVariable("id") String id, @RequestBody Menu menu) {
		return this.updateOne(menu);
	}

	@DeleteMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete Menu")
	@CacheEvict(cacheNames="Menu", key="'Menu-' + #p0")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@PostMapping(value="/update_multiple", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update multiple menus at one time")
	public Boolean multipleUpdate(@RequestBody List<Menu> menuList) {
		MenuController ctrl = BeanUtil.get(MenuController.class);
		
		for (Menu menu : menuList) {
			if (ValueUtil.isEqual(menu.getCudFlag_(), OrmConstants.CUD_FLAG_DELETE)) {
				ctrl.delete(menu.getId());
			}
		}
		
		for (Menu menu : menuList) {
			if (ValueUtil.isEqual(menu.getCudFlag_(), OrmConstants.CUD_FLAG_UPDATE)) {
				ctrl.update(menu.getId(), menu);
			}
		}
		
		for (Menu menu : menuList) {
			if (ValueUtil.isEqual(menu.getCudFlag_(), OrmConstants.CUD_FLAG_CREATE)) {
				ctrl.create(menu);
			}
		}
		
		return true;
	}
		
	@GetMapping(value="/search_with_details", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search all data with details")
	public List<Map<String, Object>> indexWithDetails(
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query,
			@RequestParam(name = "include_default_fields", required = false) boolean includeDefaultFields,
			@RequestParam(name = "is_trans_term", required = false) boolean isTransTerm) {

		if(ValueUtil.isEmpty(sort)) {
			sort = INDEX_WITH_DETAILS_DEFAULT_SORT;
		}
		
		Page<?> pageResult = this.search(this.entityClass(), page, limit, OrmConstants.ENTITY_FIELD_ID, sort, query);
		List<?> list = pageResult.getList();
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		
		for(Object data : list) {
			String id = ((Menu)data).getId();
			results.add(this.findMenuMeta(id, true, BaseConstants.MENU_OBJECT_MASTER_NAME, BaseConstants.MENU_OBJECT_COLUMNS_NAME, BaseConstants.MENU_OBJECT_BUTTONS_NAME, BaseConstants.MENU_OBJECT_PARAMS_NAME, false, false, false, isTransTerm));
		}
		
		return results;
	}
	
	@GetMapping(value = "/{id}/include_details", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find included all details by Menu ID")
	public Map<String, Object> findDetails(@PathVariable("id") String id, @RequestParam(name = "include_default_fields", required = false) boolean includeDefaultFields) {
		return this.findOneIncludedDetails(id, includeDefaultFields);
	}
	
	@GetMapping(value="/{id}/menu_columns", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Menu Columns By Menu ID")
	@Cacheable(cacheNames="MenuColumn", key="'MenuColumns-' + #p0")
	public List<MenuColumn> findMenuColumns(@PathVariable("id") String id) {
		return this.searchMenuColumns(id);
	}
	
	@PostMapping(value="/{id}/menu_columns/update_multiple", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update Multiple Menu Columns")
	@CacheEvict(cacheNames="MenuColumn", key="'MenuColumns-' + #p0")
	public Boolean updateMultipleMenuColumns(@PathVariable("id") String id, @RequestBody List<MenuColumn> menuColumnList) {
	    for(MenuColumn col : menuColumnList) {
	        // 메뉴 ID
	        if(ValueUtil.isEmpty(col.getMenuId())) {
	            col.setMenuId(id);
	        }
	        
	        // 설명 정보 설정
	        if(ValueUtil.isEmpty(col.getDescription()) && ValueUtil.isNotEmpty(col.getTerm())) {
	            String term = MessageUtil.getTerm(col.getTerm());
	            if(!term.startsWith(SysConstants.LABEL_KEY)) {
	                col.setDescription(term);
	            }
	        }
	    }
	    
		return this.cudMultipleData(MenuColumn.class, menuColumnList);
	}
	
	@GetMapping(value="/{id}/menu_details", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Menu Details By Menu ID")
	public List<MenuDetail> findMenuDetails(@PathVariable("id") String id) {
		return this.searchMenuDetails(id);
	}
	
	@GetMapping(value="/{id}/menu_buttons", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Menu Buttons By Menu ID")
	@Cacheable(cacheNames="MenuButton", key="'MenuButtons-' + #p0")
	public List<MenuButton> findMenuButtons(@PathVariable("id") String id) {
		return this.searchMenuButtons(id);
	}
	
	@PostMapping(value="/{id}/menu_buttons/update_multiple", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update Multiple Menu Buttons")
	@CacheEvict(cacheNames="MenuButton", key="'MenuButtons-' + #p0")
	public Boolean updateMultipleMenuButtons(@PathVariable("id") String id, @RequestBody List<MenuButton> menuButtonList) {
        for(MenuButton btn : menuButtonList) {
            if(ValueUtil.isEmpty(btn.getMenuId())) {
                btn.setMenuId(id);
            }
        }
        
		return this.cudMultipleData(MenuButton.class, menuButtonList);
	}
	
	@GetMapping(value="/{id}/menu_params", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find MenuParams of Menu")
	@Cacheable(cacheNames="MenuParam", key="'MenuParams-' + #p0")
	public @ResponseBody List<MenuParam> findMenuParams(@PathVariable("id") String id) {
		return this.searchMenuParams(id);
	}
	
	@PostMapping(value="/{id}/menu_params/update_multiple", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple MenuParams of Menu at one time")
	@CacheEvict(cacheNames="MenuParam", key="'MenuParams-' + #p0")
	public @ResponseBody List<MenuParam> updateMultipleMenuParams(@PathVariable("id") String id, @RequestBody List<MenuParam> menuParamList) {
        for(MenuParam btn : menuParamList) {
            if(ValueUtil.isEmpty(btn.getMenuId())) {
                btn.setMenuId(id);
            }
        }
        
		this.cudMultipleData(MenuParam.class, menuParamList);
		return (List<MenuParam>)this.queryManager.selectList(MenuParam.class, new MenuParam(id, null, null, null));
	}
	
	@PostMapping(value="/{id}/sync_menu/to_other_domains", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Syncronize menu to other domains")
	public BaseResponse syncMenuToOtherDomains(@PathVariable("id") String id) {
        // 1. 메뉴 조회
	    Menu menu = this.queryManager.select(Menu.class, id);
	    if(menu == null) {
	        return new BaseResponse(false, "ng", "Menu not found by id (" + id + ")");
	    }
	    // 2. 메뉴 정보를 다른 도메인에 동기화 처리
	    this.copyMenuDataToOtherDomains(menu);
        // 3. Clear Cache Resource Column
        BeanUtil.get(ResourceController.class).clearCache();
        // 4. Resource Column 조회 
        return new BaseResponse(true, "ok");
	}
	
    @PostMapping(value="/{id}/sub_menus/register_terms", produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Register unregistered terminologies By Menu ID")
    public BaseResponse registerTerminologiesBySubMenus(@PathVariable("id") String id) {
        List<Menu> menuList = this.subMenus(id, "id", true, false);
        for(Menu menu : menuList) {
            this.registerTerminologies(menu.getId());
        }
        
        return new BaseResponse(true, "ok");
    }
	
    @PostMapping(value="/{id}/register_terms", produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Register unregistered terminologies By Menu ID")
    public BaseResponse registerTerminologies(@PathVariable("id") String id) {
        // 1. 메뉴 컬럼 조회
    	this.registerMenuTerminologies(id);
        // 2. 컬럼 캐쉬 클리어
        BeanUtil.get(MenuController.class).clearMenuColumnsCache();
        // 3. 버튼 캐쉬 클리어
        BeanUtil.get(TerminologyController.class).clearCache();
        // 4. 결과 리턴
        return new BaseResponse(true, "ok");
    }
	
	@PutMapping(value = "/clear_menu_cache", produces = MediaType.APPLICATION_JSON_VALUE)
	@CacheEvict(cacheNames = "Menu", allEntries = true)
	public boolean clearMenuCache() {
		return true;
	}
	
	@PutMapping(value = "/clear_menu_column_cache", produces = MediaType.APPLICATION_JSON_VALUE)
	@CacheEvict(cacheNames = "MenuColumn", allEntries = true)
	public boolean clearMenuColumnsCache() {
		return true;
	}
	
	@PutMapping(value = "/clear_menu_button_cache", produces = MediaType.APPLICATION_JSON_VALUE)
	@CacheEvict(cacheNames = "MenuButton", allEntries = true)
	public boolean clearMenuButtonsCache() {
		return true;
	}
	
	@PutMapping(value = "/clear_menu_param_cache", produces = MediaType.APPLICATION_JSON_VALUE)
	@CacheEvict(cacheNames = "MenuParam", allEntries = true)
	public boolean clearMenuParamsCache() {
		return true;
	}
	
	@PutMapping(value = "/clear_cache", produces = MediaType.APPLICATION_JSON_VALUE)
	public boolean menuClearCache() {
		return BeanUtil.get(DomainController.class).requestClearCache("menu");
	}

    @GetMapping(value="/find_by_name/{name}", produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Find Menu By Name")
    @Cacheable(cacheNames="Menu", key="#p0")
    public Menu findByName(@PathVariable("name") String name) {
        AssertUtil.assertNotEmpty(SysConstants.TERM_LABEL_NAME, name);
        return this.selectByCondition(true, Menu.class, new Menu(Domain.currentDomainId(), name));
    }
    
    @PostMapping(value="/{parent_id}/update_multiple", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Update multiple menus at one time")
    public Boolean multipleUpdateByParentMenu(@PathVariable("parent_id") String parentId, @RequestBody List<Menu> menuList) {
        for(Menu menu : menuList) {
            menu.setParentId(parentId);
        }
        
        return this.multipleUpdate(menuList);
    }
    
    @PostMapping(value="/{id}/copy_menu", produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Copy menu")
    public BaseResponse copyMenu(@PathVariable("id") String id) {
        // 1. 메뉴 조회
        Menu srcMenu = this.queryManager.select(Menu.class, id);
        // 2. 메뉴 복사
        this.cloneMenu(srcMenu);
        // 3. 결과 리턴
        return new BaseResponse(false, "ok", "success");
    }
    
    @PutMapping(value="/{id}/move_menu", produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Move parent menu")
    public BaseResponse moveMenu(@PathVariable("id") String id, @RequestBody Map<String, Object> parentMenu) {
        Menu menu = this.queryManager.select(Menu.class, id);
        String newParentId = ValueUtil.toString(parentMenu.get("id"));
        menu.setParentId(newParentId);
        this.queryManager.update(menu);
        return new BaseResponse(false, "ok", "success");
    }
}