package xyz.elidom.print.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import javax.print.PrintService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.elidom.exception.ElidomException;
import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.print.entity.Printer;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.system.pdf.PdfPrintingService;
import xyz.elidom.sys.system.print.ElidomPrintingService;
import xyz.elidom.sys.util.ThrowUtil;

/**
 * PDF 프린트 서비스 (서버가 직접 인쇄 처리)
 * 
 * @author shortstop
 */
@Component
public class PdfPrintService {
    /**
     * 도메인 컨트롤러
     */
    @Autowired
    protected DomainController domainCtrl;
    /**
     * 이벤트 퍼블리셔
     */
    @Autowired
    protected EventPublisher eventPublisher;
	/**
	 * 출력 컨트롤러
	 */
	@Autowired
	private PdfPrintingService jasperSvc;
	/**
	 * 프린팅 서비스
	 */
	@Autowired
	private ElidomPrintingService printingSvc;
    
	/**
	 * PDF 프린트 인쇄 동기 모드
	 * 
	 * @param printEvent
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'normal' and #printEvent.commType == 'server' and #printEvent.isSyncMode() == true")
	public void printPdfSyncMode(PrintEvent printEvent) {
		this.printPdf(null, printEvent);
	}
	
	/**
	 * PDF 프린트 인쇄 비동기 모드
	 * 
	 * @param printEvent
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'normal' and #printEvent.commType == 'server' and #printEvent.isSyncMode() == false")
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
            
            // 2. PrintAgent에 REST 호출
            this.requestPrintReport(domainId, printer, printEvent.getPrintTemplate(), printEvent.getTemplateParams());
            
            // 3. 실행 플래그
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
	 * Print Agent로 REST PDF 인쇄 요청
	 * 
	 * @param domainId
	 * @param printer
	 * @param templateName
	 * @param params
	 */
    public void requestPrintReport(Long domainId, Printer printer, String templateName, Map<String, Object> params) {
        try {
            // 1. JasperPrint 로딩
            JasperPrint jasperPrint = this.jasperSvc.loadJasperReport(domainId, templateName, params);
            
            // 2. 인쇄 내용을 스트림에 실어서 PrintAgent에 인쇄 요청  
            if (jasperPrint != null) {
            	
                // 2.1 스트림 리소스 생성
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperPrint, baos);
                InputStream is = new ByteArrayInputStream(baos.toByteArray());
                
                // 2.2 인쇄
                PrintService ps = this.printingSvc.getPrintService(printer.getPrinterDriver());
                this.printingSvc.printPdfStream(ps, is, null, printer.getDpi());
            }
            
        } catch(ElidomException ee) {
            throw ee;

        } catch(JRException jre) {
            throw ThrowUtil.newFailToProcessTemplate(templateName + " Report", jre);

        } catch(Exception e) {
            throw new ElidomServiceException(e);
        }
    }
}
