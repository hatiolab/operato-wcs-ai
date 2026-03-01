package operato.logis.dpc.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import operato.logis.dpc.model.DpcBatchSummary;
import operato.logis.dpc.model.DpcCellBox;
import operato.logis.dpc.model.DpcZoneInput;
import operato.logis.dpc.query.store.DpcPickQueryStore;
import operato.logis.dpc.service.util.DpcBatchJobConfigUtil;
import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.entity.WorkCell;
import xyz.anythings.base.event.IClassifyEvent;
import xyz.anythings.base.event.IClassifyInEvent;
import xyz.anythings.base.event.classfy.ClassifyInEvent;
import xyz.anythings.base.event.classfy.ClassifyRunEvent;
import xyz.anythings.base.event.device.DeviceEvent;
import xyz.anythings.base.event.input.InputEvent;
import xyz.anythings.base.event.rest.DeviceProcessRestEvent;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.anythings.base.model.EquipBatchSet;
import xyz.anythings.base.service.api.IIndicationService;
import xyz.anythings.base.service.impl.LogisServiceDispatcher;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.model.BaseResponse;
import xyz.anythings.sys.service.AbstractExecutionService;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * DPC 장비 (KIOSK / Tablet)로 부터의 요청을 처리하는 서비스 
 * 
 * @author shortstop
 */
@Component("dpcDeviceProcessService")
public class DpcDeviceProcessService extends AbstractExecutionService {
	/**
	 * 서비스 디스패쳐
	 */
	@Autowired
	private LogisServiceDispatcher serviceDispatcher;
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;
	/**
	 * 피킹 쿼리 스토어
	 */
	@Autowired
	private DpcPickQueryStore dpcPickQueryStore;
	/*****************************************************************************************************
	 * 											D P C 작업 진행율 API
	 *****************************************************************************************************
	
	/**
	 * 현재 작업 진행 요약 정보 조회
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/batch_summary', 'dpc')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void batchSummary(DeviceProcessRestEvent event) {
		Map<String, Object> reqParams = event.getRequestParams();
		String equipCd = reqParams.get("equipCd").toString();
		
		// 1. 작업 배치 조회
		Long domainId = event.getDomainId();
		JobBatch batch = LogisServiceUtil.checkRunningBatchByCart(domainId, equipCd);
		
		// 2. 배치 전체 작업 진행율 정보 조회
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,equipCd", domainId, batch.getId(), equipCd);
		BatchProgressRate totalRate = new BatchProgressRate();
		totalRate.setPlanOrder(batch.getBatchOrderQty());
		totalRate.setActualOrder(batch.getResultOrderQty());
		totalRate.setRateOrder(batch.getProgressRate());
		
		// 3. 작업 투입 정보 조회
		String cartJobId = this.findCartJob(domainId, batch.getId(), equipCd);
		List<DpcZoneInput> zoneInputList = new ArrayList<DpcZoneInput>();
		List<DpcCellBox> boxCellList = new ArrayList<DpcCellBox>();
		SKU sku = null;
		
		// 4. 카트 작업 번호가 없다면 (즉 카트 작업 시작 전 이라면)
		if(ValueUtil.isEmpty(cartJobId)) {
			// 4-1. 카트의 셀 정보를 조회
			String sql = "select cell_cd, '' as class_cd, '' as box_id, 0 as pick_qty, active_flag as active from cells where domain_id = :domainId and equip_cd = :equipCd order by cell_cd";
			boxCellList = this.queryManager.selectListBySql(sql, condition, DpcCellBox.class, 0, 0);
			
		// 5. 카트 작업 번호가 있다면 (즉 카트 작업 시작 후 라면)
		} else {
			// 5-1. 보관 존 별 셀 리스트 조회
			String sql = "select from_zone_cd as zone_cd, sum(order_qty) as order_qty, sum(picked_qty) as picked_qty from orders where domain_id = :domainId and batch_id = :batchId and equip_cd = :equipCd and to_zone_cd = :cartJobId group by from_zone_cd order by from_zone_cd";
			condition.put("cartJobId", cartJobId);
			zoneInputList = this.queryManager.selectListBySql(sql, condition, DpcZoneInput.class, 0, 0);
			
			// 5-2. 상품 정보 조회
			sql = "select sku_cd, sku_barcd, sku_nm from sku where domain_id = :domainId and sku_cd in (select sku_cd from job_inputs where domain_id = :domainId and batch_id = :batchId and station_cd = :cartJobId and status in ('W', 'R'))";
			List<SKU> skuList = this.queryManager.selectListBySql(sql, condition, SKU.class, 0, 0);
			
			// 5-3. 카트별 셀 리스트를 조회
			sql = this.dpcPickQueryStore.getSearchCellBoxListQuery();
			if(!skuList.isEmpty()) {
				sku = skuList.get(0);
				condition.put("comCd", sku.getComCd());
				condition.put("skuCd", sku.getSkuCd());
			}
			
			boxCellList = this.queryManager.selectListBySql(sql, condition, DpcCellBox.class, 0, 0);
		}
		
		// 6. 리턴 결과
		DpcBatchSummary result = new DpcBatchSummary(batch.getId(), cartJobId, totalRate, zoneInputList, boxCellList, sku);
		
		// 7. 리턴 결과 설정
		event.setExecuted(true);
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, result));
	}

	/*****************************************************************************************************
	 * 											D P C 카트 작업 시작 / 마감
	 *****************************************************************************************************

	/**
	 * 카트 작업 시작 여부 체크
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/cart_job/is_started', 'dpc')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void checkCartJobStarted(DeviceProcessRestEvent event) {
		Map<String, Object> reqParams = event.getRequestParams();
		String equipCd = reqParams.get("equipCd").toString();
		
		// 1. 작업 배치 조회
		Long domainId = event.getDomainId();
		JobBatch batch = LogisServiceUtil.checkRunningBatchByCart(domainId, equipCd);
		
		// 2. 배치 전체 작업 진행율 정보 조회
		String cartJobId = this.findCartJob(domainId, batch.getId(), equipCd);
		
		// 3. 리턴 결과 설정
		event.setExecuted(true);
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, ValueUtil.isNotEmpty(cartJobId)));
	}
	
	/**
	 * 카트 시작 작업 ID 리턴
	 * 
	 * @param domainId
	 * @param batchId
	 * @param equipCd
	 * @return
	 */
	public String findCartJob(Long domainId, String batchId, String equipCd) {
		// 1. 설비 코드로 진행 중인 작업 배치 리턴
		JobBatch batch = LogisServiceUtil.checkRunningBatchByCart(domainId, equipCd);
		
		// 2. 진행 중인 카트 작업 번호 조회
		String sql = "select distinct to_zone_cd from orders where domain_id = :domainId and batch_id = :batchId and equip_cd = :equipCd and to_zone_cd is not null and to_cell_cd = :cartJobStatus";
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,equipCd,cartJobStatus", domainId, batch.getId(), equipCd, JobBatch.STATUS_RUNNING);
		String cartJobId = this.queryManager.selectBySql(sql, condition, String.class);
		
		// 3. 카트 작업 번호 리턴
		return cartJobId;
	}
	
