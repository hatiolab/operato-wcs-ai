package xyz.anythings.comm.rabbitmq.event;

import java.util.List;

import xyz.anythings.comm.rabbitmq.event.model.LogisQueueNameModel;
import xyz.anythings.sys.event.model.SysEvent;

/**
 * 어플리케이션 구동시 생성될 큐 이름을 받기 위한 event
 * @author yang
 *
 */
public class MwLogisQueueListEvent extends SysEvent {

	private List<LogisQueueNameModel> initQueueNames;

	public MwLogisQueueListEvent() {
		this(0L);
	}
	
	public MwLogisQueueListEvent(long domainId) {
		super(domainId);
	}
	
	public List<LogisQueueNameModel> getInitQueueNames() {
		return initQueueNames;
	}

	public void setInitQueueNames(List<LogisQueueNameModel> initData) {
		this.initQueueNames = initData;
	}
}
