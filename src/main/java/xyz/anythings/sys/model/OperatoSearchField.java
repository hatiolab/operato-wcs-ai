package xyz.anythings.sys.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import xyz.elidom.base.entity.MenuColumn;
import xyz.elidom.util.ValueUtil;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperatoSearchField extends OperatoConfigConvert implements IOperatoConfig{
	/**
	 * 순서 
	 */
	private int rank;
	/**
	 * 필드 명 
	 */
	private String name;
	/**
	 * 필드 라벨 
	 */
	private String label;
	/**
	 * 숨김 여부 
	 */
	private boolean hidden;
	/**
	 * 편집 타입 
	 */
	private String type;
	/**
	 * 기본 값 
	 */
	private Object value;
	/**
	 * 조회 유형 
	 */
	private String operator;
	/**
	 * 참조 유형인 경우 다른 필드 값이 변경됨에 따라서 해당 컴포넌트의 검색 조건이 바뀌어 서비스 호출을 하기 위한 설정 
	 */
	private String[] boundTo;
    /**
     * 참조 유형 : Entity, Menu, Code, DiyService...
     */
    private String refColType;
    /**
     * 오브젝트 참조 타입(resource-column, resource-selector, resource-format-selector) 의 경우 참조 오브젝트 명칭 
     */
    private String refColName;
	/**
	 * select 타입시 리스트 
	 */
	private List<Map<String, String>> options;
	/**
	 * 프로퍼티
	 */
	private Map<String, Object> props;
	/**
	 * 데이터 접근 권한 필드 여부
	 */
	private boolean isDataAccessPermitField;
    /**
     * 타 엔티티, 혹은 메뉴 참조 에디터 유형  
     */
    private final List<String> RESOURCE_REFERENCE_EDITOR_TYPES = new ArrayList<String>(Arrays.asList("resource-selector", "resource-format-selector", "permit-resource-selector", "permit-resource-format-selector"));

	public OperatoSearchField(MenuColumn column) {
	    // 컬럼 기본 정보
	    String columnName = column.getName();
	    String searchEditor = column.getSearchEditor();
	    String searchOper = column.getSearchOper();
	    
		this.setName(columnName);
		this.setRank(column.getSearchRank());
		this.setLabel(column.getTerm());
		this.setOperator(ValueUtil.isEmpty(searchOper) ? "eq" : searchOper);
		this.setDataAccessPermitField(ValueUtil.isEqual("permit-resource-code", searchEditor));
		this.setHidden(ValueUtil.isEqualIgnoreCase("hidden", searchEditor));
		this.setType(ValueUtil.isEmpty(searchEditor) ? "text" : this.convertFilterType(searchEditor));
		
		// 검색 초기값
		String defVal = column.getSearchInitVal();
		this.setValue(ValueUtil.isNotEmpty(defVal) ? this.convertFilterDefaultValue(defVal) : null);
		
		// 참조 관련 설정
		if(RESOURCE_REFERENCE_EDITOR_TYPES.contains(searchEditor) || ValueUtil.isEqual("resource-code", searchEditor) || ValueUtil.isEqual("resource-id", searchEditor) || ValueUtil.isEqual("resource-combo", searchEditor)) {
			// 참조 관계, 참조 이름 설정
		    this.setRefColType(column.getRefType());
		    this.setRefColName(column.getRefName());
		    
		    
		    // 참조 관련 BoundTo 설정
		    String refRelated = column.getRefRelated();
		    if(ValueUtil.isNotEmpty(refRelated) && refRelated.indexOf("[") >= 0 && refRelated.indexOf("]") >= 0) {
		    	String boundToStr = refRelated.substring(refRelated.indexOf("[") + 1, refRelated.indexOf("]"));
		    	this.boundTo = boundToStr.split(",");
		    }
		    
		    // TODO 참조 관련 셀렉터 ...
		}
		
		// 콤보 박스 리스트 
		if(ValueUtil.isEqualIgnoreCase(this.getType(), "select")) {
			this.setOptions(this.convertCodeData(searchEditor, "filter", column.getSearchCodeList()));
			
			// 데이터 권한 에디터 타입이면서 콤보 리스트가 있으면 기본 값 셋팅 
			if(ValueUtil.isEqual(column.getSearchEditor(), "permit-resource-code") && ValueUtil.isNotEmpty(this.getOptions())) {
				String defOptVal = ValueUtil.isNotEmpty(defVal) ? defVal : this.getOptions().get(0).get("value");
				this.setValue(defOptVal);
			}
		}
		
		// 프로퍼티 설정
		this.setProps(ValueUtil.newMap("searchOper,placeholder", this.getOperator(), column.getTerm()));
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String[] getBoundTo() {
		return boundTo;
	}

	public void setBoundTo(String[] boundTo) {
		this.boundTo = boundTo;
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

    public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Map<String, Object> getProps() {
		return props;
	}

	public void setProps(Map<String, Object> props) {
		this.props = props;
	}

	public List<Map<String, String>> getOptions() {
		return options;
	}

	public void setOptions(List<Map<String, String>> options) {
		this.options = options;
	}

	public boolean isDataAccessPermitField() {
		return isDataAccessPermitField;
	}

	public void setDataAccessPermitField(boolean isDataAccessPermitField) {
		this.isDataAccessPermitField = isDataAccessPermitField;
	}
}
