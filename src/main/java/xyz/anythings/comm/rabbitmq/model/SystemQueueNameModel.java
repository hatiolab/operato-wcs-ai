package xyz.anythings.comm.rabbitmq.model;

/**
 * System 큐 명칭 생성 모델  
 * @author yang
 *
 */
public class SystemQueueNameModel {
	
	private String domainSite;
	private String queueName;
	
	public SystemQueueNameModel(String domainSite, String queueName) {
		this.domainSite = domainSite;
		this.queueName = queueName;
	}
	
	public String getDomainSite() {
		return domainSite;
	}
	
	public String getQueueName() {
		return this.queueName;
	}
}
