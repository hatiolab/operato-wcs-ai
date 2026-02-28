package xyz.elidom.mw.rabbitmq.message.api;

import xyz.elidom.mw.rabbitmq.message.MessageProperties;

/**
 * 미들웨어 메시지를 정의
 * 
 * @author shortstop
 */
public interface IMwMsgObject {

	/**
	 * 미들웨어 메시지 헤더 정보
	 *  
	 * @return
	 */
	public MessageProperties getProperties();
	
	/**
	 * 미들웨어 메시지 헤더 설정
	 * 
	 * @param header
	 */
	public void setProperties(MessageProperties properties);
		
	/**
	 * JSON 문자열로 변환 리턴
	 * 
	 * @return
	 */
	public String toJsonString();
}
