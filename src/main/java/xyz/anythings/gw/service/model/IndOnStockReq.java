package xyz.anythings.gw.service.model;

/**
 * 재고 조정 관련 표시기 점등 요청 모델
 * 
 * @author shortstop
 */
public class IndOnStockReq {

	/**
	 * stockId
	 */
	private String stockId;
	/**
	 * 표시기 코드
	 */
	private String indCd;
	/**
	 * 표시기 색상
	 */
	private String colorCd;
	/**
	 * 적치 수량
	 */
	private Integer loadQty;
	/**
	 * 할당 수량
	 */
	private Integer allocQty;
	/**
	 * gateway path
	 */
	private String gwPath;
	
	public String getStockId() {
		return stockId;
	}
	
	public void setStockId(String stockId) {
		this.stockId = stockId;
	}
	
	public String getIndCd() {
		return indCd;
	}
	
	public void setIndCd(String indCd) {
		this.indCd = indCd;
	}
	
	public String getColorCd() {
		return colorCd;
	}
	
	public void setColorCd(String colorCd) {
		this.colorCd = colorCd;
	}
	
	public Integer getLoadQty() {
		return loadQty;
	}

	public void setLoadQty(Integer loadQty) {
		this.loadQty = loadQty;
	}

	public Integer getAllocQty() {
		return allocQty;
	}

	public void setAllocQty(Integer allocQty) {
		this.allocQty = allocQty;
	}

	public String getGwPath() {
		return gwPath;
	}
	
	public void setGwPath(String gwPath) {
		this.gwPath = gwPath;
	}

}