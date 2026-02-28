package xyz.elidom.mw.rabbitmq.model;

/**
 * Message 전송 테스트 결과 모델 
 * @author yang
 *
 */
public class PublishTestResultModel {
	boolean routed = false;

	public boolean isRouted() {
		return routed;
	}

	public void setRouted(boolean routed) {
		this.routed = routed;
	}
	
	
}
