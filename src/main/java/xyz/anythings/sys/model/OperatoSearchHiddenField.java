package xyz.anythings.sys.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import xyz.elidom.base.entity.MenuColumn;
import xyz.elidom.sys.util.ValueUtil;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperatoSearchHiddenField {
	
	/**
	 * 필드명 
	 */
	private String name;
	
	/**
	 * 연산자 
	 */
	private String operator;
	
	/**
	 * 기본 값 
	 */
	private String value;
	
	public OperatoSearchHiddenField(MenuColumn column) {
		this.setName(column.getName());
		this.setOperator(ValueUtil.isEmpty(column.getSearchOper()) ? "eq" : column.getSearchOper());
		this.setValue(column.getSearchInitVal());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
