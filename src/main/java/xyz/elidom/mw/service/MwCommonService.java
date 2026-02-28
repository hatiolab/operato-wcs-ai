package xyz.elidom.mw.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.sys.AnyConstants;
import xyz.anythings.sys.event.EventPublisher;
import xyz.elidom.mw.MwConstants;
import xyz.elidom.mw.rabbitmq.client.SystemClient;
import xyz.elidom.mw.rabbitmq.message.MessageProperties;
import xyz.elidom.mw.rabbitmq.message.api.IMwMsgObject;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 미들웨어 공통 서비스
 * 
 * @author shortstop
 */
@Component
public class MwCommonService {
	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * 도메인 컨트롤러
	 */
	@Autowired
	private DomainController domainCtrl;
	/**
	 * 이벤트 퍼블리셔
	 */
	@Autowired
	protected EventPublisher eventPublisher;
 	/**
	 * 미들웨어 시스템 클라이언트
	 */
	@Autowired
	protected SystemClient mwSystemClient;
		
	/**
	 * 메시지 로깅이 활성화 되어 있는지 도메인 별로 체크
	 * 
	 * @param domainId
	 * @return
	 */
	protected boolean isMessageLoggingEnabled(Long domainId) {
		return 	ValueUtil.toBoolean(SettingUtil.getValue(domainId, MwConstants.MW_LOG_RCV_MSG_ENABLED, AnyConstants.FALSE_STRING));
	}
	
	/**
	 * 메시지 로깅
	 * 
	 * @param domainId
	 * @param message
	 */
	protected void logInfoMessage(Long domainId, IMwMsgObject message) {
		if(this.isMessageLoggingEnabled(domainId)) {
			this.logger.info(message.toJsonString());
		}
	}
	
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
	 * 도메인 별 디폴트 큐 이름 리턴
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public String getDefaultQueueName(Long domainId, String stageCd) {
		Domain domain = this.domainCtrl.findOne(domainId, null);
		return this.getDefaultQueueName(domain);
	}
	
	/**
	 * 도메인 별 디폴트 큐 이름 리턴
	 * 
	 * @param domain
	 * @param stageCd
	 * @return
	 */
	public String getDefaultQueueName(Domain domain) {
		return domain.getId() + SysConstants.DASH + domain.getMwSiteCd();
	}
	
	/**
	 * 미들웨어 요청 메시지 헤더를 생성
	 * 
	 * @param id
	 * @param site
	 * @param sourceId
	 * @param destinationId
	 * @param equipType
	 * @param equipVendor
	 * @param equipCd
	 * @param bizType
	 * @param action
	 * @return
	 */
	public MessageProperties newRequestMessageProperties(String id, String site, String sourceId, String destinationId, String equipType, String equipVendor, String equipCd, String bizType, String action) {		
		return new MessageProperties(id, site, sourceId, destinationId, false, equipType, equipVendor, equipCd, bizType, action);
	}
	
	/**
	 * 미들웨어 요청 메시지 헤더를 생성
	 * 
	 * @param sourceId
	 * @param destinationId
	 * @param equipType
	 * @param equipVendor
	 * @param equipCd
	 * @param bizType
	 * @param action
	 * @return
	 */
	public MessageProperties newRequestMessageProperties(String sourceId, String destinationId, String equipType, String equipVendor, String equipCd, String bizType, String action) {		
		return new MessageProperties(null, null, sourceId, destinationId, false, equipType, equipVendor, equipCd, bizType, action);
	}
	
	/**
	 * 미들웨어 응답 메시지 헤더를 생성
	 * 
	 * @param id
	 * @param site
	 * @param sourceId
	 * @param destinationId
	 * @param equipType
	 * @param equipVendor
	 * @param equipCd
	 * @param bizType
	 * @param action
	 * @return
	 */
	public MessageProperties newResponseMessageProperties(String id, String site, String sourceId, String destinationId, String equipType, String equipVendor, String equipCd, String bizType, String action) {		
		return new MessageProperties(id, site, sourceId, destinationId, true, equipType, equipVendor, equipCd, bizType, action);
	}
	
	/**
	 * 미들웨어 요청 메시지로 부터 응답 메시지 헤더를 생성
	 * 
	 * @param requestMessage
	 * @return
	 */
	public MessageProperties newResponseMessageProperties(IMwMsgObject requestMessage) {
		MessageProperties reqProperties = requestMessage.getProperties();
		MessageProperties resProperties = ValueUtil.populate(reqProperties, new MessageProperties());
		resProperties.setIsReply(true);
		return resProperties;
	}
	
	/**
	 * 미들웨어를 통해 메시지를 전송한다.
	 * 
	 * @param domainId
	 * @param message
	 */
	public void send(Long domainId, IMwMsgObject message) {
		this.mwSystemClient.sendMessage(message);
		this.logInfoMessage(domainId, message);
	}
	
	/**
	 * 미들웨어를 통해 메시지를 전송한다.
	 * 
	 * @param message
	 */
	public void send(IMwMsgObject message) {
		this.mwSystemClient.sendMessage(message);
	}
}
