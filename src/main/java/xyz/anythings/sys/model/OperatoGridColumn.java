package xyz.anythings.sys.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import xyz.elidom.base.BaseConstants;
import xyz.elidom.base.entity.Menu;
import xyz.elidom.base.entity.MenuColumn;
import xyz.elidom.base.entity.Resource;
import xyz.elidom.base.rest.MenuController;
import xyz.elidom.base.rest.ResourceController;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperatoGridColumn extends OperatoConfigConvert implements IOperatoConfig {
	/**
	 * 그리드 순서 
	 */
	private int rank;
	/**
	 * 필드 명 
	 */
	private String name;
	/**
	 * 라벨 표시 여부
	 */
	private boolean label;
	/**
	 * 그리드 헤더 표현 라벨 
	 */
	private String header;
	/**
	 * 그리드 편집 타입 
	 */
	private String type;
	/**
	 * 너비 
	 */
	private int width;
	/**
	 * 정렬 가능 여부
	 * 가상 필드는 제외 함 
	 */
	private boolean sortable;
    /**
     * 참조 유형 : Entity, Menu, Code, DiyService...
     */
    private String refColType;
    /**
     * 오브젝트 참조 타입(resource-column, resource-selector) 의 경우 참조 오브젝트 명칭 
     */
    private String refColName;
    
    /**
     * 참조 URL 
     */
    private String refUrl;
    /**
     * 참조 파라미터  
     */
    private String refParams;
    /**
     * 참조 관계 필드 
     */
    private String refRelated;
    
    /**
     * 숨김 필드 
     */
    private boolean hidden;
    /**
     * 고정 컬럼
     */
    private boolean fixed;
    /**
     * 기본 값 
     */
    private String value;
	/**
	 * 레코드 상세
	 * mandatory : 필수
	 * size : 문자열 타입인 경우 글자수 
	 * editable : 수정 가능 여부 
	 * align : 정렬 
	 * options : 콤보 리스트 
	 */
	private Map<String, Object> record;
	/**
	 * excel export 
	 */
	private Map<String, Object> imex;
	/**
	 * filter 정보 
	 */
	private OperatoSearchField filter;
	
	/**
	 * 코드 데이터 ( 공통 코드, 엔티티 ) 조회 대상 컬럼 타입. 
	 */
	private final List<String> codeSelectColTypes = ValueUtil.newStringList("code-combo", "code-column", "resource-code", "permit-resource-code", "resource-id");
	/**
	 * 읽기 전용 그리드 컬럼 타입 
	 */
	private final List<String> readOnlyColTypes = ValueUtil.newStringList("readonly", "code-column", "resource-column");
	/**
	 * 오브젝트 참조 그리드 컬럼 타입 
	 */
	private final List<String> refObjectColTypes = ValueUtil.newStringList("resource-column", "resource-selector", "permit-resource-selector");
	/**
	 * Entity 메타 정보 포함 타입  
	 */
	private final List<String> includeEntityMetaTypes = ValueUtil.newStringList("resource-column", "resource-selector", "resource-format-selector", "permit-resource-selector", "permit-resource-format-selector");
	
	
	public OperatoGridColumn(MenuColumn column, boolean useExport) {
        // 기본 정보
        String gridEditor = column.getGridEditor() != null ? column.getGridEditor().trim() : null;
        String columnName = column.getName();
        
        this.setHeader(column.getTerm());
        this.setLabel(true); // List, Card 유형인 경우 라벨 표시 여부
		this.setRank(column.getGridRank());
		this.setName(columnName);
		String editorType = this.convertEditorType(columnName, gridEditor, column.getColType());
		this.setType(editorType);
		this.setWidth(column.getGridWidth());
		this.setSortable(!column.getVirtualField());
		this.setValue(this.convertDefaultValue(column.getDefVal()));
		this.setRefUrl(column.getRefUrl());
		this.setRefParams(column.getRefParams());
		this.setRefRelated(column.getRefRelated());
		
		
		// 숨김 필드 
		boolean hidden = (ValueUtil.isEqualIgnoreCase(gridEditor, "hidden") || ValueUtil.isEqualIgnoreCase(columnName, "id") || column.getGridRank() <= 0) ? true : false;
		this.setHidden(hidden);
		
		// 수정 가능 여부 
		boolean editable = this.readOnlyColTypes.contains(gridEditor) ? false : true;
		
		// 고정 컬럼
		String alignStr = ValueUtil.isEmpty(column.getGridAlign()) ? "left" : column.getGridAlign();
		boolean fixedFlag = alignStr.startsWith("fixed") ? true : false;
		this.setFixed(fixedFlag);
		alignStr = fixedFlag ? (ValueUtil.isEqual(alignStr, "fixed") ? "left" : alignStr.split("-")[1]) : alignStr;

		// 정렬 : "", "center", "far", "fixed", "fixed-center", "fixed-far"
		String align = ValueUtil.isEqualIgnoreCase("far", column.getGridAlign()) ? "right" : (ValueUtil.isEqualIgnoreCase("center", column.getGridAlign()) ? "center" : "left");
		
		// 레코드 설정
		boolean manatory = !ValueUtil.toBoolean(column.getNullable(), true);
		Map<String, Object> record = ValueUtil.newMap("editable,align,mandatory", editable, align, manatory);
		if(ValueUtil.isEqualIgnoreCase(this.getType(), "string")) {
			record.put("size", column.getColSize());
		}
		this.setRecord(record);
		
		// 코드 셀렉터 계열인 경우
		if(this.codeSelectColTypes.contains(gridEditor) && ValueUtil.isNotEmpty(column.getGridCodeList())) {
			record.put("options", this.convertCodeData(gridEditor, "column", column.getGridCodeList()));
		}
		
		// 오브젝트 레퍼런스 타입 컬럼
		if(this.refObjectColTypes.contains(gridEditor)) {
			String colName = columnName.substring(0, columnName.lastIndexOf("_"));
			this.setName(colName);
            this.setRefColType(column.getRefType());
			this.setRefColName(columnName);
			this.setSortable(false);
		}
		
		// 그리드, 폼 등에서 리소스 셀렉터 컴포넌트를 클릭했을 때 리소스 셀렉터 팝업 구성을 위해 필요한 컬럼 메타 정보는 그 때 별도로 호출하고 그리드 구성시에는 컬럼 정보는 제외하고 엔티티, 메뉴의 정보만을 리턴한다.  
		if(this.includeEntityMetaTypes.contains(gridEditor)) {
		    String refColType = column.getRefType();
		    this.setRefColType(refColType);
		    this.setRefColName(column.getRefName());
		    
		    if(ValueUtil.isEqualIgnoreCase(refColType, BaseConstants.REF_TYPE_ENTITY)) {
		        // 컬럼 타입이 엔티티 정보를 활용하는 타입은 엔티티 메타 정보를 포함한다.
		        Resource entityMeta = BeanUtil.get(ResourceController.class).findOne(SysConstants.SHOW_BY_NAME_METHOD, column.getRefName());
		        record.put("options", ValueUtil.newMap("meta", entityMeta));
		        
		    } else if(ValueUtil.isEqualIgnoreCase(refColType, BaseConstants.REF_TYPE_MENU)) {
	            // 컬럼 타입이 메뉴 정보를 활용하는 타입은 메뉴 메타 정보를 포함한다.
	            Menu menuMeta = BeanUtil.get(MenuController.class).findOne(SysConstants.SHOW_BY_NAME_METHOD, column.getRefName());
	            record.put("options", ValueUtil.newMap("meta", menuMeta));
		    }
		}
		
		// 넘버 타입의 경우 min / max 옵션 추가  
		if(ValueUtil.isEqualIgnoreCase(this.getType(), "number")) {
			// 양수 
			if(ValueUtil.isEqualIgnoreCase(gridEditor, "positive-number")) {
				record.put("options", ValueUtil.newMap("min", 0));
			// 음수 
			} else if (ValueUtil.isEqualIgnoreCase(gridEditor, "negative-number")) {
				record.put("options", ValueUtil.newMap("max", 0));
			} else {
				// min / max 옵션 ( {min},{max} )
				String validator = column.getGridValidator();
				
				if(ValueUtil.isNotEmpty(validator)) {
					String[] validators = validator.split(",",-1);
					List<String> valiNames = ValueUtil.newStringList("min","max");
					Map<String,Object> options = new HashMap<String,Object>();
					
					for(int idx = 0 ; idx < validators.length ; idx++) {
						if(ValueUtil.isNotEmpty(validators[idx])) {
							options.put(valiNames.get(idx), validators[idx]);
						}
					}
				}
			}
		}
		
		String format = column.getGridFormat();
	    // 컬럼에 포맷이 지정되어 있으면
		if (ValueUtil.isNotEmpty(format)) {
			// readonly_after_save : 입력 이 후에는 수정 불가
			if(ValueUtil.isEqualIgnoreCase("readonly-after-create", format)) {
				record.put("format", "readonly-after-create");
			// 숫자 포맷인 경우
			} else if(format.contains("#")) {
				record.put("format", format);
			} else if (format.contains(":")) {
				record.put("resource_display", format);
			}
		}
		
		// 필터 설정 
		if(ValueUtil.isNotEmpty(column.getSearchRank()) && column.getSearchRank() > 0 && ValueUtil.isNotEqual(column.getSearchEditor(), "hidden")) {
			this.setFilter(new OperatoSearchField(column)); 
		}
		
		if(useExport) {
			// 숨김 필드는 내보내기 에서 제외 
			if(!hidden) {
				String key = columnName;
				if(ValueUtil.isEqualIgnoreCase("updater", this.name) || ValueUtil.isEqualIgnoreCase("creator", this.name)) {
					key = this.name + ".name";
				}
				
				this.setImex(ValueUtil.newMap("header,key,width,type", column.getTerm(), key, column.getGridWidth() / 6, column.getColType()));
			}
		}
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isLabel() {
		return label;
	}

	public void setLabel(boolean label) {
		this.label = label;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isSortable() {
		return sortable;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	public Map<String, Object> getRecord() {
		return record;
	}

	public void setRecord(Map<String, Object> record) {
		this.record = record;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isFixed() {
		return fixed;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public OperatoSearchField getFilter() {
		return filter;
	}

	public void setFilter(OperatoSearchField filter) {
		this.filter = filter;
	}

	public Map<String, Object> getImex() {
		return imex;
	}

	public void setImex(Map<String, Object> imex) {
		this.imex = imex;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getRefColType() {
        return refColType;
    }

    public void setRefColType(String refColType) {
        this.refColType = refColType;
    }

    public String getRefColName() {
		return refColName;
	}

	public void setRefColName(String refColName) {
		this.refColName = refColName;
	}

	public String getRefUrl() {
		return refUrl;
	}

	public void setRefUrl(String refUrl) {
		this.refUrl = refUrl;
	}

	public String getRefParams() {
		return refParams;
	}

	public void setRefParams(String refParams) {
		this.refParams = refParams;
	}

	public String getRefRelated() {
		return refRelated;
	}

	public void setRefRelated(String refRelated) {
		this.refRelated = refRelated;
	}


}
