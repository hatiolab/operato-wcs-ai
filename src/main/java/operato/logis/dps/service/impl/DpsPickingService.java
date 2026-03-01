package operato.logis.dps.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import operato.logis.dps.DpsCodeConstants;
import operato.logis.dps.DpsConstants;
import operato.logis.dps.service.api.IDpsPickingService;
import operato.logis.dps.service.util.DpsBatchJobConfigUtil;
import operato.logis.dps.service.util.DpsServiceUtil;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.Station;
import xyz.anythings.base.entity.ifc.IBucket;
import xyz.anythings.base.event.IClassifyInEvent;
import xyz.anythings.base.event.IClassifyRunEvent;
import xyz.anythings.base.event.classfy.ClassifyEndEvent;
import xyz.anythings.base.event.classfy.ClassifyEvent;
import xyz.anythings.base.model.EquipBatchSet;
import xyz.anythings.base.service.api.IIndicationService;
import xyz.anythings.gw.service.util.BatchIndConfigUtil;
import xyz.anythings.sys.model.BaseResponse;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * DPS 박스 처리 포함한 피킹 서비스 트랜잭션 구현
 * 
 * @author yang
 */
@Component("dpsPickingService")
public class DpsPickingService extends AbstractPickingService implements IDpsPickingService {

	/************************************************************************************************/
	/*										배치 시작 / 마감 시 처리 										*/
	/************************************************************************************************/

	@Override
	public void batchStartAction(JobBatch batch) {
		// 1. 상위 로직 호출
		super.batchStartAction(batch);
		
		// 2. 배치 내 모든 호기의 표시기 소등
		this.serviceDispatcher.getIndicationService(batch).indicatorOffAll(batch);
	}
	
	/************************************************************************************************/
	/*											버킷 도착 / 출발 										*/
	/************************************************************************************************/
	
	@Override
	public boolean checkBoxArrived(Long domainId, String barcodeIp, String boxId) {
		// 1. 작업 스테이션 정보 조회
		Query condition = AnyOrmUtil.newConditionForExecution(domainId, "id", "equipType", "equipCd", "stationCd", "classCd");
		condition.addFilter("stationIp", barcodeIp);
		Station station = this.queryManager.selectByCondition(Station.class, condition);
		
		if(station == null) {
			throw new ElidomRuntimeException(MessageUtil.getMessage("BARCODE_IP_STATION_INFO_NOT_FOUND", "바코드 IP [{0}] 스테이션 정보를 찾을 수 없습니다.", ValueUtil.toList(barcodeIp)));
		}
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		EquipBatchSet equipBatchSet = DpsServiceUtil.findBatchByEquip(domainId, station.getEquipType(), station.getEquipCd());
		JobBatch batch = equipBatchSet.getBatch();
		int orderCount = 0;

		// 3. 진행 중인 배치가 있을 때 박스 pass | stop 여부 판단
		if(batch != null) {
			// 3.1 투입된 박스 ID인 지 체크
			Map<String, Object> queryParams = ValueUtil.newMap("domainId,batchId,boxId,stationCd,statuses", domainId, batch.getId(), boxId, station.getStationCd(), LogisConstants.JOB_STATUS_WIP);
			String query = "select count(*) from job_instances where domain_id = :domainId and batch_id = :batchId and box_id = :boxId";
			if(this.queryManager.selectBySql(query, queryParams, Integer.class) == 0) {
				throw new ElidomRuntimeException(MessageUtil.getMessage("A_IS_INVALID", "{0}이(가) 유효하지 않습니다.", ValueUtil.toList("Box ID : [" + boxId + "]")));
			}
			
			// 3.2 박스 ID가 해당 스테이션에 처리할 주문이 있는지 체크
			query = "select count(j.id) from job_instances j inner join cells c on j.domain_id = c.domain_id and j.sub_equip_cd = c.cell_cd where j.domain_id = :domainId and j.batch_id = :batchId and j.box_id = :boxId and j.status in (:statuses) and c.station_cd = :stationCd";
			orderCount = this.queryManager.selectBySql(query, queryParams, Integer.class);
			
		// 4. 진행 중인 배치가 없다면 에러
		} else {
			throw new ElidomRuntimeException(MessageUtil.getMessage("NOT_FOUND_RUNNING_BATCH", "진행 중인 배치가 존재하지 않습니다."));
		}
		
		// 5. 처리할 주문이 있다면 작업 화면에 메시지 전달
		if(orderCount > 0) {
			// 5.1 작업 스테이션에 이전에 처리한 박스가 있다면 표시기 소등
			if(ValueUtil.isNotEmpty(station.getClassCd())) {
				this.boxLeave(batch, station.getStationCd(), station.getClassCd());
			}
			
			String msg = DEVICE_RESULT_MESSAGE_BOX_ARRIVED + SysConstants.SPACE + boxId;
			this.eventPublisher.publishEvent(new ClassifyEndEvent(batch, ClassifyEvent.EVENT_STEP_ALONE, station.getStationCd(), msg));
		}
		
		// 6. 결과 리턴
		return orderCount > 0;
	}

