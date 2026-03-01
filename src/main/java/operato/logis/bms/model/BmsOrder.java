package operato.logis.bms.model;

/**
 * 주문 정보  
 * @author jhs
 */
public class BmsOrder {

	// 주문 번호 
	private String orderId;
	// 주문 라인 번호 
	private String orderLine;
	// 상품 코드 
	private String skuCd;
	// 주문 수량 
	private int orderQty;
	// 박스 추천 결과 
	private String boxTypeCd;
	
	public BmsOrder() {}
	
	public BmsOrder(String orderId, String orderLine, String skuCd, int orderQty) {
		/* 주문에 대한 기본 정보 셋팅 */
		this.setOrderId(orderId);
		this.setOrderLine(orderLine);
		this.setSkuCd(skuCd);
		this.setOrderQty(orderQty);
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderLine() {
		return orderLine;
	}

	public void setOrderLine(String orderLine) {
		this.orderLine = orderLine;
	}

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public int getOrderQty() {
		return orderQty;
	}

	public void setOrderQty(int orderQty) {
		this.orderQty = orderQty;
	}

	public String getBoxTypeCd() {
		return boxTypeCd;
	}

	public void setBoxTypeCd(String boxTypeCd) {
		this.boxTypeCd = boxTypeCd;
	}
	
}
