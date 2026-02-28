package xyz.anythings.sys.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import xyz.elidom.base.entity.MenuColumn;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperatoSortField implements IOperatoConfig{

	/**
	 * 정렬 순서
	 */
	private int rank;
	
	/**
	 * 필드 명 
	 */
	private String name;
	
	/**
	 * descending 여부
	 */
	private boolean desc;
	
	public OperatoSortField(MenuColumn column) {
		this.setRank(column.getSortRank());
		this.setName(column.getName());
		this.setDesc(column.getReverseSort() == true ? true : false);
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

	public boolean isDesc() {
		return desc;
	}

	public void setDesc(boolean desc) {
		this.desc = desc;
	}
}
