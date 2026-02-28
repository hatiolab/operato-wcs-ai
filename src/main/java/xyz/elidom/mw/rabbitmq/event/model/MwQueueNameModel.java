package xyz.elidom.mw.rabbitmq.event.model;

/**
 * 큐 명칭 생성 모델
 *   
 * @author yang
 */
public class MwQueueNameModel implements IQueueNameModel {
	/**
	 * 도메인 ID
	 */
	private long domainId;
	/**
	 * 도메인 사이트 코드
	 */
	private String domainSite;
	/**
	 * 이전 큐 이름
	 */
	private String befQueueName;
	/**
	 * 큐 이름
	 */
	private String queueName;
	/**
	 * 생성 (c), 수정 (u), 삭제 (d) 플래그
	 */
	private String cudFlag_;
	
	public MwQueueNameModel() {
	}
	
	public MwQueueNameModel(long domainId, String domainSite, String befQueueName, String queueName, String cudFlag_) {
		this.domainId = domainId;
		this.domainSite = domainSite;
		this.befQueueName = befQueueName;
		this.queueName = queueName;
		this.cudFlag_ = cudFlag_;
	}
	
	public MwQueueNameModel(long domainId, String domainSite, String queueName, String cudFlag_) {
		this.domainId = domainId;
		this.domainSite = domainSite;
		this.queueName = queueName;
		this.cudFlag_ = cudFlag_;
	}	
	
	@Override
	public String getBefQueueName() {
		return this.befQueueName;
	}
	
	public void setBefQueueName(String befQueueName) {
		this.befQueueName = befQueueName;
	}
	
	@Override
	public long getDomainId() {
		return domainId;
	}
	
	public void setDomainId(long domainId) {
		this.domainId = domainId;
	}
	
	@Override
	public String getCudFlag_() {
		return cudFlag_;
	}
	
	public void setCudFlag_(String cudFlag_) {
		this.cudFlag_ = cudFlag_;
	}
	
	public String getDomainSite() {
		return domainSite;
	}
	
	@Override
	public void setDomainSite(String domainSite) {
		this.domainSite = domainSite;
	}
	
	@Override
	public String getQueueName() {
		return this.queueName;
	}
}
