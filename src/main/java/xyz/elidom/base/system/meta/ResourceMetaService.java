package xyz.elidom.base.system.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import xyz.anythings.sys.model.IOperatoConfig;
import xyz.anythings.sys.model.OperatoAction;
import xyz.anythings.sys.model.OperatoFormField;
import xyz.anythings.sys.model.OperatoGridColumn;
import xyz.anythings.sys.model.OperatoMenuInfo;
import xyz.anythings.sys.model.OperatoSearchField;
import xyz.anythings.sys.model.OperatoSearchHiddenField;
import xyz.anythings.sys.model.OperatoSortField;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.base.BaseConfigConstants;
import xyz.elidom.base.BaseConstants;
import xyz.elidom.base.entity.Menu;
import xyz.elidom.base.entity.MenuButton;
import xyz.elidom.base.entity.MenuColumn;
import xyz.elidom.base.entity.Resource;
import xyz.elidom.base.entity.ResourceColumn;
import xyz.elidom.base.model.EntityIndex;
import xyz.elidom.base.rest.DdlController;
import xyz.elidom.base.rest.ResourceController;
import xyz.elidom.base.util.ResourceUtil;
import xyz.elidom.core.CoreConstants;
import xyz.elidom.core.entity.Code;
import xyz.elidom.core.entity.CodeDetail;
import xyz.elidom.core.rest.CodeController;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.ddl.Ddl;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.dbist.metadata.Table;
import xyz.elidom.msg.entity.Terminology;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.util.DdlUtil;
import xyz.elidom.sec.rest.RoleController;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ClassUtil;

/**
 * 엔티티 관련 메타 정보 제공 서비스
 */
public class ResourceMetaService extends AbstractRestService {

	/**
	 * index 메소드 Default Sort
	 */
	protected static final String INDEX_DEFAULT_SORT = "[{\"field\": \"name\", \"ascending\": true}]";
	/**
	 * indexWithDetails 메소드 Default Sort
	 */
	protected static final String INDEX_WITH_DETAILS_DEFAULT_SORT = "[{\"field\" : \"masterId\", \"ascending\": false}]";
	/**
	 * indexWithDetails 메소드 Default Sort
	 */
	protected static final String QUERY_SELECT_ID_BY_ENTITY_NAME = "SELECT ID FROM ENTITIES WHERE DOMAIN_ID = :domainId AND NAME = :name";
	
	@Override
	protected Class<?> entityClass() {
		return Resource.class;
	}
	
	/**
	 * Excel 내보내기
	 * 
	 * @param bundle
	 * @return
	 */
	public Workbook exportExcel(String bundle) {
		Resource condition = new Resource(Domain.currentDomainId(), null);
		condition.setBundle(bundle);
		List<Resource> entityList = this.queryManager.selectList(Resource.class, condition);
		ResourceController rscCtrl = BeanUtil.get(ResourceController.class);
		Resource entityRsc = rscCtrl.findOne(SysConstants.SHOW_BY_NAME_METHOD, "Entity");
		Resource entityColRsc = rscCtrl.findOne(SysConstants.SHOW_BY_NAME_METHOD, "ResourceColumn");
		entityRsc = rscCtrl.resourceColumns(entityRsc.getId());
		entityColRsc = rscCtrl.resourceColumns(entityColRsc.getId());
		Workbook workbook = ResourceUtil.exportEntitiesToExcel(entityList, entityRsc.resourceColumns(), entityColRsc.resourceColumns());
		return workbook;
	}
	
	/**
	 * Update Multiple Data
	 * 
	 * @param resourceList
	 * @return
	 */
	public Boolean updateMultipleData(List<Resource> resourceList) {
		ResourceController ctrl = BeanUtil.get(ResourceController.class);
		
		for (Resource r : resourceList) {
			if (ValueUtil.isEqual(r.getCudFlag_(), CoreConstants.CUD_FLAG_DELETE)) {
				ctrl.delete(r.getId());
			}
		}
		
		for (Resource r : resourceList) {
			if (ValueUtil.isEqual(r.getCudFlag_(), CoreConstants.CUD_FLAG_UPDATE)) {
				ctrl.update(r.getId(), r);
			}
		}
		
		for (Resource r : resourceList) {
			if (ValueUtil.isEqual(r.getCudFlag_(), CoreConstants.CUD_FLAG_CREATE)) {
				ctrl.create(r);
			}
		}
		
		return true;
	}

