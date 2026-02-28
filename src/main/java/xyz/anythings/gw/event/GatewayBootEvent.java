package xyz.anythings.gw.event;

import xyz.anythings.gw.entity.Gateway;

/**
 * 게이트웨이에서 부트 요청에 대한 응답시 발생하는 이벤트
 *  
 * @author shortstop
 */
public class GatewayBootEvent extends AbstractGatewayEvent {

	/**
	 * 생성자
	 * 
	 * @param eventStep
	 * @param gateway
	 */
	public GatewayBootEvent(short eventStep, Gateway gateway) {
		this.setEventStep(eventStep);
		this.setGateway(gateway);
	}
	
}
