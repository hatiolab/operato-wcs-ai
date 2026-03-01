package operato.logis.sps.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import operato.logis.sps.SpsConstants;
import operato.logis.sps.query.store.SpsQueryStore;
import operato.logis.sps.service.util.SpsBatchJobConfigUtil;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.event.main.BatchInstructEvent;
import xyz.anythings.base.service.api.IInstructionService;
import xyz.anythings.base.service.impl.AbstractInstructionService;
import xyz.anythings.base.service.impl.JobConfigProfileService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 단포용 작업 지시 서비스
 * 
 * @author shortstop
 */
@Component("spsInstructionService")
public class SpsInstructionService extends AbstractInstructionService implements IInstructionService {

	/**
	 * 커스텀 서비스 - 대상 분류
	 */
	private static final String DIY_CLASSIFY_ORDERS = "diy-sps-classify-orders";
	/**
	 * 커스텀 서비스 - 토털 피킹
	 */
	private static final String DIY_TOTAL_PICKING = "diy-sps-totalpicking";
	/**
	 * 커스텀 서비스 - 작업 지시 전 처리
	 */
	private static final String DIY_PRE_BATCH_START = "diy-sps-pre-batch-start";
	/**
	 * 커스텀 서비스 - 작업 지시 후 처리
	 */
	private static final String DIY_POST_BATCH_START = "diy-sps-post-batch-start";
	/**
	 * 커스텀 서비스 - 배치 병합 전 처리
	 */
	private static final String DIY_PRE_MERGE_BATCH = "diy-sps-pre-merge-batch";
	/**
	 * 커스텀 서비스 - 배치 병합 후 처리
	 */
	private static final String DIY_POST_MERGE_BATCH = "diy-sps-post-merge-batch";
	/**
	 * 커스텀 서비스 - 작업 지시 취소 전 처리
	 */
	private static final String DIY_PRE_CANCEL_BATCH = "diy-sps-pre-cancel-batch";
	/**
	 * 커스텀 서비스 - 작업 지시 취소 후 처리
	 */
	private static final String DIY_POST_CANCEL_BATCH = "diy-sps-post-cancel-batch";
	
	/**
	 * 작업 설정 프로파일 서비스
	 */
	@Autowired
	private JobConfigProfileService jobConfigProfileSvc;
	/**
	 * 단포 쿼리 스토어
	 */
	@Autowired
	private SpsQueryStore spsQueryStore;
	
	/**
	 * WCS 대상 분류 Event 처리 
	 * @param event
	 */
	@EventListener(classes = BatchInstructEvent.class, condition = "#event.eventType == 40 and #event.eventStep == 3 and #event.jobType == 'SPS'")
	public void targetClassing(BatchInstructEvent event) { 
		// 커스텀 서비스 호출
		Map<String, Object> diyParams = ValueUtil.newMap("domainId,waveId,isLast", event.getDomainId(), event.getPayload()[0], event.getPayload()[1]);
		this.customService.doCustomService(event.getDomainId(), DIY_CLASSIFY_ORDERS, diyParams);
		event.setExecuted(true);
	}

	@Override
	public Map<String, Object> searchInstructionData(JobBatch batch, Object... params) {
		// 단포에서는 구현이 필요없음
		return null;
	}

	@Override
	public int instructBatch(JobBatch batch, List<String> equipIdList, Object... params) {
		// 1. 랙 조회
		Rack rack = null;
		if(ValueUtil.isEmpty(equipIdList)) {
			rack = this.queryManager.selectByCondition(Rack.class, ValueUtil.newMap("areaCd,stageCd,equipGroupCd,rackType", batch.getAreaCd(), batch.getStageCd(), batch.getEquipGroupCd(), "O"));
		} else {
			rack = this.queryManager.select(Rack.class, equipIdList.get(0));
		}
		
		// 2. 배치 정보에 설정값이 없는지 체크하여 없으면 설정
		this.checkJobAndIndConfigSet(batch);
		
		// 3. 랙에 배치 할당
		rack.setBatchId(batch.getId());
		rack.setStatus(JobBatch.STATUS_RUNNING);
		this.queryManager.update(rack, "batchId", "status", "updatedAt");
		
		// 4. 소분류 코드, 방면 분류 코드 값을 설정에 따라서 주문 정보에 추가한다.
		this.doUpdateClassificationCodes(batch, params);
		
		// 5. 작업 지시 처리
		int retCnt = this.doInstructBatch(batch, rack, params);
		
		// 6. 건수 리턴
		return retCnt;
	}

