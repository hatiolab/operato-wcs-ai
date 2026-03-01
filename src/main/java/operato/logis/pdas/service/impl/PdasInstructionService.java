package operato.logis.pdas.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import operato.logis.pdas.query.store.PdasBatchQueryStore;
import operato.logis.pdas.service.util.PdasBatchJobConfigUtil;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.event.EventConstants;
import xyz.anythings.base.event.main.BatchInstructEvent;
import xyz.anythings.base.service.api.IInstructionService;
import xyz.anythings.base.service.impl.AbstractInstructionService;
import xyz.anythings.base.service.impl.JobConfigProfileService;
import xyz.anythings.sys.event.model.EventResultSet;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * PDAS 용 작업 지시 서비스
 * 
 * @author shortstop
 */
@Component("pdasInstructionService")
public class PdasInstructionService extends AbstractInstructionService implements IInstructionService {
	/**
	 * 커스텀 서비스 - 대상 분류
	 */
	private static final String DIY_CLASSIFY_ORDERS = "diy-pdas-classify-orders";
	/**
	 * 커스텀 서비스 - 토털 피킹
	 */
	private static final String DIY_TOTAL_PICKING = "diy-pdas-totalpicking";
	/**
	 * 커스텀 서비스 - 작업 지시 전 처리
	 */
	private static final String DIY_PRE_BATCH_START = "diy-pdas-pre-batch-start";
	/**
	 * 커스텀 서비스 - 작업 지시 후 처리
	 */
	private static final String DIY_POST_BATCH_START = "diy-pdas-post-batch-start";
	/**
	 * 커스텀 서비스 - 배치 병합 전 처리
	 */
	private static final String DIY_PRE_MERGE_BATCH = "diy-pdas-pre-merge-batch";
	/**
	 * 커스텀 서비스 - 배치 병합 후 처리
	 */
	private static final String DIY_POST_MERGE_BATCH = "diy-pdas-post-merge-batch";
	/**
	 * 커스텀 서비스 - 작업 지시 취소 전 처리
	 */
	private static final String DIY_PRE_CANCEL_BATCH = "diy-pdas-pre-cancel-batch";
	/**
	 * 커스텀 서비스 - 작업 지시 취소 후 처리
	 */
	private static final String DIY_POST_CANCEL_BATCH = "diy-pdas-post-cancel-batch";
	
	/**
	 * 작업 설정 프로파일 서비스
	 */
	@Autowired
	private JobConfigProfileService jobConfigProfileSvc;
	/**
	 * PDAS 분류 서비스
	 */
	@Autowired
	private PdasAssortService pdasAssortService;
	/**
	 * 배치 쿼리 스토어
	 */
	@Autowired
	private PdasBatchQueryStore pdasBatchQueryStore;

	/**
	 * WCS 대상 분류 Event 처리 
	 * @param event
	 */
	@EventListener(classes = BatchInstructEvent.class, condition = "#event.eventType == 40 and #event.eventStep == 3 and  #event.jobType == 'PDAS' ")
	public void targetClassing(BatchInstructEvent event) { 
		// 커스텀 서비스 호출
		Map<String, Object> diyParams = ValueUtil.newMap("domainId,waveId,isLast", event.getDomainId(), event.getPayload()[0], event.getPayload()[1]);
		this.customService.doCustomService(event.getDomainId(), DIY_CLASSIFY_ORDERS, diyParams);
		event.setExecuted(true);
	}
	
	@Override
	public Map<String, Object> searchInstructionData(JobBatch batch, Object... params) {
		// PDAS에서는 구현이 필요 없음
		return null;
	}
	
	@Override
	public int instructBatch(JobBatch batch, List<String> rackIdList, Object... params) {
		// 1. 배치 정보에 설정값이 없는지 체크하여 없으면 설정
		this.checkJobConfigSet(batch);
		
		// 2. 랙 조회
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		if(ValueUtil.isNotEmpty(rackIdList)) {
			condition.addFilter("id", SysConstants.IN, rackIdList);
		} else {
			condition.addFilter("rackCd", batch.getEquipCd());
		}
		List<Rack> rackList = this.queryManager.selectList(Rack.class, condition);
		
		// 3. 랙에 배치 할당
		for(Rack rack : rackList) {
			if(ValueUtil.isNotEmpty(rack.getBatchId())) {
				throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("WORK_RUNNING_RACK","랙 [{0}]에 작업이 진행 중입니다.",ValueUtil.toList(rack.getRackCd())));
			} else {
				rack.setBatchId(batch.getId());
				rack.setStatus(JobBatch.STATUS_RUNNING);
			}
		}
		
