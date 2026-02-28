package xyz.anythings.base.event.classfy;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.event.IClassifyEndEvent;
import xyz.anythings.base.event.IClassifyEvent;

/**
 * 소분류 분류 완료 이벤트 구현
 * 
 * @author shortstop
 */
public class ClassifyEndEvent extends ClassifyEvent implements IClassifyEndEvent {
	/**
	 * 분류 처리 이벤트
	 */
	private IClassifyEvent classifyEvent;
	/**
	 * 분류 처리한 스테이션 코드
	 */
	private String stationCd;
	
	/**
	 * 생성자 1
	 * 
	 * @param batch
	 * @param eventStep
	 */
	public ClassifyEndEvent(JobBatch batch, short eventStep) {
		this(batch, eventStep, null, null);
	}
	
	/**
	 * 생성자 2
	 * 
	 * @param batch
	 * @param eventStep
	 * @param stationCd
	 */
	public ClassifyEndEvent(JobBatch batch, short eventStep, String stationCd) {
		this(batch, eventStep, stationCd, null);
	}
	
	/**
	 * 생성자 3
	 * 
	 * @param batch
	 * @param eventStep
	 * @param stationCd
	 * @param result
	 */
	public ClassifyEndEvent(JobBatch batch, short eventStep, String stationCd, Object result) {
		super(batch, eventStep);
		this.stationCd = stationCd;
		this.setResult(result);
	}
	
	/**
	 * 생성자 4
	 * 
	 * @param classifyEvent
	 */
	public ClassifyEndEvent(IClassifyEvent classifyEvent) {
		this(classifyEvent, EVENT_STEP_ALONE, null, null);
	}
	
	/**
	 * 생성자 5
	 * 
	 * @param classifyEvent
	 * @param eventStep
	 */
	public ClassifyEndEvent(IClassifyEvent classifyEvent, short eventStep) {
		this(classifyEvent, eventStep, null, null);
	}
	
	/**
	 * 생성자 6
	 * 
	 * @param classifyEvent
	 * @param eventStep
	 * @param stationCd
	 */
	public ClassifyEndEvent(IClassifyEvent classifyEvent, short eventStep, String stationCd) {
		this(classifyEvent, eventStep, stationCd, null);
	}
	
	/**
	 * 생성자 7
	 * 
	 * @param classifyEvent
	 * @param eventStep
	 * @param stationCd
	 * @param result
	 */
	public ClassifyEndEvent(IClassifyEvent classifyEvent, short eventStep, String stationCd, Object result) {
		super(eventStep);
		this.classifyEvent = classifyEvent;
		this.setJobBatch(classifyEvent.getJobBatch());
		this.setResult(result);
		this.stationCd = stationCd;
	}

	@Override
	public IClassifyEvent getClassifyEvent() {
		return this.classifyEvent;
	}
	
	public void setClassifyEvent(IClassifyEvent classifyEvent) {
		this.classifyEvent = classifyEvent;
	}

	public String getStationCd() {
		return stationCd;
	}

	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
	}

}