	@Override
	public int instructTotalpicking(JobBatch batch, List<String> equipIdList, Object... params) {
		// 1. 랙 조회
		String rackCd = equipIdList.get(0);
		Rack rack = AnyEntityUtil.findEntityBy(batch.getDomainId(), true, Rack.class, "rackCd", rackCd);
		
		// 2. 커스텀 서비스 호출
		Object retVal = this.customService.doCustomService(batch.getDomainId(), DIY_TOTAL_PICKING, ValueUtil.newMap("batch,rack", batch, rack));
		
		// 3. 결과 건수 리턴
		return this.returnValueToInt(retVal);
	}

	@Override
	public int mergeBatch(JobBatch mainBatch, JobBatch newBatch, Object... params) {
		// 1. 작업 배치 정보로 설비 리스트 조회
		Rack rack = AnyEntityUtil.findEntityBy(mainBatch.getDomainId(), true, Rack.class, null, "rackCd", mainBatch.getEquipCd());
		
		// 2. 병합의 경우에는 메인 배치의 설정 셋을 가져온다.
		newBatch.setJobConfigSetId(mainBatch.getJobConfigSetId());
		newBatch.setIndConfigSetId(mainBatch.getIndConfigSetId());
		this.queryManager.update(newBatch , "jobConfigSetId", "indConfigSetId");
		
		// 3. 소분류 코드, 방면 분류 코드 값을 설정에 따라서 주문 정보에 추가한다.
		this.doUpdateClassificationCodes(newBatch, params);
		
		// 4. 대상 분류 
		this.doClassifyOrders(newBatch, rack, params);
		
		// 5. 작업 병합 처리
		int retCnt = this.doMergeBatch(mainBatch, newBatch, rack, params);
		
		// 6. 병합 건수 리턴
		return retCnt;
	}

	@Override
	public int cancelInstructionBatch(JobBatch batch) {
		// 1. 커스텀 서비스 작업 지시 취소 전 처리 호출
		Map<String, Object> svcParams = ValueUtil.newMap("batch", batch);
		this.customService.doCustomService(batch.getDomainId(), DIY_PRE_CANCEL_BATCH, svcParams);
		
		// 2. 작업 지시 취소 조건 체크
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());
		condition.addFilter(new Filter("pickedQty", OrmConstants.GREATER_THAN, 0));
		if(this.queryManager.selectSize(JobInstance.class, condition) > 0) {
			throw ThrowUtil.newValidationErrorWithNoLog(true, "NOT_ALLOWED_CANCEL_AFTER_START_JOB"); // 분류 작업시작 이후여서 취소가 불가능합니다
		}
		
		// 3. 주문 상태 원복
		Map<String, Object> queryParams = 
				ValueUtil.newMap("domainId,batchId,toStatus", batch.getDomainId(), batch.getId(), LogisConstants.EMPTY_STRING);
		String sql = this.spsQueryStore.getUpdateOrderByInstructQuery();
		this.queryManager.executeBySql(sql, queryParams);
		
		// 4. 작업 삭제
		if(!SpsBatchJobConfigUtil.isManualMode(batch)) {
			sql = "delete from job_instances where domain_id = :domainId and batch_id = :batchId";
			this.queryManager.executeBySql(sql, queryParams);
		}
		
		// 5. 랙 배치 ID 업데이트
		queryParams.put("equipCd", batch.getEquipCd());
		sql = "update racks set batch_id = null, status = null where domain_id = :domainId and rack_cd = :equipCd";
		this.queryManager.executeBySql(sql, queryParams);
		
		// 6. 주문의 호기 정보 리셋
		sql = "update orders set equip_cd = null, equip_nm = null, status = null where domain_id = :domainId and batch_id = :batchId";
		this.queryManager.executeBySql(sql, queryParams);
		
