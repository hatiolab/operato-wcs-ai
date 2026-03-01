package operato.logis.dps.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import operato.logis.dps.DpsCodeConstants;
import operato.logis.dps.model.DpsBatchInputableBox;
import operato.logis.dps.model.DpsBatchSummary;
import operato.logis.dps.query.store.DpsBatchQueryStore;
import operato.logis.dps.service.api.IDpsPickingService;
import operato.logis.dps.service.util.DpsBatchJobConfigUtil;
import operato.logis.dps.service.util.DpsServiceUtil;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.event.classfy.ClassifyEndEvent;
import xyz.anythings.base.event.device.DeviceEvent;
import xyz.anythings.base.event.rest.DeviceProcessRestEvent;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.anythings.base.model.EquipBatchSet;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.gw.service.mq.model.device.DeviceCommand;
import xyz.anythings.sys.model.BaseResponse;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * DPS 모바일 장비에서 요청하는 트랜잭션 이벤트 처리 서비스 
 * 
 * @author yang
 */
@Component
public class DpsDeviceProcessService extends AbstractLogisService {
	/**
	 * 배치 쿼리 스토어
	 */
	@Autowired
	private DpsBatchQueryStore dpsBatchQueryStore;
	/**
	 * DPS 피킹 서비스
	 */
	@Autowired
	private IDpsPickingService dpsPickingService;
	/**
	 * DPS 작업 현황 조회 서비스
	 */
	@Autowired
	private DpsJobStatusService dpsJobStatusService;
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;
	
	/*****************************************************************************************************
	 *											작 업 진 행 율 A P I
	 *****************************************************************************************************
	/**
	 * DPS 배치 작업 진행율 조회 : 진행율 + 투입 순서 리스트
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/batch_summary', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void batchSummaryEventProcess(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		int limit = ValueUtil.toInteger(params.get("limit"));
		int page = ValueUtil.toInteger(params.get("page"));
		
		// 2. 배치 조회
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 배치 서머리 조회 
		DpsBatchSummary summary = this.getBatchSummary(batch, equipType, equipCd, limit, page);

		// 4. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, summary));
		event.setExecuted(true);
	}
	
	/**
	 * B2C 배치에 대한 진행율 조회 
	 * 
	 * @param batch
	 * @param equipType
	 * @param equipCd
	 * @param limit
	 * @param page
	 * @return
	 */
	private DpsBatchSummary getBatchSummary(JobBatch batch, String equipType, String equipCd, int limit, int page) {
		
		// 1. 작업 진행율 조회  
		BatchProgressRate rate = this.dpsJobStatusService.getBatchProgressSummary(batch);
		
		// 2. 투입 정보 리스트 조회 
		Page<JobInput> inputItems = this.dpsJobStatusService.paginateInputList(batch, equipCd, null, page, limit);
		
		// 3. 파라미터
		Long domainId = batch.getDomainId();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipType", domainId, batch.getId(), equipType);
		
		if(ValueUtil.isNotEmpty(batch.getEquipCd())) {
			params.put("equipCd", equipCd);
		}
		
		// 4. 투입 가능 박스 수량 조회 
		String sql = this.dpsBatchQueryStore.getBatchInputableBoxQuery();
		Integer inputableBox = AnyEntityUtil.findItem(domainId, false, Integer.class, sql, params);
		
		// 5. 결과 리턴
		return new DpsBatchSummary(rate, inputItems, inputableBox);
	}
	
