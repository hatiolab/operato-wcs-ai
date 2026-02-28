package xyz.elidom.print.client;

import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.ErrorEvent;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.elidom.dev.rest.DiyTemplateController;
import xyz.elidom.print.PrintConstants;
import xyz.elidom.print.entity.Printer;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.util.ThreadUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 라벨 프린트 서비스 요청 - REST 통신
 * 
 * @author shortstop
 */
@Component
public class RestLabelPrintRequester {
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
     * DiyTemplate Controller
     */
    @Autowired
    private DiyTemplateController templateCtrl;
	
	/**
	 * 바코드 라벨 인쇄 동기 모드
	 * 
	 * @param printEvent
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'barcode' and #printEvent.commType == 'rest' and #printEvent.isSyncMode() == true")
	public void printLabelSyncMode(PrintEvent printEvent) {
		this.printLabel(null, printEvent);
	}
	
	/**
	 * 바코드 라벨 인쇄 비동기 모드
	 * 
	 * @param printEvent
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.printType == 'barcode' and #printEvent.commType == 'rest' and #printEvent.isSyncMode() == false")
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
			String agentUrl = printer.getPrinterAgentUrl();
			String printerName = printer.getPrinterDriver();
			
			// 4. 인쇄 요청
			int printCount = printEvent.getPrintCount() <= 0 ? 1 : printEvent.getPrintCount();
			String labelTemplate = this.templateCtrl.content(printEvent.getPrintTemplate(), printEvent.getTemplateParams());
			this.printLabels(agentUrl, printerName, labelTemplate, printCount);
			
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
	
	/**
	 * 라벨 인쇄
	 * 
	 * @param printAgentUrl
	 * @param printerName
	 * @param command
	 * @param count
	 */
    public void printLabels(String printAgentUrl, String printerName, String command, int count) {
        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName(SysConstants.CHAR_SET_UTF8)));
        printAgentUrl = printAgentUrl + PrintConstants.BARCODE_REST_URL + printerName;
        
        for(int i = 0 ; i < count ; i++) {
            rest.postForEntity(printAgentUrl, command, Boolean.class);
            ThreadUtil.sleep(100);
        }
    }
}
