package operato.logis.dps.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.logis.dps.query.store.DpsBatchQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.event.main.BatchCloseEvent;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.anythings.base.service.api.IBatchService;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * DPS 작업 배치 서비스
 * 
 * @author shortstop
 */
@Component("dpsBatchService")
public class DpsBatchService extends AbstractLogisService implements IBatchService {

	/**
	 * 커스텀 서비스 - 작업 완료 전 처리
	 */
	private static final String DIY_PRE_BATCH_STOP = "diy-dps-pre-batch-stop";
	/**
	 * 커스텀 서비스 - 작업 완료 후 처리
	 */
	private static final String DIY_POST_BATCH_STOP = "diy-dps-post-batch-stop";
	/**
	 * 커스텀 서비스 - 작업 일시 중지 전 처리
	 */
	private static final String DIY_PRE_BATCH_PAUSE = "diy-dps-pre-batch-pause";
	/**
	 * 커스텀 서비스 - 작업 일시 중지 후 처리
	 */
	private static final String DIY_POST_BATCH_PAUSE = "diy-dps-post-batch-pause";
	/**
	 * 커스텀 서비스 - 작업 재시작 전 처리
	 */
	private static final String DIY_PRE_BATCH_RESUME = "diy-dps-pre-batch-resume";
	/**
	 * 커스텀 서비스 - 작업 재시작 후 처리
	 */
	private static final String DIY_POST_BATCH_RESUME = "diy-dps-post-batch-resume";
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;
	/**
	 * DPS 쿼리 스토어
	 */
	@Autowired
	private DpsBatchQueryStore dpsQueryStore;
		
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
		
		// 4. 재고 리셋
		this.resetFreeCells(batch);
		
		// 5. 해당 배치에 대한 고정식이 아닌 호기들에 소속된 로케이션을 모두 찾아서 리셋
		this.resetRacksAndWorkCells(batch);

		// 6. 주문 가공 정보 삭제, 현재 DPS에서 주문 가공 정보 사용 안 함
		this.deletePreprocess(batch);

		// 7. JobBatch 상태 변경
		this.updateJobBatchFinished(batch, new Date());
		
		// 8. 분류 서비스 배치 마감 API 호출
		this.serviceDispatcher.getClassificationService(batch).batchCloseAction(batch);
		
