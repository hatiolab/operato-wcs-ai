package operato.logis.pdas.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.logis.pdas.query.store.PdasBatchQueryStore;
import operato.logis.pdas.query.store.PdasPickQueryStore;
import operato.logis.pdas.service.util.PdasServiceUtil;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.anythings.base.service.impl.AbstractJobStatusService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.sys.util.ValueUtil;

/**
 * PDAS 작업 현황 관련 조회 서비스
 * 
 * @author shortstop
 */
@Component("pdasJobStatusService")
public class PdasJobStatusService extends AbstractJobStatusService {

	/**
	 * PDAS 배치 관련 쿼리 스토어 
	 */
	@Autowired
	protected PdasBatchQueryStore pdasBatchQueryStore;
	/**
	 * PDAS 피킹 쿼리 스토어 
	 */
	@Autowired
	protected PdasPickQueryStore pdasPickQueryStore;
	
	@Override
	public BatchProgressRate getBatchProgressSummary(JobBatch batch) {
		
		String sql = this.pdasBatchQueryStore.getBatchProgressRateQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		return AnyEntityUtil.findItem(batch.getDomainId(), false, BatchProgressRate.class, sql, params);
	}
	
	@Override
	public List<JobInstance> searchPickingJobList(JobBatch batch, Map<String, Object> condition) {

		// 표시기 점등을 위해서 다른 테이블의 데이터도 필요해서 쿼리로 조회
		String sql = this.pdasPickQueryStore.getSearchPickingJobListQuery();
		this.addBatchConditions(batch, condition);
		return this.queryManager.selectListBySql(sql, condition, JobInstance.class, 0, 0);
	}

	@Override
	public List<JobInstance> searchInputJobList(JobBatch batch, Map<String, Object> condition) {
		// 1. 배치 조건을 검색 조건에 추가
		this.addBatchConditions(batch, condition);
		// 2. 작업 리스트 조회 
		return this.queryManager.selectList(JobInstance.class, condition);
	}

	@Override
	public JobInstance findPickingJob(Long domainId, String jobInstanceId) {
		String sql = this.pdasPickQueryStore.getSearchPickingJobListQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,jobInstanceId", domainId, jobInstanceId);
		List<JobInstance> jobList = this.queryManager.selectListBySql(sql, params, JobInstance.class, 1, 1);
		return ValueUtil.isEmpty(jobList) ? null : jobList.get(0);
	}

	@Override
	public Page<JobInput> paginateInputList(JobBatch batch, String equipCd, String status, int page, int limit) {
		return this.paginateInputList(batch, equipCd, null, status, page, limit);
	}

	@Override
	public Page<JobInput> paginateInputList(JobBatch batch, String equipCd, String stationCd, String status, int page, int limit) {
		String sql = this.pdasPickQueryStore.getBatchJobInputListQuery();
		stationCd = PdasServiceUtil.isValidStationCode(stationCd) ? stationCd : null;
		status = ValueUtil.isEmpty(status) ? null : status;
		String conditionKey = ValueUtil.isEqualIgnoreCase(status, "U") ? "notInputOnly" : "inputOnly";
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipCd,stationCd", batch.getDomainId(), batch.getId(), equipCd, stationCd);
		params.put(conditionKey, true);
		return this.queryManager.selectPageBySql(sql.toString(), params, JobInput.class, page, limit);
	}

	@Override
	public Page<JobInput> paginateNotInputList(JobBatch batch, String equipCd, String stationCd, int page, int limit) {
		String sql = this.pdasPickQueryStore.getBatchJobInputListQuery();
		stationCd = PdasServiceUtil.isValidStationCode(stationCd) ? stationCd : null;
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipCd,stationCd,notInputOnly", batch.getDomainId(), batch.getId(), equipCd, stationCd, true);
		return this.queryManager.selectPageBySql(sql.toString(), params, JobInput.class, page, limit);
	}

	@Override
	public List<JobInstance> searchInputJobList(JobBatch batch, JobInput input, String stationCd) {
		String sql = this.pdasPickQueryStore.getBatchJobInputDetailsQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,comCd,skuCd", batch.getDomainId(), batch.getId(), input.getComCd(), input.getSkuCd());
		if(PdasServiceUtil.isValidStationCode(stationCd)) {
			params.put("stationCd", stationCd);
		}
		return this.queryManager.selectListBySql(sql, params, JobInstance.class, 0, 0);
	}
	
	@Override
	public List<JobInstance> searchPickingJobList(JobBatch batch, String stationCd, String classCd) {
		String sql = this.pdasPickQueryStore.getSearchPickingJobListQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,classCd", batch.getDomainId(), batch.getId(), classCd);
		if(PdasServiceUtil.isValidStationCode(stationCd)) {
			params.put("stationCd", stationCd);
		}
		return this.queryManager.selectListBySql(sql, params, JobInstance.class, 0, 0);
	}
	
	@Override
	public List<JobInstance> searchJobStatusByCell(JobBatch batch, String stationCd, String cellCd, boolean workingCellOnly, boolean pickingCellOnly) {
		String sql = this.pdasPickQueryStore.getSearchJobStatusByCellQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,equipCd,workingCellOnly,pickingCellOnly", batch.getDomainId(), batch.getId(), batch.getEquipCd(), workingCellOnly, pickingCellOnly);

		if(PdasServiceUtil.isValidStationCode(stationCd)) {
			params.put("stationCd", stationCd);
		}
		
		if(ValueUtil.isNotEmpty(cellCd)) {
			params.put("cellCd", cellCd);
		}
		
		return this.queryManager.selectListBySql(sql, params, JobInstance.class, 0, 0);
	}
	
	@Override
	public List<JobInput> searchInputList(JobBatch batch, String equipCd, String stationCd, String selectedInputId) {
		return null;
	}
}
