/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.base.entity;


import java.util.List;

import xyz.elidom.core.entity.CodeDetail;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.ElidomStampHook;

@Table(name = "view_columns", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,onType,onId,name", indexes = { 
	@Index(name = "ix_view_column_0", columnList = "domain_id,on_type,on_id,name", unique = true),
	@Index(name = "ix_view_column_1", columnList = "domain_id,on_type,on_id,rank")
})
public class ViewColumn extends ElidomStampHook {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -4831047442995644767L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String id;

	@Column (name = "on_type", nullable = false, length = 32)
	private String onType;

	@Column (name = "on_id", nullable = false, length = 48)
	private String onId;

	@Column (name = "name", nullable = false, length = 32)
	private String name;

	@Column (name = "description")
	private String description;

	@Column (name = "rank")
	private Integer rank;

	@Column (name = "term", length = 40)
	private String term;

	@Column (name = "col_type", nullable = false, length = 15)
	private String colType;

	@Column (name = "col_size")
	private Integer colSize;

	@Column (name = "nullable")
	private Boolean nullable;

	@Column (name = "ref_type", length = 15)
	private String refType;

	@Column (name = "ref_name", length = 32)
	private String refName;

	@Column (name = "ref_url", length = 128)
	private String refUrl;

	@Column (name = "ref_params", length = 128)
	private String refParams;

	@Column (name = "ref_related", length = 128)
	private String refRelated;

	@Column (name = "search_rank")
	private Integer searchRank;

	@Column (name = "sort_rank")
	private Integer sortRank;

	@Column (name = "reverse_sort")
	private Boolean reverseSort;

	@Column (name = "virtual_field")
	private Boolean virtualField;

	@Column (name = "search_name", length = 32)
	private String searchName;

	@Column (name = "search_oper", length = 15)
	private String searchOper;

	@Column (name = "search_editor", length = 32)
	private String searchEditor;

	@Column(name = "search_init_val", length = 255)
	private String searchInitVal;
	
	@Column (name = "grid_rank")
	private Integer gridRank;

	@Column (name = "grid_editor", length = 32)
	private String gridEditor;

	@Column (name = "grid_format", length = 32)
	private String gridFormat;

	@Column (name = "grid_validator", length = 32)
	private String gridValidator;

	@Column (name = "grid_width")
	private Integer gridWidth;

	@Column (name = "grid_align", length = 10)
	private String gridAlign;

	@Column (name = "uniq_rank")
	private Integer uniqRank;

	@Column (name = "form_editor", length = 32)
	private String formEditor;

	@Column (name = "form_validator", length = 32)
	private String formValidator;

	@Column (name = "form_format", length = 128)
	private String formFormat;

	@Column (name = "def_val", length = 128)
	private String defVal;
	
	@Column(name = "range_val", length = 128)
	private String rangeVal;

	@Column (name = "ignore_on_save")
	private Boolean ignoreOnSave;
	
	/**
	 * column이 code에서 선택해야 할 경우 client에서 사용할 수 있도록 코드 명, 코드 값을 리턴한다.
	 */
	@Ignore
	private List<CodeDetail> codeList;	
	
	public ViewColumn() {	
	}
	
	public ViewColumn(String id) {
		this.name = id;
	}
	
	public ViewColumn(Long domainId, String onType, String onId) {
		this.domainId = domainId;
		this.onType = onType;
		this.onId = onId;
	}
	
	public ViewColumn(Long domainId, String onType, String onId, String name) {
		this.domainId = domainId;
		this.onType = onType;
		this.onId = onId;
		this.name = name;
	}	
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOnType() {
		return onType;
	}

	public void setOnType(String onType) {
		this.onType = onType;
	}

	public String getOnId() {
		return onId;
	}

