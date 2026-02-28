package xyz.anythings.gw.event;

import xyz.anythings.gw.entity.Gateway;
import xyz.anythings.sys.event.model.SysEvent;

/**
 * 게이트웨이 관련 최상위 이벤트 
 * 
 * @author shortstop
 */
public class AbstractGatewayEvent extends SysEvent {

	/**
	 * 이전 : 1
	 */
	public static final short EVENT_STEP_BEFORE = 1;
	/**
	 * 이후 : 2
	 */
	public static final short EVENT_STEP_AFTER = 2;
	/**
	 * 이벤트 스텝 - 1 : 전 처리 , 2 : 후 처리
	 */
	protected short eventStep;
	/**
	 * 게이트웨이 
	 */
	protected Gateway gateway;
		
	public short getEventStep() {
		return eventStep;
	}

	public void setEventStep(short eventStep) {
		this.eventStep = eventStep;
	}

	public Gateway getGateway() {
		return gateway;
	}

	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
		this.setDomainId(gateway.getDomainId());
	}
	
}
