package xyz.anythings.base.event.main;

import xyz.anythings.base.entity.BatchReceipt;

/**
 * 주문 수신 이벤트 - 주문 수신은 총 3단계로 구성된다.
 * 
 *  - 1단계 : 수신할 주문 (배치) 정보 조회
 *  - 2단계 : 1단계에서 조회한 주문 (배치)를 수신
 *  - 3단계 : 2단계에서 수신한 주문 취소 
 * 
 * @author yang
 */
public class BatchReceiveEvent extends BatchRootEvent {
	
	/**
	 * 10 : EVENT_RECEIVE_TYPE_RECEIPT
	 * 20 : EVENT_RECEIVE_TYPE_RECEIVE
	 * 30 : EVENT_RECEIVE_TYPE_CANCEL
	 */
	private short eventType;
	/**
	 * Area 코드
	 */
	protected String areaCd;
	/**
	 * 화주 코드
	 */
	protected String comCd;
	/**
	 * 작업 일자
	 */
	protected String jobDate;
	/**
	 * 작업 차수
	 */
	protected String jobSeq;
	/**
	 * 3rd-party 주문 제공자
	 */
	protected String thirdPartyProvider;
	/**
	 * 리셉트 데이터
	 */
	private BatchReceipt receiptData;
	
	/**
	 * 이벤트 생성자 1
	 * 
	 * @param domainId
	 * @param eventType
	 * @param eventStep
	 */
	public BatchReceiveEvent(long domainId, short eventType, short eventStep) {
		super(domainId, eventStep);
		
		this.setEventType(eventType);
	}
	
	/**
	 * 이벤트 생성자 2
	 * 
	 * @param domainId
	 * @param eventType
	 * @param eventStep
	 * @param areaCd
	 * @param stageCd
	 * @param jobDate
	 * @param comCd
	 */
	public BatchReceiveEvent(long domainId, short eventType, short eventStep, String areaCd, String stageCd, String jobDate, String comCd) {
		this(domainId, eventStep, eventType);
		
		this.setAreaCd(areaCd);
		this.setStageCd(stageCd);
		this.setJobDate(jobDate);
		this.setComCd(comCd);
	}

	public short getEventType() {
		return eventType;
	}

	public void setEventType(short eventType) {
		this.eventType = eventType;
	}
	
	public void setReceiptData(BatchReceipt receiptData) {
		this.receiptData = receiptData;
	}
	
	public BatchReceipt getReceiptData() {
		return receiptData;
	}

	public String getAreaCd() {
		return areaCd;
	}

	public void setAreaCd(String areaCd) {
		this.areaCd = areaCd;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public String getJobSeq() {
		return jobSeq;
	}

	public void setJobSeq(String jobSeq) {
		this.jobSeq = jobSeq;
	}

	public String getThirdPartyProvider() {
		return thirdPartyProvider;
	}

	public void setThirdPartyProvider(String thirdPartyProvider) {
		this.thirdPartyProvider = thirdPartyProvider;
	}

}
