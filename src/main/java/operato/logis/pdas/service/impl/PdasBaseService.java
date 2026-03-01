package operato.logis.pdas.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import operato.logis.pdas.service.api.IPdasAssortService;
import operato.logis.pdas.service.util.PdasServiceUtil;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.Rack;
import xyz.anythings.base.entity.Station;
import xyz.anythings.sys.AnyConstants;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * P-DAS 기본 서비스
 * 
 * @author shortstop
 */
public abstract class PdasBaseService extends AbstractPdasAssortService implements IPdasAssortService {
	
	/**
	 * 작업 정보를 1PCS 단위로 모두 분할
	 * 
	 * @param batch
	 */
	protected void splitJobInstancesByPiece(JobBatch batch) {
		Long domainId = batch.getDomainId();
		
		// 1. 배치 작업 데이터 수량이 2 이상인 것만 조회
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("batchId", batch.getId());
		condition.addFilter("pickQty", AnyConstants.GREATER_THAN, 1);
		List<JobInstance> jobList = this.queryManager.selectList(JobInstance.class, condition);
		List<JobInstance> updateList = new ArrayList<JobInstance>(jobList.size());
		
		// 2. 주문 수량이 2 이상인 작업 데이터를 수량 1인 작업으로 분할
		for(JobInstance job : jobList) {
			int orderQty = job.getPickQty();
			
			if(orderQty == 1) {
				updateList.add(job);
			} else {
				// 원 작업 정보의 수량을 1로 수정
				job.setPickQty(1);
				updateList.add(job);
				
				List<JobInstance> splitJobs = new ArrayList<JobInstance>(orderQty);
				for(int i = 1 ; i < orderQty ; i++) {
					JobInstance splitJob = new JobInstance();
					ValueUtil.populate(job, splitJob);
					splitJob.setId(null);
					splitJobs.add(splitJob);
				}
				
				this.queryManager.insertBatch(splitJobs);
			}
		}
		
		this.queryManager.updateBatch(updateList, "pickQty");
	}
	
	/**
	 * 쿼리를 위한 파라미터 생성
	 * 
	 * @param batch
	 * @param comCd
	 * @param skuCd
	 * @param stationCd
	 * @param status
	 * @param statuses
	 * @return
	 */
	protected Map<String, Object> createQueryParams(JobBatch batch, String comCd, String skuCd, String stationCd, String status, List<String> statuses) {
		Map<String, Object> queryParams = ValueUtil.newMap("domainId,batchId,equipType,equipCd", batch.getDomainId(), batch.getId(), batch.getEquipType(), batch.getEquipCd());

		if(PdasServiceUtil.isValidStationCode(stationCd)) {
			queryParams.put("stationCd", stationCd);
		}
		
		if(ValueUtil.isNotEmpty(comCd)) {
			queryParams.put("comCd", comCd);
		}
		
		if(ValueUtil.isNotEmpty(skuCd)) {
			queryParams.put("skuCd", skuCd);
		}
		
		if(ValueUtil.isNotEmpty(status)) {
			queryParams.put("status", status);
		}
		
		if(ValueUtil.isNotEmpty(statuses)) {
			queryParams.put("statuses", statuses);
		}
		
		return queryParams;
	}
	
	/**
	 * 작업 스테이션 내에 상품이 매핑되어 진행 중인 주문의 작업이 있는지 찾아서 리턴
	 * 
	 * @param batch
	 * @param comCd
	 * @param skuCd
	 * @param stationCd
	 * @return
	 */
	protected JobInstance findWorkingOrderJob(JobBatch batch, String comCd, String skuCd, String stationCd) {
		Map<String, Object> queryParams = this.createQueryParams(batch, comCd, skuCd, stationCd, LogisConstants.JOB_STATUS_WAIT, null);
		String sql = this.pdasPickQueryStore.getSearchPickingJobListQuery();
		List<JobInstance> jobList = this.queryManager.selectListBySql(sql, queryParams, JobInstance.class, 1, 1);
		return ValueUtil.isEmpty(jobList) ? null : jobList.get(0);
	}
	