	/**
	 * Resource Code 컴포넌트용 데이터 조회 - menuColumn으로 리소스 정보를 조회한다.
	 *  
	 * @param menuColumn
	 * @return
	 */
	public List<CodeDetail> searchResourceDataAsCodeByMenuColumn(MenuColumn menuColumn) {
		return this.searchResourceDataAsCodeBy(menuColumn.getRefName(), menuColumn.getRefParams());
	}
	
	/**
	 * Resource Code 컴포넌트용 데이터 조회 - entityColumn으로 리소스 정보를 조회한다.
	 * 
	 * @param entityColumn
	 * @return
	 */
	public List<CodeDetail> searchResourceDataAsCodeByEntityColumn(ResourceColumn entityColumn) {
		return this.searchResourceDataAsCodeBy(entityColumn.getRefName(), entityColumn.getRefParams());
	}
		
	/**
	 * Resource Code 컴포넌트용 데이터 조회 - entityName, searchParamStr으로 리소스 정보를 조회한다.
	 * 
	 * @param entityName
	 * @param searchParamStr
	 * @return
	 */
	public List<CodeDetail> searchResourceDataAsCodeBy(String entityName, String searchParamStr) {
		ResourceController ctrl = BeanUtil.get(ResourceController.class);
		Map<String, Object> searchParams = null;
		List<String> addSelectFields = null;
		
		if(ValueUtil.isNotEmpty(searchParamStr)) {
			searchParams = new HashMap<String, Object>();
			addSelectFields = new ArrayList<String>();
			String[] refParamsArr = searchParamStr.split(SysConstants.COMMA);
			
			for(int i = 0 ; i < refParamsArr.length ; i++) {
				String refParam = refParamsArr[i];
				String[] refParamArr = refParam.split("=");
				
				if(refParamArr[1].contains(SysConstants.COLON) == false) {
					searchParams.put(refParamArr[0], refParamArr[1]);
				} else {
					addSelectFields.add(refParamArr[0]);
				}
			}
		}
		
		Resource entity = ctrl.findOne(SysConstants.SHOW_BY_NAME_METHOD, entityName);
		String entityClassName = ResourceUtil.getEntityClassName(entity);
		Class<?> entityClass = ClassUtil.forName(entityClassName);
		boolean domainBased = ClassUtil.hasField(entityClass, SysConstants.ENTITY_FIELD_DOMAIN_ID);
		return ResourceUtil.searchRecordsAsCode(entity, domainBased, searchParams, addSelectFields);
	}
	
