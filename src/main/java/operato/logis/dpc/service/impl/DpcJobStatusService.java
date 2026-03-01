package operato.logis.dpc.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.logis.dpc.query.store.DpcBatchQueryStore;
import operato.logis.dpc.query.store.DpcPickQueryStore;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.anythings.base.service.impl.AbstractJobStatusService;
import xyz.anythings.base.service.util.LogisServiceUtil;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.sys.util.ValueUtil;

/**
 * DPC 작업 상태 서비스
 * 
 * @author shortstop
 */
@Component("dpcJobStatusService")
public class DpcJobStatusService extends AbstractJobStatusService {

	/**
	 * 쿼리 스토어
	 */
	@Autowired
	protected DpcBatchQueryStore dpcBatchQueryStore;
	/**
	 * 쿼리 스토어
	 */
	@Autowired
	protected DpcPickQueryStore dpcPickQueryStore;
	
	@Override
	public BatchProgressRate getBatchProgressSummary(JobBatch batch) {
		
		String sql = this.dpcBatchQueryStore.getBatchProgressRateQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		
		// 배치에 호기가 지정되어 있으면 지정된 호기에 대한 진행율
		if(ValueUtil.isNotEmpty(batch.getEquipType())) {
			params.put("equipType", batch.getEquipType());
		}
		
		if(ValueUtil.isNotEmpty(batch.getEquipCd())) {
			params.put("equipCd", batch.getEquipCd());
		}
		
		return AnyEntityUtil.findItem(batch.getDomainId(), false, BatchProgressRate.class, sql, params);
	}
	
	@Override
	public List<JobInput> searchInputList(JobBatch batch, String equipCd, String stationCd, String selectedInputId) {

//		// 태블릿의 현재 투입 정보 기준으로 2, 1 (next), 0 (current), -1 (previous) 정보를 표시
//		Long domainId = batch.getDomainId();
//		String sql = this.dpcPickQueryStore.getFindStationWorkingInputSeq();
//		
//		// 해당 스테이션에 존재하는 피킹 중인 가장 작은 시퀀스를 조회
//		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,stationCd,jobStatus", domainId, batch.getId(), stationCd, LogisConstants.JOB_STATUS_PICKING);
//		LogisServiceUtil.filterAllCondition(condition, "stationCd");
//		Integer inputSeq = this.queryManager.selectBySql(sql, condition, Integer.class);
//		
//		// 없다면 해당 스테이션에 존재하는 투입 중인 가장 작은 시퀀스를 조회
//		if(inputSeq == null || inputSeq < 0) {
//			condition.put("jobStatus", LogisConstants.JOB_STATUS_INPUT);
//			inputSeq = this.queryManager.selectBySql(sql, condition, Integer.class);
//		}
//		
//		// 없다면 선택된 투입 ID를 중심으로 조회
//		if((inputSeq == null || inputSeq < 0) && ValueUtil.isNotEmpty(selectedInputId)) {
//			inputSeq = this.queryManager.selectBySql(sql, ValueUtil.newMap("domainId,jobInputId", domainId, selectedInputId), Integer.class);
//		}
//		
//		// 그래도 없다면 해당 스테이션의 마지막 4개 투입 정보 조회
//		if(inputSeq == null || inputSeq < 1) {
//			condition.put("lastFour", true);
//		// 투입 순서로 작업을 위한 투입 리스트 조회
//		} else {
//			condition.put("inputSeq", inputSeq);
//		}
//		
//		sql = this.dpcPickQueryStore.getWorkingJobInputListQuery();
//		return this.queryManager.selectListBySql(sql, condition, JobInput.class, 0, 0);
		
		return null;
	}

	@Override
	public Page<JobInput> paginateInputList(JobBatch batch, String equipCd, String status, int page, int limit) {
		return this.paginateInputList(batch, equipCd, null, status, page, limit);
	}
	
