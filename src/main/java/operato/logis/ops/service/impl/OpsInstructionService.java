package operato.logis.ops.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Order;
import xyz.anythings.base.event.main.BatchInstructEvent;
import xyz.anythings.base.service.api.IInstructionService;
import xyz.anythings.base.service.impl.AbstractInstructionService;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 출고용 작업지시 서비스
 * 
 * @author shortstop
 */
@Component("opsInstructionService")
public class OpsInstructionService extends AbstractInstructionService implements IInstructionService {
	
	/**
	 * 커스텀 서비스 - 대상 분류
	 */
	private static final String DIY_CLASSIFY_ORDERS = "diy-ops-classify-orders";
	/**
	 * 커스텀 서비스 - 작업 지시 전 처리
	 */
	private static final String DIY_PRE_BATCH_START = "diy-ops-pre-batch-start";
	/**
	 * 커스텀 서비스 - 작업 지시 후 처리
	 */
	private static final String DIY_POST_BATCH_START = "diy-ops-post-batch-start";
	/**
	 * 커스텀 서비스 - 작업 지시 취소 전 처리
	 */
	private static final String DIY_PRE_CANCEL_BATCH = "diy-ops-pre-cancel-batch";
	/**
	 * 커스텀 서비스 - 작업 지시 취소 후 처리
	 */
	private static final String DIY_POST_CANCEL_BATCH = "diy-ops-post-cancel-batch";
	
	/**
	 * WCS 대상 분류 Event 처리 
	 * @param event
	 */
	@EventListener(classes = BatchInstructEvent.class, condition = "#event.eventType == 40 and #event.eventStep == 3 and #event.jobType == 'OPS'")
	public void targetClassing(BatchInstructEvent event) { 
		// 커스텀 서비스 호출
		Map<String, Object> diyParams = ValueUtil.newMap("domainId,waveId,isLast", event.getDomainId(), event.getPayload()[0], event.getPayload()[1]);
		this.customService.doCustomService(event.getDomainId(), DIY_CLASSIFY_ORDERS, diyParams);
		event.setExecuted(true);
	}
	
	@Override
	public Map<String, Object> searchInstructionData(JobBatch batch, Object... params) {
		return null;
	}
	
	@Override
	public int instructTotalpicking(JobBatch batch, List<String> equipIdList, Object... params) {
		return 0;
	}
	
	@Override
	public int mergeBatch(JobBatch mainBatch, JobBatch newBatch, Object... params) {
		return 1;
	}
	
	@Override
	public int instructBatch(JobBatch batch, List<String> equipCdList, Object... params) {
		int instructCount = 0;

		if(this.beforeInstructBatch(batch, equipCdList)) {
			instructCount += this.doInstructBatch(batch, equipCdList);
			this.afterInstructBatch(batch, equipCdList);
		}

		return instructCount;
	}

	@Override
	public int cancelInstructionBatch(JobBatch batch) {
		int cancelCount = 0;
		
		if(this.beforeCancelInstructionBatch(batch)) {
			cancelCount += this.doCancelInstructionBatch(batch);
			this.afterCancelInstructionBatch(batch);
		}
		
		return cancelCount;
	}
	
	/**
	 * 작업 지시 취소 전 처리 액션
	 * 
	 * @param batch
	 * @return
	 */
	protected boolean beforeCancelInstructionBatch(JobBatch batch) {
		// 배치 취소 전 처리 커스텀 서비스 호출 -> 커스텀 서비스에서 작업지시 취소 여부 판단
		this.customService.doCustomService(batch.getDomainId(), DIY_PRE_CANCEL_BATCH, ValueUtil.newMap("batch", batch));
		
		// 2. 배치 상태가 작업 지시 상태인지 체크
		if(ValueUtil.isNotEqual(batch.getStatus(), JobBatch.STATUS_RUNNING)) {
			// '진행 중' 상태가 아닙니다.
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("BATCH_NOT_PROCESS_WORK"));
		}
		
