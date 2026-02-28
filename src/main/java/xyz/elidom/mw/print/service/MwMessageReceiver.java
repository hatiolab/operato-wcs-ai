package xyz.elidom.mw.print.service;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.print.PrintService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.mw.print.message.Action;
import xyz.elidom.mw.print.message.MwPrintMsgObject;
import xyz.elidom.mw.print.message.PdfPrintBody;
import xyz.elidom.mw.print.message.ZplPrintBody;
import xyz.elidom.mw.rabbitmq.client.event.SystemMessageReceiveEvent;
import xyz.elidom.mw.rabbitmq.event.MwErrorEvent;
import xyz.elidom.mw.rabbitmq.message.api.IMwMsgObject;
import xyz.elidom.mw.service.MwCommonService;
import xyz.elidom.mw.util.MwMessageUtil;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.system.print.ElidomPrintingConfig;
import xyz.elidom.sys.system.print.ElidomPrintingService;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 메시징 미들웨어로 부터 프린트 관련 메시지를 접수받아 처리하는 포인트
 * 
 * 1. 바코드 인쇄 요청 처리
 * 2. PDF 인쇄 요청 처리
 * 
 * @author shortstop
 */
@Component
public class MwMessageReceiver extends MwCommonService {
	
	/**
	 * 도메인 맵 : Site Code - Domain
	 */
	private Map<String, Domain> domainMap = new ConcurrentHashMap<String, Domain>(8);
	/**
	 * 프린팅 설정
	 */
	@Autowired
	private ElidomPrintingConfig printingCfg;
	/**
	 * 프린팅 서비스
	 */
	@Autowired
	private ElidomPrintingService printingSvc;
	
	/**
	 * 도메인 맵에 siteCd - Domain 매핑 추가
	 * 
	 * @param siteCd
	 * @param domain
	 */
	private synchronized void addDomainMap(String siteCd, Domain domain) {
		if(!this.domainMap.containsKey(siteCd)) {
			this.domainMap.put(siteCd, domain);
		}
	}

	@Transactional
	@EventListener(condition = "#root.args[0].equipType == 'PRINTER'")
	public void messageReceiveEvent(SystemMessageReceiveEvent event) {
		// 1. 이벤트를 MessageObject로 파싱
		IMwMsgObject msgObj = (IMwMsgObject)MwMessageUtil.toMessageObject(event, MwPrintMsgObject.class);
		
		// 2. 사이트 도메인 조회
		String vHost = event.getVhost();
		Domain siteDomain = null;
		
		if(this.domainMap.containsKey(vHost)) {
			siteDomain = this.domainMap.get(vHost);
		} else {
			siteDomain = Domain.findByMwSiteCd(event.getVhost());
			this.addDomainMap(vHost, siteDomain);
		}

		// 3. 사이트 도메인 조회를 못했다면 에러
		if(siteDomain == null) {
			ElidomRuntimeException ee = new ElidomRuntimeException("Failed to find site by virtual host code [" + event.getVhost() + "]!");
			MwErrorEvent errorEvent = new MwErrorEvent(Domain.systemDomain().getId(), event, ee, true, true);
			this.eventPublisher.publishEvent(errorEvent);
			return;
		}
		
		// 4. 서버에서 직접 프린트하는 경우가 아니면 스킵
		if(!this.printingCfg.isServerPrintCommType(siteDomain.getId())) {
			return;
		}
		
		// 5. 스레드 로컬 변수에서 currentDomain 설정
		DomainContext.setCurrentDomain(siteDomain);
		try {
			// 6. 요청한 메시지에 대한 응답 (즉 ACK)에 대한 처리.
			if (ValueUtil.toBoolean(msgObj.getProperties().getIsReply())) {
				this.handleReplyMessage(siteDomain, msgObj);
			// 7. 타 시스템 혹은 장비에서 서버에 요청 메시지에 대한 처리.
			} else {
				this.handleReceivedMessage(siteDomain, msgObj);
			}
		} catch (Exception e) {
			// 8. 예외 처리
			MwErrorEvent errorEvent = new MwErrorEvent(Domain.systemDomain().getId(), event, e, true, true);
			this.eventPublisher.publishEvent(errorEvent);
			
		} finally {
			// 9. 스레드 로컬 변수에서 currentDomain 리셋 
			DomainContext.unsetAll();
		}
	}

