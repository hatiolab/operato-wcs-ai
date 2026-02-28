package xyz.elidom.rabbitmq.service.model;

/**
 * 메시지 큐 관리 화면  return 모델 
 * @author yang
 *
 */
public class QueueItem {
	private String siteCode;
	private String queueName;
	private int client;
	private int messageCount;
	private String messageBytes;
	private Boolean isSystemQueue;
	
	private String cudFlag_;
	
	public String getSiteCode() {
		return siteCode;
	}
	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	public int getMessageCount() {
		return messageCount;
	}
	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}
	public String getMessageBytes() {
		return messageBytes;
	}
	public void setMessageBytes(String messageBytes) {
		this.messageBytes = messageBytes;
	}
	public int getClient() {
		return client;
	}
	public void setClient(int client) {
		this.client = client;
	}
	public Boolean getIsSystemQueue() {
		return isSystemQueue;
	}
	public void setIsSystemQueue(Boolean isSystemQueue) {
		this.isSystemQueue = isSystemQueue;
	}
	
	public String getCudFlag_() {
		return cudFlag_;
	}
	public void setCudFlag_(String cudFlag_) {
		this.cudFlag_ = cudFlag_;
	}
	
}
