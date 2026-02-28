package xyz.anythings.base.event.main;

import java.util.List;

/**
 * 작업 지시 이벤트
 * 
 * @author yang
 */
public class BatchInstructEvent extends BatchRootEvent {
	
	/**
	 * 작업 지시 이벤트 타입
	 * 배치 작업 지시				EventConstants.EVENT_INSTRUCT_TYPE_INSTRUCT 		= 10;
	 * 배치 작업 지시 취소  			EventConstants.EVENT_INSTRUCT_TYPE_INSTRUCT_CANCEL 	= 20;
	 * 배치 작업 병합 				EventConstants.EVENT_INSTRUCT_TYPE_MERGE 			= 30;
	 * 배치 대상 분류  				EventConstants.EVENT_INSTRUCT_TYPE_CLASSIFICATION 	= 40;
	 * 배치 작업 지시 후 박스 요청  	EventConstants.EVENT_INSTRUCT_TYPE_BOX_REQ 			= 50;
	 * 토털 피킹  					EventConstants.EVENT_INSTRUCT_TYPE_TOTAL_PICKING 	= 60;
	 * 추천 로케이션  				EventConstants.EVENT_INSTRUCT_TYPE_RECOMMEND_CELLS 	= 70;
	 */	
	private short eventType;
	/**
	 * 할당 대상 설비 리스트 
	 */
	private List<?> equipList;
	
	/**
	 * 생성자 1
	 * 
	 * @param domainId
	 * @param eventType
	 * @param eventStep
	 */
	public BatchInstructEvent(long domainId, short eventType, short eventStep) {
		super(domainId, eventStep);
		this.setEventType(eventType);
	}

	public short getEventType() {
		return eventType;
	}

	public void setEventType(short eventType) {
		this.eventType = eventType;
	}
	
	public List<?> getEquipList() {
		return this.equipList;
	}
	
	public void setEquipList(List<?> equipList) {
		this.equipList = equipList;
	}

}
