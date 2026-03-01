package operato.logis.sps.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.logis.sps.query.store.SpsQueryStore;
import operato.logis.sps.service.api.ISpsPickingService;
import operato.logis.sps.service.util.SpsBatchJobConfigUtil;
import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.entity.SKU;
import xyz.anythings.base.event.IClassifyRunEvent;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 단포 처리 포함한 피킹 서비스 트랜잭션 구현
 * 
 * @author shortstop
 */
@Component("spsPickingService")
public class SpsPickingService extends AbstractLogisService implements ISpsPickingService {

	/**
	 * 단포 쿼리 스토어
	 */
	@Autowired
	private SpsQueryStore spsQueryStore;
	/**
	 * 작업 상태 조회 서비스
	 */
	@Autowired
	private SpsJobStatusService jobStatusService;
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;
	
	/**
	 * 커스텀 서비스 - 검수 박스 투입 전 체크
	 */
	private static final String DIY_BEFORE_INPUT_BOX_CHECK = "diy-sps-pre-input-box-check";
	
	/**
	 * 커스텀 서비스 - 박스 투입 전
	 */
	private static final String DIY_BEFORE_INPUT_BOX = "diy-sps-pre-input-box";
	/**
	 * 커스텀 서비스 - 박스 투입 후
	 */
	private static final String DIY_AFTER_INPUT_BOX = "diy-sps-post-input-box";
	
	@Override
	public String getJobType() {
		return LogisConstants.JOB_TYPE_SPS;
	}

	@Override
	public JobConfigSet getJobConfigSet(String batchId) {
		return this.serviceDispatcher.getConfigSetService().getConfigSet(batchId);
	}

	@Override
	public JobBatch updateProgressRate(JobBatch batch) {
		// 실적 요약 정보 조회
		String query = this.spsQueryStore.getBatchProgressRateQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		JobBatch jobRate = this.queryManager.selectBySql(query, params, JobBatch.class);
		
		// 작업 배치에 실적 업데이트
		batch.setResultOrderQty(jobRate.getResultOrderQty());
		batch.setResultBoxQty(jobRate.getResultBoxQty());
		batch.setResultSkuQty(jobRate.getResultSkuQty());
		batch.setResultPcs(jobRate.getResultPcs());
		batch.setProgressRate(jobRate.getProgressRate());
		this.queryManager.update(batch, "resultOrderQty", "resultBoxQty", "resultSkuQty", "resultPcs", "progressRate");
		
		// 작업 배치 리턴
		return batch;
	}

	@Override
	public String checkInput(JobBatch batch, String inputId, Object... params) {
		// 1. 투입 ID가 상품 코드 인지 체크 
		if(SpsBatchJobConfigUtil.isSkuCdValid(batch, inputId)) {
			return LogisCodeConstants.INPUT_TYPE_SKU_CD;
		}
		
		// 2. 투입 ID가 박스 코드 인지 체크
		if(SpsBatchJobConfigUtil.isBoxIdValid(batch, inputId)) {
			return LogisCodeConstants.INPUT_TYPE_BOX_ID;
		}
		
		// 3. 투입 ID가 셀 코드 인지 체크
		if(SpsBatchJobConfigUtil.isCellCdValid(batch, inputId)) {
			return LogisCodeConstants.INPUT_TYPE_CELL_CD;
		}
		
		// 4. 투입 ID가 표시기 코드 인지 체크
		if(SpsBatchJobConfigUtil.isIndCdValid(batch, inputId)) {
			return LogisCodeConstants.INPUT_TYPE_IND_CD;
		}
		
		// 5. 투입 ID가 표시기 코드 인지 체크
		if(SpsBatchJobConfigUtil.isRackCdValid(batch, inputId)) {
			return LogisCodeConstants.INPUT_TYPE_RACK_CD;
		}
		
		return null;
	}
	

	@Override
	public Object inputBoxCheckWithInsp(JobBatch batch, String comCd, String skuCd, String boxId, String boxTypeCd, Integer orderQty, Object... params) {
		// 1. 상품 Lock
		Long domainId = batch.getDomainId();
		Query query = AnyOrmUtil.newConditionForExecution(domainId);
		query.addFilter("comCd", comCd);
		query.addFilter("skuCd", skuCd);
		SKU sku = this.queryManager.selectByCondition(SKU.class, query);
		
		if(sku == null) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.SKU");
		}
		
