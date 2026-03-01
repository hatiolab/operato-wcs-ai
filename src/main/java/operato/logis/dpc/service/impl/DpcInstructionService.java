package operato.logis.dpc.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import operato.logis.dpc.query.store.DpcBatchQueryStore;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.event.main.BatchInstructEvent;
import xyz.anythings.base.service.api.IInstructionService;
import xyz.anythings.base.service.impl.AbstractInstructionService;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * DPC 작업지시 서비스
 * 
 * @author shortstop
 */
@Component("dpcInstructionService")
public class DpcInstructionService extends AbstractInstructionService implements IInstructionService {
	
	/**
	 * 커스텀 서비스 - 대상 분류
	 */
	private static final String DIY_CLASSIFY_ORDERS = "diy-dpc-classify-orders";
	/**
	 * 커스텀 서비스 - 작업 지시 전 처리
	 */
	private static final String DIY_PRE_BATCH_START = "diy-dpc-pre-batch-start";
	/**
	 * 커스텀 서비스 - 작업 지시 후 처리
	 */
	private static final String DIY_POST_BATCH_START = "diy-dpc-post-batch-start";
	/**
	 * 커스텀 서비스 - 배치 병합 전 처리
	 */
	private static final String DIY_PRE_MERGE_BATCH = "diy-dpc-pre-merge-batch";
	/**
	 * 커스텀 서비스 - 배치 병합 후 처리
	 */
	private static final String DIY_POST_MERGE_BATCH = "diy-dpc-post-merge-batch";
	/**
	 * 커스텀 서비스 - 작업 지시 취소 전 처리
	 */
	private static final String DIY_PRE_CANCEL_BATCH = "diy-dpc-pre-cancel-batch";
	/**
	 * 커스텀 서비스 - 작업 지시 취소 후 처리
	 */
	private static final String DIY_POST_CANCEL_BATCH = "diy-dpc-post-cancel-batch";
	
	/**
	 * DPC 쿼리 스토어
	 */
	@Autowired
	private DpcBatchQueryStore dpcBatchQueryStore;
	
	/**
	 * WCS 대상 분류 Event 처리 
	 * @param event
	 */
	@EventListener(classes = BatchInstructEvent.class, condition = "#event.eventType == 40 and #event.eventStep == 3 and  #event.jobType == 'DPC' ")
	public void targetClassing(BatchInstructEvent event) { 
		// 커스텀 서비스 호출
		Map<String, Object> diyParams = ValueUtil.newMap("domainId,waveId,isLast", event.getDomainId(), event.getPayload()[0], event.getPayload()[1]);
		this.customService.doCustomService(event.getDomainId(), DIY_CLASSIFY_ORDERS, diyParams);
		event.setExecuted(true);
	}
	
	@Override
	public Map<String, Object> searchInstructionData(JobBatch batch, Object... params) {
		return null;
	}
	
	@Override
	public int instructBatch(JobBatch batch, List<String> equipCdList, Object... params) {
		int instructCount = 0;
		
		if(this.beforeInstructBatch(batch, equipCdList)) {
			instructCount += this.doInstructBatch(batch, equipCdList);
			this.afterInstructBatch(batch, equipCdList);
		}
		
		return instructCount;
	}

	@Override
	public int instructTotalpicking(JobBatch batch, List<String> equipIdList, Object... params) {
		// 출고에서는 사용 안 함
		return 0;
	}

	@Override
	public int mergeBatch(JobBatch mainBatch, JobBatch newBatch, Object... params) {
		
		int mergeCount = this.beforeMergeBatch(mainBatch, newBatch, params);
		this.doMergeBatch(mainBatch, newBatch, params);
		this.afterMergeBatch(mainBatch, newBatch, params);
		return mergeCount;
	}
	
