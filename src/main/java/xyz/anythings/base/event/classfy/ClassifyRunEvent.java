package xyz.anythings.base.event.classfy;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.WorkCell;
import xyz.anythings.base.event.IClassifyRunEvent;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 소분류 분류 이벤트 구현
 * 
 * @author shortstop
 */
public class ClassifyRunEvent extends ClassifyEvent implements IClassifyRunEvent {
	/**
	 * 분류 장비
	 */
	protected String classifyDevice;
	/**
	 * 분류 액션
	 */
	protected String classifyAction;
	/**
	 * 작업 인스턴스
	 */
	protected JobInstance jobInstance;
	/**
	 * 작업이 일어난 셀 코드
	 */
	protected String cellCd;
	/**
	 * 작업이 일어나 작업 셀 
	 */
	protected WorkCell workCell;
	/**
	 * 처리 요청 수량
	 */
	protected int reqQty;
	/**
	 * 처리 결과 수량
	 */
	protected int resQty;
	/**
	 * 검수 & 피킹 여부
	 */
	protected boolean pickWithInspection;
	
	/**
	 * 소분류 분류 이벤트 생성자 1
	 * 
	 * @param eventStep
	 */
	public ClassifyRunEvent(short eventStep) {
		super(eventStep);
	}
	
	/**
	 * 소분류 분류 이벤트 생성자 2
	 * 
	 * @param batch
	 * @param eventStep
	 */
	public ClassifyRunEvent(JobBatch batch, short eventStep) {
		super(batch, eventStep);
	}
	
	/**
	 * 소분류 분류 이벤트 생성자 3
	 * 
	 * @param batch
	 * @param eventStep
	 * @param classifyDevice
	 * @param classifyAction
	 * @param job
	 * @param reqQty
	 * @param resQty
	 */
	public ClassifyRunEvent(JobBatch batch, short eventStep, String classifyDevice, String classifyAction, JobInstance job, int reqQty, int resQty) {
		super(batch, eventStep);
	
		this.setClassifyDevice(classifyDevice);
		this.setClassifyAction(classifyAction);
		this.setJobInstance(job);
		this.setReqQty(reqQty);
		this.setResQty(resQty);
	}
	
	/**
	 * 소분류 분류 이벤트 생성자 4
	 * 
	 * @param eventStep
	 * @param classifyDevice
	 * @param classifyAction
	 * @param job
	 * @param reqQty
	 * @param resQty
	 */
	public ClassifyRunEvent(short eventStep, String classifyDevice, String classifyAction, JobInstance job, int reqQty, int resQty) {
		super(eventStep);
	
		this.setClassifyDevice(classifyDevice);
		this.setClassifyAction(classifyAction);
		this.setJobInstance(job);
		this.setReqQty(reqQty);
		this.setResQty(resQty);
	}
	
	/**
	 * 소분류 분류 이벤트 생성자 5
	 * 
	 * @param batch
	 * @param eventStep
	 * @param classifyDevice
	 * @param classifyAction
	 * @param job
	 */
	public ClassifyRunEvent(JobBatch batch, short eventStep, String classifyDevice, String classifyAction, JobInstance job) {
		super(batch, eventStep);
	
		this.setClassifyDevice(classifyDevice);
		this.setClassifyAction(classifyAction);
		this.setJobInstance(job);
	}
	
	/**
	 * 소분류 분류 이벤트 생성자 6
	 * 
	 * @param eventStep
	 * @param classifyDevice
	 * @param classifyAction
	 * @param job
	 */
	public ClassifyRunEvent(short eventStep, String classifyDevice, String classifyAction, JobInstance job) {
		super(eventStep);
	
		this.setClassifyDevice(classifyDevice);
		this.setClassifyAction(classifyAction);
		this.setJobInstance(job);
	}

	@Override
	public String getClassifyDevice() {
		return this.classifyDevice;
	}

	@Override
	public void setClassifyDevice(String classifyDevice) {
		this.classifyDevice = classifyDevice;
	}

	@Override
	public String getCellCd() {
		return this.cellCd;
	}

	@Override
	public void setCellCd(String cellCd) {
		this.cellCd = cellCd;
	}

	@Override
	public String getClassifyAction() {
		return this.classifyAction;
	}

	@Override
	public void setClassifyAction(String classifyAction) {
		this.classifyAction = classifyAction;
	}

	@Override
	public JobInstance getJobInstance() {
		return jobInstance;
	}

	@Override
	public void setJobInstance(JobInstance jobInstance) {
		this.jobInstance = jobInstance;
		
		if(jobInstance != null) {
			this.cellCd = jobInstance.getSubEquipCd();
			
			if(this.jobBatch == null) {
				this.setJobBatch(AnyEntityUtil.findEntityById(true, JobBatch.class, jobInstance.getBatchId()));
			}
			
			if(LogisConstants.isB2BJobType(jobInstance.getJobType()) && ValueUtil.isNotEmpty(this.cellCd) && this.workCell == null) {
				this.setWorkCell(AnyEntityUtil.findEntityBy(jobInstance.getDomainId(), false, true, WorkCell.class, null, "batchId,cellCd", jobInstance.getBatchId(), this.cellCd));
			}
		}
	}

	@Override
	public int getReqQty() {
		return this.reqQty;
	}

	@Override
	public void setReqQty(int reqQty) {
		this.reqQty = reqQty;
	}

	@Override
	public int getResQty() {
		return this.resQty;
	}

	@Override
	public void setResQty(int resQty) {
		this.resQty = resQty;
	}

	@Override
	public boolean isPickWithInspection() {
		return pickWithInspection;
	}

	@Override
	public void setPickWithInspection(boolean pickWithInspection) {
		this.pickWithInspection = pickWithInspection;
	}

	@Override
	public WorkCell getWorkCell() {
		return this.workCell;
	}

	@Override
	public void setWorkCell(WorkCell workCell) {
		this.workCell = workCell;
	}

}