	@Override
	public BaseResponse boxArrived(JobBatch batch, String equipCd, String stationCd, String boxId, boolean singleBoxMode) {
		// 1. 투입 정보 조회
		Long domainId = batch.getDomainId();
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("batchId", batch.getId());
		condition.addFilter("equipCd", equipCd);
		condition.addFilter("stationCd", stationCd);
		condition.addFilter("boxId", boxId);
		JobInput input = this.queryManager.selectByCondition(JobInput.class, condition);
		String message = null;
		
		// 2. 투입 정보가 없다면 메시지 출력
		if(input == null) {
			condition.removeFilter("stationCd");
			int count = this.queryManager.selectSize(JobInput.class, condition);
			
			if(count == 0) {
				message = "박스 [" + boxId + "]는 투입 박스가 아닙니다.";
			} else {
				condition.addFilter("status", SysConstants.IN, LogisConstants.JOB_STATUS_WIP);
				count = this.queryManager.selectSize(JobInstance.class, condition);
				message = (count == 0) ? "주문이 완료되었습니다. 박스를 배출하세요." : "작업이 완료되었습니다. 완료 버튼을 누르세요.";
			}
			
			return new BaseResponse(true, message, new ArrayList<JobInput>(1));
			
		// 3. 투입 정보가 있다면
		} else {
			// 4. 스테이션에 락킹 - 스테이션에서 한 순간 하나의 일만 처리 (박스 스캔 & 피킹 작업 동기화)
			AnyEntityUtil.findEntityByWithLock(domainId, true, Station.class, "id", "stationCd", stationCd);
			
			// 5. 표시기 점등을 위한 작업 데이터 조회
			List<JobInstance> jobList = this.dpsJobStatusService.searchPickingJobList(batch, stationCd, input.getOrderNo());
			
			// 6. 작업 데이터로 표시기 점등 & 작업 데이터 상태 및 피킹 시작 시간 등 업데이트
			if(ValueUtil.isNotEmpty(jobList)) {
				// 6.1 투입 정보 상태 업데이트 (WAIT => RUNNING)
				if(ValueUtil.isEqualIgnoreCase(input.getStatus(), DpsCodeConstants.JOB_INPUT_STATUS_WAIT)) {
					input.setStatus(DpsCodeConstants.JOB_INPUT_STATUS_RUN);
					this.queryManager.update(input, DpsConstants.ENTITY_FIELD_STATUS, DpsConstants.ENTITY_FIELD_UPDATER_ID, DpsConstants.ENTITY_FIELD_UPDATED_AT);
				}
				
				// 6.2 작업 표시기 점등
				message = "피킹 가능한 박스입니다. 상품을 피킹하세요.";
				for(JobInstance job : jobList) {
					if(ValueUtil.toInteger(job.getPickingQty(), 0) == 0) {
						job.setPickingQty(job.getPickQty());
					}
				}
				this.serviceDispatcher.getIndicationService(batch).indicatorsOn(batch, false, jobList);
				
				// 6.3 작업 스테이션에 박스 ID 설정
				String sql = "update stations set class_cd = :classCd, updated_at = :now where domain_id = :domainId and equip_cd = :equipCd and station_cd = :stationCd";
				this.queryManager.executeBySql(sql, ValueUtil.newMap("domainId,equipCd,stationCd,classCd,now", domainId, equipCd, stationCd, boxId, new Date()));
				
			// 7. 작업 데이터가 없다면
			} else {
				// 7.1 박스 (주문)에 피킹할 작업이 남아 있는지 체크
				condition.removeFilter("stationCd");
				condition.addFilter("status", SysConstants.IN, LogisConstants.JOB_STATUS_WIP);
				int count = this.queryManager.selectSize(JobInstance.class, condition);
				message = (count == 0) ? "주문이 완료되었습니다. 박스를 배출하세요." : "작업이 완료되었습니다. 완료 버튼을 누르세요.";
			}
			
			// 8. 도착한 박스 기준으로 태블릿에 표시할 투입 박스 리스트 조회 & 이벤트 처리 결과 셋팅
			List<JobInput> inputList = singleBoxMode ? 
					ValueUtil.toList(input) : this.dpsJobStatusService.searchInputList(batch, equipCd, stationCd, input.getId());
			return new BaseResponse(true, message, inputList);
		}
	}

