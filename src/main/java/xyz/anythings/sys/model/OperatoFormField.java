package xyz.anythings.sys.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import xyz.elidom.base.entity.Menu;
import xyz.elidom.base.entity.MenuColumn;
import xyz.elidom.base.entity.Resource;
import xyz.elidom.base.rest.MenuController;
import xyz.elidom.base.rest.ResourceController;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperatoFormField extends OperatoConfigConvert implements IOperatoConfig {
	/**
	 * 그리드 순서 
	 */
	private int rank;
	/**
	 * 필드 명 
	 */
	private String name;
	/**
	 * 그리드 헤더 표현 라벨 
	 */
	private String header;
	/**
	 * 그리드 편집 타입 
	 */
	private String type;
	/**
	 * 숨김 필드 
	 */
	private boolean hidden;
	/**
	 * 저장시 무시 여부 
	 */
	private boolean isSaveIgnore;
    /**
     * 참조 유형 : Entity, Menu, Code, DiyService...
     */
    private String refColType;
	/**
	 * 오브젝트 참조 타입(resource-column, resource-selector) 의 경우 참조 오브젝트 명칭 
	 */
	private String refColName;
    /**
     * 레코드 상세 
     * mandatory : 필수 
     * editable : 수정 가능 여부 
     * align : 정렬 
     * options : 콤보 리스트 
     */
    private Map<String, Object> record;
    
	/**
	 * 코드 데이터 ( 공통 코드, 엔티티 ) 조회 대상 컬럼 타입. 
	 */
	private final List<String> codeSelectColTypes = new ArrayList<String>(Arrays.asList("code-combo", "code-column", "resource-code", "permit-resource-code", "resource-id"));
	/**
	 * 읽기 전용 그리드 컬럼 타입 
	 */
	private final List<String> readOnlyColTypes = new ArrayList<String>(Arrays.asList("readonly", "code-column", "resource-column"));
	/**
	 * 오브젝트 참조 그리드 컬럼 타입 
	 */
	private final List<String> refObjectColTypes = new ArrayList<String>(Arrays.asList("resource-column", "resource-selector", "permit-resource-selector"));
	/**
	 * Entity 메타 정보 포함 타입  
	 */
	private final List<String> includeEntityMetaTypes = new ArrayList<String>(Arrays.asList("resource-column", "resource-selector", "resource-format-selector", "permit-resource-selector", "permit-resource-format-selector"));
	
	public OperatoFormField(MenuColumn column) {
		String formEditor = column.getFormEditor() != null ? column.getFormEditor().trim() : null;
		
		// 기본 정보 
		this.setRank(column.getRank());
		this.setName(column.getName());
		this.setHeader(column.getTerm());
		this.setType(this.convertEditorType(column.getName(), formEditor, column.getColType()));
		
		// 숨김 필드 
		boolean hidden = (ValueUtil.isEqualIgnoreCase(formEditor, "hidden") || ValueUtil.isEqualIgnoreCase(column.getName(), "id") || column.getGridRank() <= 0) ? true : false;
		this.setHidden(hidden);
		
		// 수정 가능 여부 
		boolean editable = this.readOnlyColTypes.contains(formEditor) ? false : true;
				
		// 레코드 설정 
		this.setRecord(ValueUtil.newMap("editable,mandatory", editable, !ValueUtil.toBoolean(column.getNullable(), true)));
		
		// 코드를 사용하는 타입의 경우 코드 추가
		if(this.codeSelectColTypes.contains(formEditor) && ValueUtil.isNotEmpty(column.getFormCodeList())) {
			this.getRecord().put("options", this.convertCodeData(formEditor, "column", column.getFormCodeList()));
		}
		
		// 오브젝트 레퍼런스 타입 컬럼
		if( this.refObjectColTypes.contains(formEditor)) {
			String colName = column.getName().substring(0, column.getName().lastIndexOf("_"));
			this.setName(colName);
			this.setRefColType(column.getRefType());
			this.setRefColName(column.getName());
		}
		
		// 폼 에디터가 리소스 셀렉터 계열이면
		if(this.includeEntityMetaTypes.contains(formEditor)) {
            String refColType = column.getRefType();
            this.setRefColType(refColType);
            this.setRefColName(column.getRefName());
            
			// 컬럼 타입이 엔티티 정보를 활용하는 타입은 엔티티 메타 정보를 포함한다.
		    if(ValueUtil.isEqualIgnoreCase(refColType, "Entity")) {
		        //Resource entity = BeanUtil.get(ResourceController.class).resourceColumnsByMeta(column.getRefName());
		        Resource entityMeta = BeanUtil.get(ResourceController.class).findOne(SysConstants.SHOW_BY_NAME_METHOD, column.getRefName());
		        this.getRecord().put("options", ValueUtil.newMap("meta", entityMeta));
		        
		    } else if(ValueUtil.isEqualIgnoreCase(refColType, "Menu")) {
                // 컬럼 타입이 메뉴 정보를 활용하는 타입은 메뉴 메타 정보를 포함한다.
                Menu menuMeta = BeanUtil.get(MenuController.class).findOne(SysConstants.SHOW_BY_NAME_METHOD, column.getRefName());
                this.getRecord().put("options", ValueUtil.newMap("meta", menuMeta));
		    }
		}
		
		// 넘버 타입의 경우 min / max 옵션 추가  
		if(ValueUtil.isEqualIgnoreCase(this.getType(), "number")) {
			// 양수 
			if(ValueUtil.isEqualIgnoreCase(formEditor, "positive-number")) {
				record.put("options", ValueUtil.newMap("min", 0));
			// 음수 
			} else if (ValueUtil.isEqualIgnoreCase(formEditor, "negative-number")) {
				record.put("options", ValueUtil.newMap("max", 0));
			} else {
				// min / max 옵션 ( {min},{max} )
				String validator = column.getFormValidator();
				
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
		
		this.setSaveIgnore(ValueUtil.toBoolean(column.getIgnoreOnSave(), false));
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

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isSaveIgnore() {
		return isSaveIgnore;
	}

	public void setSaveIgnore(boolean isSaveIgnore) {
		this.isSaveIgnore = isSaveIgnore;
	}

	public String getRefColName() {
		return refColName;
	}

	public void setRefColName(String refColName) {
		this.refColName = refColName;
	}

    public String getRefColType() {
        return refColType;
    }

    public void setRefColType(String refColType) {
        this.refColType = refColType;
    }
    
    public Map<String, Object> getRecord() {
        return record;
    }

    public void setRecord(Map<String, Object> record) {
        this.record = record;
    }
}
