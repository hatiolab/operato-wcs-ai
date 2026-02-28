package xyz.anythings.gw.service.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import xyz.anythings.gw.service.model.IIndOnInfo;
import xyz.anythings.gw.service.model.IndOnPickReq;
import xyz.anythings.gw.service.model.IndOnStockReq;
import xyz.anythings.gw.service.mq.model.IndicatorOnInformation;
import xyz.anythings.gw.service.mq.model.MessageObject;
import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.rabbitmq.client.event.SystemMessageReceiveEvent;
import xyz.elidom.rabbitmq.message.MessageProperties;
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
	public static String messageObjectToJson(MessageObject msgObj) {
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
	public static MessageObject toMessageObject(SystemMessageReceiveEvent event) {
		try {
			return new ObjectMapper().readValue(new String(event.getMessage().getBody()), MessageObject.class);
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
	
	/**
	 * 표시기 점등 모델 생성 
	 * 
	 * @param indCd
	 * @param bizId
	 * @param color
	 * @param orgRelay
	 * @param orgBoxQty
	 * @param orgEaQty
	 * @return
	 */
	public static IIndOnInfo newIndOnInfo(String indCd, String bizId, String color, Integer orgRelay, Integer orgBoxQty, Integer orgEaQty) {
		IndicatorOnInformation indOnInfo = new IndicatorOnInformation();
		indOnInfo.setId(indCd);
		indOnInfo.setBizId(bizId);
		indOnInfo.setColor(color);
		indOnInfo.setOrgRelay(orgRelay);
		indOnInfo.setOrgBoxQty(orgBoxQty);
		indOnInfo.setOrgEaQty(orgEaQty);
		return indOnInfo;
	}
	
	/**
	 * 표시기 점등 모델 생성 
	 * 
	 * @param indCd
	 * @param bizId
	 * @param color
	 * @param orgRelay
	 * @param orgEaQty
	 * @return
	 */
	public static IIndOnInfo newIndOnInfo(String indCd, String bizId, String color, Integer orgRelay, Integer orgEaQty) {
		return newIndOnInfo(indCd, bizId, color, orgRelay, null, orgEaQty);
	}
	
	/**
	 * 표시기 점등 모델 생성 
	 * 
	 * @param indCd
	 * @param bizId
	 * @param color
	 * @param orgEaQty
	 * @return
	 */
	public static IIndOnInfo newIndOnInfo(String indCd, String bizId, String color, Integer orgEaQty) {
		return newIndOnInfo(indCd, bizId, color, null, null, orgEaQty);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 점등 모델 생성
	 * 
	 * @param batchId
	 * @param indOnPick
	 * @return
	 */
	public static IIndOnInfo newIndOnInfo(String batchId, IndOnPickReq indOnPick) {
		IndicatorOnInformation indOnInfo = new IndicatorOnInformation();
		indOnInfo.setId(indOnPick.getIndCd());
		indOnInfo.setBizId(indOnPick.getJobInstanceId());
		indOnInfo.setColor(indOnPick.getColorCd());
		// 작업 배치 범위 내에서 indOnPick 정보에 수량 설정에 따른 orgRelay, orgBoxQty, orgEaQty 값 설정
		BatchIndConfigUtil.setIndOnQty(batchId, indOnPick, indOnInfo);
		return indOnInfo;
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 점등 모델 생성
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param indOnPick
	 * @return
	 */
	public static IIndOnInfo newIndOnInfo(Long domainId, String stageCd, IndOnPickReq indOnPick) {
		IndicatorOnInformation indOnInfo = new IndicatorOnInformation();
		indOnInfo.setId(indOnPick.getIndCd());
		indOnInfo.setBizId(indOnPick.getJobInstanceId());
		indOnInfo.setColor(indOnPick.getColorCd());
		// 스테이지 범위 내에서 indOnPick 정보에 수량 설정에 따른 orgRelay, orgBoxQty, orgEaQty 값 설정
		StageIndConfigUtil.setIndOnQty(domainId, stageCd, indOnPick, indOnInfo);
		return indOnInfo;
	}
		
	/**
	 * 표시기 점등 모델 생성 
	 * 
	 * @param indCd
	 * @param bizId
	 * @param color
	 * @param segRole
	 * @param firstSegNo
	 * @param secondSegNo
	 * @param thirdSegNo
	 * @return
	 */
	public static IIndOnInfo newIndOnInfo(
			String indCd, 
			String bizId, 
			String color, 
			String[] segRole, 
			Integer firstSegNo, 
			Integer secondSegNo, 
			Integer thirdSegNo) {
		
		IndicatorOnInformation indOnInfo = new IndicatorOnInformation();
		indOnInfo.setId(indCd);
		indOnInfo.setBizId(bizId);
		indOnInfo.setColor(color);
		indOnInfo.setSegRole(segRole);
		indOnInfo.setOrgRelay(firstSegNo);
		indOnInfo.setOrgBoxQty(secondSegNo);
		indOnInfo.setOrgEaQty(thirdSegNo);
		return indOnInfo;
	}

	/**
	 * 작업 배치 범위 내에서 IndOnPickReq 배열을 gwPath - IndOnPickReq 리스트로 그루핑하여 리턴
	 * 
	 * @param batch
	 * @param indOnReqList
	 * @return
	 */
	public static Map<String, List<IIndOnInfo>> groupPickingByGwPath(
			String batchId, List<IndOnPickReq> indOnReqList) {
		
		Map<String, List<IIndOnInfo>> groupGwIndOnList = new HashMap<String, List<IIndOnInfo>>();

		for (IndOnPickReq indOnPick : indOnReqList) {
			String gwPath = indOnPick.getGwPath();
			
			List<IIndOnInfo> indOnList = 
					groupGwIndOnList.containsKey(gwPath) ? 
							groupGwIndOnList.get(gwPath) : new ArrayList<IIndOnInfo>();

			indOnList.add(newIndOnInfo(batchId, indOnPick));
			groupGwIndOnList.put(gwPath, indOnList);
		}

		return groupGwIndOnList;
	}

	/**
	 * IndicatorOnInformation 배열을 gwPath - IndicatorOnInformation 리스트로 그루핑하여 리턴
	 * 
	 * @param bizId
	 * @param color
	 * @param indOnStockReqList
	 * @return key : gatewayPath, value : IndicatorOnInformation List
	 */
	public static Map<String, List<IIndOnInfo>> groupStockByGwPath(
			String bizId, String color, List<IndOnStockReq> indOnStockReqList) {
		
		Map<String, List<IIndOnInfo>> gwIndOnListGroup = 
				new HashMap<String, List<IIndOnInfo>>();

		for (IndOnStockReq target : indOnStockReqList) {
			String gwPath = target.getGwPath();
			
			List<IIndOnInfo> indOnList = 
					gwIndOnListGroup.containsKey(gwPath) ? 
							gwIndOnListGroup.get(gwPath) : new ArrayList<IIndOnInfo>();

			indOnList.add(newIndOnInfo(target.getIndCd(), bizId, target.getColorCd() != null ? target.getColorCd() : color, target.getAllocQty(), target.getLoadQty()));
			gwIndOnListGroup.put(gwPath, indOnList);
		}

		return gwIndOnListGroup;
	}
	
	/**
	 * 표시기 설정 셋 ID 범위 내에서 IndOnPickReq 배열을 gwPath - IndOnPickReq 리스트로 그루핑하여 리턴
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param indOnReqList
	 * @return
	 */
	public static Map<String, List<IIndOnInfo>> groupTestByGwPath(
			Long domainId, String stageCd, List<IndOnPickReq> indOnReqList) {
		
		Map<String, List<IIndOnInfo>> groupGwIndOnList = new HashMap<String, List<IIndOnInfo>>();

		for (IndOnPickReq indOnPick : indOnReqList) {
			String gwPath = indOnPick.getGwPath();
			
			List<IIndOnInfo> indOnList = 
					groupGwIndOnList.containsKey(gwPath) ? 
							groupGwIndOnList.get(gwPath) : new ArrayList<IIndOnInfo>();

			indOnList.add(newIndOnInfo(domainId, stageCd, indOnPick));
			groupGwIndOnList.put(gwPath, indOnList);
		}

		return groupGwIndOnList;
	}

}
