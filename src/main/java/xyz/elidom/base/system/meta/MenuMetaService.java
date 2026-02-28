package xyz.elidom.base.system.meta;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import xyz.anythings.sys.model.IOperatoConfig;
import xyz.anythings.sys.model.OperatoAction;
import xyz.anythings.sys.model.OperatoFormField;
import xyz.anythings.sys.model.OperatoGridColumn;
import xyz.anythings.sys.model.OperatoMenuInfo;
import xyz.anythings.sys.model.OperatoMenuParams;
import xyz.anythings.sys.model.OperatoSearchField;
import xyz.anythings.sys.model.OperatoSearchHiddenField;
import xyz.anythings.sys.model.OperatoSortField;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.base.BaseConfigConstants;
import xyz.elidom.base.BaseConstants;
import xyz.elidom.base.entity.Menu;
import xyz.elidom.base.entity.MenuButton;
import xyz.elidom.base.entity.MenuColumn;
import xyz.elidom.base.entity.MenuDetail;
import xyz.elidom.base.entity.MenuParam;
import xyz.elidom.base.entity.Resource;
import xyz.elidom.base.entity.ResourceColumn;
import xyz.elidom.base.rest.MenuController;
import xyz.elidom.base.rest.ResourceController;
import xyz.elidom.base.util.ResourceUtil;
import xyz.elidom.core.entity.Code;
import xyz.elidom.core.entity.CodeDetail;
import xyz.elidom.core.rest.CodeController;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.msg.entity.Terminology;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sec.rest.PermissionController;
import xyz.elidom.sec.rest.RoleController;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.system.engine.ITemplateEngine;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * 메뉴 관련 메타 정보 제공 서비스
 */
public class MenuMetaService extends AbstractRestService {
	/**
	 * Template Engine
	 */
	@Autowired
	@Qualifier("basic")
	protected ITemplateEngine templateEngine;
	/**
	 * Menu Meta Buttons - ["import", "export", "add", "delete", "save"]
	 */
	protected static final String[] MENU_META_BUTTONS = new String[] { "import", "export", "add", "delete", "save" };
	/**
	 * Menu Meta Buttons - ["create", "show", "create", "delete", "update"]
	 */
	protected static final String[] MENU_BUTTON_AUTHS = new String[] { "create", "show", "create", "delete", "update" };
	
	@Override
	protected Class<?> entityClass() {
		return Menu.class;
	}
	
	/**
	 * 내보내기
	 * 
	 * @param parentId
	 * @return
	 */
	protected Workbook exportExcel(String parentId) {
		Menu condition = new Menu();
		condition.setDomainId(Domain.currentDomainId());
		if(ValueUtil.isNotEmpty(parentId) && ValueUtil.isNotEqual(parentId, "all")) {
			condition.setParentId(parentId);
		}
		List<Menu> menuList = this.queryManager.selectList(Menu.class, condition);
		
		MenuController menuCtrl = BeanUtil.get(MenuController.class);
		Menu menuMenu = menuCtrl.findOne(SysConstants.SHOW_BY_NAME_METHOD, "Menu");
		List<MenuColumn> menuColumn = menuCtrl.findMenuColumns(menuMenu.getId());

		ResourceController rscCtrl = BeanUtil.get(ResourceController.class);
		Resource menuRsc = rscCtrl.findOne(SysConstants.SHOW_BY_NAME_METHOD, "MenuColumn");
		Workbook workbook = ResourceUtil.exportMenusToExcel(menuList, menuColumn, menuRsc.resourceColumns());
		return workbook;
	}
	