		AnyOrmUtil.updateBatch(rackList, 100, "batchId", "status", "updatedAt");
		
		// 4. 배치 정보에 rackCd 설정
		if(ValueUtil.isEmpty(batch.getEquipCd()) && rackList.size() == 1) {
			Rack rack = rackList.get(0);
			batch.setEquipType(LogisConstants.EQUIP_TYPE_RACK);
			batch.setEquipCd(rack.getRackCd());
			batch.setEquipNm(rack.getRackNm());
			
			if(ValueUtil.isEmpty(batch.getEquipGroupCd())) {
				batch.setEquipGroupCd(rack.getEquipGroupCd());
			}
		}
		
		// 5. 소분류 코드, 방면 분류 코드 값을 설정에 따라서 주문 정보에 추가한다.
		this.doUpdateClassificationCodes(batch, params);
		
		// 6. 작업 지시 처리
		int retCnt = this.doInstructBatch(batch, rackList, params);
		
		// 7. 배치 시작 액션 수행
		this.serviceDispatcher.getAssortService(batch.getJobType()).batchStartAction(batch);
		
		// 8. 건수 리턴
		return retCnt;
	}

	@Override
	public int instructTotalpicking(JobBatch batch, List<String> equipIdList, Object... params) {
		// 1. 토털 피킹 전 처리 이벤트
		EventResultSet befResult = this.publishTotalPickingEvent(SysEvent.EVENT_STEP_BEFORE, batch, equipIdList, params);
		
		// 2. 다음 처리 취소일 경우 결과 리턴
		if(befResult.isAfterEventCancel()) {
			return ValueUtil.toInteger(befResult.getResult());
		}
		
		// 3. 커스텀 서비스 호출
		Object retVal = this.customService.doCustomService(batch.getDomainId(), DIY_TOTAL_PICKING, ValueUtil.newMap("batch,equipList", batch, equipIdList));
		
		// 4. 토털 피킹 후 처리 이벤트
		EventResultSet aftResult = this.publishTotalPickingEvent(SysEvent.EVENT_STEP_AFTER, batch, equipIdList, params);
		
		// 5. 후처리 이벤트 실행 후 리턴 결과가 있으면 해당 결과 리턴
		if(aftResult.isExecuted()) {
			if(retVal == null && aftResult.getResult() != null) {
				retVal = aftResult.getResult();
			}
		}
		
		// 6. 결과 건수 리턴
		return this.returnValueToInt(retVal);
	}

	@Override
	public int mergeBatch(JobBatch mainBatch, JobBatch newBatch, Object... params) {
		// 1. 작업 배치 정보로 설비 리스트 조회
		String sql = "select * from racks where domain_id = :domainId and batch_id = :batchId";
		Map<String, Object> queryParams = ValueUtil.newMap("domainId,batchId", mainBatch.getDomainId(), mainBatch.getId());
		List<Rack> rackList = this.queryManager.selectListBySql(sql, queryParams, Rack.class, 0, 0);
		
		// 2. 병합의 경우에는 메인 배치의 설정 셋을 가져온다.
		newBatch.setBatchGroupId(mainBatch.getId());
		newBatch.setEquipGroupCd(mainBatch.getEquipGroupCd());
		newBatch.setEquipType(mainBatch.getEquipType());
		newBatch.setEquipCd(mainBatch.getEquipCd());
		newBatch.setEquipNm(mainBatch.getEquipNm());
		newBatch.setJobConfigSetId(mainBatch.getJobConfigSetId());
		newBatch.setIndConfigSetId(mainBatch.getIndConfigSetId());
		this.queryManager.update(newBatch, "batchGroupId", "equipGroupCd", "equipType", "equipCd", "equipNm", "jobConfigSetId", "indConfigSetId");
		
		// 3. 소분류 코드, 방면 분류 코드 값을 설정에 따라서 주문 정보에 추가한다.
		this.doUpdateClassificationCodes(newBatch, params);

		// 4. 작업 병합 처리
		int retCnt = this.doMergeBatch(mainBatch, newBatch, rackList, params);
		
		// 5. 병합 건수 리턴
		return retCnt;
	}

	@Override
	public int cancelInstructionBatch(JobBatch batch) {
		// 1. 작업 지시 취소 전 처리 이벤트 
		EventResultSet befResult = this.publishInstructionCancelEvent(SysEvent.EVENT_STEP_BEFORE, batch, null);
		
		// 2. 다음 처리 취소일 경우 결과 리턴 
		if(befResult.isAfterEventCancel()) {
			return ValueUtil.toInteger(befResult.getResult());
		}
		
		// 3. 커스텀 서비스 작업 지시 취소 전 처리 호출
		Map<String, Object> svcParams = ValueUtil.newMap("batch", batch);
		this.customService.doCustomService(batch.getDomainId(), DIY_PRE_CANCEL_BATCH, svcParams);
		
		// 4. 작업 지시 취소 조건 체크
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());
		condition.addFilter(new Filter("pickedQty", OrmConstants.GREATER_THAN, 0));
		if(this.queryManager.selectSize(JobInstance.class, condition) > 0) {
			throw ThrowUtil.newValidationErrorWithNoLog(true, "NOT_ALLOWED_CANCEL_AFTER_START_JOB"); // 분류 작업시작 이후여서 취소가 불가능합니다
		}
		
		// 5. 작업 삭제
		Map<String, Object> queryParams = ValueUtil.newMap("domainId,batchId,equipCd", batch.getDomainId(), batch.getId(), batch.getEquipCd());
		String sql = "delete from job_instances where domain_id = :domainId and batch_id = :batchId";
		this.queryManager.executeBySql(sql, queryParams);
		
		// 6. 주문 업데이트
		sql = "update orders set status = null, equip_cd = null, equip_nm = null where domain_id = :domainId and batch_id = :batchId";
		this.queryManager.executeBySql(sql, queryParams);
		
		// 7. 랙 배치 ID 업데이트
		sql = "update racks set batch_id = null, status = null where domain_id = :domainId and rack_cd = :equipCd";
		this.queryManager.executeBySql(sql, queryParams);
		
		// 8. 작업 배치 정보 업데이트
		batch.setEquipGroupCd(null);
		batch.setEquipCd(null);
		batch.setEquipNm(null);
		batch.setInstructedAt(null);
		batch.setStatus(JobBatch.STATUS_READY);
		this.queryManager.update(batch, "equipGroupCd", "equipCd", "equipNm", "status", "instructedAt", "updatedAt");
		
		// 9. 커스텀 서비스 작업 지시 취소 후 처리 호출
		Object retVal = this.customService.doCustomService(batch.getDomainId(), DIY_POST_CANCEL_BATCH, svcParams);
		
		// 10. 작업 지시 취소 후 처리 이벤트
		EventResultSet aftResult = this.publishInstructionCancelEvent(SysEvent.EVENT_STEP_AFTER, batch, null);
		
		// 11. 후 처리 이벤트 실행 후 리턴 결과가 있으면 해당 결과 리턴
		if(aftResult.isExecuted()) {
			if(retVal == null && aftResult.getResult() != null) {
				retVal = aftResult.getResult();
			}
		}
		
		// 12. 결과 리턴
		return this.returnValueToInt(retVal);
	}
	
	/**
	 * 배치에 설정값이 설정되어 있는지 체크하고 기본 설정값으로 설정한다.
	 * 
	 * @param batch
	 * @param rackList
	 */
	private void checkJobConfigSet(JobBatch batch) {
		// 1. 작업 관련 설정이 없는 경우 기본 작업 설정을 찾아서 세팅
		if(ValueUtil.isEmpty(batch.getJobConfigSetId())) {
			JobConfigSet jobConfigSet = this.jobConfigProfileSvc.getStageConfigSet(batch.getDomainId(), batch.getStageCd());
			
			if(jobConfigSet == null) {
				throw ThrowUtil.newJobConfigNotSet();
			} else {
				batch.setJobConfigSetId(jobConfigSet.getId());
				batch.setJobConfigSet(jobConfigSet);
			}
		}
	}
	
	/**
	 * 작업 배치 소속 주문 데이터의 소분류, 방면 분류 코드를 업데이트 ...
	 * 
	 * @param batch
	 * @param params
	 */
	private void doUpdateClassificationCodes(JobBatch batch, Object ... params) {
		// 1. 소분류 매핑 필드 - class_cd 매핑 
		String classTargetField = PdasBatchJobConfigUtil.getBoxMappingTargetField(batch);
		
		if(ValueUtil.isNotEmpty(classTargetField)) {
			String sql = "UPDATE ORDERS SET CLASS_CD = %s WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
			sql = String.format(sql, classTargetField);
			Map<String, Object> updateParams = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
			this.queryManager.executeBySql(sql, updateParams);
			
			// 2. 방면분류 매핑 필드 - box_class_cd 매핑
			String boxClassTargetField = PdasBatchJobConfigUtil.getBoxOutClassTargetField(batch , false);
			
			if(ValueUtil.isNotEmpty(boxClassTargetField)) {
				sql = "UPDATE ORDERS SET BOX_CLASS_CD = %s WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
				sql = String.format(sql, boxClassTargetField);
				this.queryManager.executeBySql(sql, updateParams);
			}
		}
	}
	
	/**
	 * 작업 지시 처리
	 * 
	 * @param batch
	 * @param rackList
	 * @param params
	 * @return
	 */
	private int doInstructBatch(JobBatch batch, List<Rack> rackList, Object ... params) {
		// 1. 전 처리 이벤트
		EventResultSet befResult = this.publishInstructionEvent(SysEvent.EVENT_STEP_BEFORE, batch, rackList, params);
		
		// 2. 다음 처리 취소 일 경우 결과 리턴 
		if(befResult.isAfterEventCancel()) {
			return ValueUtil.toInteger(befResult.getResult());
		}
		
		// 3. 작업 지시 실행
		int resultCnt = this.processInstruction(batch, params);
		
		// 4. 후 처리 이벤트 
		EventResultSet aftResult = this.publishInstructionEvent(SysEvent.EVENT_STEP_AFTER, batch, rackList, params);
		
		// 5. 후 처리 이벤트가 실행 되고 리턴 결과가 있으면 해당 결과 리턴
		if(aftResult.isExecuted()) {
			if(resultCnt == 0 && aftResult.getResult() != null) {
				resultCnt = ValueUtil.toInteger(aftResult.getResult());
			}
		}

		// 6. 결과 리턴
		return resultCnt;
	}
	
	/**
	 * 작업 지시 실행 - 커스텀 서비스 (diy-pdas-pre-instruct-batch, diy-pdas-post-instruct-batch) 연동
	 * 
	 * @param batch
	 * @param params
	 * @return
	 */
	private int processInstruction(JobBatch batch, Object ... params) {
		// 1. 작업 지시 전 처리를 위한 커스텀 서비스 호출
		Long domainId = batch.getDomainId();
		Map<String, Object> svcParams = ValueUtil.newMap("batch,diyParams", batch, params);
		this.customService.doCustomService(domainId, DIY_PRE_BATCH_START, svcParams);

		// 2. 주문 상태 업데이트
		Map<String, Object> queryParams = ValueUtil.newMap("domainId,batchId,status,equipType,equipCd,equipNm", domainId, batch.getId(), Order.STATUS_WAIT, batch.getEquipType(), batch.getEquipCd(), batch.getEquipNm());
		String sql = "update orders set equip_type = :equipType, equip_cd = :equipCd, equip_nm = :equipNm, status = :status where DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
		this.queryManager.executeBySql(sql, queryParams);
		
		// 3. 작업 배치 업데이트
		batch.setStatus(JobBatch.STATUS_RUNNING);
		batch.setInstructedAt(new Date());
		this.queryManager.update(batch, "equipGroupCd", "equipCd", "equipNm", "status", "jobConfigSetId", "instructedAt", "updatedAt");

		// 4. 작업 데이터 생성
		String insertQuery = this.pdasBatchQueryStore.getPdasGenerateInstances();
		this.queryManager.executeBySql(insertQuery, queryParams);
		
		// 5. 셀 - 주문 매핑 정보 초기화
		sql = "update cells set class_cd = null where domain_id = :domainId and equip_type = :equipType and equip_cd = :equipCd";
		this.queryManager.executeBySql(sql, queryParams);
		
		// 6. 작업 지시 후 처리를 위한 커스텀 서비스 호출
		this.customService.doCustomService(domainId, DIY_POST_BATCH_START, svcParams);
		return 1;
	}
	
	/**
	 * 작업 병합 처리
	 * 
	 * @param mainBatch
	 * @param newBatch
	 * @param equipList
	 * @param params
	 * @return
	 */
	private int doMergeBatch(JobBatch mainBatch, JobBatch newBatch, List<?> equipList, Object... params) {
		// 1. 전처리 이벤트
		EventResultSet befResult = this.publishMergingEvent(SysEvent.EVENT_STEP_BEFORE, mainBatch, newBatch, equipList, params);
		
		// 2. 다음 처리 취소 일 경우 결과 리턴
		if(befResult.isAfterEventCancel()) {
			return ValueUtil.toInteger(befResult.getResult());
		}
		
		// 3. 배치 병합 처리
		int resultCnt = this.processMerging(mainBatch, newBatch, params);
		
		// 4. 후처리 이벤트
		EventResultSet aftResult = this.publishMergingEvent(SysEvent.EVENT_STEP_AFTER, mainBatch, newBatch, equipList, params);
		
		// 5. 후처리 이벤트가 실행 되고 리턴 결과가 있으면 해당 결과 리턴
		if(aftResult.isExecuted()) {
			if(resultCnt == 0 && aftResult.getResult() != null) {
				resultCnt = ValueUtil.toInteger(aftResult.getResult());
			}
		}

		// 6. 결과 리턴
		return resultCnt;
	}
	
	/**
	 * 작업 병합 처리
	 * 
	 * @param mainBatch
	 * @param newBatch
	 * @param params
	 * @return
	 */
	private int processMerging(JobBatch mainBatch, JobBatch newBatch, Object ... params) {
		// 1. 병합 전 커스텀 서비스 호출
		Long domainId = mainBatch.getDomainId();
		Map<String, Object> svcParams = ValueUtil.newMap("mainBatch,newBatch", mainBatch, newBatch);
		this.customService.doCustomService(domainId, DIY_PRE_MERGE_BATCH, svcParams);
		
		// 2. 주문 업데이트 (상태, 랙, 배치 ID)
		Map<String, Object> queryParams = ValueUtil.newMap("domainId,batchId,mainBatchId,equipGroupCd,equipType,equipCd,equipNm,status",
				domainId, newBatch.getId(), mainBatch.getId(), mainBatch.getEquipGroupCd(), mainBatch.getEquipType(), mainBatch.getEquipCd(), mainBatch.getEquipNm(), Order.STATUS_WAIT);
		String sql = "update orders set batch_id = :mainBatchId, status = :status, equip_group_cd = :equipGroupCd, equip_type = :equipType, equip_cd = :equipCd, equip_nm = :equipNm where domain_id = :domainId and batch_id = :batchId";
		this.queryManager.executeBySql(sql, queryParams);
		
		// 3. 작업 인스턴스 생성
		Map<String, Object> instanceGenParams = ValueUtil.newMap("domainId,batchId,wmsBatchNo,status,equipType,equipCd,equipNm", domainId, mainBatch.getId(), newBatch.getWmsBatchNo(), Order.STATUS_WAIT, mainBatch.getEquipType(), mainBatch.getEquipCd(), mainBatch.getEquipNm());
		String insertQuery = this.pdasBatchQueryStore.getPdasGenerateInstances();
		this.queryManager.executeBySql(insertQuery, instanceGenParams);
		
		// 4. 병합 후 낱개 피킹 모드이면 작업 데이터를 모두 1PCS로 분리한다.
		if(this.pdasAssortService.isPiecePickingMode(mainBatch)) {
			this.pdasAssortService.splitJobInstancesByPiece(mainBatch);
		}
		
		// 5. 작업 배치 업데이트
		newBatch.setBatchGroupId(mainBatch.getBatchGroupId());
		newBatch.setStatus(JobBatch.STATUS_MERGED);
		newBatch.setInstructedAt(new Date());
		this.queryManager.update(newBatch, "batchGroupId", "status", "instructedAt", "updatedAt");
		
		// 6. 메인 배치 업데이트
		this.updateBatchOrderCount(mainBatch);
		
		// 7. 병합 후 커스텀 서비스 호출
		Object retVal = this.customService.doCustomService(domainId, DIY_POST_MERGE_BATCH, svcParams);
		return this.returnValueToInt(retVal);
	}
	
	/**
	 * 배치 주문 수 업데이트
	 * 
	 * @param batch
	 */
	private void updateBatchOrderCount(JobBatch batch) {
		String sql = this.pdasBatchQueryStore.getPdasUpdateOrderCount();
		this.queryManager.executeBySql(sql, ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId()));
	}
	
	/**
	 * Object to integer
	 * 
	 * @param retVal
	 * @return
	 */
	private int returnValueToInt(Object retVal) {
		return (retVal == null) ? 0 : (retVal instanceof Integer) ? ValueUtil.toInteger(retVal) : 0;
	}
	
	/******************************************************************
	 * 							이벤트 전송
	/******************************************************************/
	
	/**
	 * 토털 피킹 이벤트 전송
	 * 
	 * @param eventStep
	 * @param mainBatch
	 * @param equipList
	 * @param params
	 * @return
	 */
	private EventResultSet publishTotalPickingEvent(short eventStep, JobBatch batch, List<?> equipList, Object... params) {
		return this.publishInstructEvent(EventConstants.EVENT_INSTRUCT_TYPE_TOTAL_PICKING, eventStep, batch, equipList, params);
	}

}
