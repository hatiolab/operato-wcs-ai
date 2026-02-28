package xyz.elidom.mw.util;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.mw.rabbitmq.client.event.SystemMessageReceiveEvent;
import xyz.elidom.mw.rabbitmq.message.MessageProperties;
import xyz.elidom.mw.rabbitmq.message.MwMsgObject;
import xyz.elidom.util.ValueUtil;

/**
 * 미들웨어 통신을 위한 메시지 생성 유틸리티 
 * 
 * @author shortstop
 */
public class MwMessageUtil {
	
	/**
	 * Convert MessageObject to JSON String.
	 * 
	 * @param msgObj
	 * @return
	 */
	public static String messageObjectToJson(MwMsgObject msgObj) {
		try {
			return new ObjectMapper().writeValueAsString(msgObj);
		} catch (JsonProcessingException e) {
			throw new ElidomServiceException(e.getMessage(), e);
		}
	}
	
	/**
	 * Parse SystemMessageReceiveEvent to MessageObject.
	 * 
	 * @param event
	 * @return
	 */
	public static Object toMessageObject(SystemMessageReceiveEvent event, Class<?> clazz) {
		try {
			return new ObjectMapper().readValue(new String(event.getMessage().getBody()), clazz);
		} catch (Exception e) {
			throw new ElidomServiceException(e.getMessage(), e);
		}
	}

	/**
	 * 메시징 서버로 던질 메시지의 공통 전송 요청 메시지 프로퍼티를 생성
	 * 
	 * @param msgSrcId 메시지 소스 ID
	 * @param msgDestId 목적지 고유 ID
	 * @return
	 */
	public static MessageProperties newReqMessageProp(String msgSrcId, String msgDestId) {
		return MwMessageUtil.newMessageProp(msgSrcId, msgDestId, new Date().getTime(), false);
	}

	/**
	 * 메시징 서버로 던질 메시지의 공통 전송 요청 메시지 프로퍼티를 생성
	 * 
	 * @param msgSrcId 메시지 소스 ID
	 * @param msgDestId 목적지 고유 ID
	 * @param transmissionTime 전송 시간
	 * @return
	 */
	public static MessageProperties newReqMessageProp(String msgSrcId, String msgDestId, long transmissionTime) {
		return MwMessageUtil.newMessageProp(msgSrcId, msgDestId, transmissionTime, false);
	}

	/**
	 * 메시징 서버로 던질 메시지의 공통 응답 메시지 프로퍼티를 생성
	 * 
	 * @param msgSrcId 메시지 소스 ID
	 * @param msgDestId 목적지 고유 ID
	 * @return
	 */
	public static MessageProperties newResMessageProp(String msgSrcId, String msgDestId) {
		return MwMessageUtil.newMessageProp(msgSrcId, msgDestId, new Date().getTime(), true);
	}

	/**
	 * 메시징 서버로 던질 메시지의 공통 응답 메시지 프로퍼티를 생성
	 * 
	 * @param msgSrcId 메시지 소스 ID
	 * @param msgDestId 목적지 고유 ID
	 * @param transmissionTime 전송 시간
	 * @return
	 */
	public static MessageProperties newResMessageProp(String msgSrcId, String msgDestId, long transmissionTime) {
		return MwMessageUtil.newMessageProp(msgSrcId, msgDestId, transmissionTime, true);
	}

	/**
	 * 메시징 서버로 던질 메시지의 공통 메시지 프로퍼티를 생성
	 * 
	 * @param msgSrcId 메시지 소스 ID
	 * @param msgDestId 목적지 고유 ID
	 * @param isReply
	 * @return
	 */
	public static MessageProperties newMessageProp(String msgSrcId, String msgDestId, boolean isReply) {
		return MwMessageUtil.newMessageProp(msgSrcId, msgDestId, new Date().getTime(), isReply);
	}
	
	/**
	 * 메시징 서버로 던질 메시지의 공통 메시지 프로퍼티를 생성
	 * 
	 * @param msgId 메시지 소스 ID
	 * @param msgSrcId 메시지 소스 ID
	 * @param msgDestId
	 * @param isReply
	 * @return
	 */
	public static MessageProperties newMessageProp(String msgId, String msgSrcId, String msgDestId, boolean isReply) {
		return MwMessageUtil.newMessageProp(msgId, msgSrcId, msgDestId, new Date().getTime(), isReply);
	}

	/**
	 * 메시징 서버로 던질 메시지의 공통 메시지 프로퍼티를 생성  
	 * 
	 * @param msgSrcId 메시지 소스 ID
	 * @param msgDestId 목적지 고유 ID
	 * @param transmissionTime 메시지 전송 시간
	 * @param isReply 응답 메시지 여부
	 * @return
	 */
	public static MessageProperties newMessageProp(String msgSrcId, String msgDestId, long transmissionTime, boolean isReply) {
		return MwMessageUtil.newMessageProp(null, msgSrcId, msgDestId, transmissionTime, isReply);
	}
	
	/**
	 * 메시징 서버로 던질 메시지의 공통 메시지 프로퍼티를 생성 
	 * 
	 * @param msgId 메시지 고유 ID
	 * @param msgSrcId 메시지 소스 ID (물류에서는 스테이지 용 큐 이름)
	 * @param msgDestId 목적지 고유 ID (예를 들면 게이트웨이 용 큐 이름)
	 * @param transmissionTime 메시지 전송 시간
	 * @param isReply 응답 메시지 여부
	 * @return
	 */
	public static MessageProperties newMessageProp(String msgId, String msgSrcId, String msgDestId, long transmissionTime, boolean isReply) {
		MessageProperties properties = new MessageProperties();
		properties.setId(ValueUtil.isEmpty(msgId) ? UUID.randomUUID().toString() : msgId);
		properties.setTime(transmissionTime);
		properties.setDestId(msgDestId);
		properties.setSourceId(msgSrcId);
		properties.setIsReply(isReply);
		return properties;
	}
}