	/**
	 * entityColumns 컬럼들 중 Grid 편집기가 CodeCombo인 경우 해당 컬럼에 코드 데이터를 추가한다. 
	 * 
	 * @param entityColumns
	 */
	protected void fillCodeData(List<ResourceColumn> entityColumns) {
		if(ValueUtil.toBoolean(SettingUtil.getValue(BaseConfigConstants.CODE_COMBO_DATA_FILL_AT_SERVER, SysConstants.TRUE_STRING))) {
			CodeController codeCtrl = BeanUtil.get(CodeController.class);
			RoleController roleCtrl = BeanUtil.get(RoleController.class);

			for (ResourceColumn column : entityColumns) {
				String refName = column.getRefName();
				String gridEditor = column.getGridEditor();
				String searchEditor = column.getSearchEditor();
				
				if(ValueUtil.isEqual(BaseConstants.REF_TYPE_COMMON_CODE, column.getRefType()) && ValueUtil.isNotEmpty(column.getRefName())) {
					if((ValueUtil.isNotEmpty(gridEditor) && gridEditor.startsWith(BaseConstants.GRID_CODE_EDITOR_PREFIX)) 
							|| (ValueUtil.isNotEmpty(searchEditor) && searchEditor.startsWith(BaseConstants.GRID_CODE_EDITOR_PREFIX))) {
						Code code = codeCtrl.findOne(SysConstants.SHOW_BY_NAME_METHOD, refName);
						
						if(ValueUtil.isNotEmpty(gridEditor)) column.setGridCodeList(code.getItems());
						if(ValueUtil.isNotEmpty(searchEditor)) column.setSearchCodeList(code.getItems());
					}
				}
				
				
				if(ValueUtil.isEqual(BaseConstants.REF_TYPE_ENTITY, column.getRefType()) && ValueUtil.isNotEmpty(column.getRefName())) {
					if(ValueUtil.isNotEmpty(gridEditor) || ValueUtil.isNotEmpty(searchEditor)) {
						List<String> editorTypes = ValueUtil.newStringList(gridEditor, searchEditor);
						if(editorTypes.indexOf("resource-code") > -1) {
							List<CodeDetail> codeItems = this.searchResourceDataAsCodeBy(refName, column.getRefParams());
							
							if(ValueUtil.isEqual(gridEditor, "resource-code")) column.setGridCodeList(codeItems);
							if(ValueUtil.isEqual(searchEditor, "resource-code")) column.setSearchCodeList(codeItems);
						}
						
						if(editorTypes.indexOf("permit-resource-code") > -1) {
							List<CodeDetail> codeItems = roleCtrl.getEntityPermittedDataByUserAsCode(refName);
							
							if(ValueUtil.isEqual(gridEditor, "permit-resource-code")) column.setGridCodeList(codeItems);
							if(ValueUtil.isEqual(searchEditor, "permit-resource-code")) column.setSearchCodeList(codeItems);
						}
					}
				}
			}
		}
	}
	
	/**
	 * entityColumns 컬럼들의 컬럼명을 번역한다.
	 * 
	 * @param locale
	 * @param entityColumns
	 */	
	protected void translateEntityColumnNames(String locale, List<ResourceColumn> entityColumns) {
		for (ResourceColumn column : entityColumns) {
			String termKey = ValueUtil.isEmpty(column.getTerm()) ? SysConstants.TERM_LABELS + column.getName() : column.getTerm();
			column.setTerm(MessageUtil.getLocaleTerm(locale, termKey, termKey));
		}
	}
	
	/**
	 * 리소스 메타 & 리소스 컬럼 메타 정보 리턴
	 * 
	 * @param domainId
	 * @param name
	 * @param columnsOff
	 * @return
	 */
	public Resource getResourceMeta(Long domainId, String name, boolean columnsOff) {
		// 1. entityName으로 부터 id 추출
		ResourceController ctrl = BeanUtil.get(ResourceController.class);
		Map<String, Object> entityParams = ValueUtil.newMap(OrmConstants.ENTITY_DOMAIN_ID_AND_NAME, domainId, name);
		
		String id = this.queryManager.selectBySql(QUERY_SELECT_ID_BY_ENTITY_NAME, entityParams, String.class);
		if(id == null && ValueUtil.isEqual(name, "Resource")) {
			name = "Entity";
			entityParams.put("name", name);
			id = this.queryManager.selectBySql(QUERY_SELECT_ID_BY_ENTITY_NAME, entityParams, String.class);
		}
		
		// 2. Find 부분만 캐쉬 적용
		Resource resource = ctrl.findOne(id, null);
		List<ResourceColumn> columns = columnsOff ? new ArrayList<ResourceColumn>() : resource.resourceColumns();
		String locale = User.currentUser().getLocale();
		resource.setTitle(MessageUtil.getTermByCategories(locale, name, OrmConstants.LABEL_KEY, BaseConstants.FIELD_NAME_TITLE, BaseConstants.FIELD_NAME_MENU));
		
		// 3. 아래 코드 및 용어 및 공통 코드를 조회 - 이 부분 때문에 캐쉬 적용 안 함 
		if (ValueUtil.isNotEmpty(columns)) {
			this.translateEntityColumnNames(locale, columns);
			this.fillCodeData(columns);
			resource.setItems(columns);
		}
		
		// 4. 결과 리턴
		return resource;
	}
	