	@Override
	public Object boxLeave(JobBatch batch, String stationCd, String boxId) {
		// 1. 박스의 작업 존 내 작업 정보 조회
		Map<String, Object> condition = ValueUtil.newMap("stationCd,boxId", stationCd, boxId);
		List<JobInstance> jobList = this.serviceDispatcher.getJobStatusService(batch).searchPickingJobList(batch, condition);
		
		// 2. 작업 존 내 게이트웨이 정보 조회
		Map<String, Object> gwIndMap = new HashMap<String, Object>();
		for(JobInstance job : jobList) {
			String gwPath = job.getGwPath();
			boolean isContainsGw = gwIndMap.containsKey(gwPath);
			@SuppressWarnings("unchecked")
			List<String> indList = isContainsGw ? (List<String>)gwIndMap.get(gwPath) : new ArrayList<String>();
			if(!isContainsGw) gwIndMap.put(gwPath, indList);
			indList.add(job.getIndCd());
		}

		// 3. 게이트웨이 별 작업 정보 표시기 소등
		if(ValueUtil.isNotEmpty(gwIndMap)) {
			IIndicationService indSvc = this.serviceDispatcher.getIndicationService(batch);
			Iterator<String> gwIter = gwIndMap.keySet().iterator();
			while(gwIter.hasNext()) {
				String gwPath = gwIter.next();
				@SuppressWarnings("unchecked")
				List<String> indList = (List<String>)gwIndMap.get(gwPath);
				indSvc.indicatorListOff(batch.getDomainId(), batch.getStageCd(), gwPath, indList);
			}
		}
		
		// 4. 결과 리턴
		return true;
	}
	
	/************************************************************************************************/
	/*											버킷 투입												*/
	/************************************************************************************************/

	/**
	 * 2-2. 분류 설비에 박스 투입 처리
	 * 
	 * @param inputEvent
	 */
	@Override
	public Object input(IClassifyInEvent inputEvent) {
		
		String boxId = inputEvent.getInputCode();
		JobBatch batch = inputEvent.getJobBatch();
		String boxType = DpsBatchJobConfigUtil.getInputBoxType(batch);
		boolean isBox = ValueUtil.isEqualIgnoreCase(boxType, DpsCodeConstants.BOX_TYPE_BOX);
		Object retValue = this.inputEmptyBucket(batch, isBox, boxId, null);
		inputEvent.setResult(retValue);
		inputEvent.setExecuted(true);
		return retValue;
	}
	
	/**
	 * 2-3. 투입 : 배치 작업에 공 박스 투입
	 * 
	 * @param inputEvent
	 * @return
	 */
	@Override
	@EventListener(condition = "#inputEvent.getInputType() == 'box' and #inputEvent.isForInspection() == false and #inputEvent.isExecuted() == false")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public Object inputEmptyBox(IClassifyInEvent inputEvent) {
		
		String boxId = inputEvent.getInputCode();
		JobBatch batch = inputEvent.getJobBatch();
		Object retValue = this.inputEmptyBucket(batch, true, boxId, null);
		inputEvent.setResult(retValue);
		inputEvent.setExecuted(true);
		return retValue;
	}

