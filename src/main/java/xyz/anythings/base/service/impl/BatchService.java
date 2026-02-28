package xyz.anythings.base.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.anythings.base.query.store.BatchQueryStore;
import xyz.anythings.base.util.LogisBaseUtil;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 배치 서비스 Facade
 * 
 * @author shortstop
 */
@Component
public class BatchService extends AbstractLogisService {
	
	/**
	 * 쿼리 스토어
	 */
	@Autowired
	private BatchQueryStore batchQueryStore;
	
	/**
	 * 새로운 배치 ID 생성
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param params
	 * @return
	 */
	public String newJobBatchId(Long domainId, String stageCd, Object... params) {
		return LogisBaseUtil.newJobBatchId(domainId, stageCd);
	}

	/**
	 * 해당 일의 작업 진행율
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobDate
	 * @return
	 */
	public BatchProgressRate dailyProgressRate(Long domainId, String stageCd, String jobDate) {
		// TODO 이벤트 혹은 커스텀 서비스로 커스터마이징 가능하도록 수정
		String sql = this.batchQueryStore.getDailyProgressRateQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,jobDate,stageCd", domainId, jobDate, stageCd);
		return this.queryManager.selectBySql(sql, params, BatchProgressRate.class);
	}

	/**
	 * 설비에서 진행 중인 배치 조회
	 *  
	 * @param domainId
	 * @param stageCd
	 * @param equipType
	 * @param equipCd
	 * @return
	 */
	public JobBatch findRunningBatch(Long domainId, String stageCd, String equipType, String equipCd) {
		String filterNames = "domainId,stageCd,equipType,status";
		List<Object> filterValues = ValueUtil.newList(domainId, stageCd, equipType, JobBatch.STATUS_RUNNING);
		
		if(ValueUtil.isNotEmpty(equipCd)) {
			filterNames += ",equipCd";
			filterValues.add(equipCd);
		}
		
		return AnyEntityUtil.findEntityBy(domainId, false, JobBatch.class, filterNames, filterValues.toArray());
	}

	/**
	 * 스테이지에서 작업 유형 별 진행 중인 작업 배치 리스트 조회
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param jobDate
	 * @return
	 */
	public List<JobBatch> searchRunningBatchList(Long domainId, String stageCd, String jobType, String jobDate) {
		return AnyEntityUtil.searchEntitiesBy(domainId, false, JobBatch.class, "stageCd,jobType,jobDate", stageCd, jobType, jobDate);
	}

	/**
	 * 스테이지에서 작업 유형 별 진행 중인 메인 작업 배치 리스트 조회
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param jobDate
	 * @return
	 */
	public List<JobBatch> searchRunningMainBatchList(Long domainId, String stageCd, String jobType, String jobDate) {
		String sql = "select * from job_batches where domain_id = :domainId and stage_cd = :stageCd and job_type = :jobType and job_date = :jobDate and id = batch_group_id";
		return AnyEntityUtil.searchItems(domainId, false, JobBatch.class, sql, "domainId,stageCd,jobType,jobDate", domainId, stageCd, jobType, jobDate);
	}
	
	/**
	 * 스테이지 별 진행 중인 메인 작업 배치 리스트 조회
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public List<JobBatch> searchRunningMainBatchList(Long domainId, String stageCd) {
		String sql = "select * from job_batches where domain_id = :domainId and stage_cd = :stageCd and id = batch_group_id";
		return AnyEntityUtil.searchItems(domainId, false, JobBatch.class, sql, "domainId,stageCd", domainId, stageCd);
	}
	
	/**
	 * 작업 배치가 마감 가능한 지 체크
	 * 
	 * @param batch
	 * @param closeForcibly
	 */
	public void isPossibleCloseBatch(JobBatch batch, boolean closeForcibly) {
		this.serviceDispatcher.getBatchService(batch).isPossibleCloseBatch(batch, closeForcibly);
	}

