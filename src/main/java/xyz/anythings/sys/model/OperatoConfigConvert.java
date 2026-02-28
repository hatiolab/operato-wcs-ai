package xyz.anythings.sys.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.anythings.sys.util.AnyDateUtil;
import xyz.elidom.core.entity.CodeDetail;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 화면 메타 정보를 구성하기 위한 컨버터
 */
public class OperatoConfigConvert {
    
	/**
	 * 기본 datetime 컬럼   
	 */
	private final List<String> datetimeCols = ValueUtil.newStringList("updated_at","created_at");

	/**
	 * 그리드 에디터 타입을 변경한다.
	 * @param columnName
	 * @param editorType
	 * @param columnType
	 * @return
	 */
	public String convertEditorType(String columnName, String editorType, String columnType) {
		String realEditorType = null;
		
		if(this.datetimeCols.contains(columnName)) return "datetime";
		
		if(ValueUtil.isEmpty(editorType)) {
			editorType = "string";
		}
		
		if (ValueUtil.isEqualIgnoreCase(editorType, "resource-code")) {
			realEditorType = "select";
			
		} else if (ValueUtil.isEqualIgnoreCase(editorType, "resource-id")) {
			realEditorType = "select";
			
		} else if (ValueUtil.isEqualIgnoreCase(editorType, "permit-resource-code")) {
			realEditorType = "select";
			
		} else if (ValueUtil.isEqualIgnoreCase(editorType, "code-combo")) {
			realEditorType = "select";
			
		} else if (ValueUtil.isEqualIgnoreCase(editorType, "code-column")) {
			realEditorType = "select";
			
		} else if (ValueUtil.isEqualIgnoreCase(editorType, "date-picker")) {
			realEditorType = "date";
			
		} else if (ValueUtil.isEqualIgnoreCase(editorType, "datetime-picker")) {
			realEditorType = "datetime";
			
		} else if (ValueUtil.isEqualIgnoreCase(editorType, "number") || ValueUtil.isEqualIgnoreCase(editorType, "positive-number") || ValueUtil.isEqualIgnoreCase(editorType, "negative-number")) {
			realEditorType = "number";
			
		} else if (ValueUtil.isEqualIgnoreCase(editorType, "readonly")) {
			if(ValueUtil.isEqualIgnoreCase(columnType, "boolean")) realEditorType = "checkbox";
			else if (ValueUtil.isEqualIgnoreCase(columnType, "datetime")) realEditorType = "datetime";
			else if (columnType.toLowerCase().contains("timestamp")) realEditorType = "datetime";
			else realEditorType = "string";
			
		} else if (ValueUtil.isEqualIgnoreCase(editorType, "checkbox")) {
			realEditorType = "checkbox";
			
		} else if (ValueUtil.isEqualIgnoreCase(editorType, "image-selector")) {
			realEditorType = "image";
		}
		
		return realEditorType == null ? editorType : realEditorType;
	}
	
	/**
	 * 필터 타입을 변경한다.
	 *  
	 * @param editorType
	 * @return
	 */
	public String convertFilterType(String editorType) {
		if(ValueUtil.isEmpty(editorType)) {
			return "string";
		}
		
		switch (editorType) {
			case "resource-code" :
				return "select";
			case "resource-id" :
				return "select";
			case "permit-resource-code" :
				return "select";
			case "code-combo" :
				return "select";
			case "date-picker":
				return "date";
			case "datetime-picker":
				return "datetime";
			case "readonly":
				return "string";
		}
				
		return editorType;
	}
	    
	
	/**
	 * 기본 값을 배열 또는 문자열인지 판별해  데이트 타입만 처리해 리턴
	 * 
	 * @param defaultValue
	 * @return
	 */
	public Object convertFilterDefaultValue(String defaultValue) {
		
		// comma 가 없으면 
		if(defaultValue.contains(",") == false) {
			return this.convertDefaultValue(defaultValue);
		}
		
		String[] defaultValues = defaultValue.split(",",-1);
		for(int idx = 0 ; idx < defaultValues.length ; idx++) {
			defaultValues[idx] = this.convertDefaultValue(defaultValues[idx]);
		}
		
		return defaultValues;
	}
	
	/**
	 * 기본 값을 데이트 타입만 처리해 리턴
	 * 
	 * @param defaultValue
	 * @return
	 */
	public String convertDefaultValue(String defaultValue) {
		
		// today 함수만 변환 
		if(ValueUtil.isEmpty(defaultValue) || !defaultValue.contains("today")) {
			return defaultValue;
		}
		
		defaultValue = defaultValue.trim().replaceAll("today", "");
		
		if(ValueUtil.isEmpty(defaultValue)) {
			return AnyDateUtil.currentDate();
		}
		
		// 연산자 추출 
		String oper = defaultValue.contains("+") ? "+" : defaultValue.contains("-") ? "-" : null;
		defaultValue = defaultValue.replaceAll(oper, "");
		
		// 연산자가 없으면 
		if(ValueUtil.isEmpty(oper)) {
			return AnyDateUtil.currentDate();
		}
		
		// 추가 일수 변환 
		Date now = new Date();
		int date = ValueUtil.toInteger(defaultValue, 0);
		
		if(ValueUtil.isEqualIgnoreCase("-", oper)) {
			date = date * -1;
		}
		
		now = AnyDateUtil.addDate(now, date);
		return AnyDateUtil.dateStr(now);
	}
	
	/**
	 * 코드 정보를 option 정보로 변환
	 * 
	 * @param editorType
	 * @param target
	 * @param codeList
	 * @return
	 */
	public List<Map<String,String>> convertCodeData(String editorType, String target, List<CodeDetail> codeList) {
		if(codeList == null) {
			return null;
		}
		
        List<Map<String, String>> options = new ArrayList<Map<String, String>>();
        
		if(User.isCurrentUserAdmin() || ValueUtil.isNotEqual(editorType, "permit-resource-code")) {
			// 관리자 권한을 가진 사용자와 permit 관련 에디터가 아닌 경우에만 전체 데이터를 조회 할 수 있다 .
			options.add(ValueUtil.newStringMap("value,display,name", SysConstants.EMPTY_STRING, SysConstants.EMPTY_STRING, SysConstants.EMPTY_STRING));
		}
		
		for(CodeDetail code : codeList) {
			String desc = code.getName() + "(" + code.getDescription() + ")";
			Map<String,String> option = new HashMap<String,String>();
			
			if (editorType.equals("resource-id")) {
				option.put("value", code.getId());
				option.put("display", desc);
			} else if (editorType.equals("resource-code")) {
				option.put("value", code.getName());
				option.put("display", desc);
			} else {
				option.put("value", code.getName());
				option.put("display", code.getDescription());
			}
			
			
			if(ValueUtil.isNotEmpty(code.getData1())) {
				option.put("rel_field_1",code.getData1());
			}
			if(ValueUtil.isNotEmpty(code.getData2())) {
				option.put("rel_field_2",code.getData2());
			}
			if(ValueUtil.isNotEmpty(code.getData3())) {
				option.put("rel_field_3",code.getData3());
			}
			if(ValueUtil.isNotEmpty(code.getData4())) {
				option.put("rel_field_4",code.getData4());
			}
			if(ValueUtil.isNotEmpty(code.getData5())) {
				option.put("rel_field_5",code.getData5());
			}
			
			if(ValueUtil.isEqualIgnoreCase("filter", target)) {
				option.put("name", code.getDescription());
			}
			
			options.add(option);
		}
		
		

		return options;
	}
}