	/**
	 * 엔티티 정보로 부터 화면 구성을 위한 메타 정보를 빌드하여 리턴
	 * 
	 * @param entity
	 * @param codeOnSearchForm
	 * @param columnsOff
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getScreenMeta(Resource entity, boolean codeOnSearchForm, boolean columnsOff) {
		// 0. 엔티티 컬럼 조회
		entity.resourceColumns();
		
		// 1. 엔티티 메타 데이터 조회
		Map<String, Object> menuMeta = this.toMenuMeta(entity, BaseConstants.MENU_OBJECT_MENU_NAME, BaseConstants.MENU_OBJECT_COLUMNS_NAME, columnsOff, codeOnSearchForm);
		
		// 2. 메뉴 컬럼
		List<MenuColumn> menuColumns = (List<MenuColumn>)menuMeta.get(BaseConstants.MENU_OBJECT_COLUMNS_NAME);
		
		// 3. 메뉴명 번역
		Menu menu = (Menu)menuMeta.get(BaseConstants.MENU_OBJECT_MENU_NAME);
		
		// 4. 리턴 결과 셋
		Map<String, Object> result = new HashMap<String, Object>();
		
		// 5. 메뉴 컬럼을 그리드 컬럼, 검색 폼, 기본 정렬 순서, 조회 컬럼 데이터 모델로 가공 준비
		List<OperatoGridColumn> gridConfigs = new ArrayList<OperatoGridColumn>();
		List<OperatoSearchHiddenField> searchHiddenFields = new ArrayList<OperatoSearchHiddenField>();
		List<OperatoSortField> sortFields = new ArrayList<OperatoSortField>();
		List<OperatoSearchField> searchFormFields = new ArrayList<OperatoSearchField>();
		List<String> selectFields = new ArrayList<String>();
		
		// 6. 메뉴 컬럼 정보들을 순회하면서 필요한 가공 처리
		for(MenuColumn column : menuColumns) {
			String colName = column.getName();
			String gridEditor = column.getGridEditor();
			Integer searchRank = column.getSearchRank();
			Integer gridRank = column.getGridRank();
			Integer sortRank = column.getSortRank();
			
			// 6.1 검색 폼 필드 구성
			if(ValueUtil.isNotEmpty(searchRank) && searchRank > 0) {
				if(ValueUtil.isEqualIgnoreCase(column.getSearchEditor(), "hidden")) {
					searchHiddenFields.add(new OperatoSearchHiddenField(column));
				} else {
					searchFormFields.add(new OperatoSearchField(column));
				}
			}
			
			// 6.2 그리드 컬럼 구성 조건 필터링
			if(ValueUtil.isEqualIgnoreCase("id", colName) && (ValueUtil.isEmpty(gridRank) || gridRank == 0)) {
				column.setGridRank(-10);
				selectFields.add(colName);
			}
			
			// 6.3 그리드 구성을 위한 컬럼 모델 구성
			if(ValueUtil.isNotEmpty(gridRank) && gridRank != 0) {
				OperatoGridColumn gridCol = new OperatoGridColumn(column, false);
				gridConfigs.add(gridCol);
				
				// 그리드 컬럼이 가상 컬럼이 아니라면 조회 대상 컬럼 구성
				if(ValueUtil.toBoolean(column.getVirtualField(), false) == false) {
					// 오브젝트 참조 타입의 경우 오브젝트 명으로 추가 
					if(ValueUtil.isEqualIgnoreCase(gridEditor, "resource-column") || 
					   ValueUtil.isEqualIgnoreCase(gridEditor, "resource-selector")) {
						selectFields.add(gridCol.getName());
					} else {
						selectFields.add(colName);
					}
				}
			}

			// 6.4 그리드 기본 정렬 조건 구성
			if(ValueUtil.isNotEmpty(sortRank) && sortRank != 0) {
				sortFields.add(new OperatoSortField(column));
			}
		}
		
		// 7. 최종 화면 메타 스킴으로 변환하여 리턴
		result.put("use_add_button", false); 									// 행 추가 버튼 사용 여부
		result.put("form_fields", new ArrayList<OperatoFormField>());			// 상세 폼 필드
		result.put("actions", new ArrayList<OperatoAction>()); 					// 버튼
		result.put("menu_params", null); 										// 메뉴 파라미터
		result.put("menu", new OperatoMenuInfo(menu)); 							// 메뉴 정보
		result.put("select_fields", selectFields); 								// 조회 대상 컬럼
		result.put("search_form_fields", this.sortRank(searchFormFields)); 		// 검색 폼 필드
		result.put("sort_fields", this.sortRank(sortFields)); 					// 기본 정렬 값
		result.put("search_hidden_fields", searchHiddenFields); 				// 기본 검색 조건 값
		result.put("grid_config", this.sortRank(gridConfigs)); 					// 그리드 설정
		return result;
	}
	
	/**
	 * 엔티티 메타를 메뉴 메타로 변환
	 * 
	 * @param entity
	 * @param menuName
	 * @param columnName
	 * @param columnsOff
	 * @param codeOnSearchForm
	 * @return
	 */
	protected Map<String, Object> toMenuMeta(Resource entity, String menuName, String columnName, boolean columnsOff, boolean codeOnSearchForm) {
		// 1. 결과 셋
		Map<String, Object> result = new HashMap<String, Object>();
		
		// 2. 메뉴 컬럼 조회
		List<ResourceColumn> rscColumns = columnsOff ? new ArrayList<ResourceColumn>() : entity.getItems();
		
		// 3. 코드 참조 컬럼인 경우 코드 정보를 조회하여 컬럼에 추가 정보로 설정한다.
		if(!ValueUtil.isEmpty(rscColumns) && codeOnSearchForm) {
			this.fillCodeData(rscColumns);
		}
		
		// 4. 엔티티 컬럼을 메뉴 컬럼으로 복사
		List<MenuColumn> menuColumns = new ArrayList<MenuColumn>(rscColumns.size());
		for(ResourceColumn rc : rscColumns) {
			MenuColumn mc = ValueUtil.populate(rc, new MenuColumn());
			menuColumns.add(mc);
		}
				
		// 5. menu buttons. 사용자의 메뉴 권한에 따라 메뉴 버튼을 필터링한다.
		result.put(BaseConstants.MENU_OBJECT_BUTTONS_NAME, new ArrayList<MenuButton>());
		
		// 6. menu
		Menu menu = ValueUtil.populate(entity, new Menu(), "id", "name", "description", "idField", "titleField", "descField");
		menu.setResourceUrl(entity.getSearchUrl());
		result.put(menuName, menu);
		
		// 7. menu columns
		result.put(columnName, menuColumns);

		// 8. 결과 리턴
		return result;
	}
	
