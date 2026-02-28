package xyz.anythings.base.event.main;

import xyz.anythings.base.entity.JobBatch;

/**
 * 배치 작업 마감 이벤트
 * 
 * @author yang
 */
public class BatchCloseEvent extends BatchRootEvent {

	public BatchCloseEvent(JobBatch batch, short eventStep) {
		super(batch, eventStep);
	}
}
