package xyz.anythings.base.event.classfy;

import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.WorkCell;
import xyz.anythings.base.event.IClassifyErrorEvent;
import xyz.anythings.base.event.IClassifyRunEvent;
import xyz.anythings.base.event.main.BatchRootEvent;

/**
 * 소분류 시 발생한 에러 이벤트 구현
 * 
 * @author shortstop
 */
public class ClassifyErrorEvent extends BatchRootEvent implements IClassifyErrorEvent {

	/**
	 * 소분류 이벤트
	 */
	private IClassifyRunEvent runEvent;
	/**
	 * 작업 인스턴스
	 */
	private JobInstance jobInstance;
	/**
	 * 작업 셀
	 */
	private WorkCell workCell;
	/**
	 * 예외
	 */
	private Throwable exception;
	
	/**
	 * 생성자 1
	 * 
	 * @param runEvent
	 * @param eventStep
	 * @param exception
	 */
	public ClassifyErrorEvent(IClassifyRunEvent runEvent, short eventStep, Throwable exception) {
		super(runEvent.getDomainId(), eventStep);
		
		this.setClassifyRunEvent(runEvent);
		this.setExcetpion(exception);
	}

	@Override
	public IClassifyRunEvent getClassifyRunEvent() {
		return this.runEvent;
	}

	@Override
	public void setClassifyRunEvent(IClassifyRunEvent runEvent) {
		this.runEvent = runEvent;
		
		if(runEvent != null) {
			this.setJobBatch(runEvent.getJobBatch());
		}
	}

	@Override
	public JobInstance getJobInstance() {
		return this.jobInstance;
	}

	@Override
	public void setJobInstance(JobInstance jobInstance) {
		this.jobInstance = jobInstance;
	}

	@Override
	public WorkCell getWorkCell() {
		return this.workCell;
	}

	@Override
	public void setWorkCell(WorkCell workCell) {
		this.workCell = workCell;
	}

	@Override
	public Throwable getException() {
		return this.exception;
	}

	@Override
	public void setExcetpion(Throwable exception) {
		this.exception = exception;
	}

}