	/**
	 * List 정렬, 내장된 rank 값을 비교해 정렬
	 * 
	 * @param data
	 * @return
	 */
	protected List<?> sortRank(List<?> data) {
		data = data.stream().sorted((o1, o2) -> ((IOperatoConfig)o1).getRank() - ((IOperatoConfig)o2).getRank()).collect(Collectors.toList());
		return data;
	}
	
	/**
	 * 엔티티 컬럼 정보 업데이트 처리
	 * 
	 * @param resource
	 * @param resourceColumnList
	 * @return
	 */
	protected Resource updateResourceColumns(Resource resource, List<ResourceColumn> resourceColumnList) {
		boolean isComplexKeyType = ValueUtil.isEqual(resource.getIdType(), GenerationRule.COMPLEX_KEY);
		int rank = 10;
		
		// 기본 필드에 대해서 Validation
		for (ResourceColumn column : resourceColumnList) {
			// ID Type이 Complex-key일 경우 가상 필드로 설정
			if (isComplexKeyType && column.getName().equalsIgnoreCase(SysConstants.TABLE_FIELD_ID)) {
				column.setVirtualField(true);
			}
			
			column.setColType(ValueUtil.isEmpty(column.getColType()) ? SysConstants.DATA_TYPE_STRING : column.getColType());
			rank = (column.getRank() == null || column.getRank() == 0) ? rank + 10 : column.getRank();
			column.setRank(rank);
			
			if(ValueUtil.isEqualIgnoreCase(SysConstants.DATA_TYPE_STRING, column.getColType()) && (column.getColSize() == null || column.getColSize() == 0)) {
				column.setColSize(50);
			}

			ResourceUtil.setDefaultColumnInfo(resource, column);
		}
		
		this.cudMultipleData(ResourceColumn.class, resourceColumnList);
		resource.resourceColumns();
		return resource;
	}
	
