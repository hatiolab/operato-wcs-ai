package xyz.anythings.base.event;

import xyz.anythings.base.entity.JobBatch;

/**
 * 작업 배치 기반 이벤트 인터페이스
 * 
 * @author shortstop
 */
public interface IBatchBasedEvent {

	/**
	 * 도메인 ID
	 * 
	 * @return
	 */
	public Long getDomainId();
	
	/**
	 * 도메인 ID 설정
	 * 
	 * @param domainId
	 */
	public void setDomainId(Long domainId);
	
	/**
	 * 이벤트 스텝 
	 * 
	 * @return
	 */
	public short getEventStep();
	
	/**
	 * 이벤트 스텝 설정
	 * 
	 * @param eventStep
	 */
	public void setEventStep(short eventStep);

	/**
	 * 다음 이벤트를 계속 발생 할 지 여부 
	 * 
	 * @return
	 */
	public boolean isAfterEventCancel();
	
	/**
	 * 다음 이벤트를 계속 발생 할 지 여부 설정
	 * 
	 * @param isAfterEventCancel
	 */
	public void setAfterEventCancel(boolean isAfterEventCancel);
	
	/**
	 * 이벤트 소스 리턴 - 이벤트 발생 모듈명
	 * 
	 * @return
	 */
	public String getEventSource();
	
	/**
	 * 이벤트 소스 설정
	 * 
	 * @param eventSource
	 */
	public void setEventSource(String eventSource);
	
	/**
	 * 이벤트 타겟 리턴 - 이벤트를 수신할 모듈명
	 * 
	 * @return
	 */
	public String getEventTarget();
	
	/**
	 * 이벤트 타겟 설정
	 * 
	 * @param eventTarget
	 */
	public void setEventTarget(String eventTarget);
	
	/**
	 * 작업 배치
	 * 
	 * @return
	 */
	public JobBatch getJobBatch();
	
	/**
	 * 스테이지 코드
	 * 
	 * @return
	 */
	public String getStageCd();

	/**
	 * 작업 유형
	 * 
	 * @return
	 */
	public String getJobType();
	
	/**
	 * 설비 유형
	 * 
	 * @return
	 */
	public String getEquipType();
	
	/**
	 * 처리 결과 리턴 
	 * 
	 * @return
	 */
	public Object getResult();

	/**
	 * 처리 결과 설정
	 * 
	 * @param result
	 */
	public void setResult(Object result);

	/**
	 * 처리 여부 리턴
	 * 
	 * @return
	 */
	public boolean isExecuted();

	/**
	 * 처리 여부 설정
	 * 
	 * @param isExecuted
	 */
	public void setExecuted(boolean isExecuted);

}