	/**
	 * 메뉴 화면 메타 조회
	 * 
	 * @param menuId
	 * @param isTransTerm
	 * @param ignoreOnSave
	 * @param codeOnSearchForm
	 * @param columnsOff
	 * @param buttonsOff
	 * @param paramsOff
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> findScreenMenuMeta(
			String menuId, 
			boolean isTransTerm, 
			boolean ignoreOnSave,
			boolean codeOnSearchForm,
			boolean columnsOff, 
			boolean buttonsOff, 
			boolean paramsOff) {
		
	    // 1. 메뉴 메타 데이터 조회
		Map<String, Object> menuMeta = this.findMenuMeta(menuId, isTransTerm, ignoreOnSave, codeOnSearchForm, columnsOff, buttonsOff, paramsOff);
		
		// 2. 리턴 대상 result
		Map<String, Object> result = new HashMap<String, Object>();
		
		// 3. 버튼 구성 준비
		List<MenuButton> buttons = (List<MenuButton>)menuMeta.get(BaseConstants.MENU_OBJECT_BUTTONS_NAME);
		List<OperatoAction> actions = new ArrayList<OperatoAction>();
		boolean useAddButton = false;
		boolean useExport = false;
		
		// 4. 버튼 구성
		for(MenuButton button : buttons) {
			// 추가 버튼은 그리드 설정으로 들어가야 하므로 플래그 처리
			if(ValueUtil.isEqualIgnoreCase(button.getText(), "add")) {
				useAddButton = true;
			} else {
				if(ValueUtil.isEqualIgnoreCase(button.getText(), "export")) {
					useExport = true;
				}
				
				actions.add(new OperatoAction(button));
			}
		}
		
		// 5. 메뉴 컬럼 정보 핸들링
		List<MenuColumn> menuColumns = (List<MenuColumn>)menuMeta.get(BaseConstants.MENU_OBJECT_COLUMNS_NAME);
		
		// 6. 메뉴 컬럼을 그리드 컬럼, 검색 폼, 기본 정렬 순서, 조회 컬럼 데이터 모델로 가공 준비
		List<OperatoGridColumn> gridConfigs = new ArrayList<OperatoGridColumn>();
		List<OperatoSearchHiddenField> searchHiddenFields = new ArrayList<OperatoSearchHiddenField>();
		List<OperatoSortField> sortFields = new ArrayList<OperatoSortField>();
		List<OperatoSearchField> searchFormFields = new ArrayList<OperatoSearchField>();
		List<OperatoFormField> formFields = new ArrayList<OperatoFormField>();
		List<String> selectFields = new ArrayList<String>();
		
		// 7. 메뉴 컬럼 정보들을 순회하면서 필요한 가공 처리
		for(MenuColumn column : menuColumns) {
			// 7.1 상세 폼 필드 구성 
			formFields.add(new OperatoFormField(column));
			
			// 7.2 검색 폼 필드 구성
			if(ValueUtil.isNotEmpty(column.getSearchRank()) && column.getSearchRank() > 0) {
				if(ValueUtil.isEqualIgnoreCase(column.getSearchEditor(), "hidden")) {
					searchHiddenFields.add(new OperatoSearchHiddenField(column));
				} else {
					searchFormFields.add(new OperatoSearchField(column));
				}
			}
			
			// 7.3 그리드 컬럼 구성 조건 필터링
			if(ValueUtil.isEqualIgnoreCase("id", column.getName()) && (ValueUtil.isEmpty(column.getGridRank()) || column.getGridRank() == 0)) {
				column.setGridRank(-10);
				selectFields.add(column.getName());
			}
			
			// 7.4 그리드 구성을 위한 컬럼 모델 구성
			if(ValueUtil.isNotEmpty(column.getGridRank()) && column.getGridRank() != 0) {
				OperatoGridColumn gridCol = new OperatoGridColumn(column, useExport);
				gridConfigs.add(gridCol);
				
				// 그리드 컬럼이 가상 컬럼이 아니라면 조회 대상 컬럼 구성
				if(ValueUtil.toBoolean(column.getVirtualField(), false) == false) {
					
					// 오브젝트 참조 타입의 경우 오브젝트 명으로 추가 
					if(ValueUtil.isEqualIgnoreCase(column.getGridEditor(), "resource-column") || 
					   ValueUtil.isEqualIgnoreCase(column.getGridEditor(), "resource-selector")) {
						selectFields.add(gridCol.getName());
					} else {
						selectFields.add(column.getName());
					}
				}
			}

			// 7.5 그리드 기본 정렬 조건 구성
			if(ValueUtil.isNotEmpty(column.getSortRank()) && column.getSortRank() != 0) {
				sortFields.add(new OperatoSortField(column));
			}
		}
		
		// 8. 화면 메타 모델 구성
		result.put("form_fields", this.sortRank(formFields)); 							// 폼 상세 필드 
		result.put("search_form_fields", this.sortRank(searchFormFields)); 				// 검색 폼 필드 
		result.put("actions", this.sortRank(actions)); 									// 버튼
		result.put("use_add_button", useAddButton); 									// 행 추가 버튼 사용 여부
		result.put("grid_config", this.sortRank(gridConfigs)); 							// 그리드 설정
		result.put("select_fields", selectFields); 										// 조회 대상 컬럼
		result.put("sort_fields", this.sortRank(sortFields)); 							// 기본 정렬 값
		result.put("search_hidden_fields", searchHiddenFields); 						// 기본 검색 조건 값
		result.put("menu", new OperatoMenuInfo((Menu)menuMeta.get("menu"))); 			// 메뉴 정보
		OperatoMenuParams omp = new OperatoMenuParams((List<MenuParam>)menuMeta.get(BaseConstants.MENU_OBJECT_MENU_PARAMS_NAME));
		result.put("menu_params", omp); 												// 메뉴 파람 정보

		// 9. 결과 리턴
		return result;
	}
	
	/**
	 * 메뉴 메타 조회
	 * 
	 * @param id
	 * @param isTransTerm
	 * @param ignoreOnSave
	 * @param codeOnSearchForm
	 * @param columnsOff
	 * @param buttonsOff
	 * @param paramsOff
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> findMenuMeta(String id, 
			boolean isTransTerm, 
			boolean ignoreOnSave,
			boolean codeOnSearchForm, 
			boolean columnsOff, 
			boolean buttonsOff, 
			boolean paramsOff) {

		// 1. 메뉴에 대한 권한을 조회
		List<String> menuAuthList = BeanUtil.get(PermissionController.class).findMenuPermissionsByUser(User.currentUser().getId(), id);
		
		// 2. 메뉴에 대한 최소한 권한(읽기 권한)이 있다면 메뉴 메타 정보를 조회한다.
		Map<String, Object> result = this.findMenuMeta(id, ignoreOnSave, BaseConstants.MENU_OBJECT_MENU_NAME, BaseConstants.MENU_OBJECT_COLUMNS_NAME, BaseConstants.MENU_OBJECT_BUTTONS_NAME, BaseConstants.MENU_OBJECT_MENU_PARAMS_NAME, columnsOff, buttonsOff, paramsOff, isTransTerm);
		
		// 3. 버튼 추출 후 권한 설정
		List<MenuButton> menuButtons = (List<MenuButton>)result.get(BaseConstants.MENU_OBJECT_BUTTONS_NAME);
		result.put(BaseConstants.MENU_OBJECT_BUTTONS_NAME, this.filterButtonByAuth(menuButtons, menuAuthList));
		
		// 4. 메뉴 컬럼
		List<MenuColumn> menuColumns = (List<MenuColumn>)result.get(BaseConstants.MENU_OBJECT_COLUMNS_NAME);
		
		// 5. 메뉴명 번역
		Menu menu = (Menu)result.get(BaseConstants.MENU_OBJECT_MENU_NAME);
		String menuTitle = isTransTerm ? MessageUtil.getLocaleTerm(SettingUtil.getUserLocale(), MessageUtil.getMenuTermKey(menu.getName())) : "menu." + menu.getName();
		menu.setTitle(menuTitle);
		
		// 6. 코드 참조 컬럼인 경우 코드 정보를 조회하여 컬럼에 추가 정보로 설정한다.
		if(!ValueUtil.isEmpty(menuColumns) && codeOnSearchForm) {
			this.fillCodeData(menuColumns, codeOnSearchForm);
		}
		
		// 7. 결과 리턴
		return result;
	}
	
	/**
	 * Menu Meta 정보 조회
	 * 
	 * @param id
	 * @param ignoreOnSave
	 * @param menuName
	 * @param columnName
	 * @param buttonName
	 * @param paramName
	 * @param columnsOff
	 * @param buttonsOff
	 * @param paramsOff
	 * @param isTransTerm
	 * @return
	 */
	protected Map<String, Object> findMenuMeta(String id, 
			boolean ignoreOnSave, 
			String menuName, 
			String columnName, 
			String buttonName, 
			String paramName, 
			boolean columnsOff, 
			boolean buttonsOff, 
			boolean paramsOff, 
			boolean isTransTerm) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		MenuController menuCtrl = BeanUtil.get(MenuController.class);
		Menu menu = menuCtrl.findOne(id, null);
		