	/**
	 * 카트 작업 시작
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/cart_job/start', 'dpc')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void startCartJob(DeviceProcessRestEvent event) {
		Map<String, Object> reqParams = event.getRequestParams();
		String equipCd = reqParams.get("equipCd").toString();
		
		// 1. 작업 배치 조회
		Long domainId = event.getDomainId();
		JobBatch batch = LogisServiceUtil.checkRunningBatchByCart(domainId, equipCd);
		String batchId = batch.getId();
		
		// 2. 작업 배치 락 처리
		batch = LogisServiceUtil.findBatchWithLock(domainId, batchId, false, false);
		String cartJobId = this.findCartJob(domainId, batchId, equipCd);
		
		// 3. 카트 작업이 이미 시작한 상태인지 체크
		if(ValueUtil.isNotEmpty(cartJobId)) {
			throw ThrowUtil.newValidationErrorWithNoLog("카트 작업은 이미 시작한 상태입니다. 새로고침을 버튼을 눌러주세요.");
		}
		
		// 4. 랙 정보 조회
		Rack rack = AnyEntityUtil.findEntityBy(domainId, true, Rack.class, null, "rackCd", equipCd);
		
		// 5. 셀 정보 조회
		Map<String, Object> condition = ValueUtil.newMap("domainId,equipType,equipCd,activeFlag,batchId", domainId, LogisConstants.EQUIP_TYPE_RACK, equipCd, true, batchId);
		String sql = "select id, cell_cd, ind_cd from cells where domain_id = :domainId and equip_type = :equipType and equip_cd = :equipCd and active_flag = :activeFlag";
		List<Cell> cellList = this.queryManager.selectListBySql(sql, condition, Cell.class, 0, 0);
		
		if(cellList.isEmpty()) {
			throw ThrowUtil.newValidationErrorWithNoLog("카트 [" + rack.getRackNm() + "]에 활성화된 셀이 없습니다.");
		}
		
		// 6. 작업 대상 조회
		sql = "select distinct input_seq, order_no from job_instances where domain_id = :domainId and batch_id = :batchId and box_class_cd is null order by input_seq asc";
		List<JobInstance> ordList = this.queryManager.selectListBySql(sql, condition, JobInstance.class, 1, cellList.size());
		
		// 7. 작업 대상 주문 여부 체크
		if(ValueUtil.isEmpty(ordList)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("LOGIS_NO_ORDER_TO_INPUT"));
		}
		
		// 8. 작업 대상에 카트 코드, 셀 코드, 카트 작업 번호, 투입 시간 등을 설정
		int inputSeq = ValueUtil.toInteger(batch.getLastInputSeq(), 0) + 1;
		cartJobId = ValueUtil.toString(inputSeq);
		condition.put("now", new Date());
		condition.put("cartStatus", JobBatch.STATUS_RUNNING);
		condition.put("status", LogisConstants.JOB_STATUS_WAIT);
		
		String orderSql = "update orders set equip_type = :equipType, equip_cd = :equipCd, sub_equip_cd = :cellCd, to_zone_cd = :cartJobId, to_cell_cd = :cartStatus, status = :status, updated_at = :now where domain_id = :domainId and batch_id = :batchId and order_no = :orderNo";
		String jobSql = "update job_instances set equip_type = :equipType, equip_cd = :equipCd, sub_equip_cd = :cellCd, box_class_cd = :cartJobId, status = :status, updated_at = :now where domain_id = :domainId and batch_id = :batchId and order_no = :orderNo";
		
		// 9. 작업, 주문, 셀 정보 카트 작업 할당
		for(int i = 0 ; i < cellList.size() ; i++) {
			if((i + 1) > ordList.size()) {
				break;
			}
			
			String orderNo = ordList.get(i).getOrderNo();
			Cell cell = cellList.get(i);
			String cellCd = cell.getCellCd();
			
			condition.put("cellCd", cellCd);
			condition.put("orderNo", orderNo);
			condition.put("cartJobId", cartJobId);
			
			// 9.1 주문 정보 할당
			this.queryManager.executeBySql(orderSql, condition);
			
			// 9.2 작업 정보 할당
			this.queryManager.executeBySql(jobSql, condition);
			
			// 9.3 WorkCell - classCd 할당
			WorkCell workCell = this.queryManager.selectByCondition(WorkCell.class, ValueUtil.newMap("domainId,cellCd", domainId, cellCd));
			if(workCell == null) {
				workCell = new WorkCell();
				workCell.setDomainId(domainId);
				workCell.setCellCd(cellCd);
			}
			
			workCell.setBatchId(batchId);
			workCell.setJobType(LogisConstants.JOB_TYPE_DPC);
			workCell.setComCd(batch.getComCd());
			workCell.setClassCd(orderNo);
			workCell.setActiveFlag(true);
			workCell.setBoxId(null);
			workCell.setLastJobCd(null);
			workCell.setJobInstanceId(null);
			workCell.setLastPickedQty(0);
			this.queryManager.upsert(workCell);
		}
		
		// 10. 랙에 배치 ID 할당
		rack.setBatchId(batchId);
		rack.setStatus(JobBatch.STATUS_RUNNING);
		this.queryManager.update(rack, "batchId", "status");
		
		// 11. 배치에 마지막 투입 순서 업데이트
		batch.setLastInputSeq(inputSeq);
		this.queryManager.update(batch, "lastInputSeq");
		
		// 12. 표시기 사용하는 경우 박스 매핑 표시
		if(DpcBatchJobConfigUtil.isUseIndicator(batch)) {
			IIndicationService indSvc = this.serviceDispatcher.getIndicationService(batch);
			for(Cell cell : cellList) {
				indSvc.displayForBoxMapping(batch, cell.getIndCd());
			}
		}
		
		// 13. 리턴 결과 설정
		event.setExecuted(true);
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, true));
	}
	
	/**
	 * 상품 코드 스캔으로 상품 투입
	 * 
	 * @param event 이벤트
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/input/sku', 'dpc')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void inputSKU(DeviceProcessRestEvent event) {
		Map<String, Object> reqParams = event.getRequestParams();
		String equipType = ValueUtil.toString(reqParams.get("equipType"));
		String equipCd = ValueUtil.toString(reqParams.get("equipCd"));
		String batchId = ValueUtil.toString(reqParams.get("batchId"));
		String cartJobId = ValueUtil.toString(reqParams.get("cartJobId"));
		String comCd = ValueUtil.toString(reqParams.get("comCd"));
		String skuCd = ValueUtil.toString(reqParams.get("skuCd"));
		
		// 1. 작업 배치 조회
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(event.getDomainId(), equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		if(ValueUtil.isNotEqual(batch.getId(), batchId)) {
			throw ThrowUtil.newValidationErrorWithNoLog("배치 ID가 매치되지 않습니다. 새로고침 후에 다시 작업하세요.");
		}
		
		batch.setEquipType(equipType);
		batch.setEquipCd(equipCd);
		
		// 2. 상품 투입 처리
		ClassifyInEvent inputEvent = new ClassifyInEvent(batch, SysEvent.EVENT_STEP_ALONE, false, LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU, skuCd, 1);
		inputEvent.setComCd(comCd);
		inputEvent.setPayload(new Object[] { cartJobId } );
		Object result = this.serviceDispatcher.getClassificationService(equipBatchSet.getBatch()).input(inputEvent);
		
		// 3. 결과 리턴
		event.setExecuted(true);
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, result));
	}
	
	/**
	 * 카트 작업 마감
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/cart_job/finish', 'dpc')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void finishCartJob(DeviceProcessRestEvent event) {
		Map<String, Object> reqParams = event.getRequestParams();
		String equipCd = reqParams.get("equipCd").toString();
		String cartJobId = reqParams.get("cartJobId").toString();

		// 1. 작업 배치 조회
		Long domainId = event.getDomainId();
		JobBatch batch = LogisServiceUtil.checkRunningBatchByCart(domainId, equipCd);
		String batchId = batch.getId();
		
		// 2. 작업 배치 락 처리
		batch = LogisServiceUtil.findBatchWithLock(domainId, batchId, false, false);
		
		// 3. 카트 작업 조회
		String sql = "select distinct to_zone_cd, to_cell_cd from orders where domain_id = :domainId and batch_id = :batchId and to_zone_cd = :cartJobId";
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,cartJobId", domainId, batch.getId(), cartJobId);
		xyz.anythings.base.entity.Order cartJob = this.queryManager.selectBySql(sql, condition, xyz.anythings.base.entity.Order.class);
		
		if(cartJob == null) {
			throw ThrowUtil.newValidationErrorWithNoLog("카트 작업 [" + cartJobId + "]은 존재하지 않습니다.");
		}
		
		// 4. 카트 작업이 진행 중이 아니면 에러
		if(ValueUtil.isNotEqual(JobBatch.STATUS_RUNNING, cartJob.getToCellCd())) {
			throw ThrowUtil.newValidationErrorWithNoLog("카트 작업 [" + cartJobId + "]은 이미 완료되었습니다.");
		}
		
		// 5. 작업 마감이 가능한 상태인 지 조회
		sql = "select distinct order_no from job_instances where domain_id = :domainId and batch_id = :batchId and box_class_cd = :cartJobId and pick_qty > picked_qty";
		int notFinishedCount = this.queryManager.selectSizeBySql(sql, condition);
		if(notFinishedCount > 0) {
			throw ThrowUtil.newValidationErrorWithNoLog("완료되지 않은 주문이 있어서 카트 작업 [" + cartJobId + "]을 완료할 수 없습니다.");
		}
		
		// 6. 셀 정보 클리어
		condition = ValueUtil.newMap("domainId,equipType,equipCd,activeFlag,batchId,cartJobId,nowStr,now", domainId, LogisConstants.EQUIP_TYPE_RACK, equipCd, true, batchId, cartJobId, DateUtil.currentTimeStr(), new Date());
		sql = "update cells set class_cd = null where domain_id = :domainId and equip_type = :equipType and equip_cd = :equipCd and active_flag = :activeFlag";
		this.queryManager.executeBySql(sql, condition);
		sql = "update work_cells set com_cd = null, ind_cd = null, box_id = null, class_cd = null, last_job_cd = null, last_picked_qty = 0, job_instance_id = null, status = null, updated_at = :now where domain_id = :domainId and active_flag = :activeFlag and cell_cd in (select cell_cd from cells where domain_id = :domainId and equip_type = :equipType and equip_cd = :equipCd)";
		this.queryManager.executeBySql(sql, condition);
		
		// 7. 작업 대상 카트 작업 상태 업데이트
		sql = "update orders set to_cell_cd = :nowStr, updated_at = :now where domain_id = :domainId and batch_id = :batchId and to_zone_cd = :cartJobId";
		this.queryManager.executeBySql(sql, condition);
		
		// 8. 카트 (랙) 정보 클리어
		sql = "update racks set batch_id = null, status = null where domain_id = :domainId and rack_cd = :equipCd";
		this.queryManager.executeBySql(sql, condition);
		
		// 9. 표시기 사용시 모든 셀 표시기 소등
		if(DpcBatchJobConfigUtil.isUseIndicator(batch)) {
			IIndicationService indSvc = this.serviceDispatcher.getIndicationService(batch);
			indSvc.indicatorOffAll(batch, true);
		}
		
		// 10. 리턴 결과 설정
		event.setExecuted(true);
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, true));
	}

	/*****************************************************************************************************
	 * 											D P C 투 입 A P I
	 *****************************************************************************************************
	/**
	 * DPC 보관 존 별 피킹 리스트 조회
	 *
	 * @param event
	 * @return
	 */ 
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/picking_list_by_zone', 'dpc')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void searchPickingListByZone(DeviceProcessRestEvent event) {
		// 1. 파라미터
		Map<String, Object> reqParams = event.getRequestParams();
		String cartJobId = reqParams.get("cartJobId").toString();
		String equipCd = reqParams.get("equipCd").toString();
		String zoneCd = reqParams.get("zoneCd").toString();
		
