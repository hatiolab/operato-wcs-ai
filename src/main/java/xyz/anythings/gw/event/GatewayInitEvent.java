package xyz.anythings.gw.event;

import xyz.anythings.gw.entity.Gateway;

/**
 * 게이트웨이 초기화 완료 보고에 대한 처리 이벤트
 * 
 * @author shortstop
 */
public class GatewayInitEvent extends AbstractGatewayEvent {
	
	/**
	 * 생성자 1 
	 * 
	 * @param eventStep
	 * @param gateway
	 */
	public GatewayInitEvent(short eventStep, Gateway gateway) {
		this.setEventStep(eventStep);
		this.setGateway(gateway);
	}

}
