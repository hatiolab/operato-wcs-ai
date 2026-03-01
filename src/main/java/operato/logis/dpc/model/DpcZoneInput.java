package operato.logis.dpc.model;

/**
 * DPC 보관 존 투입 정보
 * 
 * @author shortstop
 */
public class DpcZoneInput {
	/**
	 * 보관 존 코드
	 */
	private String zoneCd;
	/**
	 * 보관 셀 코드
	 */
	private String cellCd;
	/**
	 * 주문 번호
	 */
	private String classCd;
	/**
	 * 고객사 코드
	 */
	private String comCd;
	/**
	 * 상품 코드
	 */
	private String skuCd;
	/**
	 * 상품 바코드
	 */
	private String skuBarcd;
	/**
	 * 상품 명
	 */
	private String skuNm;
	/**
	 * 총 주문 수량
	 */
	private int orderQty;
	/**
	 * 총 피킹 수량
	 */
	private int pickedQty;
	
	public String getZoneCd() {
		return zoneCd;
	}
	
	public void setZoneCd(String zoneCd) {
		this.zoneCd = zoneCd;
	}
	
	public String getCellCd() {
		return cellCd;
	}
	
	public void setCellCd(String cellCd) {
		this.cellCd = cellCd;
	}
	
	public String getClassCd() {
		return classCd;
	}

	public void setClassCd(String classCd) {
		this.classCd = classCd;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getSkuCd() {
		return skuCd;
	}
	
	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}
	
	public String getSkuBarcd() {
		return skuBarcd;
	}
	
	public void setSkuBarcd(String skuBarcd) {
		this.skuBarcd = skuBarcd;
	}
	
	public String getSkuNm() {
		return skuNm;
	}
	
	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}
	
	public int getOrderQty() {
		return orderQty;
	}
	
	public void setOrderQty(int orderQty) {
		this.orderQty = orderQty;
	}
	
	public int getPickedQty() {
		return pickedQty;
	}
	
	public void setPickedQty(int pickedQty) {
		this.pickedQty = pickedQty;
	}

}