	/**
	 * 2-3. 투입 : 배치 작업에 공 트레이 투입
	 * 
	 * @param inputEvent
	 * @return
	 */
	@Override
	@EventListener(condition = "#inputEvent.getInputType() == 'tray' and #inputEvent.isForInspection() == false and #inputEvent.isExecuted() == false")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public Object inputEmptyTray(IClassifyInEvent inputEvent) {
		
		String trayCd = inputEvent.getInputCode();
		JobBatch batch = inputEvent.getJobBatch();
		Object retValue = this.inputEmptyBucket(batch, false, trayCd, null);
		inputEvent.setResult(retValue);
		inputEvent.setExecuted(true);
		return retValue;
	}
	
	/**
	 * 합포 박스 또는 트레이 투입
	 * 
	 * @param batch
	 * @param isBox
	 * @param boxId
	 * @param boxTypeCd
	 * @param params
	 * @return
	 */
	@Override
	public Object inputEmptyBucket(JobBatch batch, boolean isBox, String boxId, String boxTypeCd, Object... params) {
		
		// 1. 투입 가능한 버킷인지 체크 (박스 or 트레이)
		//	-> 박스 타입이면 박스 타입에 락킹 (즉 동일 박스 타입의 박스는 동시에 하나씩만 투입 가능) / 트레이 타입이면 버킷에 락킹 (하나의 버킷은 한 번에 하나만 투입 가능)
		IBucket bucket = this.vaildInputBucketByBucketCd(batch, boxId, boxTypeCd, isBox, true);
		
		// 2. 박스 투입 전 체크 - 주문 번호 조회 
		String orderNo = this.beforeInputEmptyBucket(batch, isBox, bucket);

		// 3. 표시기 색상 결정
		String indColor = ValueUtil.isEmpty(bucket.getBucketColor()) ? BatchIndConfigUtil.getDpsJobColor(batch.getId()) : bucket.getBucketColor();
		
		// 4. 주문 번호로 매핑된 작업을 모두 조회
		List<JobInstance> jobList = this.dpsJobStatusService.searchPickingJobList(batch, null, orderNo);

		if(ValueUtil.isEmpty(jobList)) {
			// 투입 가능한 주문이 없습니다.
			throw new ElidomRuntimeException(MessageUtil.getMessage(MessageUtil.getMessage("LOGIS_NO_ORDER_TO_INPUT")));
		}
		
		// 5. 작업 데이터에 박스 ID 설정
		for(JobInstance job : jobList) {
			job.setBoxId(boxId);
		}
		
		// 6. 투입
		this.doInputEmptyBucket(batch, orderNo, bucket, indColor);
		
		// 7. 박스 투입 후 액션 
		this.afterInputEmptyBucket(batch, bucket, orderNo);
		
		// 8. 투입 정보 리턴
		return jobList;
	}
	
	/***********************************************************************************************/
	/*											소분류												*/
	/***********************************************************************************************/

	/**
	 * 3-3. 소분류 : 피킹 작업 확정 처리
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 */
	@Override
	@EventListener(condition = "#exeEvent.getClassifyAction() == 'ok' and #exeEvent.isExecuted() == false and #exeEvent.getJobType() == 'DPS'")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void confirmPick(IClassifyRunEvent exeEvent) {
		
		JobBatch batch = exeEvent.getJobBatch();
		// 1. JobInstance 조회 
		JobInstance job = exeEvent.getJobInstance();
		// 2. 확정 처리 
		boolean pickWithInspection = exeEvent.isPickWithInspection();
		this.confirmPick(batch, job, exeEvent.getResQty(), pickWithInspection);
		// 3. 표시기에서 확정하지 않았다면 표시기 소등
		if(ValueUtil.isNotEqual(exeEvent.getClassifyDevice(), LogisConstants.DEVICE_INDICATOR)) {
			// 3.1 작업이 완료되었다면 아래 처럼 소등 그렇지 않다면 점등
			if(job.getPickedQty() == job.getPickQty()) {
				String displayStr = StringUtils.leftPad("" + job.getPickedQty(), 6);
				this.serviceDispatcher.getIndicationService(batch).displayForString(batch.getDomainId(), batch.getId(), batch.getStageCd(), batch.getJobType(), job.getGwPath(), job.getIndCd(), displayStr);
				
			// 3.2 작업이 완료되지 않았고 피킹 & 검수 모드이면 표시기 수량 점등 ...
			} else {
				if(pickWithInspection) {
					this.serviceDispatcher.getIndicationService(batch).indicatorsOn(batch, false, ValueUtil.toList(job));
				}
			}
		}
		// 4. 실행 여부 체크
		exeEvent.setExecuted(true);
	}
	