	public void setOnId(String onId) {
		this.onId = onId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getColType() {
		return colType;
	}

	public void setColType(String colType) {
		this.colType = colType;
	}

	public Integer getColSize() {
		return colSize;
	}

	public void setColSize(Integer colSize) {
		this.colSize = colSize;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	public String getRefType() {
		return refType;
	}

	public void setRefType(String refType) {
		this.refType = refType;
	}

	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
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

	public Integer getSearchRank() {
		return searchRank;
	}

	public void setSearchRank(Integer searchRank) {
		this.searchRank = searchRank;
	}

	public Integer getSortRank() {
		return sortRank;
	}

	public void setSortRank(Integer sortRank) {
		this.sortRank = sortRank;
	}

	public Boolean getReverseSort() {
		return reverseSort;
	}

	public void setReverseSort(Boolean reverseSort) {
		this.reverseSort = reverseSort;
	}

	public Boolean getVirtualField() {
		return virtualField;
	}

	public void setVirtualField(Boolean virtualField) {
		this.virtualField = virtualField;
	}

	public String getSearchName() {
		return searchName;
	}

	public void setSearchName(String searchName) {
		this.searchName = searchName;
	}

	public String getSearchOper() {
		return searchOper;
	}

	public void setSearchOper(String searchOper) {
		this.searchOper = searchOper;
	}

	public String getSearchEditor() {
		return searchEditor;
	}

	public void setSearchEditor(String searchEditor) {
		this.searchEditor = searchEditor;
	}
	
	public String getSearchInitVal() {
		return searchInitVal;
	}

	public void setSearchInitVal(String searchInitVal) {
		this.searchInitVal = searchInitVal;
	}

	public Integer getGridRank() {
		return gridRank;
	}

	public void setGridRank(Integer gridRank) {
		this.gridRank = gridRank;
	}

	public String getGridEditor() {
		return gridEditor;
	}

	public void setGridEditor(String gridEditor) {
		this.gridEditor = gridEditor;
	}

	public String getGridFormat() {
		return gridFormat;
	}

	public void setGridFormat(String gridFormat) {
		this.gridFormat = gridFormat;
	}

	public String getGridValidator() {
		return gridValidator;
	}

	public void setGridValidator(String gridValidator) {
		this.gridValidator = gridValidator;
	}

	public Integer getGridWidth() {
		return gridWidth;
	}

	public void setGridWidth(Integer gridWidth) {
		this.gridWidth = gridWidth;
	}

	public String getGridAlign() {
		return gridAlign;
	}

	public void setGridAlign(String gridAlign) {
		this.gridAlign = gridAlign;
	}

	public Integer getUniqRank() {
		return uniqRank;
	}

	public void setUniqRank(Integer uniqRank) {
		this.uniqRank = uniqRank;
	}

	public String getFormEditor() {
		return formEditor;
	}

	public void setFormEditor(String formEditor) {
		this.formEditor = formEditor;
	}

	public String getFormValidator() {
		return formValidator;
	}

	public void setFormValidator(String formValidator) {
		this.formValidator = formValidator;
	}

	public String getFormFormat() {
		return formFormat;
	}

	public void setFormFormat(String formFormat) {
		this.formFormat = formFormat;
	}

	public String getDefVal() {
		return defVal;
	}

	public void setDefVal(String defVal) {
		this.defVal = defVal;
	}
	
	public String getRangeVal() {
		return rangeVal;
	}

	public void setRangeVal(String rangeVal) {
		this.rangeVal = rangeVal;
	}

	public Boolean getIgnoreOnSave() {
		return ignoreOnSave;
	}

	public void setIgnoreOnSave(Boolean ignoreOnSave) {
		this.ignoreOnSave = ignoreOnSave;
	}
	
	/**
	 * @return the codeList
	 */
	public List<CodeDetail> getCodeList() {
		return codeList;
	}

	/**
	 * @param codeList the codeList to set
	 */
	public void setCodeList(List<CodeDetail> codeList) {
		this.codeList = codeList;
	}
	
}