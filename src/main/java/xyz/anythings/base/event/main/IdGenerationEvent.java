package xyz.anythings.base.event.main;

import xyz.anythings.sys.event.model.SysEvent;

/**
 * 작업 배치 ID 관련 이벤트
 * 
 * @author yang
 */
public class IdGenerationEvent extends SysEvent {
	
	/**
	 * 스테이지 코드
	 */
	private String stageCd;
	/**
	 * 이벤트 유형 
	 * 배치 ID 생성 이벤트 : EventConstants.EVENT_ID_GENERATION_BATCH_ID = 10
	 */
	private short eventType;
	
	public IdGenerationEvent() {
	}
	
	public IdGenerationEvent(Long domainId, String stageCd, short eventType) {
		this.domainId = domainId;
		this.stageCd = stageCd;
		this.eventStep = SysEvent.EVENT_STEP_ALONE;
		this.eventType = eventType;
	}
	
	public IdGenerationEvent(Long domainId, String stageCd, short eventStep, short eventType) {
		this.domainId = domainId;
		this.stageCd = stageCd;
		this.eventStep = eventStep;
		this.eventType = eventType;
	}
	
	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

	public short getEventType() {
		return eventType;
	}

	public void setEventType(short eventType) {
		this.eventType = eventType;
	}

}