		if(ignoreOnSave) {
			menu.setDomainId(null);
			menu.setCreator(null);
			menu.setUpdater(null);
			menu.setCreatorId(null);
			menu.setUpdaterId(null);
			menu.setCreatedAt(null);
			menu.setUpdatedAt(null);
		}
		
		// 1. 메뉴 파라미터 조회
		List<MenuParam> menuParams = paramsOff ? new ArrayList<MenuParam>() : menuCtrl.findMenuParams(id);
		
		// 2. 메뉴 컬럼 조회
		List<MenuColumn> menuColumns = columnsOff ? new ArrayList<MenuColumn>() : menuCtrl.findMenuColumns(id);
		
		// 3. ignore 컬럼 제외
		if (ignoreOnSave && ValueUtil.isNotEmpty(menuColumns)) {
			List<MenuColumn> ignoreColumns = new ArrayList<MenuColumn>();
			
			for (MenuColumn column : menuColumns) {
				if (column.getIgnoreOnSave()) {
					ignoreColumns.add(column);
				}
			}
			
			for(MenuColumn ignoreColumn : ignoreColumns) {
				menuColumns.remove(ignoreColumn);
			}
		}
		
		// 4. 컬럼 라벨 번역
		if(isTransTerm) {
			this.translateMenuColumnNames(menuColumns);
		}
		
		// 5. menu buttons. 사용자의 메뉴 권한에 따라 메뉴 버튼을 필터링한다.
		List<MenuButton> menuButtons = buttonsOff ? new ArrayList<MenuButton>() : menuCtrl.findMenuButtons(id);
		if(ValueUtil.isNotEmpty(menuButtons)) {
			this.translateButtonNames(isTransTerm, menuButtons);
		}
		result.put(buttonName, menuButtons);
		
		// 6. menu
		result.put(menuName, menu);
		
		// 7. menu columns
		result.put(columnName, menuColumns);

		// 8. menu params
		result.put(paramName, menuParams);
		