	/**
	 * 설비에서 요청한 메시지에 대한 응답에 대한 처리.
	 * 
	 * @param siteDomain
	 * @param msgObj
	 */
	private void handleReplyMessage(Domain siteDomain, IMwMsgObject msgObj) {
		this.logInfoMessage(siteDomain.getId(), msgObj);
	}

	/**
	 * 표시기 측에서의 처리 이벤트를 실행한다.
	 * 
	 * @param siteDomain
	 * @param msgObj
	 */
	private void handleReceivedMessage(Domain siteDomain, IMwMsgObject msgObj) throws Exception {
		// 1. 메시지 로깅
		this.logInfoMessage(siteDomain.getId(), msgObj);
		
		// 2. 액션 추출
		String action = msgObj.getProperties().getAction();
		IMwMsgObject resMsgObj = null;
		
		switch (action) {
			// 3. ZPL Print
			case Action.Values.ZplPrint :
				resMsgObj = this.handleZplPrintMessage(siteDomain, msgObj);
				if(resMsgObj != null) {
					this.send(siteDomain.getId(), msgObj);
				}
				
				break;

			// 4. PDF Print
			case Action.Values.PdfPrint :
				resMsgObj = this.handlePdfPrintMessage(siteDomain, msgObj);
				if(resMsgObj != null) {
					this.send(siteDomain.getId(), msgObj);
				}
				
				break;
			
			// 5. Unkown action 메시지 처리
			default :
				this.handleUnkownMessage(siteDomain, msgObj);
		}
	}

	/**
	 * Unkown action type에 대한 메시지 처리
	 * 
	 * @param siteDomain
	 * @param msgObj
	 */
	private void handleUnkownMessage(Domain siteDomain, IMwMsgObject msgObj) {
		throw new ElidomRuntimeException("Unknown type Message Received");
	}
	
	/**
	 * ZPL 인쇄 메시지 처리
	 * 
	 * @param siteDomain
	 * @param msgObj
	 * @return
	 * @throws Exception
	 */
	public IMwMsgObject handleZplPrintMessage(Domain siteDomain, IMwMsgObject msgObj) throws Exception {
		// 1. 메시지 추출
		MwPrintMsgObject printMsg = (MwPrintMsgObject)msgObj;
		ZplPrintBody printBody = (ZplPrintBody)printMsg.getBody();

		// 2. 프린터 추출
		String printer = printBody.getPrinter();
		PrintService ps = this.printingSvc.getPrintService(printer);
		
		// 3. 직접 인쇄 처리
		if(ps != null) {
			this.printingSvc.printZplCode(ps, printBody.getZpl(), null);
		}
		
		// 4. ACK 메시지 전달을 위한 프로퍼티 처리
		printMsg.getProperties().setIsAckMsg(true);
		printMsg.getProperties().setIsReply(true);
		return printMsg;
	}
	
	/**
	 * PDF 인쇄 메시지 처리
	 * 
	 * @param siteDomain
	 * @param msgObj
	 * @return
	 */
	public IMwMsgObject handlePdfPrintMessage(Domain siteDomain, IMwMsgObject msgObj) {
		// 1. 메시지 추출
		MwPrintMsgObject printMsg = (MwPrintMsgObject)msgObj;
		PdfPrintBody printBody = (PdfPrintBody)printMsg.getBody();
		
		// 2. 프린터 추출
		String printer = printBody.getPrinter();
		PrintService ps = this.printingSvc.getPrintService(printer);
		
		// 3. 인쇄 처리
		if(ps != null) {
			this.printingSvc.printPdfStream(ps, new ByteArrayInputStream(printBody.getPdf()), printer, printBody.getDpi());
		}
		
		// 4. ACK 메시지 전달을 위한 프로퍼티 처리
		printMsg.getProperties().setIsAckMsg(true);
		printMsg.getProperties().setIsReply(true);
		return printMsg;
	}
}