	/**
	 * 배치 병합 전 체크
	 * 
	 * @param mainBatch
	 * @param newBatch
	 * @param params
	 * @return
	 */
	private int beforeMergeBatch(JobBatch mainBatch, JobBatch newBatch, Object... params) {
		
		// 1. 병합 전 커스텀 처리
		this.customService.doCustomService(mainBatch.getDomainId(), DIY_PRE_MERGE_BATCH, ValueUtil.newMap("mainBatch,newBatch", mainBatch, newBatch));
		
		// 2. 메인 배치 상태 체크
		if(ValueUtil.isNotEqual(mainBatch.getStatus(), JobBatch.STATUS_RUNNING)) {
			// 메인 작업배치가 진행 중인 상태에서만 병합 가능합니다
			throw ThrowUtil.newValidationErrorWithNoLog(true, "ALLOWED_MERGE_WHEN_MAIN_BATCH_RUN");
		}
		
		// 3. 병합 배치 상태 체크
		if(ValueUtil.isNotEqual(newBatch.getStatus(), JobBatch.STATUS_WAIT) && ValueUtil.isNotEqual(newBatch.getStatus(), JobBatch.STATUS_READY)) {
			// 병합 대상 작업배치가 주문가공대기 상태에서만 가능합니다
			throw ThrowUtil.newValidationErrorWithNoLog(true, "ALLOWED_MERGE_WHEN_TARGET_BATCH_WAIT");
		}
		
		// 4. 병합할 신규 셀 수
		return 0;
	}
	
	/**
	 * 배치 병합 처리
	 * 
	 * @param mainBatch
	 * @param newBatch
	 * @param params
	 */
	private void doMergeBatch(JobBatch mainBatch, JobBatch newBatch, Object... params) {
		
		// 1. 작업 인스턴스 정보 생성
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId", newBatch.getDomainId(), newBatch.getId());
		String sql = this.dpcBatchQueryStore.getGenerateJobsByInstructionQuery();
		this.queryManager.executeBySql(sql, condition);
		
		// 2. 작업 인스턴스 정보 업데이트
		condition = ValueUtil.newMap("domainId,batchId,mainBatchId", newBatch.getDomainId(), newBatch.getId(), mainBatch.getId());
		sql = "UPDATE JOB_INSTANCES SET BATCH_ID = :mainBatchId WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
		this.queryManager.executeBySql(sql, condition);
		
		// 3. 병합하려는 배치의 주문을 메인 배치로 병합
		sql = "UPDATE ORDERS SET BATCH_ID = :mainBatchId WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
		this.queryManager.executeBySql(sql, condition);
		
		// 4. 작업 배치 업데이트
		newBatch.setBatchGroupId(mainBatch.getBatchGroupId());
		newBatch.setStatus(JobBatch.STATUS_MERGED);
		newBatch.setInstructedAt(new Date());
		this.queryManager.update(newBatch, "batchGroupId", "status", "instructedAt", "updatedAt");
		
		// 5. 메인 배치 업데이트
		sql = this.dpcBatchQueryStore.getUpdateOrderCount();
		this.queryManager.executeBySql(sql, ValueUtil.newMap("domainId,batchId", mainBatch.getDomainId(), mainBatch.getId()));
	}
	
	/**
	 * 배치 병합 후 처리
	 * 
	 * @param mainBatch
	 * @param newBatch
	 * @param params
	 */
	private void afterMergeBatch(JobBatch mainBatch, JobBatch newBatch, Object... params) {
		// 1. 병합 후 처리 커스텀 서비스 호출
		this.customService.doCustomService(mainBatch.getDomainId(), DIY_POST_MERGE_BATCH, ValueUtil.newMap("mainBatch,newBatch", mainBatch, newBatch));
		
		// 2. 작업 병합 이벤트 전송
		this.publishMergingEvent(SysEvent.EVENT_STEP_AFTER, mainBatch, newBatch, null);
	}

	@Override
	public int cancelInstructionBatch(JobBatch batch) {
		int cancelCount = 0;
		
		if(this.beforeCancelInstructionBatch(batch)) {
			cancelCount += this.doCancelInstructionBatch(batch);
			this.afterCancelInstructionBatch(batch);
		}
		
		return cancelCount;
	}
	
	/**
	 * 작업 취소 전 처리 액션
	 * 
	 * @param batch
	 * @return
	 */
	protected boolean beforeCancelInstructionBatch(JobBatch batch) {
		
		// 1. 배치 취소 전 처리 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_PRE_CANCEL_BATCH, ValueUtil.newMap("batch", batch));
		
		// 2. 하나라도 처리되었다면 취소 불가
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());
		condition.addFilter(new Filter("pickedQty", OrmConstants.GREATER_THAN, 0));
		
		if(this.queryManager.selectSize(JobInstance.class, condition) > 0) {
			// 분류 작업 시작 이후여서 취소가 불가능합니다
			throw ThrowUtil.newValidationErrorWithNoLog(true, "NOT_ALLOWED_CANCEL_AFTER_START_JOB");
		}
		
