package xyz.anythings.base.event.input;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.sys.event.model.SysEvent;

/**
 * 투입 이벤트
 * 
 * @author shortstop
 */
public class InputEvent extends SysEvent {
	
	/**
	 * 작업 배치
	 */
	private JobBatch batch;
	/**
	 * 투입 정보
	 */
	private JobInput jobInput;
	/**
	 * 작업 유형
	 */
	private String jobType;

	/**
	 * 생성자 1
	 */
	public InputEvent() {
		super();
	}
	
	/**
	 * 생성자 2
	 * 
	 * @param domainId
	 */
	public InputEvent(Long domainId) {
		super(domainId);
	}
	
	/**
	 * 생성자 3
	 * 
	 * @param jobInput
	 * @param jobType
	 */
	public InputEvent(JobInput jobInput, String jobType) {
		this.domainId = jobInput.getDomainId();
		this.jobInput = jobInput;
		this.jobType = jobType;
	}
	
	/**
	 * 생성자 4
	 * 
	 * @param batch
	 * @param jobInput
	 */
	public InputEvent(JobBatch batch, JobInput jobInput) {
		this.domainId = batch.getDomainId();
		this.batch = batch;
		this.jobInput = jobInput;
		this.jobType = batch.getJobType();
	}
	
	public JobBatch getBatch() {
		return batch;
	}
	
	public void setBatch(JobBatch batch) {
		this.batch = batch;
	}
	
	public JobInput getJobInput() {
		return jobInput;
	}
	
	public void setJobInput(JobInput jobInput) {
		this.jobInput = jobInput;
	}
	
	public String getJobType() {
		return jobType;
	}
	
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

}
