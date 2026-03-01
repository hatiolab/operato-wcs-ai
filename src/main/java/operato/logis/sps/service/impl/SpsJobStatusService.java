package operato.logis.sps.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import operato.logis.sps.model.SpsSkuSummary;
import operato.logis.sps.query.store.SpsQueryStore;
import operato.logis.sps.service.api.ISpsJobStatusService;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxItem;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 단포 작업 현황 관련 조회 서비스
 * 
 * @author shortstop
 */
@Component("spsJobStatusService")
public class SpsJobStatusService extends AbstractLogisService implements ISpsJobStatusService {
	/**
	 * 단포 쿼리 스토어
	 */
	@Autowired
	private SpsQueryStore spsQueryStore;
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public Integer findNextInputSeq(JobBatch batch) {
		
		// 작업 배치의 마지막 투입 시퀀스를 조회 후 하나 올려서 리턴
		JobBatch findBatch = AnyEntityUtil.findEntityByIdWithLock(true, JobBatch.class, batch.getId(), "id", "lastInputSeq");
		int lastInputSeq = (findBatch.getLastInputSeq() == null) ? 1 : findBatch.getLastInputSeq() + 1;
		batch.setLastInputSeq(lastInputSeq);
		this.queryManager.update(batch, "lastInputSeq");
		return lastInputSeq;
	}
	
	@Override
	public List<JobInstance> searchPickingJobList(JobBatch batch, String stationCd, String classCd) {
		String sql = this.spsQueryStore.getSearchPickingJobListQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,stageCd,equipType,stationCd,classCd,statuses", batch.getDomainId(), batch.getId(), batch.getStageCd(), batch.getEquipType(), stationCd, classCd, LogisConstants.JOB_STATUS_WIPC);
		return this.queryManager.selectListBySql(sql, params, JobInstance.class, 0, 0);
	}

	@Override
	public List<JobInstance> searchPickingJobList(JobBatch batch, Map<String, Object> condition) {
		String sql = this.spsQueryStore.getSearchPickingJobListQuery();
		this.addBatchConditions(batch, condition);
		return this.queryManager.selectListBySql(sql, condition, JobInstance.class, 0, 0);
	}

	@Override
	public JobInstance findPickingJob(Long domainId, String jobInstanceId) {
		String sql = this.spsQueryStore.getSearchPickingJobListQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,jobInstanceId", domainId, jobInstanceId);
		List<JobInstance> jobList = this.queryManager.selectListBySql(sql, params, JobInstance.class, 1, 1);
		return ValueUtil.isEmpty(jobList) ? null : jobList.get(0);
	}

	@Override
	public List<JobInstance> searchJobList(JobBatch batch, Map<String, Object> condition) {
		String sql = this.spsQueryStore.getSearchPickingJobListQuery();
		this.addBatchConditions(batch, condition);
		return this.queryManager.selectListBySql(sql, condition, JobInstance.class, 0, 0);
	}

	@Override
	public Page<BoxPack> paginateBoxList(JobBatch batch, Map<String, Object> condition, int page, int limit) {
		// 1. 배치 조건을 검색 조건에 추가
		this.addBatchConditions(batch, condition);
		Query query = AnyOrmUtil.newConditionForExecution(batch.getDomainId(), page, limit);
		
		// 2. 필터 조건에 검색 조건을 모두 추가
		Iterator<String> keyIter = condition.keySet().iterator();
		while(keyIter.hasNext()) {
			String key = keyIter.next();
			Object val = condition.get(key);
			query.addFilter(key, val);
		}
		
		// 3. 페이지네이션 쿼리 실행
		return this.queryManager.selectPage(BoxPack.class, query);
	}

	@Override
	public List<BoxPack> searchBoxList(JobBatch batch, Map<String, Object> condition) {
		// 1. 배치 조건을 검색 조건에 추가
		this.addBatchConditions(batch, condition);
		// 2. 박스 조회
		return this.queryManager.selectList(BoxPack.class, condition);
	}

	@Override
	public List<BoxItem> searchBoxItems(Long domainId, String boxPackId) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("boxPackId", boxPackId);
		return this.queryManager.selectList(BoxItem.class, condition);
	}

	@Override
	public List<SpsSkuSummary> searchSkuJobSummary(JobBatch batch, String comCd, String skuCd) {
		String skuJobSummaryQuery = this.spsQueryStore.getSearchSkuJobSummaryQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,comCd,skuCd", batch.getDomainId(), batch.getId(), comCd, skuCd);
		return AnyEntityUtil.searchItems(batch.getDomainId(), false, SpsSkuSummary.class, skuJobSummaryQuery, params);
	}

	/**
	 * 검색 조건에 배치 조건을 추가
	 * 
	 * @param batch
	 * @param condition
	 */
	protected void addBatchConditions(JobBatch batch, Map<String, Object> condition) {
		if(!condition.containsKey("domainId")) {
			condition.put("domainId", batch.getDomainId());
		}
		
		if(!condition.containsKey("batchId")) {
			condition.put("batchId", batch.getId());
		}
		
		if(ValueUtil.isNotEmpty(batch.getEquipType()) && !condition.containsKey("equipType")) {
			condition.put("equipType", batch.getEquipType());
		}
		
		if(ValueUtil.isNotEmpty(batch.getEquipCd()) && !condition.containsKey("equipCd")) {
			condition.put("equipCd", batch.getEquipCd());
		}
	}

}