		// 9. 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_POST_BATCH_STOP, ValueUtil.newMap("batch", batch));
	}

	@Override
	public void isPossibleCloseBatchGroup(Long domainId, String batchGroupId, boolean closeForcibly) {
		// 1. 작업 배치 상태 체크
		String sql = "select id from job_batches where domain_id = :domainId and batch_group_id = :batchGroupId and status = :runStatus";
		Map<String, Object> params = ValueUtil.newMap("domainId,batchGroupId,runStatus", domainId, batchGroupId, JobBatch.STATUS_RUNNING);
		int runCount = this.queryManager.selectSizeBySql(sql, params);
		
		if(runCount == 0) {
			// 진행 중인 작업배치가 아닙니다
			throw ThrowUtil.newStatusIsNotIng("terms.label.job_batch");
		}

		// 2. batchId별 작업 실행 데이터 중에 완료되지 않은 것이 있는지 체크
		if(!closeForcibly) {
			// batchId별 작업 실행 데이터 체크
			sql = "select distinct equip_cd from job_instances where domain_id = :domainId and batch_id in (select id from job_batches where domain_id = :domainId and batch_group_id = :batchGroupId) and status in (:statuses) order by equip_cd asc";
			params.put("statuses", LogisConstants.JOB_STATUS_WIPC);
			List<String> equipCdList = this.queryManager.selectListBySql(sql, params, String.class, 0, 0);
			
			// batchId별 작업 실행 데이터 체크
			if(!equipCdList.isEmpty()) {
				// {0} 등 {1}개의 호기에서 작업이 끝나지 않았습니다.
				String msg = MessageUtil.getMessage("ASSORTING_NOT_FINISHED_IN_RACKS", "{0} 등 {1}개의 호기에서 작업이 끝나지 않았습니다.", ValueUtil.toList(equipCdList.get(0), ValueUtil.toString(equipCdList.size())));
				throw ThrowUtil.newValidationErrorWithNoLog(msg);
			}
		}
	}

	@Override
	public int closeBatchGroup(Long domainId, String batchGroupId, boolean forcibly) {
		String sql = "select id from job_batches where domain_id = :domainId and batch_group_id = :batchGroupId and status = :runStatus";
		Map<String, Object> params = ValueUtil.newMap("domainId,batchGroupId,runStatus", domainId, batchGroupId, JobBatch.STATUS_RUNNING);
		List<JobBatch> batchList = this.queryManager.selectListBySql(sql, params, JobBatch.class, 0, 0);
		
		for(JobBatch batch : batchList) {
			this.closeBatch(batch, forcibly);
		}
		
		return 0;
	}

	@Override
	public void isPossibleCancelBatch(JobBatch batch) {
		// 작업 지시 취소 조건 체크
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());
		condition.addFilter(new Filter("pickedQty", OrmConstants.GREATER_THAN, 0));
		
		if(this.queryManager.selectSize(JobInstance.class, condition) > 0) {
			// 분류 작업시작 이후여서 취소가 불가능합니다
			throw ThrowUtil.newValidationErrorWithNoLog(true, "MPS_NOT_ALLOWED_CANCEL_AFTER_START_JOB");
		}
	}

	/**
	 * 해당 배치의 랙, 작업 셀 정보 리셋
	 *
	 * @param batch
	 */
	protected void resetRacksAndWorkCells(JobBatch batch) {
		Map<String, Object> params = ValueUtil.newMap("domainId,equipCd,batchId", batch.getDomainId(), batch.getEquipCd(), batch.getId());
		this.queryManager.executeBySql("UPDATE STATIONS SET CLASS_CD = null WHERE DOMAIN_ID = :domainId AND EQUIP_CD IN (SELECT RACK_CD FROM RACKS WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId)", params);
	  	this.queryManager.executeBySql("UPDATE RACKS SET STATUS = null, BATCH_ID = null WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId", params);
	  	this.queryManager.executeBySql("DELETE FROM WORK_CELLS WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId", params);
	}
	
	/**
	 * 해당 배치의 Free Cell 정보 리셋
	 *
	 * @param batch
	 */
	protected void resetFreeCells(JobBatch batch) {
		// 1. 일시 중지 상태인 배치의 랙 조회  
		String sql = "SELECT DISTINCT EQUIP_CD AS EQUIP_CD FROM JOB_BATCHES WHERE DOMAIN_ID = :domainId AND JOB_TYPE = :jobType AND STATUS = :pausedStatus AND EQUIP_CD IN (SELECT RACK_CD FROM RACKS WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId)";
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,jobType,pausedStatus,fixedFlag", batch.getDomainId(), batch.getId(), batch.getJobType(), JobBatch.STATUS_PAUSED, false);
		List<String> pausedRacks = this.queryManager.selectListBySql(sql, params, String.class, 0, 0);
		
		// 2. 일시 중지 상태 호기는 재고 초기화 대상에서 제외
		if(ValueUtil.isNotEmpty(pausedRacks)) {
			params.put("exceptRacks", pausedRacks);
		}
		
		// 3. 프리 셀 리셋 처리
		sql = "UPDATE STOCKS SET FIXED_FLAG = :fixedFlag, COM_CD = null, SKU_CD = null, SKU_BARCD = null, SKU_NM = null, STOCK_QTY = 0, LOAD_QTY = 0, ALLOC_QTY = 0, PICKED_QTY = 0, LAST_TRAN_CD = null WHERE DOMAIN_ID = :domainId AND EQUIP_CD in (select rack_cd from racks where domain_id = :domainId and batch_id = :batchId) AND (FIXED_FLAG IS NULL OR FIXED_FLAG = :fixedFlag) #if($exceptRacks) AND EQUIP_CD NOT IN (:exceptRacks) #end ";
		this.queryManager.executeBySql(sql, params);

		// 4. 고정 셀 리셋 처리
		params.put("fixedFlag", true);
		sql = "UPDATE STOCKS SET STOCK_QTY = CASE WHEN ((LOAD_QTY - PICKED_QTY) < 0) THEN 0 ELSE LOAD_QTY - PICKED_QTY END, LOAD_QTY = CASE WHEN ((LOAD_QTY - PICKED_QTY) < 0) THEN 0 ELSE LOAD_QTY - PICKED_QTY END, ALLOC_QTY = 0, PICKED_QTY = 0, LAST_TRAN_CD = null WHERE DOMAIN_ID = :domainId AND EQUIP_CD in (select rack_cd from racks where domain_id = :domainId and batch_id = :batchId) AND FIXED_FLAG = :fixedFlag #if($exceptRacks) AND EQUIP_CD NOT IN (:exceptRacks) #end ";
		this.queryManager.executeBySql(sql, params);
	}
	
	/**
	 * 주문 가공 정보를 모두 삭제한다.
	 *
	 * @param batch
	 * @return
	 */
	protected void deletePreprocess(JobBatch batch) {
		this.queryManager.executeBySql("DELETE FROM ORDER_PREPROCESSES WHERE BATCH_ID = :batchId", ValueUtil.newMap("batchId", batch.getId()));
	}
	
	/**
	 * 작업 배치를 마감 처리
	 * 
	 * @param batch
	 * @param finishedAt
	 */
	protected void updateJobBatchFinished(JobBatch batch, Date finishedAt) {
		// 배치 마감을 위한 물량 주문 대비 최종 실적 요약 정보 조회
		String query = this.dpsQueryStore.getDpsBatchResultSummaryQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		BatchProgressRate finalResult = this.queryManager.selectBySql(query, params, BatchProgressRate.class);
		
		// 작업 배치에 최종 결과 업데이트
		batch.setResultPcs(finalResult.getActualPcs());
		batch.setResultOrderQty(finalResult.getActualOrder());
		batch.setResultBoxQty(finalResult.getActualSku());
		batch.setUph(finalResult.getUph());
		batch.setProgressRate(finalResult.getRateOrder());
		batch.setEquipRuntime(finalResult.getRateSku());
		batch.setStatus(JobBatch.STATUS_END);
		batch.setFinishedAt(finishedAt);
		this.queryManager.update(batch, "resultOrderQty", "resultBoxQty", "resultPcs", "progressRate", "uph", "equipRuntime", "status", "finishedAt");
	}

	@Override
	public void isPossibleChangeEquipment(JobBatch batch, String toEquipCd) {
		// 1. 배치 상태 체크
		String status = batch.getStatus();
		if(ValueUtil.isNotEqual(status, JobBatch.STATUS_WAIT) && ValueUtil.isNotEqual(status, JobBatch.STATUS_READY)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("RACK_SWITCH_POSSIBLE_BEFORE_RUN"));
		}
		
		String fromRackCd = batch.getEquipCd();
		if(ValueUtil.isEmpty(fromRackCd)) {
			// 2. 배치에 랙이 존재하는지 체크
			String msg = MessageUtil.getMessage("NOT_FOUND", "{0}({1}) 을(를) 찾을수 없습니다.", ValueUtil.toList(MessageUtil.getTerm("label.job_batch", "JobBatch"), MessageUtil.getTerm("label.rack", "Rack")));
			throw ThrowUtil.newValidationErrorWithNoLog(msg);
		} else {
			// 3. 배치의 랙과 전환하려는 랙이 같은지 체크
			if(ValueUtil.isEqualIgnoreCase(fromRackCd, toEquipCd)) {
				throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("SELECT_DIFFERENT_RACK_CURRENT_BATCH"));
			}
		}
	}

	@Override
	public void changeEquipment(JobBatch batch, String toEquipCd) {
		// 1. 주문의 설비 정보만 이동 처리
		String orderSql = "UPDATE ORDERS SET EQUIP_CD = :toEquipCd WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId AND EQUIP_CD = :fromEquipCd";
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,fromEquipCd,toEquipCd", batch.getDomainId(), batch.getId(), batch.getEquipCd(), toEquipCd);
		this.queryManager.executeBySql(orderSql, params);
		
		// 2. 작업 배치 설비 정보 변경
		batch.setEquipCd(toEquipCd);
		this.queryManager.update(batch, "equipCd", "updaterId", "updatedAt");
	}

	@Override
	public void isPossiblePauseBatch(JobBatch batch) {
		if(ValueUtil.isNotEqual(batch.getStatus(), JobBatch.STATUS_RUNNING)) {
			// 진행 중인 작업배치가 아닙니다
			throw ThrowUtil.newStatusIsNotIng("terms.label.job_batch");
		}
		
		// 작업 일시 중지 전 처리 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_PRE_BATCH_PAUSE, ValueUtil.newMap("batch", batch));
	}

	@Override
	public void pauseBatch(JobBatch batch) {
		// 1. 작업 일시 중지 후 처리 커스텀 서비스 호출
		Long domainId = batch.getDomainId();
		Object result = this.customService.doCustomService(domainId, DIY_POST_BATCH_PAUSE, ValueUtil.newMap("batch", batch));
		
		// 2. 커스텀 서비스가 존재하지 않거나 리턴이 없다면 기본 로직 수행
		if(result == null) {
			// 2.1 배치가 할당된 랙 리스트 
			Query condition = AnyOrmUtil.newConditionForExecution(domainId);
			condition.addFilter("batchId", batch.getId());
			List<Rack> rackList = this.queryManager.selectList(Rack.class, condition);

			// 2.2 랙 작업 정보 클리어 
			List<String> rackCdList = new ArrayList<String>();
			for(Rack rack : rackList) {
				rackCdList.add(rack.getRackCd());
				rack.setBatchId(null);
				rack.setStatus(null);
			}

			// 2.3 작업 배치에 Rack 리스트 저장 
			batch.setEquipCd(ValueUtil.listToString(rackCdList));
			batch.setStatus(JobBatch.STATUS_PAUSED);
			this.queryManager.update(batch, "equipCd", "status");

			// 2.4 랙 정보 업데이트
			this.queryManager.updateBatch(rackList, "batchId", "status");
		}
	}

	@Override
	public void isPossibleResumeBatch(JobBatch batch) {
		if(ValueUtil.isNotEqual(batch.getStatus(), JobBatch.STATUS_PAUSED)) {
			// 일시 중지 상태가 아닙니다.
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("INVALID_STATUS"));
		}
		
		// 작업 재시작 전 처리 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_PRE_BATCH_RESUME, ValueUtil.newMap("batch", batch));
	}

	@Override
	public void resumeBatch(JobBatch batch) {
		// 1. 작업 재시작 후 처리 커스텀 서비스 호출
		Long domainId = batch.getDomainId();
		Object result = this.customService.doCustomService(domainId, DIY_POST_BATCH_RESUME, ValueUtil.newMap("batch", batch));
		
		if(result == null) {
			// 2. 배치가 할당될 Rack 리스트 추출
			List<String> assignRackList = ValueUtil.newStringList(batch.getEquipCd().split(SysConstants.COMMA));
			Query condition = AnyOrmUtil.newConditionForExecution(domainId);
			condition.setFilter("rackCd", SysConstants.IN, assignRackList);
			List<Rack> rackList = this.queryManager.selectList(Rack.class, condition);

			// 3. 작업 할당될 랙 체크 
			for(Rack rack : rackList) {
				if(ValueUtil.isNotEmpty(rack.getBatchId())) {
					throw ThrowUtil.newValidationErrorWithNoLog("[" + rack.getRackNm() + "] 에서 작업이 진행중입니다. 배치를 재시작 할 수 없습니다.");
				}
				
				rack.setBatchId(batch.getId());
				rack.setStatus(JobBatch.STATUS_RUNNING);
			}

			// 4. 랙에 배치 작업 할당
			this.queryManager.updateBatch(rackList, "batchId", "status");

			// 5. 작업 배치 상태 업데이트 
			batch.setStatus(JobBatch.STATUS_RUNNING);
			this.queryManager.update(batch, "status");
		}
	}
}
