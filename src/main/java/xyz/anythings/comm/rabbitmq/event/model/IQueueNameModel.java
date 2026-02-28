package xyz.anythings.comm.rabbitmq.event.model;

/**
 * 래빗엠큐 큐 생성을 위한 인터페이스 
 * @author yang
 *
 */
public interface IQueueNameModel {
	public String getBefQueueName();
	public String getQueueName();
	public void setDomainSite(String domainSite);
	public String getDomainSite();
	public long getDomainId();
	public String getCudFlag_();
}
