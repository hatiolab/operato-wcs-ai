package xyz.elidom.mw.print.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.elidom.mw.print.message.Action;
import xyz.elidom.mw.print.message.IPrintBody;
import xyz.elidom.mw.print.message.MwPrintMsgObject;
import xyz.elidom.mw.print.message.PdfPrintBody;
import xyz.elidom.mw.rabbitmq.message.MessageProperties;
import xyz.elidom.mw.rabbitmq.message.api.IMwMsgObject;
import xyz.elidom.mw.service.MwMessageSender;
import xyz.elidom.print.entity.Printer;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.system.pdf.PdfPrintingService;

/**
 * PDF 프린트 서비스 요청 - M/W를 통해 인쇄 요청
 * 
 * @author shortstop
 */
@Component
public class MwPdfPrintRequester {
	/**
	 * 도메인 컨트롤러
	 */
	@Autowired
	private DomainController domainCtrl;
	/**
	 * 미들웨어 센더
	 */
	@Autowired
	private MwMessageSender mwSender;
	/**
	 * PDF 프린팅 서비스
	 */
	@Autowired
	private PdfPrintingService pdfPrintService;
	/**
	 * 이벤트 퍼블리셔
	 */
	@Autowired
	protected EventPublisher eventPublisher;

	/**
	 * PDF 프린트 인쇄 동기 모드
	 * 
	 * @param printEvent
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'normal' and #printEvent.commType == 'mw' and #printEvent.isSyncMode() == true")
	public void printPdfSyncMode(PrintEvent printEvent) {
		this.printPdf(null, printEvent);
	}
	
	/**
	 * PDF 프린트 인쇄 비동기 모드
	 * 
	 * @param printEvent
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'normal' and #printEvent.commType == 'mw' and #printEvent.isSyncMode() == false")
	public void printPdfAsyncMode(PrintEvent printEvent) {
		this.printPdf(null, printEvent);
	}

	/**
	 * PDF 출력
	 * 
	 * @param currentDomain
	 * @param printEvent
	 */
	private void printPdf(Domain currentDomain, PrintEvent printEvent) {
		// 현재 도메인 조회
		Long domainId = printEvent.getDomainId();
		Domain domain = currentDomain != null ? currentDomain : this.domainCtrl.findOne(domainId, null);
		
		// 현재 도메인이 없다면 (즉 비동기 모드인 경우) 현재 도메인 설정
		if(currentDomain == null) {
			DomainContext.setCurrentDomain(domain);
		}
		
		try {
			// 1. 프린터 조회
			Printer printer = Printer.findByIdOrName(domainId, printEvent.getPrinterId(), true);
			
			// 2. JasperReport 정보를 로딩
			byte[] pdfBytes = this.pdfPrintService.loadPdfReportToBytes(domainId, printEvent.getPrintTemplate(), printEvent.getTemplateParams());
			
			// 3. 미들웨어 전송
			this.sendMwMessage(domain, printEvent, printer, pdfBytes);
						
			// 4. 프린트 이벤트 처리 플래그
			printEvent.setExecuted(true);
			
		} catch (Exception e) {
			// 예외 처리
			ErrorEvent errorEvent = new ErrorEvent(domainId, "PRINT_PDF_ERROR", e, null, true, true);
			this.eventPublisher.publishEvent(errorEvent);
			
		} finally {
			// 스레드 로컬 변수에서 currentDomain 리셋
			if(currentDomain == null) {
				DomainContext.unsetAll();
			}
		}
	}
	
	/**
	 * PDF 인쇄 메시지 미들웨어 전송
	 * 
	 * @param domain
	 * @param printEvent
	 * @param printer
	 * @param pdfBytes
	 */
	private void sendMwMessage(Domain domain, PrintEvent printEvent, Printer printer, byte[] pdfBytes) {
		String sourceId = this.mwSender.getDefaultQueueName(domain);
		MessageProperties properties = this.mwSender.newRequestMessageProperties(sourceId, printer.getPrinterAgentUrl(), "PRINT", printer.getPrinterNm(), printer.getPrinterCd(), "PRINT", Action.Values.PdfPrint);
		IPrintBody body = new PdfPrintBody(printer.getPrinterDriver(), printer.getDpi(), pdfBytes, printEvent.getPrintCount());
		IMwMsgObject message = new MwPrintMsgObject(properties, body);
		this.mwSender.send(message);
	}
}
