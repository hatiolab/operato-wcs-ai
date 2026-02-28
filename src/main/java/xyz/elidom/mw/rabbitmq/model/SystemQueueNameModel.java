package xyz.elidom.mw.rabbitmq.model;

/**
 * 시스템 큐 명칭 생성 모델
 * 
 * @author yang
 */
public class SystemQueueNameModel {
	
	/**
	 * 도메인 사이트 - Virtual Host 명
	 */
	private String domainSite;
	/**
	 * 큐 이름
	 */
	private String queueName;
	
	/**
	 * 생성자
	 * 
	 * @param domainSite
	 * @param queueName
	 */
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