	/**
	 * 작업 스테이션 내에 상품이 포함된 대기(신규) 주문의 작업이 있는지 찾아서 리턴 
	 * 
	 * @param batch
	 * @param comCd
	 * @param skuCd
	 * @param stationCd
	 * @return
	 */
	protected JobInstance findWaitingOrderJob(JobBatch batch, String comCd, String skuCd, String stationCd) {
		Map<String, Object> queryParams = this.createQueryParams(batch, comCd, skuCd, stationCd, LogisConstants.JOB_STATUS_WAIT, null);
		String sql = this.pdasPickQueryStore.getFindWaitingPickingJobQuery();
		List<JobInstance> jobList = this.queryManager.selectListBySql(sql, queryParams, JobInstance.class, 1, 1);
		return ValueUtil.isEmpty(jobList) ? null : jobList.get(0);
	}

	/**
	 * 다음에 작업할 셀 조회
	 * 
	 * @param batch
	 * @param stationCd
	 * @param exceptionWhenNotFound
	 * @return
	 */
	protected String findNextWorkingCellCd(JobBatch batch, String stationCd, boolean exceptionWhenNotFound) {
		Map<String, Object> queryParams = this.createQueryParams(batch, null, null, stationCd, null, null);
		String sql = this.pdasPickQueryStore.getFindLoadCellToPickQuery();
		String cellCd = this.queryManager.selectBySql(sql, queryParams, String.class);
	
		if(exceptionWhenNotFound && ValueUtil.isEmpty(cellCd)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NO_LEFT_CELL_NEW_ORDER"));
		}
		
		return cellCd;
	}
	
