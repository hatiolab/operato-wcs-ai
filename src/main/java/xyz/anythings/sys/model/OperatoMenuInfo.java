package xyz.anythings.sys.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import xyz.elidom.base.entity.Menu;

/**
 * 메뉴 메타 정보
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperatoMenuInfo {
	/**
	 * 메뉴 ID 
	 */
	private String id;
	
	/**
	 * 로케일 변환 된 타이틀 
	 */
	private String title;
		
	/**
	 * 상세 폼 리소스 명 ( 메뉴 의 name )  
	 */
	private String detailFormResource;
	
	/**
	 * 상세 폼 이름 
	 */
	private String detailFormName;
	
	/**
	 * 페이지네이터 사용 여부 
	 */
	private Boolean usePagination;
	
	/**
	 * 기본 URL 
	 */
	private String resourceUrl;
	
	/**
	 * 저장 URL 
	 */
	private String saveUrl;
	
	/**
	 * ID Field
	 */
	private String idField;
	
	/**
	 * Title Filed 
	 */
	private String titleField;
	
	/**
	 * Description Field
	 */
	private String descField;
	
	/**
	 * 조회 결과 리스트 필드 
	 */
	private String itemsResField;
	
	/**
	 * 조회 결과 토탈 카운트 필드 
	 */
	private String totalResField;

	/**
	 * 메뉴 메타 정보
	 * 
	 * @param menu
	 */
	public OperatoMenuInfo(Menu menu) {
		this.setId(menu.getId());
		this.setDetailFormName(menu.getDetailFormId());
		this.setDetailFormResource(menu.getDetailLayout());
		this.setUsePagination(menu.getPagination());
		this.setResourceUrl(menu.getResourceUrl());
		this.setSaveUrl(menu.getGridSaveUrl());
		this.setIdField(menu.getIdField());
		this.setTitleField(menu.getTitleField());
		this.setDescField(menu.getDescField());
		this.setItemsResField(menu.getItemsProp());
		this.setTotalResField(menu.getTotalProp());
		this.setTitle(menu.getTitle());
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDetailFormResource() {
		return detailFormResource;
	}

	public void setDetailFormResource(String detailFormResource) {
		this.detailFormResource = detailFormResource;
	}

	public String getDetailFormName() {
		return detailFormName;
	}

	public void setDetailFormName(String detailFormName) {
		this.detailFormName = detailFormName;
	}

	public Boolean getUsePagination() {
		return usePagination;
	}

	public void setUsePagination(Boolean usePagination) {
		this.usePagination = usePagination;
	}

	public String getResourceUrl() {
		return resourceUrl;
	}

	public void setResourceUrl(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}

	public String getSaveUrl() {
		return saveUrl;
	}

	public void setSaveUrl(String saveUrl) {
		this.saveUrl = saveUrl;
	}

	public String getIdField() {
		return idField;
	}

	public void setIdField(String idField) {
		this.idField = idField;
	}

	public String getTitleField() {
		return titleField;
	}

	public void setTitleField(String titleField) {
		this.titleField = titleField;
	}

	public String getDescField() {
		return descField;
	}

	public void setDescField(String descField) {
		this.descField = descField;
	}

	public String getItemsResField() {
		return itemsResField;
	}

	public void setItemsResField(String itemsResField) {
		this.itemsResField = itemsResField;
	}

	public String getTotalResField() {
		return totalResField;
	}

	public void setTotalResField(String totalResField) {
		this.totalResField = totalResField;
	}
}
