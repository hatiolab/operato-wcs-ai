package operato.logis.sps.service.impl;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.logis.sps.query.store.SpsQueryStore;
import operato.logis.sps.service.util.SpsBatchJobConfigUtil;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.event.main.BatchCloseEvent;
import xyz.anythings.base.service.api.IBatchService;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 단포 작업 배치 서비스
 * 
 * @author shortstop
 */
@Component("spsBatchService")
public class SpsBatchService extends AbstractLogisService implements IBatchService {

	/**
	 * 커스텀 서비스 - 작업 마감 커스터마이징 처리
	 */
	private static final String DIY_CLOSE_BATCH = "diy-sps-batch-stop";
	/**
	 * 커스텀 서비스 - 배치 취소 커스터마이징 처리
	 */
	private static final String DIY_CANCEL_BATCH = "diy-sps-cancel-batch";
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;
	/**
	 * 단포 쿼리 스토어
	 */
	@Autowired
	private SpsQueryStore spsQueryStore;
	
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

		// 5. batchId별 작업 실행 데이터 체크
		if(!SpsBatchJobConfigUtil.isManualMode(batch)) {
			count = this.queryManager.selectSize(JobInstance.class, condition);
			if(count == 0) {
				// 해당 배치의 작업실행 정보가 없습니다 --> 작업을 찾을 수 없습니다.
				throw ThrowUtil.newNotFoundRecord("terms.label.job");
			}
	
			// 6. batchId별 작업 실행 데이터 중에 완료되지 않은 것이 있는지 체크
			if(!closeForcibly) {
				condition.addFilter("status", OrmConstants.IN, LogisConstants.JOB_STATUS_WIPC);
				if(this.queryManager.selectSize(JobInstance.class, condition) > 0) {
					// {0} 등 {1}개의 호기에서 작업이 끝나지 않았습니다.
					String msg = MessageUtil.getMessage("ASSORTING_NOT_FINISHED_IN_RACKS", "{0} 등 {1}개의 랙에서 작업이 끝나지 않았습니다.", ValueUtil.toList(batch.getEquipCd(), "1"));
					throw ThrowUtil.newValidationErrorWithNoLog(msg);
				}
			}
		}
	}

	@Override
	public void closeBatch(JobBatch batch, boolean forcibly) {
		// 1. 작업 마감 가능 여부 체크 
		this.isPossibleCloseBatch(batch, forcibly);

		// 2. JobBatch 상태 변경
		this.updateJobBatchFinished(batch, new Date());
		
		// 3. 커스텀 서비스 배치 마감 API 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_CLOSE_BATCH, ValueUtil.newMap("batch", batch));
	}

	@Override
	public void isPossibleCloseBatchGroup(Long domainId, String batchGroupId, boolean closeForcibly) {
		// 단포에서는 그룹 마감 필요 없음
	}

	@Override
	public int closeBatchGroup(Long domainId, String batchGroupId, boolean forcibly) {
		// 단포에서는 그룹 마감 필요 없음
		return 0;
	}

	@Override
	public void isPossibleCancelBatch(JobBatch batch) {
		// 1. 커스텀 서비스 작업 지시 취소 전 처리 호출
		Map<String, Object> svcParams = ValueUtil.newMap("batch", batch);
		this.customService.doCustomService(batch.getDomainId(), DIY_CANCEL_BATCH, svcParams);
		
		// 2. 배치 취소 조건 체크
		if(!SpsBatchJobConfigUtil.isManualMode(batch)) {
			Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
			condition.addFilter("batchId", batch.getId());
			condition.addFilter(new Filter("pickedQty", OrmConstants.GREATER_THAN, 0));
			if(this.queryManager.selectSize(JobInstance.class, condition) > 0) {
				throw ThrowUtil.newValidationErrorWithNoLog(true, "NOT_ALLOWED_CANCEL_AFTER_START_JOB"); // 분류 작업시작 이후여서 취소가 불가능합니다
			}
		}
	}

	@Override
	public void isPossibleChangeEquipment(JobBatch batch, String toEquipCd) {
		
		Long domainId = batch.getDomainId();
		
		// 1. 타겟 배치의 호기를 조회
		Rack targetRack = AnyEntityUtil.findEntityBy(domainId, true, Rack.class, "rackCd", toEquipCd);
		if(ValueUtil.isNotEmpty(targetRack.getBatchId())) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("BATCH_IS_ALREADY_IN_THE_RACK"));
		}
		
		// 2. 소스 배치의 호기를 조회
		Rack sourceRack = AnyEntityUtil.findEntityBy(domainId, true, Rack.class, "rackCd", batch.getEquipCd());
		
		// 3. 소스, 타겟 배치의 호기 유형이 같은지 체크
		if(ValueUtil.isNotEqual(targetRack.getRackType(), sourceRack.getRackType())) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("DIFFERENT_SOURCE_TARGET_RACK"));
		}
		
		// 4. 분류 시작 여부 체크
		if(!SpsBatchJobConfigUtil.isManualMode(batch)) {
			String sql = "select count(id) from job_instances where domain_id = :domainId and batch_id = :batchId and picked_qty > 0";
			int count = this.queryManager.selectSizeBySql(sql, ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId()));
		
			if(count > 0) {
				throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NOT_POSSIBLE_CHANGE_FACILITIES_WORK_START"));
			}
		}
	}

	@Override
	public void changeEquipment(JobBatch batch, String toEquipCd) {
		
		Long domainId = batch.getDomainId();
		
		// 1. 소스 랙의 배치 정보 리셋
		Rack sourceRack = AnyEntityUtil.findEntityBy(domainId, true, Rack.class, "rackCd", batch.getEquipCd());
		sourceRack.setBatchId(null);
		sourceRack.setStatus(null);
		this.queryManager.update(sourceRack, "batchId", "status", "updatedAt");
		
		// 2. 타겟 랙의 배치 정보 업데이트
		Rack targetRack = AnyEntityUtil.findEntityBy(domainId, true, Rack.class, "rackCd", toEquipCd);
		targetRack.setBatchId(batch.getId());
		targetRack.setStatus(JobBatch.STATUS_RUNNING);
		this.queryManager.update(targetRack, "batchId", "status", "updatedAt");
		
		// 3. 작업 정보 업데이트
		if(!SpsBatchJobConfigUtil.isManualMode(batch)) {
			String sql = "update job_instances set equip_cd = :newEquipCd where domain_id = :domainId and batch_id = :batchId";
			this.queryManager.executeBySql(sql, ValueUtil.newMap("batchId,newEquipCd", batch.getId(), toEquipCd));
		}
		
		// 4. 배치 정보 업데이트
		batch.setEquipCd(toEquipCd);
		this.queryManager.update(batch, "equipCd");
	}

	/**
	 * 작업 배치를 마감 처리
	 * 
	 * @param batch
	 * @param finishedAt
	 */
	protected void updateJobBatchFinished(JobBatch batch, Date finishedAt) {
		// 1. 파라미터 
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		
		// 2. 작업 배치에 최종 실적 결과 업데이트
		if(SpsBatchJobConfigUtil.isManualMode(batch)) {
			// 2.1 매뉴얼인 경우 모두 작업 한 걸로 처리
			batch.setResultOrderQty(batch.getBatchOrderQty());
			batch.setResultBoxQty(batch.getBatchOrderQty());
			batch.setResultSkuQty(batch.getBatchSkuQty());
			batch.setResultPcs(batch.getBatchPcs());
			batch.setProgressRate(100.0f);
			params.put("manualMode", true);
		} else {
			// 2.2 배치 마감을 위한 물량 주문 대비 최종 실적 요약 정보 조회
			String query = this.spsQueryStore.getBatchProgressRateQuery();
			JobBatch batchResult = this.queryManager.selectBySql(query, params, JobBatch.class);
			batch.setResultOrderQty(batchResult.getResultOrderQty());
			batch.setResultBoxQty(batchResult.getResultBoxQty());
			batch.setResultSkuQty(batchResult.getResultSkuQty());
			batch.setResultPcs(batchResult.getResultPcs());
			batch.setProgressRate(batchResult.getProgressRate());
			params.put("pickingMode", true);
		}
		
		// 3. 배치 마감을 위한 UPH, 설비가동시간 조회
		String query = this.spsQueryStore.getBatchResultSummaryQuery();
		JobBatch finalResult = this.queryManager.selectBySql(query, params, JobBatch.class);
		batch.setUph(finalResult.getUph());
		batch.setEquipRuntime(finalResult.getEquipRuntime());
		batch.setStatus(JobBatch.STATUS_END);
		batch.setFinishedAt(finishedAt);
		
		// 4. 배치 업데이트
		this.queryManager.update(batch, "resultOrderQty", "resultBoxQty", "resultSkuQty", "resultPcs", "progressRate", "uph", "equipRuntime", "status", "finishedAt");
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
