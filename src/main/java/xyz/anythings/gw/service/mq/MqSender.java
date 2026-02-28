package xyz.anythings.gw.service.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.gw.service.mq.model.IMessageBody;
import xyz.anythings.gw.service.mq.model.MessageObject;
import xyz.anythings.gw.service.util.MwMessageUtil;
import xyz.elidom.rabbitmq.client.SystemClient;
import xyz.elidom.rabbitmq.message.MessageProperties;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.util.ValueUtil;

/**
 * 미들웨어로 메시지 전송하는 Sender
 * 
 * @author shortstop
 */
@Component
public class MqSender extends MqCommon {

	/**
	 * 도메인 컨트롤러
	 */
	@Autowired
	private DomainController domainCtrl;
	/**
	 * 미들웨어 시스템 클라이언트
	 */
	@Autowired
	private SystemClient mwSystemClient;
	
	/**
	 * 도메인 별 미들웨어 가상 호스트 코드를 조회
	 * 
	 * @param domainId
	 * @return
	 */
	public String getVirtualHost(Long domainId) {
		Domain domain = this.domainCtrl.findOne(domainId, null);
		return domain.getMwSiteCd();
	}
	
	/**
	 * 스테이지 코드별 큐 이름을 리턴 
	 * 
	 * @param virtualHost
	 * @param stageCd
	 * @return
	 */
	public String getStageQueueName(String virtualHost, String stageCd) {
		//return virtualHost + SysConstants.SLASH + stageCd;
		return stageCd;
	}
	
	/**
	 * 미들웨어를 통해 메시지 전송 목적지 msgDestId로 메시지 msgBody 내용을 전송 요청한다. 
	 * 
	 * @param virtualHost 각 사이트 (도메인) 별로 도메인 별로 가상 호스트 코드가 결정된다.
	 * @param stageCd 스테이지 코드
	 * @param msgDestId
	 * @param msgBody
	 */
	public void sendRequest(String virtualHost, String stageCd, String msgDestId, IMessageBody msgBody) {
		this.send(virtualHost, stageCd, MwMessageUtil.newMessageProp(stageCd, msgDestId, false), msgBody);
	}
	
	/**
	 * 미들웨어를 통해 메시지 전송 목적지 msgDestId로 메시지 msgBody 내용을 전송요청한다.
	 * 
	 * @param domainId 도메인 ID로 virtualHost를 조회 
	 * @param stageCd 스테이지 코드
	 * @param msgDestId
	 * @param msgBody
	 */
	public void sendRequest(Long domainId, String stageCd, String msgDestId, IMessageBody msgBody) {
		String virtualHost = this.getVirtualHost(domainId);
		this.sendRequest(virtualHost, stageCd, msgDestId, msgBody);
	}
	
	/**
	 * 미들웨어를 통해 메시지 전송 목적지 msgDestId로 메시지 msgBody 내용을 전송요청한다. 
	 * 
	 * @param virtualHost 각 사이트 (도메인) 별로 도메인 별로 가상 호스트 코드가 결정된다.
	 * @param stageCd 스테이지 코드
	 * @param msgId
	 * @param msgDestId
	 * @param msgBody
	 */
	public void sendRequest(String virtualHost, String stageCd, String msgId, String msgDestId, IMessageBody msgBody) {
		this.send(virtualHost, stageCd, MwMessageUtil.newMessageProp(msgId, msgDestId, false), msgBody);
	}
	
	/**
	 * 미들웨어를 통해 메시지 전송 목적지 msgDestId로 메시지 msgBody 내용을 전송요청한다.
	 * 
	 * @param domainId 도메인 ID로 virtualHost를 조회 
	 * @param stageCd 스테이지 코드
	 * @param msgId
	 * @param msgDestId
	 * @param msgBody
	 */
	public void sendRequest(Long domainId, String stageCd, String msgId, String msgDestId, IMessageBody msgBody) {
		String virtualHost = this.getVirtualHost(domainId);
		this.sendRequest(virtualHost, stageCd, msgId, msgDestId, msgBody);
	}

	/**
	 * 미들웨어를 통해 메시지 msgBody 내용으로 메시지 전송 목적지 msgDestId에 응답을 요청한다. 
	 * 
	 * @param virtualHost
	 * @param stageCd 스테이지 코드
	 * @param msgDestId
	 * @param msgBody
	 */
	public void sendResponse(String virtualHost, String stageCd, String msgDestId, IMessageBody msgBody) {
		this.send(virtualHost, stageCd, MwMessageUtil.newMessageProp(stageCd, msgDestId, true), msgBody);
	}
	
	/**
	 * 미들웨어를 통해 메시지 msgBody 내용으로 메시지 전송 목적지 msgDestId에 응답을 요청한다.
	 * 
	 * @param domainId
	 * @param stageCd 스테이지 코드
	 * @param msgDestId
	 * @param msgBody
	 */
	public void sendResponse(Long domainId, String stageCd, String msgDestId, IMessageBody msgBody) {
		String virtualHost = this.getVirtualHost(domainId);
		this.sendResponse(virtualHost, stageCd, msgDestId, msgBody);
	}
	
	/**
	 * 미들웨어를 통해 ID가 msgId인 메시지를 msgBody 내용으로 메시지 전송 목적지 msgDestId에 응답을 요청한다.
	 * 
	 * @param virtualHost
	 * @param stageCd 스테이지 코드
	 * @param msgId
	 * @param msgDestId
	 * @param msgBody
	 */
	public void sendResponse(String virtualHost, String stageCd, String msgId, String msgDestId, IMessageBody msgBody) {
		this.send(virtualHost, stageCd, MwMessageUtil.newMessageProp(msgId, msgDestId, true), msgBody);
	}
	
	/**
	 * 미들웨어를 통해 ID가 msgId인 메시지를 msgBody 내용으로 메시지 전송 목적지 msgDestId에 응답을 요청한다.
	 * 
	 * @param domainId
	 * @param stageCd 스테이지 코드
	 * @param msgId
	 * @param msgDestId
	 * @param msgBody
	 */
	public void sendResponse(Long domainId, String stageCd, String msgId, String msgDestId, IMessageBody msgBody) {
		String virtualHost = this.getVirtualHost(domainId);
		this.sendResponse(virtualHost, stageCd, msgId, msgDestId, msgBody);
	}
	
	/**
	 * 미들웨어를 통해 프로퍼티가 property이고 본문이 msgBody인 메시지를 전송한다.
	 * 
	 * @param domainId
	 * @param stageCd 스테이지 코드
	 * @param msgProp
	 * @param msgBody
	 */
	public void send(Long domainId, String stageCd, MessageProperties msgProp, IMessageBody msgBody) {
		String vHost = this.getVirtualHost(domainId);
		this.send(vHost, stageCd, msgProp, msgBody);
	}

	/**
	 * 미들웨어를 통해 프로퍼티가 property이고 본문이 msgBody인 메시지를 전송한다.
	 * 
	 * @param virtualHost
	 * @param stageCd 스테이지 코드
	 * @param msgProp
	 * @param msgBody
	 */
	public void send(String virtualHost, String stageCd, MessageProperties msgProp, IMessageBody msgBody) {
		MessageObject message = new MessageObject();
		message.setProperties(msgProp);
		
		if (ValueUtil.isNotEmpty(msgBody)) {
			message.setBody(msgBody);
		}

		String value = MwMessageUtil.messageObjectToJson(message);
		String stageQueueName = this.getStageQueueName(virtualHost, stageCd);
		this.mwSystemClient.sendMessage(virtualHost, stageQueueName, msgProp.getDestId(), value);
	}

}