	/**
	 * 작업 배치 마감
	 * 
	 * @param batch
	 * @param forcibly
	 */
	public void closeBatch(JobBatch batch, boolean forcibly) {
		this.serviceDispatcher.getBatchService(batch).closeBatch(batch, forcibly);
	}

	/**
	 * 배치 그룹이 마감 가능한 지 체크
	 * 
	 * @param domainId
	 * @param batchGroupId
	 * @param closeForcibly
	 */
	public void isPossibleCloseBatchGroup(Long domainId, String batchGroupId, boolean closeForcibly) {
		JobBatch batch = this.findByBatchGroupId(domainId, batchGroupId);
		this.serviceDispatcher.getBatchService(batch).isPossibleCloseBatchGroup(domainId, batchGroupId, closeForcibly);
	}

	/**
	 * 작업 배치 그룹 마감
	 * 
	 * @param domainId
	 * @param batchGroupId
	 * @param forcibly
	 * @return
	 */
	public int closeBatchGroup(Long domainId, String batchGroupId, boolean forcibly) {
		JobBatch batch = this.findByBatchGroupId(domainId, batchGroupId);
		return this.serviceDispatcher.getBatchService(batch).closeBatchGroup(domainId, batchGroupId, forcibly);
	}

	/**
	 * 작업 배치 취소 가능한 지 여부
	 * 
	 * @param batch
	 */
	public void isPossibleCancelBatch(JobBatch batch) {
		this.serviceDispatcher.getBatchService(batch).isPossibleCancelBatch(batch);
	}

	/**
	 * 배치 그룹 ID로 메인 작업 배치 조회
	 * 
	 * @param domainId
	 * @param batchGroupId
	 * @return
	 */
	private JobBatch findByBatchGroupId(Long domainId, String batchGroupId) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId, 1, 1);
		condition.addFilter("batchGroupId", batchGroupId);
		condition.addSelect("id", "job_type");
		List<JobBatch> batches = this.queryManager.selectList(JobBatch.class, condition);
		if(batches.isEmpty()) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.JobBatch");
		}
		
		return batches.get(0);
	}

	/**
	 * 작업 배치 호기 전환이 가능한 지 여부 체크 
	 * 
	 * @param batch 작업 배치
	 * @param toEquipCd 전환할 호기
	 */
	public void isPossibleChangeEquipment(JobBatch batch, String toEquipCd) {
		this.serviceDispatcher.getBatchService(batch).isPossibleChangeEquipment(batch, toEquipCd);
	}
	
	/**
	 * 배치 호기 전환
	 * 
	 * @param batch 작업 배치
	 * @param toEquipCd 전환할 호기
	 */
	public void changeEquipment(JobBatch batch, String toEquipCd) {
		this.serviceDispatcher.getBatchService(batch).changeEquipment(batch, toEquipCd);
	}

	/**
	 * 작업 배치가 일시 중지 가능한 배치인지 체크
	 * 
	 * @param batch
	 */
	public void isPossiblePauseBatch(JobBatch batch) {
		this.serviceDispatcher.getBatchService(batch).isPossiblePauseBatch(batch);
	}
	
	/**
	 * 작업 배치 일시 중지 처리
	 * 
	 * @param batch
	 */
	public void pauseBatch(JobBatch batch) {
		this.serviceDispatcher.getBatchService(batch).pauseBatch(batch);
	}
	
	/**
	 * 작업 배치가 재 시작 가능한 배치인지 체크
	 * 
	 * @param batch
	 */
	public void isPossibleResumeBatch(JobBatch batch) {
		this.serviceDispatcher.getBatchService(batch).isPossibleResumeBatch(batch);
	}
	
	/**
	 * 작업 배치 재 시작 처리
	 * 
	 * @param batch
	 */
	public void resumeBatch(JobBatch batch) {
		this.serviceDispatcher.getBatchService(batch).resumeBatch(batch);
	}
}