		// 3. 주문에 NULL 혹은 WAIT 상태가 아닌 상태가 하나라도 있는지 체크 
		String sql = "select id from orders where domain_id = :domainId and batch_id = :batchId and status is not null and status != :status";
		Map<String, Object> checkParams = ValueUtil.newMap("domainId,batchId,status", batch.getDomainId(), batch.getId(), LogisConstants.JOB_STATUS_WAIT);
		if(this.queryManager.selectSizeBySql(sql, checkParams) > 0) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NOT_ALLOWED_CANCEL_AFTER_START_JOB"));
		}
		
		return true;
	}
	
	/**
	 * 작업 지시  취소 처리 로직
	 * 
	 * @param batch
	 * @return
	 */
	protected int doCancelInstructionBatch(JobBatch batch) {
		// 1. 작업 배치 정보 조회
		Long domainId = batch.getDomainId();
		
		// 2. 작업 배치 정보 업데이트
		batch.setStatus(JobBatch.STATUS_READY);
		batch.setInstructedAt(null);
		this.queryManager.update(batch, "status", "instructedAt");
		
		// 3. 주문 상태 업데이트
		String sql = "update orders set status = null where domain_id = :domainId and batch_id = :batchId";
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", domainId, batch.getId());
		return this.queryManager.executeBySql(sql, params);
	}
	
	/**
	 * 배치 취소 후 처리 액션
	 * 
	 * @param batch
	 * @return
	 */
	protected void afterCancelInstructionBatch(JobBatch batch) {
		// 1. 배치 취소 후 처리 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_POST_CANCEL_BATCH, ValueUtil.newMap("batch", batch));
		
		// 2. 작업 지시 취소 이벤트 전송
		this.publishInstructionCancelEvent(SysEvent.EVENT_STEP_AFTER, batch, null);
	}
	
	/**
	 * 작업 지시 전 처리 액션
	 *
	 * @param batch
	 * @param rackList
	 * @return
	 */
	protected boolean beforeInstructBatch(JobBatch batch, List<String> equipIdList) {
		// 1. 배치 시작 전 처리 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_PRE_BATCH_START, ValueUtil.newMap("batch", batch));

		// 2. 배치 상태가 작업 지시 상태인지 체크
		if(ValueUtil.isNotEqual(batch.getStatus(), JobBatch.STATUS_READY)) {
			// '작업 지시 대기' 상태가 아닙니다.
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getTerm("terms.text.is_not_wait_state", "JobBatch status is not 'READY'"));
		}
		
		// 3. 동일 날짜에 동일 차수에 진행 중인 오더 피킹이 있는지 체크
		String sql = "select id from job_batches where domain_id = :domainId and job_type = :jobType and status = :status";
		Map<String, Object> checkParams = ValueUtil.newMap("domainId,jobType,status", batch.getDomainId(), batch.getJobType(), JobBatch.STATUS_RUNNING);
		if(this.queryManager.selectSizeBySql(sql, checkParams) > 0) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("RUNNING_ORDER_PICKING_BATCH_PLZ_COMPLETE_BEFORE_BATCH"));
		}

		return true;
	}
	
	/**
	 * 작업 지시 처리 로직
	 *
	 * @param batch
	 * @param equipCdList
	 * @return
	 */
	protected int doInstructBatch(JobBatch batch, List<String> equipCdList) {
		// 1. 랙 조회
		Long domainId = batch.getDomainId();
		
		// 2. 배치 상태 업데이트
		batch.setStatus(JobBatch.STATUS_RUNNING);
		batch.setInstructedAt(new Date());
		
		// 3. 배치에 설비 코드가 없다면 설비 그룹에서 조회하여 하나만 나온다면 자동 설정 
		if(ValueUtil.isEmpty(batch.getEquipCd())) {
			String equipCd = ValueUtil.isEmpty(equipCdList) ? null : equipCdList.get(0);
			
			// 배치에 설비 코드가 설정되지 않았다면 배치에 설정된 설비 그룹에서 단포 호기를 조회 
			if(equipCd == null) {
				Map<String, Object> queryParams = ValueUtil.newMap("domainId,equipGroupCd,jobType,activeFlag", domainId, batch.getEquipGroupCd(), batch.getJobType(), true);
				String sql = "select rack_cd from racks where domain_id = :domainId and equip_group_cd = :equipGroupCd and job_type = :jobType and active_flag = :activeFlag";
				equipCdList = this.queryManager.selectListBySql(sql, queryParams, String.class, 0, 0);
				if(ValueUtil.isNotEmpty(equipCdList) && equipCdList.size() == 1) {
					equipCd = equipCdList.get(0);
				}
			}
			
			batch.setEquipCd(equipCd);
		}
		
		// 4. 배치에 호기 선택이 없다면 에러 ...
		if(ValueUtil.isEmpty(batch.getEquipCd())) {
			throw ThrowUtil.newValidationErrorWithNoLog(true, "RACK_NOT_EXIST");
		}
		
		this.queryManager.update(batch, "status", "equipCd", "instructedAt");
		
		// 5. 주문 상태 변경
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,status,equipCd", domainId, batch.getId(), Order.STATUS_WAIT, batch.getEquipCd());
		String sql = "update orders set status = :status, equip_type = 'Rack', equip_cd = :equipCd where domain_id = :domainId and batch_id = :batchId";
		return this.queryManager.executeBySql(sql, params);
	}
	
	/**
	 * 작업 지시 후 처리 액션
	 *
	 * @param batch
	 * @param rackList
	 * @return
	 */
	protected boolean afterInstructBatch(JobBatch batch, List<String> equipIdList) {
		// 1. 배치 시작 후 처리 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), DIY_POST_BATCH_START, ValueUtil.newMap("batch", batch));
		
		// 2. 작업 지시 이벤트 전송
		this.publishInstructionEvent(SysEvent.EVENT_STEP_AFTER, batch, equipIdList);
		return true;
	}

}