		// 2. 작업 배치
		Long domainId = event.getDomainId();
		JobBatch batch = LogisServiceUtil.checkRunningBatchByCart(domainId, equipCd);
		
		// 3. 이벤트 처리 결과 셋팅
		String sql = "select from_zone_cd as zone_cd, from_cell_cd as cell_cd, sku_cd, sku_barcd, max(sku_nm) as sku_nm, sum(order_qty) as order_qty, sum(picked_qty) as picked_qty from orders where domain_id = :domainId and batch_id = :batchId and to_zone_cd = :cartJobId and from_zone_cd = :zoneCd group by from_zone_cd, from_cell_cd, sku_cd, sku_barcd order by from_cell_cd, sku_cd";
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,cartJobId,zoneCd", domainId, batch.getId(), cartJobId, zoneCd);
		List<DpcZoneInput> inputList = this.queryManager.selectListBySql(sql, condition, DpcZoneInput.class, 0, 0);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inputList));
		event.setExecuted(true);
	}
	
	/**
	 * DPC 피킹 히스토리 조회
	 *
	 * @param event
	 * @return
	 */ 
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/picking_history', 'dpc')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void searchPickingHistory(DeviceProcessRestEvent event) {
		// 1. 파라미터
		Map<String, Object> reqParams = event.getRequestParams();
		Long domainId = event.getDomainId();
		String cartJobId = reqParams.get("cartJobId").toString();
		String equipCd = reqParams.get("equipCd").toString();
		String zoneCd = reqParams.get("zoneCd").toString();
		String cellCd = reqParams.get("cellCd").toString();
		String skuCd = reqParams.get("skuCd").toString();
		
		// 2. 작업 배치
		JobBatch batch = LogisServiceUtil.checkRunningBatchByCart(domainId, equipCd);
		
		// 3. 이벤트 처리 결과 셋팅
		String sql = "select sub_equip_cd as cell_cd, class_cd, sum(order_qty) as order_qty, sum(picked_qty) as picked_qty from orders where domain_id = :domainId and batch_id = :batchId and to_zone_cd = :cartJobId and from_zone_cd = :zoneCd and from_cell_cd = :cellCd and sku_cd = :skuCd group by sub_equip_cd, class_cd";
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,cartJobId,zoneCd,cellCd,skuCd", domainId, batch.getId(), cartJobId, zoneCd, cellCd, skuCd);
		List<DpcZoneInput> inputList = this.queryManager.selectListBySql(sql, condition, DpcZoneInput.class, 0, 0);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inputList));
		event.setExecuted(true);
	}
	
	/**
	 * DPC 마지막 상품 투입 취소
	 *
	 * @param event
	 * @return
	 */ 
	@SuppressWarnings("unchecked")
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/cancel/input/sku', 'dpc')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void cancelInputSku(DeviceProcessRestEvent event) {
		// 1. 파라미터
		Map<String, Object> reqParams = event.getRequestParams();
		String equipType = reqParams.get("equipType").toString();
		String equipCd = reqParams.get("equipCd").toString();
		
		// 2. 작업 배치
		Long domainId = event.getDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 마지막 투입 상품 조회
		Integer lastInputSeq = batch.getLastInputSeq();
		if(lastInputSeq == null || lastInputSeq < 1) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NOT_EXIST_INPUT_PRODUCT"));
		}
		
		// 4. 작업 현황 조회
		Map<String, Object> condition = ValueUtil.newMap("inputSeq", batch.getLastInputSeq());
		List<JobInstance> jobList = this.serviceDispatcher.getJobStatusService(batch).searchPickingJobList(batch, condition);
		
		// 5. 소등 대상 게이트웨이 & 표시기 리스트 추출
		Map<String, Object> gwIndMap = new HashMap<String, Object>();
		for(JobInstance job : jobList) {
			if(job.isDoneJob()) {
				throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("PRODUCTS_CLASSIFIED_CANNOT_CANCELED_PLZ_INDIVIDUAL_CANCEL"));
			} else {
				if(ValueUtil.isEqualIgnoreCase(job.getStatus(), LogisConstants.JOB_STATUS_PICKING)) {
					String gwPath = job.getGwPath();
					boolean isContainsGw = gwIndMap.containsKey(gwPath);
					List<String> indList = isContainsGw ? (List<String>)gwIndMap.get(gwPath) : new ArrayList<String>();
					if(!isContainsGw) gwIndMap.put(gwPath, indList);
					indList.add(job.getIndCd());
				}
			}
		}
		
		// 6. 투입 삭제 처리
		condition = ValueUtil.newMap("domainId,batchId,inputSeq", domainId, batch.getId(), batch.getLastInputSeq());
		JobInput jobInput = this.queryManager.selectByCondition(JobInput.class, condition);
		if(jobInput == null) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("LAST_INPUT_SEQ_NO_INFORMATION"));
		} else {
			this.queryManager.delete(jobInput);
		}
		
		// 7. 작업 현황 업데이트
		String sql = "update job_instances set status = :status, picking_qty = 0, picked_qty = 0, input_at = null, pick_started_at = null, pick_ended_at = null, input_seq = 0, ind_cd = null, color_cd = null where domain_id = :domainId and batch_id = :batchId and input_seq = :inputSeq and com_cd = :comCd and sku_cd = :skuCd";
		condition.put("status", LogisConstants.JOB_STATUS_WAIT);
		condition.put("comCd", jobInput.getComCd());
		condition.put("skuCd", jobInput.getSkuCd());
		this.queryManager.executeBySql(sql, condition);
		
		// 8. 배치 마지막 시퀀스 업데이트
		batch.setLastInputSeq(batch.getLastInputSeq() - 1);
		this.queryManager.update(batch, "lastInputSeq");
		
		// 9. 표시기 사용시 표시기 소등
		if(DpcBatchJobConfigUtil.isUseIndicator(batch)) {
			IIndicationService indSvc = this.serviceDispatcher.getIndicationService(batch);
			if(ValueUtil.isNotEmpty(gwIndMap)) {
				Iterator<String> gwIter = gwIndMap.keySet().iterator();
				while(gwIter.hasNext()) {
					String gwPath = gwIter.next();
					List<String> indList = (List<String>)gwIndMap.get(gwPath);
					indSvc.indicatorListOff(domainId, batch.getStageCd(), gwPath, indList);
				}
			}
		}
		
		// 10. 투입 이벤트 Publish
		this.eventPublisher.publishEvent(new InputEvent(batch, jobInput));
		
		// 11. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, null));
		event.setExecuted(true);
	}
	
	/*****************************************************************************************************
	 * 											D P C 피 킹 A P I
	 *****************************************************************************************************

	/**
	 * 태블릿에서 DPC 피킹
	 *
	 * @param event
	 * @return
	 */ 
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/classify/confirm', 'dpc')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void confirmPicking(DeviceProcessRestEvent event) {
		// 1. 파라미터
		Map<String, Object> reqParams = event.getRequestParams();
		String equipType = ValueUtil.toString(reqParams.get("equipType"));
		String equipCd = ValueUtil.toString(reqParams.get("equipCd"));
		String deviceType = ValueUtil.toString(reqParams.get("deviceType"));
		String batchId = ValueUtil.toString(reqParams.get("batchId"));
		String cartJobId = ValueUtil.toString(reqParams.get("cartJobId"));
		String classCd = ValueUtil.toString(reqParams.get("classCd"));
		String skuCd = ValueUtil.toString(reqParams.get("skuCd"));
		int pickQty = ValueUtil.toInteger(reqParams.get("pickQty"));
		
		// 2. 작업 배치
		Long domainId = event.getDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		if(ValueUtil.isNotEqual(batch.getId(), batchId)) {
			throw ThrowUtil.newValidationErrorWithNoLog("배치 ID가 매치되지 않습니다. 새로고침 후에 다시 작업하세요.");
		}
		
		// 3. 작업 조회
		Map<String, Object> condition = ValueUtil.newMap("boxClassCd,classCd,skuCd,status", cartJobId, classCd, skuCd, LogisConstants.JOB_STATUS_PICKING);
		List<JobInstance> jobList = this.serviceDispatcher.getJobStatusService(batch).searchPickingJobList(batch, condition);
		if(ValueUtil.isEmpty(jobList)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NOT_EXIST_ORDER"));
		}
		
		// 4. 소분류 이벤트 생성
		JobInstance job = jobList.get(0);
		int resQty = job.getPickingQty();
		
		if(pickQty >= resQty) {
			ClassifyRunEvent classifyEvent = new ClassifyRunEvent(batch
					, SysEvent.EVENT_STEP_ALONE
					, deviceType.toLowerCase()
					, LogisCodeConstants.CLASSIFICATION_ACTION_CONFIRM
					, job
					, job.getPickingQty()
					, resQty);
			
			// 5. 이벤트 발생 
			this.eventPublisher.publishEvent(classifyEvent);
		}
		
		// 6. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, null));
		event.setExecuted(true);
	}
	
	/*****************************************************************************************************
	 * 											DPC 표시기 검수 A P I
	 *****************************************************************************************************
	/**
	 * DAS 표시기를 이용한 검수 처리
	 *
	 * @param event
	 * @return
	 */ 
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspect_by_indicator', 'dpc')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void inspectByIndicator(DeviceProcessRestEvent event) {
		// 1. 파라미터
		Map<String, Object> reqParams = event.getRequestParams();
		String equipType = reqParams.get("equipType").toString();
		String equipCd = reqParams.get("equipCd").toString();
		String comCd = reqParams.get("comCd").toString();
		String skuCd = reqParams.get("skuCd").toString();
		
		// 2. 작업 배치 조회
		Long domainId = event.getDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, equipType, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 3. 이벤트 처리 결과 셋팅
		IClassifyInEvent inspectionEvent = new ClassifyInEvent(batch, SysEvent.EVENT_STEP_ALONE, true, LogisCodeConstants.CLASSIFICATION_INPUT_TYPE_SKU, skuCd, 0);
		inspectionEvent.setComCd(comCd);
		Object result = this.serviceDispatcher.getAssortService(batch).inputForInspection(inspectionEvent);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, result));
		event.setExecuted(true);
	}
	
	/*****************************************************************************************************
	 * 											D P C 송 장 인 쇄 A P I
	 *****************************************************************************************************
	
	/**
	 * 배치내 랙의 모든 셀에 남은 상품으로 일괄 풀 박스 처리
	 *
	 * @param event
	 * @return
	 */ 
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/batch_boxing', 'dpc')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void fullboxingAllRemained(DeviceProcessRestEvent event) {
		// 1. 파라미터 처리
		Map<String, Object> reqParams = event.getRequestParams();
		String equipCd = ValueUtil.toString(reqParams.get("equipCd"));
		
		// 2. 작업 배치 조회
		Long domainId = event.getDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, LogisConstants.EQUIP_TYPE_RACK, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		this.serviceDispatcher.getBoxingService(batch).batchBoxing(batch);
		
		// 3. 결과 리턴
		event.setExecuted(true);
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING));
	}
	
	/**
	 * 송장 출력
	 *
	 * @param event
	 * @return
	 */ 
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/print_invoice', 'dpc')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void printInvoiceLabel(DeviceProcessRestEvent event) {
		Map<String, Object> reqParams = event.getRequestParams();
		String equipCd = ValueUtil.toString(reqParams.get("equipCd"));
		String boxId = ValueUtil.toString(reqParams.get("boxId"));
		String invoiceId = ValueUtil.toString(reqParams.get("invoiceId"));
		String cellCd = ValueUtil.toString(reqParams.get("cellCd"));
		String printerId = ValueUtil.toString(reqParams.get("printerId"));
		
		// 1. 작업 배치 조회
		Long domainId = event.getDomainId();
		EquipBatchSet equipBatchSet = LogisServiceUtil.checkRunningBatch(domainId, LogisConstants.EQUIP_TYPE_RACK, equipCd);
		JobBatch batch = equipBatchSet.getBatch();
		
		// 2. 박스 정보 조회
		BoxPack box = AnyEntityUtil.findEntityBy(domainId, false, BoxPack.class, "*", "boxId", boxId);
		
		if(box == null) {
			box = AnyEntityUtil.findEntityBy(domainId, false, BoxPack.class, "*", "invoiceId", invoiceId);
		}
		
		// 3. 프린터 ID 조회
		if(ValueUtil.isEmpty(printerId)) {
			printerId = AnyEntityUtil.findItemOneColumn(domainId, true, String.class, Cell.class, "printer_cd", "cellCd", cellCd);
		}
		
		// 4. 프린트 이벤트 전송
		String labelTemplate = DpcBatchJobConfigUtil.getInvoiceLabelTemplate(batch);
		Map<String, Object> printParams = ValueUtil.newMap("batch,box", batch, box);
		PrintEvent printEvent = new PrintEvent(domainId, batch.getJobType(), printerId, labelTemplate, printParams);
		printEvent.setPrintType("barcode");
		this.eventPublisher.publishEvent(printEvent);
		
		// 5. 이벤트 결과 처리
		event.setExecuted(true);
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING));
	}

	/*****************************************************************************************************
	 * 											이 벤 트 처 리 A P I
	 *****************************************************************************************************
	/**
	 * 피킹 처리 완료 이벤트 처리
	 * 
	 * @param classifyEvent
	 */
	@EventListener(classes = IClassifyEvent.class, condition = "#classifyEvent.jobType == 'DPC'")
	public void pickingEventHandler(IClassifyEvent classifyEvent) {
		if(classifyEvent.getResult() != null && classifyEvent.getResult() instanceof String) {
			String jobCd = (String)classifyEvent.getResult();
			JobBatch batch = classifyEvent.getJobBatch();
			Long domainId = batch.getDomainId();
			String[] deviceTypeList = BatchJobConfigUtil.getDeviceList(batch);
			
			if(deviceTypeList != null) {
				for(String deviceType : deviceTypeList) {
					DeviceEvent event = new DeviceEvent(domainId, deviceType, batch.getStageCd(), batch.getEquipType(), batch.getEquipCd(), null, null, batch.getJobType(), "info", jobCd);
					this.eventPublisher.publishEvent(event);
				}
			}
		}
	}

//	/**
//	 * DPC 송장 라벨 인쇄 --> anythings-printing 모듈의 LabelPrintService에서 공통적으로 처리
//	 * 
//	 * @param printEvent
//	 */
//	@Async
//	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = PrintEvent.class, condition = "#printEvent.jobType == 'DPC'")
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
//			ErrorEvent errorEvent = new ErrorEvent(domain.getId(), "DAS_PRINT_LABEL_ERROR", e, null, true, true);
//			this.eventPublisher.publishEvent(errorEvent);
//			
//		} finally {
//			// 스레드 로컬 변수에서 currentDomain 리셋 
//			DomainContext.unsetAll();
//		}
//	}
}