		// 7. 작업 배치 정보 업데이트
		batch.setEquipCd(null);
		batch.setEquipNm(null);
		batch.setStatus(JobBatch.STATUS_READY);
		batch.setInstructedAt(null);
		this.queryManager.update(batch, "equipCd", "equipNm", "status", "instructedAt", "updatedAt");
		
		// 8. 커스텀 서비스 작업 지시 취소 후 처리 호출
		Object retVal = this.customService.doCustomService(batch.getDomainId(), DIY_POST_CANCEL_BATCH, svcParams);
		
		// 9. 결과 리턴
		return this.returnValueToInt(retVal);
	}

	/**
	 * 작업 배치 소속 주문 데이터의 소분류, 방면 분류 코드를 업데이트 ...
	 * 
	 * @param batch
	 * @param params
	 */
	private void doUpdateClassificationCodes(JobBatch batch, Object ... params) {
		// 1. 소분류 매핑 필드 - class_cd 매핑
		String classTargetField = SpsBatchJobConfigUtil.getBoxMappingTargetField(batch);
		
		if(ValueUtil.isNotEmpty(classTargetField)) {
			String sql = "UPDATE ORDERS SET CLASS_CD = %s WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
			sql = String.format(sql, classTargetField);
			Map<String, Object> updateParams = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
			this.queryManager.executeBySql(sql, updateParams);
		}

		// 2. 방면분류 매핑 필드 - box_class_cd 매핑
		String boxClassTargetField = SpsBatchJobConfigUtil.getBoxOutClassTargetField(batch , false);
		
		if(ValueUtil.isNotEmpty(boxClassTargetField)) {
			String sql = "UPDATE ORDERS SET BOX_CLASS_CD = %s WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
			sql = String.format(sql, boxClassTargetField);
			Map<String, Object> updateParams = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
			this.queryManager.executeBySql(sql, updateParams);
		}
	}

	/**
	 * 배치에 설정값이 설정되어 있는지 체크하고 기본 설정값으로 설정한다.
	 * 
	 * @param batch
	 */
	private void checkJobAndIndConfigSet(JobBatch batch) {
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
	 * 대상 분류 프로세싱 - 커스텀 서비스 (diy-dps-classify-orders) 연동
	 * 
	 * @param batch
	 * @param params
	 * @return
	 */
	private int doClassifyOrders(JobBatch batch, Object... params) {
		// 1. 단포 대상 분류
		Map<String, Object> queryParams = ValueUtil.newMap("domainId,batchId,orderType", batch.getDomainId(), batch.getId(), SpsConstants.SPS_ORDER_TYPE_OT);
		String sql = this.spsQueryStore.getClassifyOrdersQuery();
		int classifyCount = this.queryManager.executeBySql(sql, queryParams);
		
		// 2. 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_CLASSIFY_ORDERS, ValueUtil.newMap("batch", batch));
		return classifyCount;
	}
	
	/**
	 * 작업 지시 실행 - 커스텀 서비스 (diy-dps-pre-instruct-batch, diy-dps-post-instruct-batch) 연동
	 * 
	 * @param batch
	 * @param rack
	 * @param params
	 * @return
	 */
	private int doInstructBatch(JobBatch batch, Rack rack, Object ... params) {
		// 1. 작업 지시 전 처리를 위한 커스텀 서비스 호출
		Long domainId = batch.getDomainId();
		Map<String, Object> svcParams = ValueUtil.newMap("batch,diyParams", batch, params);
		this.customService.doCustomService(domainId, DIY_PRE_BATCH_START, svcParams);
		
		// 2. 단포 주문 업데이트
		Map<String, Object> queryParams = ValueUtil.newMap("domainId,equipGroupCd,equipCd,equipNm,toStatus,batchId",
				domainId, rack.getEquipGroupCd(), rack.getRackCd(), rack.getRackNm(), Order.STATUS_WAIT, batch.getId());
		String sql = this.spsQueryStore.getUpdateOrderByInstructQuery();
		this.queryManager.executeBySql(sql, queryParams);
		
		// 3. 단포 작업 생성
		if(!SpsBatchJobConfigUtil.isManualMode(batch)) {
			sql = this.spsQueryStore.getCreateJobInstancesQuery();
			this.queryManager.executeBySql(sql, queryParams);
		}
		
		// 4. 작업 배치 업데이트
		batch.setEquipGroupCd(rack.getEquipGroupCd());
		batch.setEquipCd(rack.getRackCd());
		batch.setEquipNm(rack.getRackNm());
		batch.setStatus(JobBatch.STATUS_RUNNING);
		batch.setInstructedAt(new Date());
		this.queryManager.update(batch, "equipGroupCd", "equipCd", "equipNm", "status", "jobConfigSetId", "indConfigSetId", "instructedAt", "updatedAt");
		
		// 5. 작업 지시 후 처리를 위한 커스텀 서비스 호출
		this.customService.doCustomService(domainId, DIY_POST_BATCH_START, svcParams);
		return 1;
	}
	
	/**
	 * 작업 병합 처리
	 * 
	 * @param mainBatch
	 * @param newBatch
	 * @param rack
	 * @param params
	 * @return
	 */
	private int doMergeBatch(JobBatch mainBatch, JobBatch newBatch, Rack rack, Object... params) {
		// 1. 병합 전 커스텀 서비스 호출
		Long domainId = mainBatch.getDomainId();
		Map<String, Object> svcParams = ValueUtil.newMap("mainBatch,newBatch", mainBatch, newBatch);
		this.customService.doCustomService(domainId, DIY_PRE_MERGE_BATCH, svcParams);
		
		// 2. 주문 업데이트
		Map<String, Object> queryParams = ValueUtil.newMap("domainId,batchId,equipGroupCd,equipCd,equipNm,toStatus",
				domainId, newBatch.getId(), mainBatch.getEquipGroupCd(), mainBatch.getEquipCd(), mainBatch.getEquipNm(), Order.STATUS_WAIT);
		String sql = this.spsQueryStore.getUpdateOrderByMergeQuery();
		this.queryManager.executeBySql(sql, queryParams);
		
		// 3. 단포 작업 생성
		if(!SpsBatchJobConfigUtil.isManualMode(mainBatch)) {
			sql = this.spsQueryStore.getCreateJobInstancesQuery();
			this.queryManager.executeBySql(sql, queryParams);
		}
		
		// 4. 병합 주문의 배치 ID를 모두 메인 배치로 변경
		sql = "update orders set batch_id = :mainBatchId where domain_id = :domainId and batch_id = :mergedBatchId";
		Map<String, Object> updateParams = ValueUtil.newMap("domainId,mainBatchId,mergedBatchId", domainId, mainBatch.getId(), newBatch.getId());
		this.queryManager.executeBySql(sql, updateParams);
		
		// 5. 병합 작업의 배치 ID를 모두 메인 배치로 변경
		if(!SpsBatchJobConfigUtil.isManualMode(mainBatch)) {
			sql = "update job_instances set batch_id = :mainBatchId where domain_id = :domainId and batch_id = :mergedBatchId";
			this.queryManager.executeBySql(sql, updateParams);
		}
		
		// 6. 작업 배치 업데이트
		newBatch.setBatchGroupId(mainBatch.getBatchGroupId());
		newBatch.setEquipType(mainBatch.getEquipGroupCd());
		newBatch.setEquipCd(mainBatch.getEquipCd());
		newBatch.setEquipNm(mainBatch.getEquipNm());
		newBatch.setStatus(JobBatch.STATUS_MERGED);
		newBatch.setInstructedAt(new Date());
		this.queryManager.update(newBatch, "batchGroupId", "equipGroupCd", "equipCd", "equipNm", "status", "instructedAt", "updatedAt");
		
		// 7. 메인 배치 주문 수 업데이트
		sql = this.spsQueryStore.getUpdateOrderCountQuery();
		this.queryManager.executeBySql(sql, ValueUtil.newMap("domainId,batchId", mainBatch.getDomainId(), mainBatch.getId()));
		
		// 8. 병합 후 커스텀 서비스 호출
		Object retVal = this.customService.doCustomService(domainId, DIY_POST_MERGE_BATCH, svcParams);
		return this.returnValueToInt(retVal);
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
}