	/**
	 * 엔티티 기본 컬럼 추가
	 * 
	 * @param resource
	 * @return
	 */
	protected Resource createDefaultResourceColumns(Resource resource) {
		// Column 추출
		List<ResourceColumn> columns = resource.resourceColumns();
		String id = resource.getId();
		int rank = 0;
		
		// 기본 정보 설정
		if(ValueUtil.isEmpty(columns)) {
			columns = new ArrayList<ResourceColumn>(5);
			
			for(String defaultField : CoreConstants.TABLE_FIELD_DEFAULT_LIST) {
				if(ValueUtil.isEqual(defaultField, SysConstants.TABLE_FIELD_ID) || defaultField.indexOf(SysConstants.CHAR_UNDER_SCORE) > 0) {
					rank = rank + 10;
					ResourceColumn column = new ResourceColumn(id, defaultField);
					ResourceUtil.setDefaultColumnInfo(resource, column);
					column.setRank(rank);
					columns.add(column);
				}
			}
			
			this.queryManager.insertBatch(columns);
			
		// 용어, 랭킹 정보, 설명 등이 없다면 기본 정보 설정
		} else {
			for(String defaultField : CoreConstants.TABLE_FIELD_DEFAULT_LIST) {
				if(ValueUtil.isEqual(defaultField, SysConstants.TABLE_FIELD_ID) || defaultField.indexOf(SysConstants.CHAR_UNDER_SCORE) > 0) {
					ResourceColumn c = null;
					
					for(ResourceColumn column : columns) {
						if(ValueUtil.isEqualIgnoreCase(defaultField, column.getName())) {
							c = column;
							break;
						}
					}
					
					if(c == null) {
						c = new ResourceColumn(id, defaultField);
						columns.add(c);
					}
				}
			}
			
			for(ResourceColumn column : columns) {
				rank = rank + 10;
				column.setRank(rank);
				ResourceUtil.setDefaultColumnInfo(resource, column);
			}
			
			this.queryManager.upsertBatch(columns);
		}
		
		return resource;
	}
	
	/**
	 * 엔티티 컬럼별 용어 생성
	 * 
	 * @param resource
	 */
    protected void translateResourceColumns(Resource resource) {
        // 1. Resource 조회
        Long domainId = resource.getDomainId();
        
        // 2. 리소스 컬럼 조회
        List<ResourceColumn> columns = resource.resourceColumns();
        List<String> unregTerms = new ArrayList<String>();
        
        for(ResourceColumn column : columns) {
            String unregTerm = ResourceUtil.translateResourceColumn(column, true);
            
            if(unregTerm != null) {
                unregTerms.add(unregTerm);
            }
        }

        // 3. 변경된 컬럼 저장
        this.cudMultipleData(ResourceColumn.class, columns);
        
        // 4. 용어 처리
        if(!unregTerms.isEmpty()) {
            Code code = BeanUtil.get(CodeController.class).findByName(domainId, SysConstants.LANG_EN, SysConstants.LANGUAGE_CODE);
            List<CodeDetail> codeList = code.getItems();
            List<Terminology> newTerms = new ArrayList<Terminology>(unregTerms.size() * codeList.size());
             
            for(String name : unregTerms) {
                for(CodeDetail cd : codeList) {
                    newTerms.add(new Terminology(domainId, name, cd.getName(), SysConstants.LABEL_KEY, SysConstants.LABEL_KEY + SysConstants.DOT + name));
                }
            }
            
            AnyOrmUtil.insertBatch(newTerms, 100);
        }
    }
	
