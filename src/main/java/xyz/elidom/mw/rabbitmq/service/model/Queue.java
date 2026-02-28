package xyz.elidom.mw.rabbitmq.service.model;

/**
 * rabbitmq 메시지 큐 모델
 * 
 * @author yang
 */
public class Queue {
	
	private int consumers;
	private String vhost;
	private int messageReady;
	private DetailRate messagesReadyDetails;
	private String name;
	
	public int getConsumers() {
		return consumers;
	}
	
	public void setConsumers(int consumers) {
		this.consumers = consumers;
	}
	
	public int getMessageReady() {
		return messageReady;
	}
	
	public void setMessageReady(int messageReady) {
		this.messageReady = messageReady;
	}
	
	public DetailRate getMessagesReadyDetails() {
		return messagesReadyDetails;
	}
	
	public void setMessagesReadyDetails(DetailRate messagesReadyDetails) {
		this.messagesReadyDetails = messagesReadyDetails;
	}
	
	public String getVhost() {
		return vhost;
	}
	
	public void setVhost(String vhost) {
		this.vhost = vhost;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}