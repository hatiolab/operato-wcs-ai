package xyz.anythings.base.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxItem;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.anythings.base.query.store.BatchQueryStore;
import xyz.anythings.base.query.store.BoxQueryStore;
import xyz.anythings.base.service.api.IJobStatusService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.util.ValueUtil;

/**
 * 작업 상태 서비스 기본 구현 - 각 분류 설비 모듈별로 이 클래스를 확장해서 구현
 * 
 * @author shortstop
 */
public abstract class AbstractJobStatusService extends AbstractLogisService implements IJobStatusService {

	/**
	 * 배치 쿼리 스토어
	 */
	@Autowired
	protected BatchQueryStore batchQueryStore;
	/**
	 * 박스 쿼리 스토어 
	 */
	@Autowired
	protected BoxQueryStore boxQueryStore;
	
	@Override
	public BatchProgressRate getBatchProgressSummary(JobBatch batch) {
		
		String sql = this.batchQueryStore.getBatchProgressRateQuery();
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
	public JobInput findLatestInput(JobBatch batch) {
		
		String qry = this.batchQueryStore.getLatestJobInputQuery();
		Map<String,Object> params = ValueUtil.newMap("domainId,batchId,equipType", batch.getDomainId(), batch.getId(), batch.getEquipType());
		
		if(ValueUtil.isNotEmpty(batch.getEquipCd())) {
			params.put("equipCd", batch.getEquipCd());
		}
		
		return AnyEntityUtil.findItem(batch.getDomainId(), false, JobInput.class, qry, params);
	}

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
	public List<JobInstance> searchJobList(JobBatch batch, Map<String, Object> condition) {
		
		// 1. 배치 조건을 검색 조건에 추가
		this.addBatchConditions(batch, condition);
		// 2. 작업 리스트 조회 
		return this.queryManager.selectList(JobInstance.class, condition);
	}

	@Override
	public JobInstance findUnboxedJob(JobBatch batch, String subEquipCd) {
		
		// 1. 배치 조건을 검색 조건에 추가, 상태가 '피킹 완료'인 작업만 조회
		Map<String, Object> condition = ValueUtil.newMap("subEquipCd,status", subEquipCd, LogisConstants.JOB_STATUS_FINISH);
		this.addBatchConditions(batch, condition);
		List<JobInstance> jobs = this.queryManager.selectList(JobInstance.class, condition);
		
		// 2. 없다면 '피킹 시작'인 작업을 조회
		if(ValueUtil.isEmpty(jobs)) {
			condition.put("status", LogisConstants.JOB_STATUS_PICKING);
			jobs = this.queryManager.selectList(JobInstance.class, condition);
		}
		
		return ValueUtil.isEmpty(jobs) ? null : jobs.get(0);
	}

	@Override
	public BoxPack findLatestBox(JobBatch batch, String subEquipCd) {
		
		String sql = this.boxQueryStore.getFindLatestBoxOfCellQuery();
		Map<String, Object> condition = ValueUtil.newMap("subEquipCd", subEquipCd);
		this.addBatchConditions(batch, condition);
		return this.queryManager.selectBySql(sql, condition, BoxPack.class);
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
	public int totalOrderQtyByJob(JobInstance job) {
		// 각 모듈별로 각자 구현
		return 0;
	}

	@Override
	public int totalPickedQtyByJob(JobInstance job) {
		// 각 모듈별로 각자 구현
		return 0;
	}

	@Override
	public int totalPickQtyByJob(JobInstance job) {
		// 각 모듈별로 각자 구현
		return 0;
	}

	@Override
	public int toPcsQty(Integer boxInQty, Integer boxQty, Integer pcsQty) {
		return (boxInQty != null && boxQty != null) ? ((boxInQty * boxQty) + pcsQty) : pcsQty;
	}

	@Override
	public int toPcsQty(JobInstance job, Integer boxQty, Integer pcsQty) {
		// 각 모듈별로 각자 구현
		return 0;
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
