package xyz.elidom.mw.rabbitmq.message;

import xyz.elidom.mw.rabbitmq.message.api.IMwMsgObject;

/**
 * 미들웨어 메시지 정의 추상 클래스
 * 
 * @author shortstop
 */
public abstract class MwMsgObject implements IMwMsgObject {
	/**
	 * 메시지 헤더
	 */
	protected MessageProperties properties;
	
	/**
	 * 생성자
	 */
	protected MwMsgObject() {
	}
	
	/**
	 * 생성자
	 * 
	 * @param header 메시지 헤더
	 */
	protected MwMsgObject(MessageProperties properties) {
		this.properties = properties;
	}
	
	@Override
	public MessageProperties getProperties() {
		return this.properties;
	}

	@Override
	public void setProperties(MessageProperties properties) {
		this.properties = properties;
	}
}