	/**
	 * entityId로 엔티티를 조회하여 엔티티 명과 동일한 메뉴를 찾아서 메뉴 정보를 소스로 엔티티 정보를 타겟으로 동기화
	 * 
	 * @param entityId
	 */
	public void syncResourceFromMenu(String entityId) {
		// 1. Resource 조회
		Resource resource = this.queryManager.select(Resource.class, entityId);
		
		// 2. 메뉴 조회
		Menu condition = new Menu(resource.getDomainId(), resource.getName());
		Menu menu = this.queryManager.selectByCondition(Menu.class, condition);
		
		if(menu != null) {
			resource.resourceColumns();
			List<ResourceColumn> items = resource.getItems();
			
			Query mcCond = AnyOrmUtil.newConditionForExecution(resource.getDomainId());
			mcCond.addFilter("menuId", menu.getId());
			mcCond.addOrder("rank", true);
			List<MenuColumn> menuCols = this.queryManager.selectList(MenuColumn.class, mcCond);
			
			for(MenuColumn menuCol : menuCols) {
				ResourceColumn rc = null;
				for(ResourceColumn rscCol : items) {
					if(ValueUtil.isEqualIgnoreCase(rscCol.getName(), menuCol.getName())) {
						rc = rscCol;
						break;
					}
				}
				
				if(rc == null) {
					rc = ValueUtil.populate(menuCol, new ResourceColumn());
					rc.setEntityId(resource.getId());
					rc.setId(null);
					this.queryManager.insert(rc);
				} else {
					String rcId = rc.getId();
					ResourceColumn newRc = ValueUtil.populate(menuCol, rc);
					newRc.setEntityId(resource.getId());
					newRc.setId(rcId);
					this.queryManager.update(newRc);
				}
			}
		} else {
			throw ThrowUtil.newValidationErrorWithNoLog("Menu named [" + resource.getName() + "] doesn't exist!");
		}
	}
	
	/**
	 * EntityClass 기준으로 테이블 컬럼, 엔티티 컬럼을 동기화 
	 * 
	 * @param id
	 * @param resouceColumnSync
	 * @param menuColumnSync
	 * @return
	 */
	protected int syncTableAndResourceColumns(String id, boolean resouceColumnSync, boolean menuColumnSync) {
		// 1. Resource 데이터 추출
		Resource resource = this.queryManager.select(Resource.class, id);
		Class<?> entityClass = Resource.findClassByEntityName(resource.getName());
		
		// 2. 엔티티 - 테이블 컬럼 동기화
		Table domainTable = BeanUtil.get(Ddl.class).getTable(Domain.class);
		int changeCount = DdlUtil.syncEntityColumns(domainTable.getDomain(), entityClass);
		
		// 3. 엔티티 - 엔티티 컬럼 동기화
		if(resouceColumnSync) {
			ResourceUtil.syncEntityColumnsWithEntity(entityClass, resource);
		}
		
		// 4. 엔티티 - 메뉴 컬럼 동기화
		if(menuColumnSync) {
			String sql = "select id from menus where domain_id = :domainId and name = :resourceName and resource_type = 'ENTITY' and resource_name = :resourceName";
			String menuId = this.queryManager.selectBySql(sql, ValueUtil.newMap("domainId,resourceName", resource.getDomainId(), resource.getName()), String.class);
			if(menuId != null) {
				ResourceUtil.syncMenuColumnsWithEntity(menuId);
			}
		}
		
		// 5. 인덱스 동기화
		DdlController ddlCtrl = BeanUtil.get(DdlController.class);
		Ddl ddl = BeanUtil.get(Ddl.class);
		String tableName = resource.getTableName();
		List<EntityIndex> indexes = ddlCtrl.indexList(id);
		
		// 5.1 인덱스 리스트
		List<String> indexNameList = new ArrayList<String>();
		for(EntityIndex index : indexes) {
			indexNameList.add(index.getEntityIdxName());
		}
		
		// 5.2 인덱스 전체 삭제
		String dropIndexTemplate = ddl.getDdlMapper().dropIndexTemplate();
		ddl.executeDDL(tableName, dropIndexTemplate, ValueUtil.newMap("tableName,indexes", tableName, indexNameList));
		
		// 5.3 인덱스 생성
		for(EntityIndex index : indexes) {			
			String createIndexTemplate = ddl.getDdlMapper().indexTemplate();
			Map<String, Object> indexMap = index.toMapByEntity();
			@SuppressWarnings("unchecked")
			Map<String, Object> paramMap = ValueUtil.newMap("tableName,indexes,idxTableSpaceName", tableName, ValueUtil.toList(indexMap), ddl.getIndexTableSpace());
			ddl.executeDDL(tableName, createIndexTemplate, paramMap);
		}

		return changeCount;
	}
	
