package xyz.anythings.base.event.main;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.event.IBatchBasedEvent;
import xyz.anythings.sys.event.model.SysEvent;

/**
 * 작업 관련 최상위 이벤트 구현
 *  
 * @author yang
 */
public class BatchRootEvent extends SysEvent implements IBatchBasedEvent {
	
	/**
	 * 작업 배치
	 */
	protected JobBatch jobBatch;
	/**
	 * 스테이지 코드
	 */
	protected String stageCd;
	/**
	 * 작업 유형
	 */
	protected String jobType;
	/**
	 * 설비 유형
	 */
	protected String equipType;
	
	/**
	 * 생성자 1 
	 * 
	 * @param eventStep
	 */
	public BatchRootEvent(short eventStep) {
		super();
		
		this.setEventStep(eventStep);
		this.setAfterEventCancel(false);
	}
	
	/**
	 * 생성자 2
	 * 
	 * @param domainId
	 * @param eventStep
	 */
	public BatchRootEvent(long domainId, short eventStep) {
		super(domainId);
		
		this.setEventStep(eventStep);
		this.setAfterEventCancel(false);
	}
	
	/**
	 * 생성자 3 
	 * 
	 * @param batch
	 * @param eventStep
	 */
	public BatchRootEvent(JobBatch batch, short eventStep) {
		super(batch.getDomainId());
		
		this.setJobBatch(batch);
		this.setEventStep(eventStep);
		this.setAfterEventCancel(false);
	}
	
	public JobBatch getJobBatch() {
		return this.jobBatch;
	}
	
	public void setJobBatch(JobBatch jobBatch) {
		this.jobBatch = jobBatch;
		
		if(jobBatch != null) {
			this.stageCd = jobBatch.getStageCd();
			this.equipType = jobBatch.getEquipType();
			this.jobType = jobBatch.getJobType();
		}
	}
	
	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

	public String getJobType() {
		return this.jobType;
	}
	
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	
	public String getEquipType() {
		return this.equipType;
	}
	
	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}

}
