package xyz.anythings.base.event.classfy;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.WorkCell;
import xyz.anythings.base.event.IClassifyOutEvent;
import xyz.anythings.sys.util.AnyEntityUtil;

/**
 * 소분류 Out 이벤트 구현
 * 
 * @author shortstop
 */
public class ClassifyOutEvent extends ClassifyRunEvent implements IClassifyOutEvent {
	
	/**
	 * 박스 ID
	 */
	protected String boxId;
	
	/**
	 * 소분류 Out 이벤트 생성자 1
	 * 
	 * @param eventStep
	 * @param classifyDevice
	 * @param classifyAction
	 * @param job
	 * @param reqQty
	 * @param resQty
	 */
	public ClassifyOutEvent(short eventStep, String classifyDevice, String classifyAction, JobInstance job, int reqQty, int resQty) {
		super(eventStep, classifyDevice, classifyAction, job, reqQty, resQty);
	}
	
	/**
	 * 소분류 Out 이벤트 생성자 2
	 * 
	 * @param batch
	 * @param eventStep
	 * @param classifyDevice
	 * @param classifyAction
	 * @param job
	 * @param reqQty
	 * @param resQty
	 */
	public ClassifyOutEvent(JobBatch batch, short eventStep, String classifyDevice, String classifyAction, JobInstance job, int reqQty, int resQty) {
		super(batch, eventStep, classifyDevice, classifyAction, job, reqQty, resQty);
	}
	
	/**
	 * 소분류 Out 이벤트 생성자 3
	 * 
	 * @param eventStep
	 * @param classifyDevice
	 * @param classifyAction
	 * @param job
	 */
	public ClassifyOutEvent(short eventStep, String classifyDevice, String classifyAction, JobInstance job) {
		super(eventStep, classifyDevice, classifyAction, job);
	}
	
	/**
	 * 소분류 Out 이벤트 생성자 4
	 * 
	 * @param batch
	 * @param eventStep
	 * @param classifyDevice
	 * @param classifyAction
	 * @param job
	 * @param reqQty
	 * @param resQty
	 */
	public ClassifyOutEvent(JobBatch batch, short eventStep, String classifyDevice, String classifyAction, JobInstance job) {
		super(batch, eventStep, classifyDevice, classifyAction, job);
	}

	@Override
	public String getBoxId() {
		return this.boxId;
	}

	@Override
	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}
	
	@Override
	public void setWorkCell(WorkCell workCell) {
		this.workCell = workCell;
		
		if(this.workCell != null) {
			this.setBoxId(this.workCell.getBoxId());
			
			if(this.jobBatch == null) {
				this.setJobBatch(AnyEntityUtil.findEntityById(true, JobBatch.class, workCell.getBatchId()));
			}
		}
	}

}
