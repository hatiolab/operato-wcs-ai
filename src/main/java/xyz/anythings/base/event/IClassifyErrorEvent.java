package xyz.anythings.base.event;

import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.WorkCell;

/**
 * 소분류 작업 시 발생한 에러 이벤트
 * 
 * @author shortstop
 */
public interface IClassifyErrorEvent extends IBatchBasedEvent {

	/**
	 * 분류 이벤트 리턴 
	 * 
	 * @return
	 */
	public IClassifyRunEvent getClassifyRunEvent();
	
	/**
	 * 분류 이벤트 설정
	 * 
	 * @param executionEvent
	 */
	public void setClassifyRunEvent(IClassifyRunEvent executionEvent);
		
	/**
	 * 작업 인스턴스 리턴
	 * 
	 * @return
	 */
	public JobInstance getJobInstance();
	
	/**
	 * 작업 인스턴스 설정
	 * 
	 * @param jobInstance
	 */
	public void setJobInstance(JobInstance jobInstance);
	
	/**
	 * 작업 셀 정보 리턴
	 * 
	 * @return
	 */
	public WorkCell getWorkCell();
	
	/**
	 * 작업 셀 정보 설정 
	 * 
	 * @param workCell
	 */
	public void setWorkCell(WorkCell workCell);
	
	/**
	 * 분류 처리 중 발생한 예외 리턴 
	 * 
	 * @return
	 */
	public Throwable getException();
	
	/**
	 * 분류 처리 중 발생한 예외 설정
	 * 
	 * @param exception
	 */
	public void setExcetpion(Throwable exception);
}