		// 9. 결과 리턴
		return result;
	}
	
	/**
	 * 메뉴 복사
	 * 
	 * @param srcMenu
	 * @return
	 */
	protected Menu cloneMenu(Menu srcMenu) {
        // 1. 메뉴 조회
        String id = srcMenu.getId();
        
        // 2. 원 메뉴에 메뉴 컬럼, 메뉴 버튼, 메뉴 파라미터 정보 조회
        List<MenuColumn> srcMenuColumns = this.searchMenuColumns(id);
        List<MenuButton> srcMenuButtons = this.searchMenuButtons(id);
        List<MenuParam> srcMenuParams = this.searchMenuParams(id);
        
        // 3. 타겟 메뉴를 소스 메뉴로 부터 복사 후 생성
        Menu tarMenu = ValueUtil.populate(srcMenu, new Menu());
        tarMenu.setId(null);
        tarMenu.setName(srcMenu.getName() + "_copy");
        tarMenu.setRouting(srcMenu.getRouting() + "_copy");
        this.queryManager.insert(tarMenu);
        String targetMenuId = tarMenu.getId();
        
        // 4. 메뉴 컬럼, 메뉴 버튼, 메뉴 파라미터 리스트 준비
        List<MenuColumn> tarCols = new ArrayList<MenuColumn>(srcMenuColumns.size());
        List<MenuButton> tarBtns = new ArrayList<MenuButton>(srcMenuButtons.size());
        List<MenuParam> tarParams = new ArrayList<MenuParam>(srcMenuParams.size());
        
        // 5. 타겟 메뉴에 소스 메뉴 컬럼 복사
        for(MenuColumn col : srcMenuColumns) {
            MenuColumn tarCol = ValueUtil.populate(col, new MenuColumn());
            tarCol.setMenuId(targetMenuId);
            tarCol.setId(null);
            tarCols.add(tarCol);
        }
        
        // 6. 타겟 메뉴에 소스 메뉴 버튼 복사
        for(MenuButton btn : srcMenuButtons) {
            MenuButton tarBtn = ValueUtil.populate(btn, new MenuButton());
            tarBtn.setMenuId(targetMenuId);
            tarBtn.setId(null);                    
            tarBtns.add(tarBtn);
        }
        
        // 7. 타겟 메뉴에 소스 메뉴 파라미터 복사
        for(MenuParam prm : srcMenuParams) {
            MenuParam tarParam = ValueUtil.populate(prm, new MenuParam());
            tarParam.setMenuId(targetMenuId);
            tarParam.setId(null);
            tarParams.add(tarParam);
        }
        
        // 8. 메뉴 컬럼, 메뉴 버튼, 메뉴 파라미터 생성
        if(!tarCols.isEmpty()) {
            this.queryManager.insertBatch(tarCols);
        }
        
        if(!tarBtns.isEmpty()) {
            this.queryManager.insertBatch(tarBtns);
        }
        
        if(!tarParams.isEmpty()) {
            this.queryManager.insertBatch(tarParams);
        }
        
        // 9. 결과 리턴
        return tarMenu;
    }
	
	/**
	 * 메뉴로 엔티티 정보 동기화
	 * 
	 * @param menu
	 * @return
	 */
	protected Map<String, Object> syncEntityByMenu(Menu menu) {
		MenuController menuCtrl = BeanUtil.get(MenuController.class);
		String id = menu.getId();
		int buttonCount = this.queryManager.selectSize(MenuButton.class, new MenuButton(id));
		int columnCount = this.queryManager.selectSize(MenuColumn.class, new MenuColumn(id));
		
		// 1. 버튼이 존재하지 않으면 생성
		if(buttonCount == 0) {
			List<MenuButton> menuButtons = new ArrayList<MenuButton>();
			for(int i = 0 ; i < MENU_META_BUTTONS.length ; i++) {
				menuButtons.add(this.newMenuButton(id, MENU_META_BUTTONS[i], MENU_BUTTON_AUTHS[i], null, null));
			}
			
			menuCtrl.updateMultipleMenuButtons(id, menuButtons);
		}
		
		// 2. 엔티티에서 그대로 참조한 메뉴에 대해서만 메뉴 컬럼 동기화
		if(ValueUtil.isEqual(menu.getResourceType(), BaseConstants.RESOURCE_TYPE_ENTITY)) {
			Resource entity = BeanUtil.get(ResourceController.class).findOne(SysConstants.SHOW_BY_NAME_METHOD, menu.getResourceName());
			
			// 1. 컬럼이 존재하지 않으면 메뉴 컬럼 생성
			if(columnCount == 0) {
				// 1.1 엔티티 컬럼 조회
				List<ResourceColumn> entityColumns = entity.resourceColumns();
				// 1.2 엔티티 컬럼의 모든 정보를 메뉴 컬럼에 모두 복사
				List<MenuColumn> menuColumns = this.copyResourceColumn(id, entityColumns);
				// 1.3 메뉴 컬럼 생성
				BeanUtil.get(MenuController.class).updateMultipleMenuColumns(id, menuColumns);
			
			// 2. 컬럼이 존재하면 메뉴 컬럼 동기화 - colType, colSize, nullable 만 동기화
			} else {
				// 2.1 엔티티 클래스 - 엔티티 컬럼 동기화
				BeanUtil.get(ResourceController.class).syncResourceColumnsWithEntity(entity.getId());
				// 2.2 엔티티 컬럼 - 메뉴 컬럼 동기화
				int changeCount = ResourceUtil.syncMenuColumnsWithEntity(id);
				// 2.3 메뉴 컬럼 캐쉬 클리어
				if(changeCount > 0) {
					BeanUtil.get(MenuController.class).clearCache();
				}
			}
		}
		
		return this.findMenuMeta(id, true, false, false, false, false, false);
	}
    
    /**
     * 메뉴 컬럼을 조회
     * 
     * @param id
     * @return
     */
	protected List<MenuColumn> searchMenuColumns(String id) {
		Query query = new Query();
		query.addFilter(new Filter(BaseConstants.FIELD_NAME_MENU_ID, id));
		query.addOrder(BaseConstants.FIELD_NAME_RANK, true);
		return this.queryManager.selectList(MenuColumn.class, query);
	}
	
	/**
	 * 메뉴 상세 정보 조회
	 * 
	 * @param id
	 * @return
	 */
	protected List<MenuDetail> searchMenuDetails(String id) {
		Query query = new Query();
		query.addFilter(new Filter(BaseConstants.FIELD_NAME_MENU_ID, id));
		return this.queryManager.selectList(MenuDetail.class, query);
	}
	
	/**
	 * 메뉴 버튼 조회
	 * 
	 * @param id
	 * @return
	 */
	protected List<MenuButton> searchMenuButtons(String id) {
		Query query = new Query();
		query.addOrder("rank", true);
		query.addFilter(new Filter(BaseConstants.FIELD_NAME_MENU_ID, id));
		return this.queryManager.selectList(MenuButton.class, query);
	}
	
	/**
	 * 메뉴 파라미터 조회
	 * 
	 * @param id
	 * @return
	 */
	protected List<MenuParam> searchMenuParams(String id) {
	    Query condition = AnyOrmUtil.newConditionForExecution(Domain.currentDomainId());
	    condition.addFilter("menuId", id);
	    condition.addOrder("name", true);
		return (List<MenuParam>)this.queryManager.selectList(MenuParam.class, condition);
	}
	
	/**
	 * Create Menu Column Objects From Resource Columns
	 *  
	 * @param menuId
	 * @param entityColumns
	 * @return
	 */
	protected List<MenuColumn> copyResourceColumn(String menuId, List<ResourceColumn> entityColumns) {
		List<MenuColumn> menuColumns = new ArrayList<MenuColumn>();
		for(ResourceColumn entityColumn : entityColumns) {
			MenuColumn menuColumn = new MenuColumn(menuId);
			menuColumn = ValueUtil.populate(entityColumn, menuColumn);
			menuColumn.setId(null);
			menuColumn.setCudFlag_(OrmConstants.CUD_FLAG_CREATE);
			menuColumns.add(menuColumn);
		}
		
		return menuColumns;
	}
	
	/**
	 * List 정렬
	 * 
	 * @param data
	 * @return
	 */
	protected List<?> sortRank(List<?> data) {
		data = data.stream().sorted((o1, o2) -> ((IOperatoConfig)o1).getRank() - ((IOperatoConfig)o2).getRank()).collect(Collectors.toList());
		return data;
	}
	
	
	/**
	 * menuColumns 컬럼들의 컬럼명을 번역한다.
	 * 
	 * @param menuColumns
	 */	
	protected void translateMenuColumnNames(List<MenuColumn> menuColumns) {
		String locale = SettingUtil.getUserLocale();
		for (MenuColumn column : menuColumns) {
			String termKey = (column.getTerm() == null) ? SysConstants.TERM_LABELS + column.getName() : column.getTerm();
			column.setTerm(MessageUtil.getLocaleTerm(locale, termKey, termKey));
		}
	}
	
	/**
	 * menuButtons 버튼들의 버튼명을 번역한다.
	 * 
	 * @param isTransTerm 버튼 번역 여부
	 * @param menuButtons
	 */
	protected void translateButtonNames(boolean isTransTerm, List<MenuButton> menuButtons) {
		String locale = SettingUtil.getUserLocale();
		for (MenuButton button : menuButtons) {
			String termKey = "button." + button.getText();
			button.setTitle(isTransTerm ? MessageUtil.getLocaleTerm(locale, termKey, termKey) : termKey);
		}
	}
	
	/**
	 * menuButtonList를 authList로 필터링한다.
	 * 
	 * @param menuButtonList
	 * @param authList
	 * @return
	 */
	protected List<MenuButton> filterButtonByAuth(List<MenuButton> menuButtonList, List<String> authList) {
		if(ValueUtil.isEmpty(authList)) {
			return new ArrayList<MenuButton>();
		}
		
		if(ValueUtil.isEqual(authList.get(0), BaseConstants.MENU_QUERY_ALL_MODE)) {
			return menuButtonList;
		}

		List<MenuButton> filteredList = new ArrayList<MenuButton>();
		for(MenuButton button : menuButtonList) {
			if(ValueUtil.isEmpty(button.getAuth()) || authList.contains(button.getAuth())) {
				filteredList.add(button);
			}
		}
		
		return filteredList;
	}	
	
	/**
	 * new menu button object
	 * 
	 * @param menuId
	 * @param text
	 * @param auth
	 * @param icon
	 * @param style
	 * @return
	 */
	protected MenuButton newMenuButton(String menuId, String text, String auth, String icon, String style) {
		MenuButton button = new MenuButton(menuId);
		button.setText(text);
		button.setAuth(auth);
		button.setIcon(icon);
		button.setStyle(style);
		button.setCudFlag_(OrmConstants.CUD_FLAG_CREATE);
		return button;
	}
	
	/**
	 * 메뉴 정보를 다른 도메인에 복사
	 * 
	 * @param menu
	 */
	protected void copyMenuDataToOtherDomains(Menu menu) {
		// 1. 메뉴 ID
		String id = menu.getId();
	    
	    // 2. 부모 메뉴
	    Menu parentMenu = this.queryManager.select(Menu.class, menu.getParentId());
        List<MenuColumn> menuColumns = this.searchMenuColumns(id);
        List<MenuButton> menuButtons = this.searchMenuButtons(id);
        List<MenuParam> menuParams = this.searchMenuParams(id);
        
        // 3. Domain 조회
        List<Domain> domains = this.queryManager.selectList(Domain.class, new Domain());
        
        // 4. 도메인 별로 동일한 이름의 메뉴 & 메뉴 컬럼 제거
        for(Domain domain : domains) {
            if(ValueUtil.isNotEqual(menu.getDomainId(), domain.getId())) {
                Menu mCondition = new Menu(domain.getId(), menu.getName());
                Menu domainMenu = this.queryManager.selectByCondition(Menu.class, mCondition);
                
                if(domainMenu != null) {
                    // 4.1 메뉴 컬럼 삭제
                    MenuColumn mcCondition = new MenuColumn(domainMenu.getId());
                    mcCondition.setDomainId(domain.getId());
                    this.queryManager.deleteList(MenuColumn.class, mcCondition);
                    
                    // 4.2 메뉴 버튼 삭제
                    MenuButton mbCondition = new MenuButton(domainMenu.getId());
                    mbCondition.setDomainId(domain.getId());
                    this.queryManager.deleteList(MenuButton.class, mbCondition);
                    
                    // 4.3 메뉴 파라미터 삭제
                    MenuParam mpCondition = new MenuParam();
                    mpCondition.setDomainId(domain.getId());
                    mpCondition.setMenuId(domainMenu.getId());
                    this.queryManager.deleteList(MenuParam.class, mpCondition);
                    
                    // 4.4 메뉴 삭제
                    this.queryManager.delete(domainMenu);
                }
            }
        }
        
        // 5. 도메인 별로 메뉴 복사
        for(Domain domain : domains) {
            if(ValueUtil.isNotEqual(menu.getDomainId(), domain.getId())) {
                // 부모 메뉴 조회
                Menu mpCondition = new Menu(domain.getId(), parentMenu.getName());
                Menu domainParent = this.queryManager.selectByCondition(Menu.class, mpCondition);
                List<MenuColumn> newCols = new ArrayList<MenuColumn>(menuColumns.size());
                List<MenuButton> newBtns = new ArrayList<MenuButton>(menuButtons.size());
                List<MenuParam> newPrms = new ArrayList<MenuParam>(menuParams.size());
                
                // 메뉴 생성
                Menu domainMenu = ValueUtil.populate(menu, new Menu());
                domainMenu.setDomainId(domain.getId());
                domainMenu.setId(null);
                domainMenu.setParentId(domainParent != null ? domainParent.getId() : null);
                this.queryManager.insert(domainMenu);
                
                // 메뉴 컬럼 생성
                for(MenuColumn col : menuColumns) {
                    MenuColumn domainMenuCol = ValueUtil.populate(col, new MenuColumn());
                    domainMenuCol.setDomainId(domain.getId());
                    domainMenuCol.setMenuId(domainMenu.getId());
                    domainMenuCol.setId(null);                    
                    newCols.add(domainMenuCol);
                }
                
                // 메뉴 버튼 생성
                for(MenuButton btn : menuButtons) {
                    MenuButton domainMenuBtn = ValueUtil.populate(btn, new MenuButton());
                    domainMenuBtn.setDomainId(domain.getId());
                    domainMenuBtn.setMenuId(domainMenu.getId());
                    domainMenuBtn.setId(null);                    
                    newBtns.add(domainMenuBtn);
                }
                
                // 메뉴 파라미터 생성
                for(MenuParam prm : menuParams) {
                    MenuParam domainMenuPrm = ValueUtil.populate(prm, new MenuParam());
                    domainMenuPrm.setDomainId(domain.getId());
                    domainMenuPrm.setMenuId(domainMenu.getId());
                    domainMenuPrm.setId(null);
                    newPrms.add(domainMenuPrm);
                }
                
                // 메뉴 컬럼, 메뉴 버튼, 메뉴 파라미터 생성
                this.queryManager.insertBatch(newCols);
                this.queryManager.insertBatch(newBtns);
                this.queryManager.insertBatch(newPrms);
            }
        }
	}
	
	/**
	 * menuColumns 컬럼들 중 Search 폼 편집기나 Grid 편집기가 CodeCombo인 경우 해당 컬럼에 코드 데이터를 추가한다.
	 * 
	 * @param menuColumns
	 * @param includeSearchField 검색 폼에도 적용할 지 여부
	 */
	protected void fillCodeData(List<MenuColumn> menuColumns, boolean includeSearchField) {
		// 서버 사이드에서 참조형 데이터를 채워 리턴하는 설정인 경우만 처리
		if(ValueUtil.toBoolean(SettingUtil.getValue(BaseConfigConstants.CODE_COMBO_DATA_FILL_AT_SERVER, SysConstants.TRUE_STRING))) {
			// 캐쉬 적용을 위해 참조형 엔티티 컨트롤러를 빈으로 준비
			CodeController codeCtrl = BeanUtil.get(CodeController.class);
			ResourceController entityCtrl = BeanUtil.get(ResourceController.class);
			RoleController roleCtrl = BeanUtil.get(RoleController.class);
		
			// 메뉴 컬럼 순회하면서 참조형 정보에 대해서만 처리
			for (MenuColumn column : menuColumns) {
				String refType = column.getRefType();
				String refName = column.getRefName();
				String gridEditor = column.getGridEditor();
				String searchEditor = column.getSearchEditor();
				String formEditor = column.getFormEditor();
				
				// 1. 참조형 설정이 유효하지 않은 경우 스킵
				if (ValueUtil.isEmpty(refType) || ValueUtil.isEmpty(refName) || (ValueUtil.isEmpty(gridEditor) && ValueUtil.isEmpty(searchEditor) && ValueUtil.isEmpty(formEditor))) {
					continue;
				}
				
				// 2. 참조 타입이 공통 코드
				if (ValueUtil.isEqual(BaseConstants.REF_TYPE_COMMON_CODE, refType)) {
					if((ValueUtil.isNotEmpty(gridEditor) && gridEditor.startsWith(BaseConstants.GRID_CODE_EDITOR_PREFIX)) 
							|| (ValueUtil.isNotEmpty(formEditor) && formEditor.startsWith(BaseConstants.GRID_CODE_EDITOR_PREFIX))
							|| ((includeSearchField && ValueUtil.isNotEmpty(searchEditor) && searchEditor.startsWith(BaseConstants.GRID_CODE_EDITOR_PREFIX)))) {
						Code code = codeCtrl.findOne(SysConstants.SHOW_BY_NAME_METHOD, refName);
						
						if(ValueUtil.isNotEmpty(gridEditor)) column.setGridCodeList(code.getItems());
						if(ValueUtil.isNotEmpty(formEditor)) column.setFormCodeList(code.getItems());
						if(includeSearchField && ValueUtil.isNotEmpty(searchEditor)) column.setSearchCodeList(code.getItems());
					}
					
				// 3. 참조 타입이 엔티티
				} else if(ValueUtil.isEqual(BaseConstants.REF_TYPE_ENTITY, refType)) {
					List<String> editorTypes = ValueUtil.newStringList(gridEditor, formEditor, searchEditor);
					
					if(editorTypes.contains("resource-code") || editorTypes.contains("resource-id")) {
						List<CodeDetail> codeItems = entityCtrl.searchResourceDataAsCodeByMenuColumn(column);
						
						if(ValueUtil.isEqual(gridEditor, "resource-code") || ValueUtil.isEqual(gridEditor, "resource-id")) column.setGridCodeList(codeItems);
						if(ValueUtil.isEqual(formEditor, "resource-code") || ValueUtil.isEqual(formEditor, "resource-id")) column.setFormCodeList(codeItems);
						if(includeSearchField && (ValueUtil.isEqual(searchEditor,"resource-code") || ValueUtil.isEqual(searchEditor, "resource-id"))) column.setSearchCodeList(codeItems);
					}
					
					if(editorTypes.contains("relate-resource-code")) {
						List<CodeDetail> codeItems = entityCtrl.searchResourceDataAsCodeByMenuColumn(column);
						
						if(ValueUtil.isEqual(gridEditor, "relate-resource-code")) column.setGridCodeList(codeItems);
						if(ValueUtil.isEqual(formEditor, "relate-resource-code")) column.setFormCodeList(codeItems);
						if(includeSearchField && ValueUtil.isEqual(searchEditor,"relate-resource-code")) column.setSearchCodeList(codeItems);
					}

					
					if(editorTypes.contains("permit-resource-code")) {
						List<CodeDetail> codeItems = roleCtrl.getEntityPermittedDataByUserAsCode(refName);
						
						if(ValueUtil.isEqual(gridEditor, "permit-resource-code")) column.setGridCodeList(codeItems);
						if(ValueUtil.isEqual(formEditor, "permit-resource-code")) column.setFormCodeList(codeItems);
						if(includeSearchField && ValueUtil.isEqual(searchEditor, "permit-resource-code")) column.setSearchCodeList(codeItems);
					}
					
				// 4. 참조 타입이 URL
				} else if(ValueUtil.isEqual(BaseConstants.REF_TYPE_URL, refType)) {
					// TODO 참조 타입이 URL인 경우 - URL을 호출하여 CodeDetail 형태로 변환하여 리턴 ...
					
				}
			}
		}
	}
	
	/**
	 * 메뉴 용어 등록
	 * 
	 * @param id
	 */
    protected void registerMenuTerminologies(String id) {
    	// 1. 메뉴 컨트롤러 추출
    	MenuController menuCtrl = BeanUtil.get(MenuController.class);
    	
        // 2. 메뉴 컬럼 조회
        Menu menu = menuCtrl.findOne(id, null);
        Long domainId = menu.getDomainId();
        List<String> unregTerms = new ArrayList<String>();
        
        // 3. 메뉴 타이틀 체크
        String menuTermKey = "menu." + menu.getName();
        String title = MessageUtil.getTerm(menuTermKey);
        if(ValueUtil.isEmpty(title) || title.startsWith("menu.")) {
            unregTerms.add("menu___" + menu.getName());
        }
        
        // 4. 메뉴 컬럼 용어 체크
        List<MenuColumn> columns = menuCtrl.findMenuColumns(id);
        for(MenuColumn column : columns) {
            String unregTerm = ResourceUtil.translateMenuColumn(column, true);
            
            if(unregTerm != null) {
                String term = "label___" + unregTerm;
                if(!unregTerms.contains(term)) {
                    unregTerms.add(term);
                }
            }
        }

        // 5. 메뉴 버튼 용어 체크
        List<MenuButton> buttons = menuCtrl.findMenuButtons(id);
        
        for(MenuButton column : buttons) {
            String unregTerm = ResourceUtil.translateMenuButton(column);
            
            if(unregTerm != null) {
                String term = "button___" + unregTerm;
                if(!unregTerms.contains(term)) {
                    unregTerms.add(term);
                }
            }
        }

        // 6. 등록되지 않은 용어가 있다면 등록
        if(!unregTerms.isEmpty()) {
            // 6.1 로케일 리스트 조회
            Code code = BeanUtil.get(CodeController.class).findByName(domainId, SysConstants.LANG_EN, SysConstants.LANGUAGE_CODE);
            List<CodeDetail> codeList = code.getItems();
            
            // 6.2 용어를 로케일당 하나씩 등록
            List<Terminology> newTerms = new ArrayList<Terminology>();
            for(String termKey : unregTerms) {
                for(CodeDetail cd : codeList) {
                    String[] termKeyArr = termKey.split("___");
                    String category = termKeyArr[0];
                    String name = termKeyArr[1];
                    newTerms.add(new Terminology(domainId, name, cd.getName(), category, "terms" + SysConstants.DOT + category + SysConstants.DOT + name));
                }
            }
            
            AnyOrmUtil.insertBatch(newTerms, 100);
        }
        
        // 7. 변경된 컬럼 저장
        this.cudMultipleData(MenuColumn.class, columns);
    }
    
	/**
	 * menuList에 menu title 다국어를 모두 적용 
	 * 
	 * @param isTransTerm 다국어 번역 여부
	 * @param menuList 메뉴 리스트
	 */
	protected void applyMenuTitle(boolean isTransTerm, List<Menu> menuList) {
		String locale = SettingUtil.getUserLocale();
		
		if(ValueUtil.isNotEmpty(menuList)) {
			for (Menu menu : menuList) {
				String title = isTransTerm ? MessageUtil.getLocaleTerm(locale, MessageUtil.getMenuTermKey(menu.getName()), menu.getName()) : "menu." + menu.getName();
				menu.setTitle(title);
			}
		}
	}
	
	/**
	 * convert template
	 * 
	 * @param template
	 * @param templateParams
	 * @return
	 */
	protected String convertTemplate(String template, Map<String, Object> templateParams) {
		StringWriter writer = new StringWriter();
		this.templateEngine.processTemplate(template, writer, templateParams, null);
		return writer.toString();
	}
	
	/**
	 * 메뉴, 메뉴 버튼, 메뉴 컬럼, 메뉴 파라미터 모두 캐쉬 클리어
	 * 
	 * @return
	 */
	public boolean clearCache() {
		MenuController menuCtrl = BeanUtil.get(MenuController.class);
		menuCtrl.clearMenuCache();
		menuCtrl.clearMenuColumnsCache();
		menuCtrl.clearMenuButtonsCache();
		menuCtrl.clearMenuParamsCache();
		return true;
	}
}
