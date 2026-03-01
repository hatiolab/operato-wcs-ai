package operato.logis.pdas.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import operato.logis.pdas.service.util.PdasServiceUtil;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.event.classfy.ClassifyEndEvent;
import xyz.anythings.base.event.device.DeviceEvent;
import xyz.anythings.base.event.rest.DeviceProcessRestEvent;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.anythings.gw.service.mq.model.device.DeviceCommand;
import xyz.anythings.sys.model.BaseResponse;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * PDAS 모바일 장비에서 요청하는 트랜잭션 이벤트 처리 서비스
 * 
 * @author shortstop
 */
@Component
public class PdasDeviceProcessService extends AbstractLogisService {
	/**
	 * PDAS 작업 정보 조회 서비스
	 */
	@Autowired
	private PdasJobStatusService pdasJobStatusService;
	/**
	 * PDAS 분류 서비스
	 */
	@Autowired
	private PdasAssortService pdasAssortService;
	
	/*****************************************************************************************************
	 *										작 업 진 행 율 A P I
	 *****************************************************************************************************/
	/**
	 * PDAS 배치 작업 진행율 조회
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/batch_summary', 'pdas')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void getBatchProgress(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		
		// 2. 배치 조회
		JobBatch batch = PdasServiceUtil.checkPdasRunningBatch(event.getDomainId(), equipType, equipCd);
		
		// 3. 배치 서머리 조회 
		BatchProgressRate progressRate = this.pdasJobStatusService.getBatchProgressSummary(batch);

		// 4. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, progressRate));
		event.setExecuted(true);
	}
	
	/*****************************************************************************************************
	 *										 주 문 조 회 A P I
	 *****************************************************************************************************/
	/**
	 * PDAS 상품 스캔시 처리할 주문 조회
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/find_order', 'pdas')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void findOrderToAssort(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String stationCd = params.get("stationCd").toString();
		String comCd = params.get("comCd").toString();
		String skuCd = params.get("skuCd").toString();
		
		// 2. 배치 조회
		JobBatch batch = PdasServiceUtil.checkPdasRunningBatch(event.getDomainId(), equipType, equipCd);
		
		// 3. 피킹할 주문을 조회
		JobInstance job = this.pdasAssortService.findJobToAssort(batch, comCd, skuCd, stationCd);
		
		// 4. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, job));
		event.setExecuted(true);
	}
	
	/*****************************************************************************************************
	 *										 주 문 처 리 A P I
	 *****************************************************************************************************/
	/**
	 * PDAS 셀 상태 체크
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/check_cell', 'pdas')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void checkCellJobStatus(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String stationCd = params.get("stationCd").toString();
		String cellCd = params.get("cellCd").toString();
		
		// 2. 배치 체크
		JobBatch batch = PdasServiceUtil.checkPdasRunningBatch(event.getDomainId(), equipType, equipCd);
		
		// 3. 셀 번호 유효성 체크
		cellCd = this.checkCellCd(batch, cellCd);
		
		// 4. 셀의 현재 상태 체크
		JobInstance job = this.pdasAssortService.checkCellJobStatus(batch, stationCd, cellCd);
		
		// 5. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, job));
		event.setExecuted(true);
	}
	
	/**
	 * PDAS 박스 상태 체크
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/check_box', 'pdas')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void checkBoxStatus(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String stationCd = params.get("stationCd").toString();
		String boxId = params.get("boxId").toString();
		
		// 2. 배치 체크
		JobBatch batch = PdasServiceUtil.checkPdasRunningBatch(event.getDomainId(), equipType, equipCd);
		
		// 3. 셀의 현재 상태 체크
		BoxPack box = this.pdasAssortService.checkBoxStatus(batch, stationCd, boxId);
		
		// 4. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, box));
		event.setExecuted(true);
	}
	
	/**
	 * PDAS 주문 - 셀 매핑 처리
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/assign', 'pdas')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void assignCellToOrder(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String stationCd = params.get("stationCd").toString();
		String cellCd = params.get("cellCd").toString();
		String jobInstanceId = params.get("jobInstanceId").toString();
		
		// 2. 배치 체크
		JobBatch batch = PdasServiceUtil.checkPdasRunningBatch(event.getDomainId(), equipType, equipCd);
		
		// 3. 셀 번호 유효성 체크
		cellCd = this.checkCellCd(batch, cellCd);
		
		// 4. 작업 (주문)과 셀 매핑
		JobInstance job = this.pdasAssortService.assignJobToCell(batch, jobInstanceId, stationCd, cellCd);
		
		// 5. 분류 처리
		job = this.pdasAssortService.assortJob(batch, jobInstanceId, cellCd, stationCd);
		
		// 6. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, job));
		event.setExecuted(true);
	}
	
	/**
	 * PDAS 주문 - 박스 ID 선 매핑 처리
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/box_mapping', 'pdas')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void assignBoxToOrder(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String stationCd = params.get("stationCd").toString();
		String cellCd = params.get("cellCd").toString();
		String boxId = params.get("boxId").toString();
		String jobInstanceId = params.get("jobInstanceId").toString();
		
		// 2. 배치 체크
		JobBatch batch = PdasServiceUtil.checkPdasRunningBatch(event.getDomainId(), equipType, equipCd);
		
		// 3. 셀 번호 유효성 체크
		cellCd = this.checkCellCd(batch, cellCd);
		
		// 4. 작업 (주문)과 박스 ID 매핑
		JobInstance job = this.pdasAssortService.assignOrderToBox(batch, jobInstanceId, stationCd, cellCd, boxId);
		
		// 5. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, job));
		event.setExecuted(true);
	}
	
	/**
	 * PDAS 주문 중분류 처리
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/middle_assort', 'pdas')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void middleAssortOrder(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String jobInstanceId = params.get("jobInstanceId").toString();
		
		// 2. 배치 체크
		JobBatch batch = PdasServiceUtil.checkPdasRunningBatch(event.getDomainId(), equipType, equipCd);
		
		// 3. 분류 처리
		JobInstance job = this.pdasAssortService.middleAssortJob(batch, jobInstanceId);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, job));
		event.setExecuted(true);
	}
	
	/**
	 * PDAS 주문 분류 처리
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/assort', 'pdas')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void assortOrder(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String stationCd = params.get("stationCd").toString();
		String cellCd = params.get("cellCd").toString();
		String jobInstanceId = params.get("jobInstanceId").toString();
		
		// 2. 배치 체크
		JobBatch batch = PdasServiceUtil.checkPdasRunningBatch(event.getDomainId(), equipType, equipCd);
		
		// 3. 셀 번호 유효성 체크
		cellCd = this.checkCellCd(batch, cellCd);
		
		// 4. 분류 처리
		JobInstance job = this.pdasAssortService.assortJob(batch, jobInstanceId, cellCd, stationCd);
		
		// 5. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, job));
		event.setExecuted(true);
	}
	
	/**
	 * PDAS 주문 - 박스 ID 후 매핑 및 주문 분류 완료 (박싱) 처리
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/boxing', 'pdas')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void boxProcessOrder(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String stationCd = params.get("stationCd").toString();
		String jobInstanceId = params.get("jobInstanceId").toString();
		String boxId = params.get("boxId").toString();
		boolean boxReusable = params.containsKey("boxReusable") ? ValueUtil.toBoolean(params.get("boxReusable"), false) : false;
		
		// 2. 배치 조회
		JobBatch batch = PdasServiceUtil.checkPdasRunningBatch(event.getDomainId(), equipType, equipCd);
		
		// 3. 주문 박싱 처리
		JobInstance job = this.pdasAssortService.boxingJob(batch, jobInstanceId, boxId, boxReusable, stationCd);
		
		// 4. 결과 리턴
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, job));
		event.setExecuted(true);
	}
	
	/*****************************************************************************************************
	 * 											이 벤 트 처 리 A P I
	 *****************************************************************************************************
	/**
	 * 분류 처리 완료 이벤트 처리
	 * 
	 * @param classifyEndEvent
	 */
	@EventListener(classes = ClassifyEndEvent.class, condition = "#classifyEndEvent.jobType == 'PDAS'")
	public void assortEventHandler(ClassifyEndEvent classifyEndEvent) {
		JobBatch batch = classifyEndEvent.getJobBatch();
		JobInstance job = (JobInstance)classifyEndEvent.getResult();
		Cell cell = null;
		
		Object[] payloads = classifyEndEvent.getPayload();
		if(ValueUtil.isNotEmpty(payloads)) {
			cell = (Cell)payloads[0];
		}
		
		Long domainId = batch.getDomainId();
		String[] deviceTypeList = BatchJobConfigUtil.getDeviceList(batch);
		// 상태가 'B'이면 박스 ID 매핑 작업 처리, 'F'이면 분류 처리 중, 'C'이면 작업 취소
		String status = job.getStatus();
		String message = ValueUtil.isEqualIgnoreCase(status, LogisConstants.JOB_STATUS_BOXED) ? "input_box" : 
						(ValueUtil.isEqualIgnoreCase(status, LogisConstants.JOB_STATUS_CANCEL) ? "cancel" : DeviceCommand.COMMAND_REFRESH);
		
		if(deviceTypeList != null) {
			for(String deviceType : deviceTypeList) {
				DeviceEvent event = new DeviceEvent(domainId, deviceType, batch.getStageCd(), batch.getEquipType(), batch.getEquipCd(), cell.getStationCd(), null, batch.getJobType(), "info", message);
				this.eventPublisher.publishEvent(event);
			}
		}
	}

