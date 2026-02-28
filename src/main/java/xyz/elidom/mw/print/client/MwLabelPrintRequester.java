package xyz.elidom.mw.print.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.elidom.dev.rest.DiyTemplateController;
import xyz.elidom.mw.print.message.Action;
import xyz.elidom.mw.print.message.IPrintBody;
import xyz.elidom.mw.print.message.MwPrintMsgObject;
import xyz.elidom.mw.print.message.ZplPrintBody;
import xyz.elidom.mw.rabbitmq.message.MessageProperties;
import xyz.elidom.mw.service.MwMessageSender;
import xyz.elidom.print.entity.Printer;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.util.ValueUtil;

/**
 * 라벨 프린트 서비스 요청 - M/W를 통해 인쇄 요청
 * 
 * @author shortstop
 */
@Component
public class MwLabelPrintRequester {
	/**
	 * 도메인 컨트롤러
	 */
	@Autowired
	private DomainController domainCtrl;
	/**
	 * DiyTemplate Controller
	 */
	@Autowired
	private DiyTemplateController templateCtrl;
	/**
	 * 미들웨어 메시지 센더
	 */
	@Autowired
	private MwMessageSender mwSender;
	/**
	 * 이벤트 퍼블리셔
	 */
	@Autowired
	protected EventPublisher eventPublisher;
	
	/**
	 * 바코드 라벨 인쇄 동기 모드
	 * 
	 * @param printEvent
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'barcode' and #printEvent.commType == 'mw' and #printEvent.isSyncMode() == true")
	public void printLabelSyncMode(PrintEvent printEvent) {
		
		this.printLabel(null, printEvent);
	}
	
	/**
	 * 바코드 라벨 인쇄 비동기 모드
	 * 
	 * @param printEvent
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'barcode' and #printEvent.commType == 'mw' and #printEvent.isSyncMode() == false")
	public void printLabelAsyncMode(PrintEvent printEvent) {
		
		this.printLabel(null, printEvent);
	}
	
	/**
	 * 라벨 프린트 처리
	 * 
	 * @param currentDomain
	 * @param printEvent
	 */
	private void printLabel(Domain currentDomain, PrintEvent printEvent) {
		
		// 1. 현재 도메인 조회
		Long domainId = printEvent.getDomainId();
		Domain domain = currentDomain != null ? currentDomain : this.domainCtrl.findOne(domainId, null);
		
		// 2. 현재 도메인이 없다면 (즉 비동기 모드인 경우) 현재 도메인 설정
		if(currentDomain == null) {
			DomainContext.setCurrentDomain(domain);
		}
		
		try {
			// 3. 인쇄 옵션 정보 추출
			Printer printer = Printer.findByIdOrName(domainId, printEvent.getPrinterId(), true);
			
			// 4. 라벨 템플릿 & 메시지 생성
			String labelTemplate = this.templateCtrl.content(printEvent.getPrintTemplate(), printEvent.getTemplateParams());
			String sourceId = this.mwSender.getDefaultQueueName(domain);
			MessageProperties properties = this.mwSender.newRequestMessageProperties(sourceId, printer.getPrinterAgentUrl(), "PRINT", printer.getPrinterNm(), printer.getPrinterCd(), "PRINT", Action.Values.ZplPrint);
			IPrintBody body = new ZplPrintBody(printer.getPrinterDriver(), labelTemplate, printEvent.getPrintCount());
			MwPrintMsgObject message = new MwPrintMsgObject(properties, body);
			this.mwSender.send(message);

			// 5. 이벤트 처리 결과 설정
			printEvent.setExecuted(true);
			
		} catch (Exception e) {
			// 6. 예외 처리
			String errorType = (ValueUtil.isEmpty(printEvent.getJobType()) ? SysConstants.EMPTY_STRING : printEvent.getJobType() + SysConstants.DASH) + "PRINT_LABEL_ERROR"; 
			ErrorEvent errorEvent = new ErrorEvent(domainId, errorType, e, null, true, true);
			this.eventPublisher.publishEvent(errorEvent);
			
		} finally {
			// 7. 스레드 로컬 변수에서 currentDomain 리셋
			if(currentDomain == null) {
				DomainContext.unsetAll();
			}
		}
	}
}
