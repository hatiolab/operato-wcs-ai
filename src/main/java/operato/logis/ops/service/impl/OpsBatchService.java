package operato.logis.ops.service.impl;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.logis.ops.query.store.OpsQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.event.main.BatchCloseEvent;
import xyz.anythings.base.service.api.IBatchService;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 오더 피킹 배치 서비스
 * 
 * @author shortstop
 */
@Component("opsBatchService")
public class OpsBatchService extends AbstractLogisService implements IBatchService {

	/**
	 * 커스텀 서비스 - 작업 완료 전 처리
	 */
	private static final String DIY_PRE_BATCH_STOP = "diy-ops-pre-batch-stop";
	/**
	 * 커스텀 서비스 - 작업 완료 후 처리
	 */
	private static final String DIY_POST_BATCH_STOP = "diy-ops-post-batch-stop";
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;
	/**
	 * 쿼리 스토어
	 */
	@Autowired
	private OpsQueryStore opsQueryStore;
	
	@Override
	public void isPossibleCloseBatch(JobBatch batch, boolean closeForcibly) {
		// 1. 배치 마감 전 처리 이벤트 전송
		BatchCloseEvent event = new BatchCloseEvent(batch, SysEvent.EVENT_STEP_BEFORE);
		event = (BatchCloseEvent)this.eventPublisher.publishEvent(event);
		
		// 2. 이벤트 취소라면 ...
		if(event.isAfterEventCancel()) {
			return;
		}
		
		// 3. 작업 배치 상태 체크
		if(ValueUtil.isNotEqual(batch.getStatus(), JobBatch.STATUS_RUNNING)) {
			// 진행 중인 작업배치가 아닙니다
			throw ThrowUtil.newStatusIsNotIng("terms.label.job_batch");
		}

		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter(new Filter("batchId", batch.getId()));

		// 4. batchId별 수신 주문이 존재하는지 체크
		int count = this.queryManager.selectSize(Order.class, condition);
		if(count == 0) {
			// 해당 배치의 주문정보가 없습니다 --> 주문을 찾을 수 없습니다.
			throw ThrowUtil.newNotFoundRecord("terms.label.order");
		}

		// 5. batchId별 작업 실행 데이터 중에 완료되지 않은 것이 있는지 체크
		if(!closeForcibly) {
			condition.addFilter("status", LogisConstants.JOB_STATUS_WAIT);
			if(this.queryManager.selectSize(Order.class, condition) > 0) {
				throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("WORK_IS_NOT_FINISHED_CONFIRM_CLOSE"));
			}
		}
		
		// 7. 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_PRE_BATCH_STOP, ValueUtil.newMap("batch", batch));
	}

	@Override
	public void closeBatch(JobBatch batch, boolean forcibly) {
		// 1. 작업 마감 가능 여부 체크 
		this.isPossibleCloseBatch(batch, forcibly);

		// 2. 배치 마감 후 처리 이벤트 전송
		BatchCloseEvent event = new BatchCloseEvent(batch, SysEvent.EVENT_STEP_AFTER);
		event = (BatchCloseEvent)this.eventPublisher.publishEvent(event);
		
		// 3. 이벤트 취소라면 ...
		if(event.isAfterEventCancel()) {
			return;
		}
		
		// 4. 배치 수량 업데이트
		//String sql = "update orders set result_order_qty = batch_order_qty, result_sku_qty = batch_sku_qty, result_pcs = batch_pcs where domain_id = :domainId and batch_id = :batchId";
		//Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		//this.queryManager.executeBySql(sql, params);
		
		// 5. JobBatch 상태 변경
		this.updateJobBatchFinished(batch, new Date());
		
		// 6. 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_POST_BATCH_STOP, ValueUtil.newMap("batch", batch));
	}

	@Override
	public void isPossibleCloseBatchGroup(Long domainId, String batchGroupId, boolean closeForcibly) {
	}

	@Override
	public int closeBatchGroup(Long domainId, String batchGroupId, boolean forcibly) {
		return 0;
	}

	@Override
	public void isPossibleCancelBatch(JobBatch batch) {
	}
	
	/**
	 * 작업 배치를 마감 처리
	 * 
	 * @param batch
	 * @param finishedAt
	 */
	protected void updateJobBatchFinished(JobBatch batch, Date finishedAt) {
		// 작업 배치 수량 업데이트
		batch.setResultOrderQty(batch.getBatchOrderQty());
		batch.setResultBoxQty(batch.getBatchOrderQty());
		batch.setResultSkuQty(batch.getBatchSkuQty());
		batch.setResultPcs(batch.getBatchPcs());
		batch.setLastInputSeq(batch.getBatchSkuQty());
		batch.setProgressRate(100.0f);
		this.queryManager.update(batch, "resultOrderQty", "resultBoxQty", "resultSkuQty", "lastInputSeq", "resultPcs", "progressRate");
		
		// 배치 마감을 위한 물량 주문 대비 최종 실적 요약 정보 조회
		String query = this.opsQueryStore.getBatchSummaryByClosingQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		JobBatch finalResult = this.queryManager.selectBySql(query, params, JobBatch.class);
		
		// 작업 배치에 최종 결과 업데이트
		batch.setUph(finalResult.getUph());
		batch.setEquipRuntime(finalResult.getEquipRuntime());
		batch.setStatus(JobBatch.STATUS_END);
		batch.setFinishedAt(finishedAt);
		this.queryManager.update(batch, "uph", "equipRuntime", "status", "finishedAt");
	}

	@Override
	public void isPossibleChangeEquipment(JobBatch batch, String toEquipCd) {
		throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NOT_SUPPORTED_METHOD"));
	}

	@Override
	public void changeEquipment(JobBatch batch, String toEquipCd) {
		throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NOT_SUPPORTED_METHOD"));
	}

	@Override
	public void isPossiblePauseBatch(JobBatch batch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pauseBatch(JobBatch batch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void isPossibleResumeBatch(JobBatch batch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resumeBatch(JobBatch batch) {
		// TODO Auto-generated method stub
		
	}

}
