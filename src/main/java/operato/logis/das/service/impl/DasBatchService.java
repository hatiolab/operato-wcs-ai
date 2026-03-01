package operato.logis.das.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.logis.das.query.store.DasQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.Order;
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
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 출고 작업 배치 서비스
 * 
 * @author shortstop
 */
@Component("dasBatchService")
public class DasBatchService extends AbstractLogisService implements IBatchService {

	/**
	 * 커스텀 서비스 - 작업 완료 전 처리
	 */
	private static final String DIY_PRE_BATCH_STOP = "diy-das-pre-batch-stop";
	/**
	 * 커스텀 서비스 - 작업 완료 후 처리
	 */
	private static final String DIY_POST_BATCH_STOP = "diy-das-post-batch-stop";
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;
	/**
	 * DAS 쿼리 스토어
	 */
	@Autowired
	private DasQueryStore dasQueryStore;
		
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
		
		// 4. 해당 배치에 랙, 작업 셀 정보 리셋
		this.resetRacksAndCells(batch);

		// 5. 주문 가공 정보 삭제
		this.deletePreprocess(batch);

		// 6. JobBatch 상태 변경
		this.updateJobBatchFinished(batch, new Date());
		
		// 7. 분류 서비스 배치 마감 API 호출
		this.serviceDispatcher.getAssortService(batch).batchCloseAction(batch);
		
		// 8. 커스텀 서비스 호출
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

