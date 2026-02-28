package xyz.anythings.comm.rabbitmq.event;

import java.util.List;

import xyz.anythings.comm.rabbitmq.event.model.IQueueNameModel;
import xyz.anythings.sys.event.model.SysEvent;

/**
 *  큐에 대한 CUD 처리를 위한 이벤트 
 * @author yang
 *
 */
public class MwQueueManageEvent extends SysEvent {
	
	// 변경 내용 
	private List<IQueueNameModel> modelList;

	public MwQueueManageEvent(long domainId, List<IQueueNameModel> models) {
		super(domainId);
		this.modelList = models;
	}

	public List<IQueueNameModel> getModelList() {
		return modelList;
	}

	public void setModelList(List<IQueueNameModel> modelList) {
		this.modelList = modelList;
	}
}
