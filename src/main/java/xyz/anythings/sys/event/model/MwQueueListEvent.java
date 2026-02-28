package xyz.anythings.sys.event.model;

import java.util.List;

import xyz.elidom.mw.rabbitmq.event.model.IQueueNameModel;

/**
 * 애플리케이션 초기화 시 생성할 M/W 큐 리스트 요청 event
 *  
 * @author yang
 */
public class MwQueueListEvent extends SysEvent {
	/**
	 * 초기화 시 생성할 큐 리스트
	 */
	private List<IQueueNameModel> queueNames;
	
	public MwQueueListEvent(Long domainId) {
		super(domainId);
	}
	
	public void setQueueNames(List<IQueueNameModel> queueNames) {
		this.queueNames = queueNames;
	}
	
	public List<IQueueNameModel> getQueueNames() {
		return this.queueNames;
	}
}