	/**
	 * 작업 스테이션에 피킹 중인 작업이 있는지 체크
	 * 
	 * @param batch
	 * @param stationCd
	 * @param exceptionWhenFound
	 */
	protected boolean checkWorkStationPicking(JobBatch batch, String stationCd, boolean exceptionWhenFound) {
		// 동일 작업 스테이션에 동시 작업이 불가능하므로 현재 피킹 중인 작업이 있는지 체크
		Map<String, Object> queryParams = this.createQueryParams(batch, null, null, stationCd, LogisConstants.JOB_STATUS_PICKING, null);
		String sql = this.pdasPickQueryStore.getFindStationPickingJobQuery();
		List<String> workingCells = this.queryManager.selectListBySql(sql, queryParams, String.class, 1, 1);
		boolean hasWorkingJob = ValueUtil.isNotEmpty(workingCells);
		
		if(hasWorkingJob && exceptionWhenFound) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("WORK_ZONE_CELL_WORKING","작업 존의 셀 [{0}]에 현재 피킹 중인 작업이 있습니다.",ValueUtil.toList(workingCells.get(0))));
		}
		
		// 작업에 이상이 없다면 true 리턴
		return !hasWorkingJob;
	}
	
	/**
	 * 중분류 작업 조회
	 * 
	 * @param batch
	 * @param stationCd
	 * @param comCd
	 * @param skuCd
	 * @return
	 */
	protected JobInstance findMiddleAssortJob(JobBatch batch, String stationCd, String comCd, String skuCd) {
		// 다른 작업 스테이션에 상품이 매핑된 주문이 있는지 체크
		Map<String, Object> queryParams = this.createQueryParams(batch, comCd, skuCd, stationCd, LogisConstants.JOB_STATUS_WAIT, null);
		String sql = this.pdasPickQueryStore.getFindMiddleAssortQuery();
		List<JobInstance> jobList = this.queryManager.selectListBySql(sql, queryParams, JobInstance.class, 1, 1);
		JobInstance job = ValueUtil.isEmpty(jobList) ? null : jobList.get(0);
		
		if(job != null) {
			// 중분류 플래그 추가
			job.setBoxClassCd(job.getStationCd());
			// 중분류 되었다는 플래그 추가
			job.setStatus("M");
		}
		
		return job;
	}
	
	/**
	 * 다른 작업 스테이션에 소속된 SKU인지 체크
	 * 
	 * @param batch
	 * @param comCd
	 * @param skuCd
	 * @return
	 */
	protected boolean checkSkuBelongsToOtherStation(JobBatch batch, String comCd, String skuCd, String stationCd) {
		Map<String, Object> queryParams = this.createQueryParams(batch, comCd, skuCd, stationCd, LogisConstants.JOB_STATUS_WAIT, null);
		String sql = this.pdasPickQueryStore.getCheckOtherStationMappingJobQuery();
		return this.queryManager.selectSizeBySql(sql, queryParams) > 0;
	}
	
	/**
	 * 작업 배치 주문 내에 존재하는 상품인지 체크
	 * 
	 * @param batch
	 * @param comCd
	 * @param skuCd
	 * @param exceptionWhenInvalid
	 * @return
	 */
	protected boolean checkValidSkuByBatch(JobBatch batch, String comCd, String skuCd, boolean exceptionWhenInvalid) {
		Map<String, Object> queryParams = this.createQueryParams(batch, comCd, skuCd, null, null, null);
		String sql = this.pdasPickQueryStore.getCheckWrongSkuQuery();
		boolean validSku = this.queryManager.selectSizeBySql(sql, queryParams) > 0;
		
		if(exceptionWhenInvalid && !validSku) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("PRODUCT_NOT_EXIST_ORDER"));
		}
		
		return validSku;
 	}
	
	/**
	 * 주문에 박스가 이미 매핑되었는지 여부 체크
	 * 
	 * @param batch
	 * @param classCd
	 * @param exceptionWhenUsed
	 * @return
	 */
	protected boolean checkOrderAlreadyHasBox(JobBatch batch, String classCd, boolean exceptionWhenUsed) {
		Long domainId = batch.getDomainId();
		String sql = "select distinct box_id from job_instances where domain_id = :domainId and batch_id = :batchId and class_cd = :classCd and box_id is not null";
		Map<String, Object> qParams = ValueUtil.newMap("domainId,batchId,classCd", domainId, batch.getId(), classCd);
		
		// 박스 ID 체크 - 이미 사용한 박스 ID인지 체크
		List<String> boxIdList = this.queryManager.selectListBySql(sql, qParams, String.class, 1, 1);
		boolean orderHasBox = ValueUtil.isNotEmpty(boxIdList);
		
		if(exceptionWhenUsed && orderHasBox) {
			String boxId = boxIdList.get(0);
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("ORDER_ALREADY_MAPPED_BOX","주문 [{0}]에 박스 ID [{1}]가 이미 매핑되어 있습니다.",ValueUtil.toList(classCd, boxId)));
		}
		
		// 매핑 여부
		return orderHasBox;
	}
	
	/**
	 * 동일 작업 스테이션에서 동일 상품을 작업할 수 있다는 가정하에 작업 전에 작업 스테이션에 Lock
	 * 
	 * @param domainId
	 * @param rackCd
	 * @param stationCd
	 */
	protected String lockToWorkStation(Long domainId, String rackCd, String stationCd) {
		boolean validStationCd = PdasServiceUtil.isValidStationCode(stationCd);
		
		// 작업 전 Locking
		if(validStationCd) {
			// 작업 스테이션이 있다면 작업 스테이션에 Locking
			AnyEntityUtil.findEntityBy(domainId, true, true, Station.class, "id,equipType,equipCd,stationCd,stationNm,stationType", "stationCd", stationCd);
			return stationCd;
			
		} else {
			// 작업 스테이션이 없다면 호기에 Locking
			AnyEntityUtil.findEntityByCodeWithLock(domainId, true, Rack.class, "rackCd", rackCd);
			return null;
		}
	}

}