	/**
	 * 작업 확정 처리
	 * 
	 * @param batch
	 * @param job
	 * @param resQty
	 */
	@Override
	public void confirmPick(JobBatch batch, JobInstance job, int resQty, boolean pickWithInspection) {
		// 1. 스테이션에 락킹 - 스테이션에서 한 순간 하나의 일만 처리 (박스 스캔 & 피킹 작업 동기화)
		AnyEntityUtil.findEntityByWithLock(job.getDomainId(), true, Station.class, "id", "stationCd", job.getStationCd());
		
		// 2. 작업 상태 체크
		if(ValueUtil.isNotEqual(job.getStatus(), DpsConstants.JOB_STATUS_PICKING)) {
			throw new ElidomServiceException(MessageUtil.getMessage("CONFIRM_PROCESS_ONLY_PICKING_STATUS"));
		}
		
		// 3. 합포의 경우에 CELL 사용 
		Long domainId = batch.getDomainId();
		Cell cell = (ValueUtil.isEqualIgnoreCase(job.getOrderType(), DpsCodeConstants.DPS_ORDER_TYPE_MT)) ? 
				    AnyEntityUtil.findEntityBy(domainId, true, Cell.class, null, "equipType,equipCd,cellCd", job.getEquipType(), job.getEquipCd(), job.getSubEquipCd()) : null;
		
		// 4. 작업 처리 전 액션
		int pickQty = this.beforeConfirmPick(batch, job, cell, resQty);
		
		if(pickQty > 0) {
			// 5. 분류 작업 처리
			this.doConfirmPick(batch, job, cell, pickQty, pickWithInspection);
			// 6. 작업 처리 후 액션
			this.afterComfirmPick(batch, job, cell, pickQty);
		}
	}

	/**
	 * 3-4. 소분류 : 피킹 취소 (예정 수량보다 분류 처리할 실물이 작아서 처리할 수 없는 경우 취소 처리)
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 */
	@Override
	@EventListener(condition = "#exeEvent.getClassifyAction() == 'cancel' and #exeEvent.isExecuted() == false and #exeEvent.getJobType() == 'DPS'")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void cancelPick(IClassifyRunEvent exeEvent) {
		// 1. 모바일 장비에 메시지 전달
		JobBatch batch = exeEvent.getJobBatch();
		JobInstance job = exeEvent.getJobInstance();
		String stationCd = job.getStationCd();
		this.eventPublisher.publishEvent(new ClassifyEndEvent(batch, ClassifyEvent.EVENT_STEP_ALONE, stationCd, DEVICE_RESULT_MESSAGE_CANCEL));
		
		// 2. 표시기에서 확정하지 않았다면 표시기 소등
		if(ValueUtil.isNotEqual(exeEvent.getClassifyDevice(), LogisConstants.DEVICE_INDICATOR)) {
			String displayStr = StringUtils.leftPad("0", 6);
			this.serviceDispatcher.getIndicationService(batch).displayForString(batch.getDomainId(), batch.getId(), batch.getStageCd(), batch.getJobType(), job.getGwPath(), job.getIndCd(), displayStr);
		}
		
		// 3. 실행 플래그
		exeEvent.setExecuted(true);
	}

	/**
	 * 3-5. 소분류 : 수량을 조정하여 분할 피킹 처리
	 * 
	 * @param exeEvent 분류 작업 이벤트
	 * @return
	 */
	@Override
	@EventListener(condition = "#exeEvent.getClassifyAction() == 'modify' and #exeEvent.isExecuted() == false and #exeEvent.getJobType() == 'DPS'")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public int splitPick(IClassifyRunEvent exeEvent) {
		// TODO 구현
		
		// 실행 플래그
		exeEvent.setExecuted(true);
		return 0;
	}

}