		return true;
	}
	
	/**
	 * 작업 취소 처리 로직
	 * 
	 * @param batch
	 * @return
	 */
	protected int doCancelInstructionBatch(JobBatch batch) {
		// 1. 작업 인스턴스 삭제
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		this.queryManager.deleteList(JobInstance.class, condition);
		
		// 2. 주문 정보 업데이트
		String sql = "UPDATE ORDERS SET EQUIP_CD = null, SUB_EQUIP_CD = null, BOX_ID = null, STATUS = null, PICKED_QTY = 0 WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
		this.queryManager.executeBySql(sql, condition);
		
		// 3. 작업 배치 정보 업데이트
		batch.setStatus(JobBatch.STATUS_READY);
		batch.setInstructedAt(null);
		this.queryManager.update(batch, "status", "instructedAt");
		
		// 4. WorkCell Reset
		sql = "UPDATE WORK_CELLS SET COM_Cd = null, CLASS_CD = null, BOX_ID = null, LAST_JOB_CD = null, LAST_PICKED_QTY = 0, JOB_INSTANCE_ID = null, STATUS = null WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
		this.queryManager.executeBySql(sql, condition);
		
		// 5. Cell Reset
		sql = "UPDATE CELLS SET CLASS_CD = null WHERE DOMAIN_ID = :domainId AND EQUIP_CD IN (SELECT RACK_CD FROM RACKS WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId)";
		this.queryManager.executeBySql(sql, condition);
		
		// 6. Rack Reset
		sql = "UPDATE RACKS SET STATUS = null, BATCH_ID = null WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
		this.queryManager.executeBySql(sql, condition);
		return 0;
	}
	
	/**
	 * 작업 취소 후 처리 액션
	 * 
	 * @param batch
	 * @return
	 */
	protected void afterCancelInstructionBatch(JobBatch batch) {
		// 1. 배치 취소 후 처리 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_POST_CANCEL_BATCH, ValueUtil.newMap("batch", batch));
		
		// 2. 작업 지시 취소 이벤트 전송
		this.publishInstructionCancelEvent(SysEvent.EVENT_STEP_AFTER, batch, null);
	}
	
	/**
	 * 작업 지시 전 처리 액션
	 *
	 * @param batch
	 * @param rackList
	 * @return
	 */
	protected boolean beforeInstructBatch(JobBatch batch, List<String> equipIdList) {
		// 1. 배치 시작 전 처리 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_PRE_BATCH_START, ValueUtil.newMap("batch", batch));

		// 2. 배치 상태가 작업 지시 상태인지 체크
		if(ValueUtil.isNotEqual(batch.getStatus(), JobBatch.STATUS_READY)) {
			// '작업 지시 대기' 상태가 아닙니다
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getTerm("terms.text.is_not_wait_state", "JobBatch status is not 'READY'"));
		}
		return true;
	}
	
	/**
	 * 작업 지시 처리 로직
	 *
	 * @param batch
	 * @param rackList
	 * @return
	 */
	protected int doInstructBatch(JobBatch batch, List<String> rackList) {
		// 1. 작업 인스턴스 정보 생성
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		String insertQuery = this.dpcBatchQueryStore.getGenerateJobsByInstructionQuery();
		int jobCnt = this.queryManager.executeBySql(insertQuery, params);
		
		// 2. 배치 상태 업데이트 
		batch.setStatus(JobBatch.STATUS_RUNNING);
		batch.setInstructedAt(new Date());
		this.queryManager.update(batch, "status", "instructedAt");
		
		// 3. 생성된 작업 인스턴스 개수 리턴
		return jobCnt;
	}
	
	/**
	 * 작업 지시 후 처리 액션
	 *
	 * @param batch
	 * @param rackList
	 * @return
	 */
	protected boolean afterInstructBatch(JobBatch batch, List<String> equipIdList) {
		// 1. 배치 시작 액션 처리 
		this.serviceDispatcher.getAssortService(batch).batchStartAction(batch);
		
		// 2. 배치 시작 후 처리 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_POST_BATCH_START, ValueUtil.newMap("batch", batch));
		
		// 3. 작업 지시 이벤트 전송
		this.publishInstructionEvent(SysEvent.EVENT_STEP_AFTER, batch, equipIdList);
		return true;
	}
}