		// 2. 작업 배치별 작업 실행 데이터 중에 완료되지 않은 것이 있는지 체크
		if(!closeForcibly) {
			// 작업 배치별 작업 실행 데이터 체크
			sql = "select distinct equip_cd from job_instances where domain_id = :domainId and batch_id in (select id from job_batches where domain_id = :domainId and batch_group_id = :batchGroupId) and status in (:statuses) order by equip_cd asc";
			params.put("statuses", LogisConstants.JOB_STATUS_WIPC);
			List<String> equipCdList = this.queryManager.selectListBySql(sql, params, String.class, 0, 0);
			
			// 작업 배치별 작업 실행 데이터 체크
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
			throw ThrowUtil.newValidationErrorWithNoLog(true, "MPS_NOT_ALLOWED_CANCEL_AFTER_START_JOB"); // 분류 작업시작 이후여서 취소가 불가능합니다
		}
	}

	/**
	 * 해당 배치의 랙, 작업 셀 정보 리셋
	 *
	 * @param batch
	 * @return
	 */
	protected void resetRacksAndCells(JobBatch batch) {
		Map<String, Object> params = ValueUtil.newMap("domainId,equipCd,batchId", batch.getDomainId(), batch.getEquipCd(), batch.getId());
		this.queryManager.executeBySql("UPDATE RACKS SET STATUS = null, BATCH_ID = null WHERE DOMAIN_ID = :domainId AND RACK_CD = :equipCd", params);
		this.queryManager.executeBySql("UPDATE CELLS SET CLASS_CD = null WHERE DOMAIN_ID = :domainId AND EQUIP_CD = :equipCd", params);
		this.queryManager.executeBySql("DELETE FROM WORK_CELLS WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId", params);
	}
	
	/**
	 * 주문 가공 정보를 모두 삭제한다.
	 *
	 * @param batch
	 * @return
	 */
	protected void deletePreprocess(JobBatch batch) {
		this.queryManager.executeBySql("DELETE FROM ORDER_PREPROCESSES WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId", ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId()));
	}
	
	/**
	 * 작업 배치를 마감 처리
	 * 
	 * @param batch
	 * @param finishedAt
	 */
	protected void updateJobBatchFinished(JobBatch batch, Date finishedAt) {
		// 배치 마감을 위한 물량 주문 대비 최종 실적 요약 정보 조회
		String query = this.dasQueryStore.getDasBatchResultSummaryQuery();
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
		// 1. 랙이 다른 랙인지 체크
		String fromRackCd = batch.getEquipCd();
		if(ValueUtil.isNotEmpty(fromRackCd) && ValueUtil.isEqualIgnoreCase(batch.getEquipCd(), toEquipCd)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("SELECT_DIFFERENT_RACK_CURRENT_BATCH"));
		}
		
		// 2. 배치 상태 체크
		String status = batch.getStatus();
		if(ValueUtil.isNotEqual(status, JobBatch.STATUS_WAIT) && ValueUtil.isNotEqual(status, JobBatch.STATUS_READY)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("RACK_SWITCH_POSSIBLE_BEFORE_RUN"));
		}
		
		// 3. 동일 일자, 동일 차수, 동일 호기에 할당된 작업 배치가 있는지 체크
		// String sql = "select id from job_batches where domain_id = :domainId and job_date = :jobDate and job_seq = :jobSeq and equip_type = :equipType and equip_cd = :equipCd and status in (:statuses)";
		// Map<String, Object> params = ValueUtil.newMap("domainId,jobDate,jobSeq,equipType,equipCd,statuses", batch.getDomainId(), batch.getJobDate(), batch.getJobSeq(), batch.getEquipType(), toEquipCd, ValueUtil.toList(JobBatch.STATUS_WAIT, JobBatch.STATUS_READY));
		// if(this.queryManager.selectSizeBySql(sql, params) > 0) {
		//	 throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("SAME_DATE_ORDER_SEQUENCE_ALREADY_EXIST"));
		// }
		
		// 4. From Cell Count
		String sql = "select count(*) from cells where domain_id = :domainId and equip_type = :equipType and equip_cd = :equipCd and active_flag = :activeFlag";
		Map<String, Object> condition = ValueUtil.newMap("domainId,equipType,equipCd,activeFlag", batch.getDomainId(), batch.getEquipType(), batch.getEquipCd(), true);
		int fromCellCount = this.queryManager.selectBySql(sql, condition, Integer.class);
		
		// 5. To Cell Count
		sql = "select count(*) from cells where domain_id = :domainId and equip_type = :equipType and equip_cd = :equipCd and active_flag = :activeFlag";
		condition.put("equipCd", toEquipCd);
		int toCellCount = this.queryManager.selectBySql(sql, condition, Integer.class);
		
		// 6. 셀 개수 체크
		if(fromCellCount > toCellCount) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("DIFFERENT_SWITCH_NO_RACK_CELL"));
		}
	}

	@Override
	public void changeEquipment(JobBatch batch, String toEquipCd) {
		Long domainId = batch.getDomainId();
		
		// 1. toEquipCd, batch 차수에 해당하는 작업 배치가 존재하는지 체크하여 존재하면 toEquipCd의 최대 차수로 Up
		Map<String, Object> queryParams = ValueUtil.newMap("domainId,batchId,jobSeq,batchGroupId,fromEquipCd,toEquipCd", domainId, batch.getId(), batch.getJobSeq(), batch.getBatchGroupId(), batch.getEquipCd(), toEquipCd);
		String sql = "SELECT MAX(JOB_SEQ) AS JOB_SEQ FROM JOB_BATCHES WHERE DOMAIN_ID = :domainId AND BATCH_GROUP_ID = :batchGroupId AND EQUIP_CD = :toEquipCd";
		String maxSeqStr = this.queryManager.selectBySql(sql, queryParams, String.class);
		int toJobSeq = (maxSeqStr == null) ? 1 : ValueUtil.toInteger(maxSeqStr) + 1;
		queryParams.put("toJobSeq", toJobSeq);
		
		// 2. 새로운 작업 배치 ID 생성
		String newBatchId = batch.getBatchGroupId() + LogisConstants.DASH + toEquipCd + LogisConstants.DASH + toJobSeq;
		queryParams.put("newBatchId", newBatchId);
		
		// 3. 랙 이름 조회
		sql = "SELECT RACK_NM FROM RACKS WHERE DOMAIN_ID = :domainId AND RACK_CD = :toEquipCd";
		String toEquipNm = this.queryManager.selectBySql(sql, queryParams, String.class);
		queryParams.put("toEquipNm", toEquipNm);
		
		// 4. 주문 가공 정보에 셀 매핑이 되었는지 여부 체크 ...
		sql = "SELECT COUNT(*) FROM ORDER_PREPROCESSES WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId AND SUB_EQUIP_CD IS NOT NULL AND SUB_EQUIP_CD != ''";
		int opCellMappingCount = this.queryManager.selectBySql(sql, queryParams, Integer.class);
		
		// 5. 주문 정보에 셀 매핑이 되었는지 여부 체크 ...
		sql = "SELECT COUNT(*) FROM ORDERS WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId AND SUB_EQUIP_CD IS NOT NULL AND SUB_EQUIP_CD != ''";
		int orderCellMappingCount = this.queryManager.selectBySql(sql, queryParams, Integer.class);
		
		// 6. 셀 매핑이 되어있다면 ...
		if(opCellMappingCount > 0 || orderCellMappingCount > 0) {
			// 6.1 From 랙을 셀 순서대로 조회
			queryParams.put("activeFlag", true);
			sql = "select cell_cd from cells where domain_id = :domainId and equip_cd = :fromEquipCd and active_flag = :activeFlag order by cell_seq asc";
			List<String> fromCellList = this.queryManager.selectListBySql(sql, queryParams, String.class, toJobSeq, orderCellMappingCount);
			int fromCellCount = fromCellList.size();
			
			// 6.2 To 랙을 셀 순서대로 조회
			sql = "select cell_cd from cells where domain_id = :domainId and equip_cd = :toEquipCd and active_flag = :activeFlag order by cell_seq asc";
			List<String> toCellList = this.queryManager.selectListBySql(sql, queryParams, String.class, toJobSeq, orderCellMappingCount);
			int toCellCount = toCellList.size();
			
			// 6.3 주문 가공 / 주문의 From Cell과 To Cell을 그대로 이동
			String preprocessSql = "UPDATE ORDER_PREPROCESSES SET BATCH_ID = :newBatchId, EQUIP_CD = :toEquipCd, EQUIP_NM = :toEquipNm, SUB_EQUIP_CD = :toCellCd WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId AND SUB_EQUIP_CD = :fromCellCd";
			String orderSql = "UPDATE ORDERS SET BATCH_ID = :newBatchId, JOB_SEQ = :toJobSeq, EQUIP_CD = :toEquipCd, EQUIP_NM = :toEquipNm, SUB_EQUIP_CD = :toCellCd WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId AND SUB_EQUIP_CD = :fromCellCd";

			for(int i = 0 ; i < fromCellCount ; i++) {
				if(toCellCount > i) {
					queryParams.put("fromCellCd", fromCellList.get(i));
					queryParams.put("toCellCd", toCellList.get(i));
					
					if(opCellMappingCount > 0) {
						this.queryManager.executeBySql(preprocessSql, queryParams);
					}
					
					if(orderCellMappingCount > 0) {
						this.queryManager.executeBySql(orderSql, queryParams);
					}
				}
			}
		}

		// 7. 주문 가공 / 주문의 데이터가 이동이 안 된 것이 있다면 모두 이동
		String preprocessSql = "UPDATE ORDER_PREPROCESSES SET BATCH_ID = :newBatchId, EQUIP_CD = :toEquipCd, EQUIP_NM = :toEquipNm, CLASS_CD = :toEquipCd WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
		String orderSql = "UPDATE ORDERS SET BATCH_ID = :newBatchId, JOB_SEQ = :toJobSeq, EQUIP_CD = :toEquipCd, EQUIP_NM = :toEquipNm WHERE DOMAIN_ID = :domainId AND BATCH_ID = :batchId";
		this.queryManager.executeBySql(preprocessSql, queryParams);
		this.queryManager.executeBySql(orderSql, queryParams);
		
		// 8. 작업 배치 변경
		queryParams.put("updaterId", User.currentUser() != null ? User.currentUser().getId() : null);
		queryParams.put("updatedAt", new Date());
		sql = "UPDATE JOB_BATCHES SET JOB_SEQ = :toJobSeq, EQUIP_CD = :toEquipCd, EQUIP_NM = :toEquipNm, ID = :newBatchId, UPDATER_ID = :updaterId, UPDATED_AT = :updatedAt WHERE DOMAIN_ID = :domainId AND ID = :batchId";
		this.queryManager.executeBySql(sql, queryParams);
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