	@Override
	public Page<JobInput> paginateInputList(JobBatch batch, String equipCd, String stationCd, String status, int page, int limit) {
		/*String sql = this.dpcPickQueryStore.getBatchJobInputListQuery();
		status = ValueUtil.isEmpty(status) ? null : status;
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,equipCd,stationCd,status", batch.getDomainId(), batch.getId(), equipCd, stationCd, status);
		LogisServiceUtil.filterAllCondition(condition, "stationCd");
		return this.queryManager.selectPageBySql(sql.toString(), condition, JobInput.class, page, limit);*/
		
		return null;
	}
	
	@Override
	public Page<JobInput> paginateNotInputList(JobBatch batch, String equipCd, String stationCd, int page, int limit) {
		/*String sql = this.dpcPickQueryStore.getBatchNotInputListQuery();
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,equipCd,stationCd", batch.getDomainId(), batch.getId(), equipCd, stationCd);
		LogisServiceUtil.filterAllCondition(condition, "stationCd");
		return this.queryManager.selectPageBySql(sql.toString(), condition, JobInput.class, page, limit);*/
		
		return null;
	}

	@Override
	public List<JobInstance> searchInputJobList(JobBatch batch, JobInput input, String stationCd) {
		String sql = this.dpcPickQueryStore.getSearchPickingJobListQuery();
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,inputSeq,stationCd", batch.getDomainId(), batch.getId(), input.getInputSeq(), stationCd);
		LogisServiceUtil.filterAllCondition(condition, "stationCd");
		return this.queryManager.selectListBySql(sql, condition, JobInstance.class, 0, 0);
	}

	@Override
	public List<JobInstance> searchInputJobList(JobBatch batch, Map<String, Object> condition) {
		LogisServiceUtil.filterAllCondition(condition, "stationCd");
		this.addBatchConditions(batch, condition);
		return this.queryManager.selectList(JobInstance.class, condition);
	}

	@Override
	public List<JobInstance> searchPickingJobList(JobBatch batch, String stationCd, String classCd) {
		// 표시기 점등을 위해서 다른 테이블의 데이터도 필요해서 쿼리로 조회
		String sql = this.dpcPickQueryStore.getSearchPickingJobListQuery();
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,classCd,statuses", batch.getDomainId(), batch.getId(), classCd, LogisConstants.JOB_STATUS_WIPC);
		LogisServiceUtil.filterAllCondition(condition, "stationCd");
		return this.queryManager.selectListBySql(sql, condition, JobInstance.class, 0, 0);
	}

	@Override
	public List<JobInstance> searchPickingJobList(JobBatch batch, Map<String, Object> condition) {
		// 표시기 점등을 위해서 다른 테이블의 데이터도 필요해서 쿼리로 조회
		String sql = this.dpcPickQueryStore.getSearchPickingJobListQuery();
		this.addBatchConditions(batch, condition);
		LogisServiceUtil.filterAllCondition(condition, "stationCd");
		return this.queryManager.selectListBySql(sql, condition, JobInstance.class, 0, 0);
	}
	
	@Override
	public List<JobInstance> searchJobStatusByCell(JobBatch batch, String stationCd, String cellCd, boolean workingCellOnly, boolean pickingCellOnly) {
		/*String sql = this.dpcPickQueryStore.getSearchJobStatusByCellQuery();
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,equipCd,workingCellOnly", batch.getDomainId(), batch.getId(), batch.getEquipCd(), workingCellOnly);
		
		if(ValueUtil.isNotEmpty(cellCd)) {
			condition.put("cellCd", cellCd);
		}
		
		LogisServiceUtil.filterAllCondition(condition, "stationCd");
		return this.queryManager.selectListBySql(sql, condition, JobInstance.class, 0, 0);*/
		
		return null;
	}

	@Override
	public JobInstance findPickingJob(Long domainId, String jobInstanceId) {
		String sql = this.dpcPickQueryStore.getSearchPickingJobListQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,id", domainId, jobInstanceId);
		List<JobInstance> jobList = this.queryManager.selectListBySql(sql, params, JobInstance.class, 1, 1);
		return ValueUtil.isEmpty(jobList) ? null : jobList.get(0);
	}

}
