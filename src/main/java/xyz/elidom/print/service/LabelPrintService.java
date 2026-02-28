package xyz.elidom.print.service;

import javax.print.PrintService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.elidom.dev.rest.DiyTemplateController;
import xyz.elidom.print.entity.Printer;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.system.print.ElidomPrintingConfig;
import xyz.elidom.sys.system.print.ElidomPrintingService;
import xyz.elidom.util.ValueUtil;

/**
 * 라벨 프린트 서비스 (서버가 직접 인쇄 처리)
 * 
 * @author shortstop
 */
@Component
public class LabelPrintService {
    /**
     * 도메인 컨트롤러
     */
    @Autowired
    protected DomainController domainCtrl;
    /**
     * DiyTemplate Controller
     */
    @Autowired
    private DiyTemplateController templateCtrl;
    /**
     * 이벤트 퍼블리셔
     */
    @Autowired
    protected EventPublisher eventPublisher;
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
	 * 바코드 라벨 인쇄 동기 모드
	 * 
	 * @param printEvent
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'barcode' and #printEvent.commType == 'server' and #printEvent.isSyncMode() == true")
	public void printLabelSyncMode(PrintEvent printEvent) {
		this.printLabel(null, printEvent);
	}
	
	/**
	 * 바코드 라벨 인쇄 비동기 모드
	 * 
	 * @param printEvent
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'barcode' and #printEvent.commType == 'server' and #printEvent.isSyncMode() == false")
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
			// 3. 프린터 조회
			Printer printer = Printer.findByIdOrName(domainId, printEvent.getPrinterId(), true);
			String printerName = (printer != null) ? printer.getPrinterNm() : this.printingCfg.getDefaultPrinterName();
			PrintService ps = this.printingSvc.getPrintService(printerName);
			
			// 4. 템플릿 엔진 실행
			int printCount = printEvent.getPrintCount() <= 0 ? 1 : printEvent.getPrintCount();
			String labelTemplate = this.templateCtrl.content(printEvent.getPrintTemplate(), printEvent.getTemplateParams());
			
			// 5. 인쇄 카운트 만큼 인쇄
			for(int i = 0 ; i < printCount ; i++) {
				this.printingSvc.printZplCode(ps, labelTemplate, null);
			}
			
		} catch (Exception e) {
			// 5. 예외 처리
			String errorType = (ValueUtil.isEmpty(printEvent.getJobType()) ? SysConstants.EMPTY_STRING : printEvent.getJobType() + SysConstants.DASH) + "PRINT_LABEL_ERROR"; 
			ErrorEvent errorEvent = new ErrorEvent(domainId, errorType, e, null, true, true);
			this.eventPublisher.publishEvent(errorEvent);
			
		} finally {
			// 6. 스레드 로컬 변수에서 currentDomain 리셋
			if(currentDomain == null) {
				DomainContext.unsetAll();
			}
		}
	}
}
