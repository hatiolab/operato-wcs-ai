package operato.logis.sps.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import operato.logis.sps.model.SpsJobSummary;
import operato.logis.sps.model.SpsSkuSummary;
import operato.logis.sps.service.api.ISpsJobStatusService;
import operato.logis.sps.service.api.ISpsPickingService;
import operato.logis.sps.service.util.SpsBatchJobConfigUtil;
import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.event.IClassifyRunEvent;
import xyz.anythings.base.event.classfy.ClassifyRunEvent;
import xyz.anythings.base.event.rest.DeviceProcessRestEvent;
import xyz.anythings.base.model.EquipBatchSet;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.model.BaseResponse;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.util.ValueUtil;

/**
 * 단포 모바일 장비에서 요청하는 트랜잭션 이벤트 처리 서비스 
 * 
 * @author shortstop
 */
@Component
public class SpsDeviceProcessService extends AbstractLogisService {
	/**
	 * 단포 피킹 서비스
	 */
	@Autowired
	private ISpsPickingService spsPickingService;
	/**
	 * 단포 작업 현황 조회 서비스
	 */
	@Autowired
	private ISpsJobStatusService spsJobStatusService;
	
	/*****************************************************************************************************
	 *										작 업 진 행 율 A P I
	 *****************************************************************************************************
	/**
	 * 단포 배치 작업 진행율 조회
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/batch_summary', 'sps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void searchBatchSummary(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		
		// 2. 배치 조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, batch));
		event.setExecuted(true);
	}
	
	/**
	 * 단포 배치 작업 진행율 업데이트
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/update_batch_result', 'sps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void updateBatchSummary(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		
		// 2. 배치 조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		batch = this.spsPickingService.updateProgressRate(batch);
		
		// 3. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, batch));
		event.setExecuted(true);
	}
	
	/*****************************************************************************************************
	 *											 단 포 처 리 A P I
	 *****************************************************************************************************
	/**
	 * 단포 상품 변경
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/sku_change', 'sps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void changeSku(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String comCd = params.get("comCd").toString();
		String skuCd = params.get("skuCd").toString();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 상품에 대한 단포 작업 정보 조회
		List<SpsSkuSummary> skuJobSummary = this.spsJobStatusService.searchSkuJobSummary(batch, comCd, skuCd);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, skuJobSummary));
		event.setExecuted(true);
	}
	
	/**
	 * 주문 번호로 작업 정보 조회
	 *
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/order_items', 'sps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void searchJobsByOrder(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String orderNo = params.get("orderNo").toString();
		
		// 2. 배치 조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 작업 정보 조회
		Map<String, Object> condition = ValueUtil.newMap("orderNo", orderNo);
		List<JobInstance> jobList = this.spsJobStatusService.searchPickingJobList(batch, condition);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, jobList));
		event.setExecuted(true);
	}
	
	/**
	 * 작업 대상 작업 인스턴스 조회
	 *
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/search_target_jobs', 'sps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void searchJobsBy(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String comCd = params.get("comCd").toString();
		String skuCd = params.get("skuCd").toString();
		String boxTypeCd = params.get("boxTypeCd").toString();
		Integer pcs = ValueUtil.toInteger(params.get("pcs"));
		
		// 2. 배치 조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 작업 정보 조회
		Map<String, Object> condition = ValueUtil.newMap("comCd,skuCd,boxTypeCd,pickQty", comCd, skuCd, boxTypeCd, pcs);
		List<JobInstance> jobList = this.spsJobStatusService.searchPickingJobList(batch, condition);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, jobList));
		event.setExecuted(true);
	}
	

	/**
	 * 단포(검수) 박스 투입 전 체크 
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/check/input', 'sps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void inputBoxCheckWithInsp(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String comCd = params.get("comCd").toString();
		String skuCd = params.get("skuCd").toString();
		String boxId = params.get("boxId").toString();
		Integer orderQty = ValueUtil.toInteger(params.get("orderQty"), 0);
		String boxTypeCd = params.containsKey("boxTypeCd") ? params.get("boxTypeCd").toString() : null;
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		Long domainId = event.getDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.findBatchByEquip(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 단포(검수) 박스 투입 전 체크 서비스 호출 
		JobInstance job = (JobInstance)this.spsPickingService.inputBoxCheckWithInsp(batch, comCd, skuCd, boxId, boxTypeCd, orderQty);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, job));
		event.setExecuted(true);
	}
	/**
	 * 단포 박스 투입
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/box_input', 'sps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void inputBox(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String comCd = params.get("comCd").toString();
		String skuCd = params.get("skuCd").toString();
		String boxId = params.get("boxId").toString();
		String boxTypeCd = params.containsKey("boxTypeCd") ? params.get("boxTypeCd").toString() : null;
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		Long domainId = event.getDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.findBatchByEquip(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 단포 박스 투입 서비스 호출 (단포는 무조건 박스 타입이 box)
		JobInstance job = (JobInstance)this.spsPickingService.inputBox(batch, comCd, skuCd, boxId, boxTypeCd);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, job));
		event.setExecuted(true);
	}

	/**
	 * 단포 피킹 처리
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/pick/run', 'sps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void runPickSinglePack(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		String jobId = event.getRequestParams().get("jobId").toString();
		
		// 2. 작업 데이터 조회
		JobInstance job = AnyEntityUtil.findEntityById(true, JobInstance.class, jobId);
		
		// 3. 작업 배치 조회
		JobBatch batch = AnyEntityUtil.findEntityById(true, JobBatch.class, job.getBatchId());
		
		// 4. 피킹 검수 설정 확인
		int reqQty = job.getPickQty();
		int resQty = job.getPickedQty();
		if(SpsBatchJobConfigUtil.isPickingWithInspectionEnabled(batch)) {
			resQty = 1;
		}
		
		// 5. 확정 처리
		IClassifyRunEvent classEvent = new ClassifyRunEvent(batch
			, SysEvent.EVENT_STEP_ALONE
			, LogisConstants.DEVICE_KIOSK
			, LogisCodeConstants.CLASSIFICATION_ACTION_CONFIRM
			, job
			, ValueUtil.isEmpty(reqQty) ? 0 : reqQty
			, resQty);
		this.spsPickingService.confirmPick(classEvent);
		
		// 6. 작업 완료가 되었다면 단포 작업 현황 조회
		if(job.getPickedQty() >= job.getPickQty()) {
			// 상품에 대한 단포 작업 정보 조회
			List<SpsSkuSummary> singlePack = this.spsJobStatusService.searchSkuJobSummary(batch, job.getComCd(), job.getSkuCd());
			// 처리 결과 설정
			SpsJobSummary result = new SpsJobSummary(singlePack, job);
			event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, result));
		
		} else {
			// 처리 결과 설정
			event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, job));
		}

		event.setExecuted(true);
	}
	
	/**
	 * 단포 피킹 처리 완료
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/pick/finish', 'sps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void finishPickSinglePack(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Long domainId = event.getDomainId();
		String jobId = event.getRequestParams().get("jobId").toString();
		String printerId = event.getRequestParams().get("printerId").toString();
		
		// 2. 작업 데이터 조회
		JobInstance job = AnyEntityUtil.findEntityById(true, JobInstance.class, jobId);
		
		// 3. 작업 배치 조회
		JobBatch batch = AnyEntityUtil.findEntityById(true, JobBatch.class, job.getBatchId());
		
		// 4. 피킹 검수 설정 확인
		int reqQty = job.getPickQty();
		int resQty = job.getPickQty();
		
		// 5. 확정 처리
		IClassifyRunEvent classEvent = new ClassifyRunEvent(batch
			, SysEvent.EVENT_STEP_ALONE
			, LogisConstants.DEVICE_KIOSK
			, LogisCodeConstants.CLASSIFICATION_ACTION_CONFIRM
			, job
			, reqQty
			, resQty);
		this.spsPickingService.confirmPick(classEvent);
		
		// 6. 상품에 대한 단포 작업 정보 조회
		List<SpsSkuSummary> singlePack = this.spsJobStatusService.searchSkuJobSummary(batch, job.getComCd(), job.getSkuCd());
		
		// 7. 처리 결과 설정
		SpsJobSummary result = new SpsJobSummary(singlePack, job);
		
		// 8. 송장 출력
		String labelTemplate = BatchJobConfigUtil.getInvoiceLabelTemplate(batch);
		BoxPack box = AnyEntityUtil.findEntityBy(domainId, false, BoxPack.class, "domainId,invoiceId", domainId, job.getInvoiceId());
		PrintEvent printEvent = new PrintEvent(domainId, job.getJobType(), printerId, labelTemplate, ValueUtil.newMap("batch,box", batch, box));
		printEvent.setPrintType("barcode");
		this.eventPublisher.publishEvent(printEvent);
		
		// 9. 결과 리턴
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, result));
		event.setExecuted(true);
	}
	
	/**
	 * 단포(검수 피킹 처리 완료
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/pick/finish/insp', 'sps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void finishPickSinglePackWithInsp(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Long domainId = event.getDomainId();
		String jobId = event.getRequestParams().get("jobId").toString();
		String printerId = event.getRequestParams().get("printerId").toString();
		String boxId = event.getRequestParams().get("boxId").toString();
		
		// 2. 작업 데이터 조회 (lock)
		JobInstance job = AnyEntityUtil.findEntityByIdWithLock(true, JobInstance.class, jobId);
		job.setBoxId(boxId);
		
		// 3. 작업 배치 조회
		JobBatch batch = AnyEntityUtil.findEntityById(true, JobBatch.class, job.getBatchId());
		
		// 4. 피킹 검수 설정 확인
		int reqQty = job.getPickQty();
		int resQty = job.getPickQty();
		
		// 6. 확정 처리
		IClassifyRunEvent classEvent = new ClassifyRunEvent(batch
			, SysEvent.EVENT_STEP_ALONE
			, LogisConstants.DEVICE_KIOSK
			, LogisCodeConstants.CLASSIFICATION_ACTION_CONFIRM
			, job
			, reqQty
			, resQty);
		this.spsPickingService.confirmPickWithInsp(classEvent);
		
		// 7. 상품에 대한 단포 작업 정보 조회
		List<SpsSkuSummary> singlePack = this.spsJobStatusService.searchSkuJobSummary(batch, job.getComCd(), job.getSkuCd());
		
		// 8. 처리 결과 설정
		SpsJobSummary result = new SpsJobSummary(singlePack, job);
		
		// 9. 송장 출력
		String labelTemplate = BatchJobConfigUtil.getInvoiceLabelTemplate(batch);
		BoxPack box = AnyEntityUtil.findEntityBy(domainId, false, BoxPack.class, "domainId,invoiceId", domainId, job.getInvoiceId());
		PrintEvent printEvent = new PrintEvent(domainId, job.getJobType(), printerId, labelTemplate, ValueUtil.newMap("batch,box", batch, box));
		printEvent.setPrintType("barcode");
		this.eventPublisher.publishEvent(printEvent);
		
		// 10. 결과 리턴
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, result));
		event.setExecuted(true);
	}

	/*****************************************************************************************************
	 * 											송 장 인 쇄 A P I
	 *****************************************************************************************************
	/**
	 * 단포 송장 출력
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/print_invoice', 'sps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void printInvoiceLabel(DeviceProcessRestEvent event) {
		Map<String, Object> reqParams = event.getRequestParams();
		String equipCd = ValueUtil.toString(reqParams.get("equipCd"));
		String boxId = ValueUtil.toString(reqParams.get("boxId"));
		String invoiceId = ValueUtil.toString(reqParams.get("invoiceId"));
		String printerId = ValueUtil.toString(reqParams.get("printerId"));
		Long domainId = Domain.currentDomainId();
		
		// 1. 작업 배치 조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, LogisConstants.EQUIP_TYPE_RACK, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 박스 정보 조회
		BoxPack box = AnyEntityUtil.findEntityBy(domainId, false, BoxPack.class, "domainId,invoiceId", domainId, invoiceId);
		
		if(box == null && ValueUtil.isNotEmpty(boxId)) {
			box = AnyEntityUtil.findEntityBy(domainId, true, BoxPack.class, "domainId,boxId", domainId, boxId);
		}
		
		// 3. 프린트 이벤트 전송
		String labelTemplate = BatchJobConfigUtil.getInvoiceLabelTemplate(batch);
		Map<String, Object> printParams = ValueUtil.newMap("batch,box", batch, box);
		PrintEvent printEvent = new PrintEvent(batch.getDomainId(), batch.getJobType(), printerId, labelTemplate, printParams);
		printEvent.setPrintType("barcode");
		this.eventPublisher.publishEvent(printEvent);
		
		// 4. 이벤트 결과 처리
		event.setExecuted(true);
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING));
	}
	
	/*****************************************************************************************************
	 * 											이 벤 트 처 리 A P I
	 *****************************************************************************************************
//	/**
//	 * SPS 송장 라벨 인쇄 API 삭제 --> anythings-printing 모듈의 LabelPrintService에서 공통적으로 처리
//	 * 
//	 * @param printEvent
//	 */
//	@Async
//	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.jobType == 'SPS'")
//	public void printLabel(PrintEvent printEvent) {
//		
//		// 현재 도메인 조회
//		Long domainId = printEvent.getDomainId();
//		Domain domain = this.domainCtrl.findOne(domainId, null);
//		// 현재 도메인 설정
//		DomainContext.setCurrentDomain(domain);
//		
//		try {
//			// 인쇄 옵션 정보 추출
//			Printer printer = this.queryManager.select(Printer.class, printEvent.getPrinterId());
//			printer = (printer == null) ? this.queryManager.selectByCondition(Printer.class, ValueUtil.newMap("domainId,printerCd", domainId, printEvent.getPrinterId())) : printer;
//			String agentUrl = printer.getPrinterAgentUrl();
//			String printerName = printer.getPrinterDriver();
//			
//			// 인쇄 요청
//			this.printerCtrl.printLabelByLabelTemplate(agentUrl, printerName, printEvent.getPrintTemplate(), printEvent.getTemplateParams());
//			printEvent.setExecuted(true);
//			
//		} catch (Exception e) {
//			// 예외 처리
//			ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "SPS_PRINT_LABEL_ERROR", e, null, true, true);
//			this.eventPublisher.publishEvent(errorEvent);
//			
//		} finally {
//			// 스레드 로컬 변수에서 currentDomain 리셋
//			DomainContext.unsetAll();
//		}
//	}

}
