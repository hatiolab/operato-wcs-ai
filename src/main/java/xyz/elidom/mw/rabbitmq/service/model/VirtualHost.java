package xyz.elidom.mw.rabbitmq.service.model;

/**
 * RabbitMQ 사이트 모델
 * 
 * @author yang
 */
public class VirtualHost {
	
	private String name;
	private String nameStr;
	private DetailRate recvOctDetails;
	private DetailRate sendOctDetails;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DetailRate getRecvOctDetails() {
		return recvOctDetails;
	}

	public void setRecvOctDetails(DetailRate recvOctDetails) {
		this.recvOctDetails = recvOctDetails;
	}

	public DetailRate getSendOctDetails() {
		return sendOctDetails;
	}

	public void setSendOctDetails(DetailRate sendOctDetails) {
		this.sendOctDetails = sendOctDetails;
	}

	public String getNameStr() {
		return nameStr;
	}

	public void setNameStr(String nameStr) {
		this.nameStr = nameStr;
	}
}
