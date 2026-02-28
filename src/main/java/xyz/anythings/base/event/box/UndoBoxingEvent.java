package xyz.anythings.base.event.box;

import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.sys.event.model.SysEvent;

/**
 * 풀 박스 취소 이벤트 
 * 
 * @author shortstop
 */
public class UndoBoxingEvent extends SysEvent {
	/**
	 * 패킹 박스
	 */
	private BoxPack boxPack;
	
	/**
	 * 생성자 1
	 */
	public UndoBoxingEvent() {
		super();
	}
	
	/**
	 * 생성자 2
	 * 
	 * @param domainId
	 */
	public UndoBoxingEvent(Long domainId) {
		super(domainId);
	}

	/**
	 * 생성자 3
	 * 
	 * @param eventStep
	 * @param boxPack
	 */
	public UndoBoxingEvent(short eventStep, BoxPack boxPack) {
		this.setDomainId(boxPack.getDomainId());
		this.eventStep = eventStep;
		this.boxPack = boxPack;
	}

	public BoxPack getBoxPack() {
		return boxPack;
	}

	public void setBoxPack(BoxPack boxPack) {
		this.boxPack = boxPack;
	}

}
