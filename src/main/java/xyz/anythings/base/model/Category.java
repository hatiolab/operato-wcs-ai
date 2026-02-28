package xyz.anythings.base.model;

import java.util.List;

import xyz.anythings.base.entity.SKU;

/**
 * 중분류 모델 
 * 
 * @author shortstop
 */
public class Category {
	
	/**
	 * 배치 그룹 ID
	 */
	private String batchGroupId;
	/**
	 * 상품 코드 
	 */
	private String skuCd;
	/**
	 * 상품 명 
	 */
	private String skuNm;
	/**
	 * 박스 입수 수량
	 */
	private Integer boxInQty;
	/**
	 * 팔레트 입수 박스 
	 */
	private Integer pltBoxQty;
	/**
	 * 상품 무게
	 */
	private Float skuWt;
	/**
	 * 설비 코드별 분류 수량 정보 
	 */
	private List<CategoryItem> items;
	
	/**
	 * 기본 생성자 
	 */
	public Category() {
	}
	
	/**
	 * 중분류 생성자 
	 * 
	 * @param batchGroupId
	 * @param sku
	 * @param items
	 */
	public Category(String batchGroupId, SKU sku) {
		this.batchGroupId = batchGroupId;
		this.skuCd = sku.getSkuCd();
		this.skuNm = sku.getSkuNm();
		this.boxInQty = sku.getBoxInQty();
		this.pltBoxQty = sku.getPltBoxQty();
		this.skuWt = sku.getSkuWt();
	}
	
	/**
	 * 중분류 생성자 
	 * 
	 * @param batchGroupId
	 * @param sku
	 * @param items
	 */
	public Category(String batchGroupId, SKU sku, List<CategoryItem> items) {
		this.batchGroupId = batchGroupId;
		this.skuCd = sku.getSkuCd();
		this.skuNm = sku.getSkuNm();
		this.boxInQty = sku.getBoxInQty();
		this.pltBoxQty = sku.getPltBoxQty();
		this.skuWt = sku.getSkuWt();
		this.setItems(items);
	}

	public String getBatchGroupId() {
		return batchGroupId;
	}

	public void setBatchGroupId(String batchGroupId) {
		this.batchGroupId = batchGroupId;
	}

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public String getSkuNm() {
		return skuNm;
	}

	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}

	public Integer getBoxInQty() {
		return boxInQty;
	}

	public void setBoxInQty(Integer boxInQty) {
		this.boxInQty = boxInQty;
	}

	public Integer getPltBoxQty() {
		return pltBoxQty;
	}

	public void setPltBoxQty(Integer pltBoxQty) {
		this.pltBoxQty = pltBoxQty;
	}
	
	public Float getSkuWt() {
		return skuWt;
	}

	public void setSkuWt(Float skuWt) {
		this.skuWt = skuWt;
	}
	
	public List<CategoryItem> getItems() {
		return items;
	}

	public void setItems(List<CategoryItem> items) {
		this.items = items;
	}

}