	/*****************************************************************************************************
	 * 											기 타 메 소 드
	 *****************************************************************************************************/

	/**
	 * 셀 번호가 유효한 지 체크하고 표시기 코드로 넘어온 경우 셀 코드로 변환하여 리턴한다.
	 * 
	 * @param batch
	 * @param cellCd
	 * @return
	 */
	private String checkCellCd(JobBatch batch, String cellCd) {
		// 1. 셀 코드인 지 체크
		if(BatchJobConfigUtil.isCellCdValid(batch, cellCd)) {
		// 2. 표시기 코드인 지 체크
		} else if(BatchJobConfigUtil.isIndCdValid(batch, cellCd)) {
			// 표시기 코드로 부터 셀 코드를 찾아 리턴한다.
			cellCd = this.findCellCdByIndCd(batch, cellCd);
		// 3. 긴 표시기 코드라면 파싱하여 셀 코드를 찾아 리턴한다.
		} else {
			// 표시기 긴 코드를 파싱하여 처리
			String indCd = LogisServiceUtil.parseIndicatorCode(batch.getDomainId(), batch.getStageCd(), cellCd);
			// 표시기 코드 룰에 맞다면 표시기 코드로 셀 코드를 찾아 리턴
			if(BatchJobConfigUtil.isIndCdValid(batch, indCd)) {
				cellCd = this.findCellCdByIndCd(batch, indCd);
			}
		}
		
		// 4. 그래도 셀 코드가 없다면 유효하지 않은 셀 번호
		if(ValueUtil.isEmpty(cellCd)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("INVALID_CELL_NO"));
		}
		
		// 5. 셀 번호 리턴
		return cellCd;
	}
	
	/**
	 * 표시기 코드로 부터 셀 코드를 찾아 리턴한다.
	 * 
	 * @param batch
	 * @param indCd
	 * @return
	 */
	private String findCellCdByIndCd(JobBatch batch, String indCd) {
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId(), "cellCd");
		condition.addFilter("equipType", batch.getEquipType());
		condition.addFilter("equipCd", batch.getEquipCd());
		condition.addFilter("indCd", indCd);
		Cell cell = this.queryManager.selectByCondition(Cell.class, condition);
		return cell == null ? null : cell.getCellCd();
	}
	
//	/**
//	 * 송장 라벨 인쇄 API
//	 * 
//	 * @param printEvent
//	 */
//	@Async
//	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.jobType == 'PDAS'")
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
//			ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "PDAS_PRINT_LABEL_ERROR", e, null, true, true);
//			this.eventPublisher.publishEvent(errorEvent);
//			
//		} finally {
//			// 스레드 로컬 변수에서 currentDomain 리셋
//			DomainContext.unsetAll();
//		}
//	}
}