	/**
	 * 다른 도메인과 엔티티 정보 동기화
	 * 
	 * @param entityId
	 */
	protected void syncWithOtherDomains(String entityId) {
		// 1. Resource 조회
		Resource resource = this.queryManager.select(Resource.class, entityId);
		resource.resourceColumns();
		List<ResourceColumn> items = resource.getItems();
		
		// 2. Domain 조회
		List<Domain> domains = this.queryManager.selectList(Domain.class, new Domain());
		
		// 3. 도메인 별로 동일한 이름의 엔티티 & 엔티티 컬럼 제거
		for(Domain domain : domains) {
			if(ValueUtil.isNotEqual(resource.getDomainId(), domain.getId())) {
				Resource rCondition = new Resource(domain.getId(), resource.getName());
				Resource domainRsc = this.queryManager.selectByCondition(Resource.class, rCondition);
				
				if(domainRsc != null) {
					ResourceColumn rcCondition = new ResourceColumn(domainRsc.getId());
					rcCondition.setDomainId(domain.getId());
					this.queryManager.deleteList(ResourceColumn.class, rcCondition);
					this.queryManager.delete(domainRsc);
				}
			}
		}
		
		// 4. 도메인 별로 엔티티 복사
		for(Domain domain : domains) {
			if(ValueUtil.isNotEqual(resource.getDomainId(), domain.getId())) {
				Resource domainRsc = ValueUtil.populate(resource, new Resource());
				domainRsc.setDomainId(domain.getId());
				domainRsc.setId(null);
				this.queryManager.insert(domainRsc);
				List<ResourceColumn> newItems = new ArrayList<ResourceColumn>(items.size());
				
				for(ResourceColumn item : items) {
					ResourceColumn domainRscCol = ValueUtil.populate(item, new ResourceColumn());
					domainRscCol.setDomainId(domain.getId());
					domainRscCol.setEntityId(domainRsc.getId());
					domainRscCol.setId(null);
					newItems.add(domainRscCol);
				}
				
				this.queryManager.insertBatch(newItems);
			}
		}
	}
	
	/**
	 * domainId, entityName으로 확장 리소스 조회 리턴
	 * 
	 * @param entityName
	 * @return
	 */
	public Resource findExtendedResource(Long domainId, String entityName) {
		Resource extEntity = new Resource();
		ResourceController resourceController = BeanUtil.get(ResourceController.class);

		try {
			Resource master = resourceController.findOne(SysConstants.SHOW_BY_NAME_METHOD, entityName);
			if (ValueUtil.isEmpty(master) || !ValueUtil.toBoolean(master.getExtEntity())) {
				return extEntity;
			}

			StringJoiner sql = new StringJoiner("\n");
			sql.add("SELECT ID FROM ENTITIES");
			sql.add("WHERE DOMAIN_ID = :domainId");
			sql.add("AND MASTER_ID = :masterId");
			sql.add("AND NAME LIKE '%Ext'");

			Map<String, Object> paramMap = ValueUtil.newMap("domainId,masterId", domainId, master.getId());
			extEntity = queryManager.selectBySql(sql.toString(), paramMap, Resource.class);
			if (ValueUtil.isNotEmpty(extEntity)) {
				extEntity = resourceController.resourceColumns(extEntity.getId());
			}
		} catch (Exception e) {
			return extEntity;
		}

		return extEntity;
	}
	
	/**
	 * clear cache
	 * 
	 * @return
	 */
	public boolean clearCache() {
		ResourceController resourceCtrl = BeanUtil.get(ResourceController.class);
		resourceCtrl.clearResourceCache();
		resourceCtrl.clearResourceColumnCache();
		return true;
	}
}