	/**
	 * 주문 상세 정보 조회 
	 *  
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/order_items', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void searchOrderItems(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String orderNo = params.get("orderNo").toString();
		
		// 2. 배치 조회
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		List<JobInstance> jobList = this.dpsJobStatusService.searchInputJobList(batch, ValueUtil.newMap("orderNo", orderNo));

		// 4. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, jobList));
		event.setExecuted(true);
	}
	
	/*****************************************************************************************************
	 *											박 스 투 입 A P I
	 *****************************************************************************************************
	/**
	 * DPS 박스 유형별 소요량 조회 
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/box_requirement', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void getBoxRequirementList(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipCd = params.get("equipCd").toString();
		String equipType = params.get("equipType").toString();
		Long domainId = event.getDomainId();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 진행 중인 배치가 있을때만 조회 
		if(!ValueUtil.isEmpty(batch)) {
			// 3.1. 호기별 배치 분리 여부
			// 투입 대상 박스 리스트 조회시 별도의 로직 처리 필요 
			boolean useSeparatedBatch = DpsBatchJobConfigUtil.isSeparatedBatchByRack(batch);
			
			String query = this.dpsBatchQueryStore.getBatchInputableBoxByTypeQuery();
			Map<String,Object> queryParams = ValueUtil.newMap("domainId,batchId,equipType,stageCd", domainId, batch.getId(), equipType, batch.getStageCd());
			if(ValueUtil.isEmpty(batch.getComCd())) {
				queryParams.put("comCd", batch.getComCd());
			}
			
			// 3.2. 호기가 분리된 배치의 경우
			if(useSeparatedBatch) {
				queryParams.put("equipCd", equipCd);
			}
			
			List<DpsBatchInputableBox> inputableBoxs = AnyEntityUtil.searchItems(domainId, true, DpsBatchInputableBox.class, query, queryParams);
			event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inputableBoxs));
		} else {
			event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, null));
		}

		// 4. 이벤트 처리 결과 셋팅 
		event.setExecuted(true);
	}
	
	/**
	 * DPS 박스 투입
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/input_box', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void inputBox(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String boxId = params.get("boxId").toString();
		String boxTypeCd = params.containsKey("boxTypeCd") ? params.get("boxTypeCd").toString() : null;
		// TODO 설정에서 가져오기 ...
		boolean boxTypeCheck = params.containsKey("boxTypeCheck") ? ValueUtil.toBoolean(params.get("boxTypeCheck")) : false;
		int limit = ValueUtil.toInteger(params.get("limit"));
		int page = ValueUtil.toInteger(params.get("page"));
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 박스 유형 체크
		if(boxTypeCheck) {
			String boxTypeOfBoxId = this.dpsPickingService.getBoxTypeByBoxId(batch, boxId);
			if(ValueUtil.isNotEqual(boxTypeCd, boxTypeOfBoxId)) {
				throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NOT_CORRECT_BOX_TYPE"));
			}
		}
		
		// 4. 박스 투입
		this.dpsPickingService.inputEmptyBucket(batch, true, boxId, boxTypeCd);
		
		// 5. 배치 서머리 조회
		DpsBatchSummary summary = this.getBatchSummary(batch, equipType, equipCd, limit, page);

		// 6. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, summary));
		event.setExecuted(true);
	}
	
	/**
	 * DPS 박스 투입
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/input_bucket', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void inputBucket(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String boxId = params.get("bucketCd").toString();
		String boxTypeCd = params.containsKey("boxTypeCd") ? params.get("boxTypeCd").toString() : null;
		String inputType = params.get("inputType").toString();
		int limit = ValueUtil.toInteger(params.get("limit"));
		int page = ValueUtil.toInteger(params.get("page"));
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		boolean isBox = ValueUtil.isEqualIgnoreCase(inputType, DpsCodeConstants.CLASSIFICATION_INPUT_TYPE_BOX) ? true : false;
		
		// 3. 박스 투입 (박스 or 트레이)
		this.dpsPickingService.inputEmptyBucket(batch, isBox, boxId, boxTypeCd);
		
		// 4. 배치 서머리 조회 
		DpsBatchSummary summary = this.getBatchSummary(batch, equipType, equipCd, limit, page);

		// 5. 이벤트 처리 결과 셋팅 
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, summary));
		event.setExecuted(true);
	}
	
	/**
	 * DPS 작업 존 박스 도착
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/bucket_arrive', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void bucketArrived(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String stationCd = params.get("stationCd").toString();
		String boxId = params.get("bucketCd").toString();
		// 작업 화면에 박스 하나만 표시할 지 여부
		boolean singleBoxMode = params.containsKey("singleBoxMode") ? ValueUtil.toBoolean(params.get("singleBoxMode")) : false;
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		Long domainId = event.getDomainId();
		EquipBatchSet equipBatch = DpsServiceUtil.findBatchByEquip(domainId, equipType, equipCd);
		JobBatch batch = equipBatch.getBatch();
		
		// 3. 박스 도착 처리
		BaseResponse response = this.dpsPickingService.boxArrived(batch, equipCd, stationCd, boxId, singleBoxMode);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(response);
		event.setExecuted(true);
	}
	
	/**
	 * DPS 작업 존 작업 완료 & 박스 출발
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/bucket_leave', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void bucketLeave(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String equipType = params.get("equipType").toString();
		String equipCd = params.get("equipCd").toString();
		String stationCd = params.get("stationCd").toString();
		String boxId = params.get("bucketCd").toString();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		Long domainId = event.getDomainId();
		EquipBatchSet equipBatch = DpsServiceUtil.findBatchByEquip(domainId, equipType, equipCd);
		JobBatch batch = equipBatch.getBatch();
		
		// 3. 박스 출발 처리
		this.dpsPickingService.boxLeave(batch, stationCd, boxId);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setExecuted(true);
	}
	
	/**
	 * DPS 박스가 해당 존에서 피킹할 주문이 있는지 체크
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/has_orders_at_station', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void hasOrdersAtStation(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String barcodeIpAddr = params.get("barcode_ip").toString();
		String boxId = params.get("box_id").toString();
		
		// 2. 스테이션에 처리할 주문이 있는지 체크
		boolean hasOrders = this.dpsPickingService.checkBoxArrived(event.getDomainId(), barcodeIpAddr, boxId);
		
		// 3. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, hasOrders ? "0" : "1"));
		event.setExecuted(true);
	}

	/*****************************************************************************************************
	 *											기 타  A P I
	 *****************************************************************************************************	
	/**
	 * DPS 박스의 송장 발행 정보를 리턴
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/invoice_info', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void getInvoiceOfBox(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		Long domainId = event.getDomainId();
		String boxId = params.get("box_id").toString();
		
		// 2. 주문 라벨 정보 조회
		String diyServiceName = "diy-dps-get-invoice-info";
		Map<String, Object> parameters = ValueUtil.newMap("domainId,boxId", domainId, boxId);
		Object retObj = this.customService.doCustomService(domainId, diyServiceName, parameters);
		
		// 3. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, retObj));
		event.setExecuted(true);
	}
	
	/**
	 * DPS 박스의 아이템 정보를 리턴
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/invoice_items', 'dps')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void getInvoiceItems(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		Long domainId = event.getDomainId();
		String batchId = params.get("batch_id").toString();
		String boxId = params.get("box_id").toString();
		
		// 2. 주문 라벨 정보 조회
		String diyServiceName = "diy-dps-get-invoice-items";
		Map<String, Object> parameters = ValueUtil.newMap("batchId,boxId", batchId, boxId);
		Object retObj = this.customService.doCustomService(domainId, diyServiceName, parameters);
		
		// 3. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, retObj));
		event.setExecuted(true);
	}
	
	/**
	 * 분류 처리 완료 이벤트 처리
	 * 
	 * @param classifyEndEvent
	 */
	@EventListener(classes = ClassifyEndEvent.class, condition = "#classifyEndEvent.jobType == 'DPS'")
	public void pickingEventHandler(ClassifyEndEvent classifyEndEvent) {
		JobBatch batch = classifyEndEvent.getJobBatch();
		String stationCd = classifyEndEvent.getStationCd();
		String message = classifyEndEvent.getResult() == null ? DeviceCommand.COMMAND_REFRESH_DETAILS : ValueUtil.toString(classifyEndEvent.getResult());
		Long domainId = batch.getDomainId();
		String[] deviceTypeList = DpsBatchJobConfigUtil.getDeviceList(batch);
		
		if(deviceTypeList != null) {
			for(String deviceType : deviceTypeList) {
				DeviceEvent event = new DeviceEvent(domainId, deviceType, batch.getStageCd(), batch.getEquipType(), batch.getEquipCd(), stationCd, null, batch.getJobType(), "info", message);
				this.eventPublisher.publishEvent(event);
			}
		}
	}
}
