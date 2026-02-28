package xyz.anythings.base.event.master;

import xyz.anythings.sys.event.model.SysEvent;

/**
 * 상품 수신 이벤트
 * 
 * @author shortstop
 */
public class SkuReceiptEvent extends SysEvent {

	/**
	 * 상품
	 */
	public static final String RECEIVE_TYPE_SKU = "sku";
	/**
	 * 상품 바코드
	 */
	public static final String RECEIVE_TYPE_SKU_BARCD = "sku_barcd";
	
	/**
	 * 고객사 코드 
	 */
	private String comCd;
	/**
	 * 수신 유형 
	 */
	private String receiveType;
	/**
	 * 수신 예정 수량 
	 */
	private int planCount;
	/**
	 * 수신 중 오류 수량
	 */
	private int errorCount;
	/**
	 * 수신 완료 수량
	 */
	private int receivedCount;
	/**
	 * 가장 최근에 수신 받은 시각 
	 */
	private String lastReceivedAt;
	/**
	 * 3rd-party 주문 제공자
	 */
	protected String thirdPartyProvider;

	
	public SkuReceiptEvent() {
		super();
	}
	
	public SkuReceiptEvent(Long domainId, String receiveType, String comCd, short eventStep) {
		this.domainId = domainId;
		this.receiveType = receiveType;
		this.comCd = comCd;
		this.eventStep = eventStep;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getReceiveType() {
		return receiveType;
	}

	public void setReceiveType(String receiveType) {
		this.receiveType = receiveType;
	}

	public int getPlanCount() {
		return planCount;
	}

	public void setPlanCount(int planCount) {
		this.planCount = planCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public int getReceivedCount() {
		return receivedCount;
	}

	public void setReceivedCount(int receivedCount) {
		this.receivedCount = receivedCount;
	}

	public String getLastReceivedAt() {
		return lastReceivedAt;
	}

	public void setLastReceivedAt(String lastReceivedAt) {
		this.lastReceivedAt = lastReceivedAt;
	}

	public String getThirdPartyProvider() {
		return thirdPartyProvider;
	}

	public void setThirdPartyProvider(String thirdPartyProvider) {
		this.thirdPartyProvider = thirdPartyProvider;
	}
}
