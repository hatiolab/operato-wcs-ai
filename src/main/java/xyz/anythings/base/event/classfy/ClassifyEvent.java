package xyz.anythings.base.event.classfy;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.event.IClassifyEvent;
import xyz.anythings.base.event.main.BatchRootEvent;

/**
 * 소분류 최상위 이벤트 구현
 *  
 * @author shortstop
 */
public class ClassifyEvent extends BatchRootEvent implements IClassifyEvent {

	public ClassifyEvent(short eventStep) {
		super(eventStep);
	}
	
	public ClassifyEvent(JobBatch batch, short eventStep) {
		super(batch, eventStep);
	}

}
