package xyz.anythings.base.event.main;

import xyz.anythings.base.entity.JobBatch;

/**
 * 주문 가공 이벤트
 * 
 * @author shortstop
 */
public class BatchPreprocessEvent extends BatchRootEvent {
	
	/**
	 * 주문 가공 이벤트 타입
	 * 주문 가공 요약 정보	EventConstants.EVENT_PREPROCESS_SUMMARY 			= 10;
	 * 설비 수동 할당	 	EventConstants.EVENT_PREPROCESS_EQUIP_MANUAL_ASSIGN = 20;	
	 * 설비 자동 할당	 	EventConstants.EVENT_PREPROCESS_EQUIP_AUTO_ASSIGN 	= 30;
	 * 셀 할당			EventConstants.EVENT_PREPROCESS_SUB_EQUIP_ASSIGN 	= 40;	
	 * 주문 가공 완료	 	EventConstants.EVENT_PREPROCESS_COMPLETE 			= 50;	
	 */	
	private short eventType;

	/**
	 * 주문 가공 이벤트 생성자
	 * 
	 * @param batch
	 * @param eventStep
	 * @param eventType
	 */
	public BatchPreprocessEvent(JobBatch batch, short eventStep, short eventType) {
		super(batch, eventStep);
		this.setEventType(eventType);
	}

	public short getEventType() {
		return eventType;
	}

	public void setEventType(short eventType) {
		this.eventType = eventType;
	}

}