		// 2. 커스텀 서비스 실행
		Object retVal = this.customService.doCustomService(batch.getDomainId(), DIY_BEFORE_INPUT_BOX_CHECK, ValueUtil.newMap("batch,comCd,skuCd,boxId,boxTypeCd,orderQty", batch, comCd, skuCd, boxId, boxTypeCd, orderQty));
		JobInstance job = null;
		
		// 3. 박스와 매핑하고자 하는 작업 정보를 조회한다.
		if(retVal == null) {
			// 2.1 작업 정보 조회
			String instanceId = this.findNextMappingJob(batch, comCd, skuCd, boxTypeCd, boxId, orderQty);
			job = AnyEntityUtil.findEntityById(false, JobInstance.class, instanceId);
			// 2.2 박스 투입 가능 여부 확인
			this.checkUsableBox(batch, job, boxId, boxTypeCd);
			
		// 4. 커스텀 서비스에서 지정한 작업 정보가 있다면 그걸 이용한다.
		} else {
			job = (JobInstance)retVal;
		}
		
		// 5. 작업을 찾지 못한 경우
		if(job == null) {
			// 투입 가능한 주문이 없습니다.
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage(MessageUtil.getMessage("LOGIS_NO_ORDER_TO_INPUT")));
		}
		
		// 5. 결과 리턴
		return job;
	}	

	@Override
	public Object inputBox(JobBatch batch, String comCd, String skuCd, String boxId, String boxTypeCd, Object... params) {
		// 1. 상품 Lock
		Long domainId = batch.getDomainId();
		Query query = AnyOrmUtil.newConditionForExecution(domainId);
		query.addFilter("comCd", comCd);
		query.addFilter("skuCd", skuCd);
		SKU sku = this.queryManager.selectByCondition(SKU.class, query);
		
		if(sku == null) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.SKU");
		}
		
		// 2. 박스 투입 전 체크 - 작업 조회
		JobInstance job = this.beforeInputBox(batch, comCd, skuCd, boxId, boxTypeCd);
		
		// 3. 기존 작업에 대한 재 작업인 경우
		if(ValueUtil.isEqualIgnoreCase(job.getBoxId(), boxId)) {
			return job;
		}
		
		// 4. 작업 / 주문 정보 업데이트
		int inputSeq = ValueUtil.toInteger(job.getInputSeq(), 0);
		if(inputSeq == 0) {
			// 4.1 작업 정보 업데이트
			inputSeq = this.jobStatusService.findNextInputSeq(batch);
			job.setInputSeq(inputSeq);
			job.setBoxId(boxId);
			job.setStatus(LogisConstants.JOB_STATUS_PICKING);
			job.setInputAt(DateUtil.currentTimeStr());
			if(ValueUtil.isEmpty(job.getBoxTypeCd())) {
				job.setBoxTypeCd(boxTypeCd);
			}
			this.queryManager.update(job, "inputSeq", "boxTypeCd", "boxId", "status", "inputAt", "updaterId", "updatedAt");
			
			// 4.2 주문 정보 업데이트
			Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,boxId,classCd", domainId, batch.getId(), boxId, job.getClassCd());
			String sql = "update orders set box_id = :boxId where domain_id = :domainId and batch_id = :batchId and class_cd = :classCd";
			this.queryManager.executeBySql(sql, condition);
		}
		
		// 5. 박스 투입 후 액션
		this.afterInputBox(batch, job);
		
		// 6. 작업 리턴
		return job;
	}

	@Override
	public void confirmPick(IClassifyRunEvent exeEvent) {
		JobBatch batch = exeEvent.getJobBatch();
		// 1. JobInstance 조회 
		JobInstance job = exeEvent.getJobInstance();
		// 2. 확정 처리 
		this.confirmPick(batch, job, exeEvent.getResQty());
		// 3. 실행 여부 체크
		exeEvent.setExecuted(true);
	}
	

	@Override
	public void confirmPickWithInsp(IClassifyRunEvent exeEvent) {
		JobBatch batch = exeEvent.getJobBatch();
		// 1. JobInstance 조회 
		JobInstance job = exeEvent.getJobInstance();
		
		String boxId = job.getBoxId();
		String boxTypeCd = job.getBoxTypeCd();
		Long domainId = job.getDomainId();
		
		// 2.박스ID 사용여부 확인 
		this.checkUsableBox(batch, job, boxId, boxTypeCd);
		
		// 3. 작업 상태 확인
		if(ValueUtil.isNotEqual(job.getStatus(), LogisConstants.JOB_STATUS_WAIT)) {
			throw new ElidomServiceException(MessageUtil.getMessage("WORK_ALREADY_PROCESSED"));
		}
		
		// 4. 작업 / 주문 정보 업데이트
		int inputSeq = ValueUtil.toInteger(job.getInputSeq(), 0);
		if(inputSeq == 0) {
			// 4.1 작업 정보 업데이트
			inputSeq = this.jobStatusService.findNextInputSeq(batch);
			job.setInputSeq(inputSeq);
			job.setStatus(LogisConstants.JOB_STATUS_PICKING);
			job.setInputAt(DateUtil.currentTimeStr());
			if(ValueUtil.isEmpty(job.getBoxTypeCd())) {
				job.setBoxTypeCd(boxTypeCd);
			}
			
			this.queryManager.update(job, "inputSeq", "boxTypeCd", "boxId", "status", "inputAt", "updaterId", "updatedAt");
			
			// 4.2 주문 정보 업데이트
			Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,boxId,classCd", domainId, batch.getId(), boxId, job.getClassCd());
			String sql = "update orders set box_id = :boxId where domain_id = :domainId and batch_id = :batchId and class_cd = :classCd";
			this.queryManager.executeBySql(sql, condition);
		}
		
		// 5. 확정 처리 
		this.confirmPick(batch, job, exeEvent.getResQty());
		// 6. 실행 여부 체크
		exeEvent.setExecuted(true);
	}


	@Override
	public BoxPack cancelBoxing(Long domainId, String boxPackId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkBoxingEnd(JobBatch batch, String orderNo, String boxId) {
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		condition.addFilter("batchId", batch.getId());
		condition.addFilter("orderNo", orderNo);
		if(ValueUtil.isNotEmpty(boxId)) {
			condition.addFilter("boxId", boxId);
		}
		condition.addFilter("status", LogisConstants.IN, LogisConstants.JOB_STATUS_WIP);
		return this.queryManager.selectSize(JobInstance.class, condition) == 0;
	}
	
	/************************************************************************************************/
	/*											박스 투입 												*/
	/************************************************************************************************/

	/**
	 * 박스 투입 전 액션
	 * 
	 * @param batch
	 * @param comCd
	 * @param skuCd
	 * @param boxId
	 * @param boxTypeCd
	 * @return
	 */
	protected JobInstance beforeInputBox(JobBatch batch, String comCd, String skuCd, String boxId, String boxTypeCd) {
		// 1. 커스텀 서비스 실행
		Object retVal = this.customService.doCustomService(batch.getDomainId(), DIY_BEFORE_INPUT_BOX, ValueUtil.newMap("batch,comCd,skuCd,boxId,boxTypeCd", batch, comCd, skuCd, boxId, boxTypeCd));
		JobInstance job = null;
		
		// 2. 박스와 매핑하고자 하는 작업 정보를 조회한다.
		if(retVal == null) {
			// 2.1 작업 정보 조회
			String instanceId = this.findNextMappingJob(batch, comCd, skuCd, boxTypeCd, boxId, null);
			job = AnyEntityUtil.findEntityById(false, JobInstance.class, instanceId);
			// 2.2 박스 투입 가능 여부 확인
			this.checkUsableBox(batch, job, boxId, boxTypeCd);
			
		// 3. 커스텀 서비스에서 지정한 작업 정보가 있다면 그걸 이용한다.
		} else {
			job = (JobInstance)retVal;
		}
		
		// 4. 작업을 찾지 못한 경우
		if(job == null) {
			// 투입 가능한 주문이 없습니다.
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage(MessageUtil.getMessage("LOGIS_NO_ORDER_TO_INPUT")));
		}
		
		// 5. 결과 리턴
		return job;
	}
	
	/**
	 * 박스 투입 후 액션
	 * 
	 * @param batch
	 * @param job
	 */
	protected void afterInputBox(JobBatch batch, JobInstance job) {
		// 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_AFTER_INPUT_BOX, ValueUtil.newMap("batch,job", batch, job));
	}
	
	/**
	 * 단포 주문의 투입될 박스와 매핑될 작업 정보를 조회
	 * 
	 * @param batch
	 * @param comCd
	 * @param skuCd
	 * @param boxTypeCd
	 * @param boxId
	 * @return
	 */
	protected String findNextMappingJob(JobBatch batch, String comCd, String skuCd, String boxTypeCd, String boxId, Integer orderQty) {
		// 1. 박스 매핑 컬럼 조회
		String boxMappingColumn = SpsBatchJobConfigUtil.getBoxMappingTargetField(batch);
		
		// 2. 다음 처리할 작업 조회, TODO 체크 - 쿼리에 boxId 조건이 없음
		String sql = this.spsQueryStore.getFindNextMappingJobQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,mapColumn,batchId,boxId,boxTypeCd,comCd,skuCd", batch.getDomainId(), boxMappingColumn, batch.getId(), boxId, boxTypeCd, comCd, skuCd);
		
		if(ValueUtil.isNotEmpty(orderQty)) {
			params.put("orderQty", orderQty);
		}
		
		String nextJobId = AnyEntityUtil.findItem(batch.getDomainId(), false, String.class, sql, params);
		
		// 3. 존재하지 않은 경우 에러
		if(ValueUtil.isEmpty(nextJobId)) {
			// 박스에 할당할 주문 정보가 존재하지 않습니다
			throw new ElidomRuntimeException(MessageUtil.getMessage(MessageUtil.getMessage("LOGIS_NO_ORDER_TO_ASSIGN_BOX")));
		}

		return nextJobId;
	}
	
	/**
	 * 박스의 사용 가능 여부를 확인
	 * 
	 * @param batch
	 * @param boxId
	 * @param boxTypeCd
	 */
	protected void checkUsableBox(JobBatch batch, JobInstance job, String boxId, String boxTypeCd) {
		// 1. 박스 ID 유니크 범위 설정
		String uniqueScope = SpsBatchJobConfigUtil.getBoxIdUniqueScope(batch, LogisConstants.BOX_ID_UNIQUE_SCOPE_GLOBAL);
		
		// 2. 파라미터 셋팅 
		Map<String, Object> params = ValueUtil.newMap("domainId,boxId,batchId,uniqueScope", batch.getDomainId(), boxId, batch.getId(), uniqueScope);
		
		// 3. 중복 박스 ID가 존재하는지 쿼리
		String sql = this.spsQueryStore.getBoxIdUniqueCheckQuery();
		
		// 4. 존재하지 않으면 사용 가능
		boolean usedBoxId = this.queryManager.selectBySql(sql, params, Integer.class) > 0;
		
		// 5. 사용한 박스이면 예외 발생
		if(usedBoxId) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("BOX_ID_ALREADY_USED","박스 ID [{0}]는 이미 사용한 박스입니다.",ValueUtil.toList(boxId)));
		}
		
		// 6. 기존에 맵핑된 작업을 재 사용하는 것이 아니면 이미 처리된 박스인 지 체크, TODO 이 로직 체크
		if(ValueUtil.isNotEmpty(job.getBoxId()) && ValueUtil.isNotEqual(job.getBoxId(), boxId)) {
			Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
			condition.addFilter("batchId", batch.getId());
			condition.addFilter("boxId", boxId);
			condition.addFilter("status", "notin", LogisConstants.JOB_STATUS_WIPC);
			
			if(this.queryManager.selectSize(JobInstance.class, condition) > 0) {
				// 이미 처리된 항목입니다.
				throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("ALREADY_BEEN_PROCEEDED"));
			}
		}
	}
	
	/************************************************************************************************/
	/*											소 분 류												*/
	/************************************************************************************************/
	/**
	 * 작업 확정 처리
	 * 
	 * @param batch
	 * @param job
	 * @param resQty
	 */
	private void confirmPick(JobBatch batch, JobInstance job, int resQty) {
		// 1. 작업 상태 체크
		if(job.isDoneJob()) {
			throw new ElidomServiceException(MessageUtil.getMessage("WORK_ALREADY_PROCESSED"));
		}
		
		if(ValueUtil.isNotEqual(job.getStatus(), LogisConstants.JOB_STATUS_WAIT) && ValueUtil.isNotEqual(job.getStatus(), LogisConstants.JOB_STATUS_PICKING)) {
			throw new ElidomServiceException(MessageUtil.getMessage("CONFIRM_PROCESS_ONLY_PICKING_OR_WAIT_STATUS"));
		}
		
		// 2. 작업 처리 전 액션 
		int pickQty = this.beforeConfirmPick(batch, job, resQty);
		
		if(pickQty > 0) {
			// 3. 분류 작업 처리
			this.doConfirmPick(batch, job, pickQty);
			// 4. 작업 처리 후 액션
			this.afterComfirmPick(batch, job, pickQty);
		}
	}
	
	/**
	 * 소분류 작업 처리 전 처리 액션
	 * 
	 * @param batch
	 * @param job
	 * @param pickedQty
	 * @return
	 */
	protected int beforeConfirmPick(JobBatch batch, JobInstance job, int pickedQty) {
		// 1. 작업이 이미 완료되었다면 리턴
		if(job.isDoneJob()) {
			return 0;
		// 2. 이미 모두 처리되었다면 스킵
		} else if(job.getPickedQty() >= job.getPickQty()) {
			return 0;
		}
		
		// 3. 피킹 수량 보정 - 주문 수량 보다 처리 수량이 큰 경우 차이 값 만큼만 처리
		if(job.getPickedQty() + pickedQty > job.getPickQty()) {
			pickedQty = job.getPickQty() - job.getPickedQty();
		}
		
		// 4. 피킹 수량 리턴
		return pickedQty;
	}
	
	/**
	 * 소분류 작업 처리
	 * 
	 * @param batch
	 * @param job
	 * @param pickedQty
	 */
	protected void doConfirmPick(JobBatch batch, JobInstance job, int pickedQty) {
		job.setPickedQty(job.getPickedQty() == null ? pickedQty : job.getPickedQty() + pickedQty);
		job.setPickingQty(0);
		String currentTime = DateUtil.currentTimeStr();
		
		if(ValueUtil.isEmpty(job.getInputAt())) {
			job.setInputAt(currentTime);
		}
		
		if(ValueUtil.isEmpty(job.getPickStartedAt())) {
			job.setPickStartedAt(currentTime);
		}
		
		if(job.getPickedQty() >= job.getPickQty()) {
			job.setStatus(LogisConstants.JOB_STATUS_FINISH);
			job.setPickEndedAt(currentTime);
		}
		
		this.queryManager.update(job, "inputAt", "pickStartedAt", "pickEndedAt", "pickedQty", "pickingQty", LogisConstants.ENTITY_FIELD_STATUS, LogisConstants.ENTITY_FIELD_UPDATER_ID, LogisConstants.ENTITY_FIELD_UPDATED_AT);
	}

	/**
	 * 소분류 작업 처리 후 처리 액션
	 * 
	 * @param batch
	 * @param job
	 * @param pickQty
	 */
	protected void afterComfirmPick(JobBatch batch, JobInstance job, Integer pickQty) {
		// 1. 주문(박스)에 대한 피킹이 모두 완료되었는지 체크
		if(this.checkBoxingEnd(batch, job.getOrderNo(), job.getBoxId())) {
			// 1-1. 작업의 '상태' 및 '박싱 시각'을 업데이트
			String sql = "update job_instances set status = :status, boxed_at = :currentTime, inspected_qty = pick_qty, manual_insp_status = 'P', manual_inspected_at = :currentTime where domain_id = :domainId and batch_id = :batchId and class_cd = :classCd";
			Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,classCd,status,currentTime,comCd,skuCd", job.getDomainId(), job.getBatchId(), job.getClassCd(), LogisConstants.JOB_STATUS_BOXED, job.getPickEndedAt(), job.getComCd(), job.getSkuCd());
			this.queryManager.executeBySql(sql, condition);
			
			// 1-2. 작업 정보와 연관된 주문을 조회해서 피킹 확정 수량을 업데이트한다.
			condition.put("status", Order.STATUS_FINISHED);
			sql = "update orders set picked_qty = order_qty, status = :status where DOMAIN_ID = :domainId AND BATCH_ID = :batchId AND CLASS_CD = :classCd AND COM_CD = :comCd AND SKU_CD = :skuCd";
			this.queryManager.executeBySql(sql, condition);
		}
	}

}
