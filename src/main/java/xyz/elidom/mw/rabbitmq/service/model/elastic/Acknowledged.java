package xyz.elidom.mw.rabbitmq.service.model.elastic;

/**
 * elastic 결과 return 모델
 * 
 * @author yang
 */
public class Acknowledged {
	
	private Boolean acknowledged;

	public Boolean getAcknowledged() {
		return acknowledged;
	}

	public void setAcknowledged(Boolean acknowledged) {
		this.acknowledged = acknowledged;
	}
}
