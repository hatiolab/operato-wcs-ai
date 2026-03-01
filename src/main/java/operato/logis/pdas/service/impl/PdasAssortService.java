package operato.logis.pdas.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.logis.pdas.service.util.PdasServiceUtil;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.service.util.RuntimeIndServiceUtil;
import xyz.anythings.gw.entity.IndConfigSet;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * PDAS 분류 서비스
 * 
 * @author shortstop
 */
@Component
public class PdasAssortService extends PdasBaseService {
	
	@Override
	public void batchStartAction(JobBatch batch) {
		// 1. 작업 설정 셋 추가 
		this.serviceDispatcher.getConfigSetService().addConfigSet(batch);
		
		// 2. 표시기 설정 셋 추가
		IndConfigSet configSet = batch.getIndConfigSet() != null ? batch.getIndConfigSet() : (ValueUtil.isEmpty(batch.getIndConfigSetId()) ? null : this.queryManager.select(IndConfigSet.class, batch.getIndConfigSetId()));
		this.indConfigSetService.addConfigSet(batch.getId(), configSet);
		
		// 3. 낱개 피킹 모드이면 작업 데이터를 모두 1PCS로 분리한다.
		if(this.isPiecePickingMode(batch)) {
			this.splitJobInstancesByPiece(batch);
		}
		
		// 4. 표시기 소등
		if(this.isUseIndicator(batch)) {
			this.serviceDispatcher.getIndicationService(batch).indicatorOffAll(batch, true);
		}
	}
	
	@Override
	public void batchCloseAction(JobBatch batch) {
		// 1. 표시기 설정 셋 제거
		this.indConfigSetService.clearConfigSet(batch.getId());
		
		// 2. 작업 설정 셋 제거 
		this.serviceDispatcher.getConfigSetService().clearConfigSet(batch.getId());
	}
	
	@Override
	public JobInstance checkCellJobStatus(JobBatch batch, String stationCd, String cellCd) {
		Long domainId = batch.getDomainId();
		
		// 1. 셀 조회
		Cell cell = this.findCellToWork(domainId, batch.getEquipCd(), stationCd, cellCd, false);
		String classCd = cell.getClassCd();
		
		// 2. 분류 코드가 없다면 셀이 빈 상태
		if(ValueUtil.isEmpty(classCd)) {
			JobInstance job = new JobInstance();
			job.setStatus(LogisConstants.N_CAP_STRING);
			return job;
		}
		
		// 3. 셀에 작업이 매핑된 상태인지 체크
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,cellCd,classCd,statuses", domainId, batch.getId(), cell.getCellCd(), classCd, LogisConstants.JOB_STATUS_BERC);
		String sql = "select *  from job_instances where domain_id = :domainId and batch_id = :batchId and sub_equip_cd = :cellCd and class_cd = :classCd";
		List<JobInstance> jobList = this.queryManager.selectListBySql(sql, params, JobInstance.class, 0, 0);
		boolean isBoxMapped = true;
		boolean isAllJobFinished = true;
		
		// 4. 주문 체크
		for(JobInstance j : jobList) {
			if(ValueUtil.isEmpty(j.getBoxId())) {
				isBoxMapped = false;
			}
			
			if(!LogisConstants.JOB_STATUS_FC.contains(j.getStatus())) {
				isAllJobFinished = false;
			}
		}
		
		JobInstance job = jobList.get(0);
		
		// 5. 주문 분류 완료 상태
		if(isBoxMapped) {
			job.setStatus(LogisConstants.JOB_STATUS_BOXED);
			return job;
			
		// 6. 작업이 진행 중인 상태
		} else {
			job.setStatus(isAllJobFinished ? LogisConstants.JOB_STATUS_BOXED : LogisConstants.JOB_STATUS_PICKING);
			return job;
		}
	}
	

	@Override
	public BoxPack checkBoxStatus(JobBatch batch, String stationCd, String boxId) {
		BoxPack box = new BoxPack();
		boolean isUsedBox = this.boxService.isUsedBoxId(batch, boxId, false);
		box.setStatus(isUsedBox ? "E" : "N");
		return box;
	}
	
	@Override
	public JobInstance findJobToAssort(JobBatch batch, String comCd, String skuCd, String stationCd) {
		Long domainId = batch.getDomainId();
		
		// 1. SKU가 배치 내에 포함되어 있는지 체크
		this.checkValidSkuByBatch(batch, comCd, skuCd, true);
		
		// 2. 작업 전 Locking - 동일 작업 스테이션에서 동일 상품을 작업할 수 있다는 가정하에 작업 전에 작업 스테이션에 Lock
		stationCd = this.lockToWorkStation(domainId, batch.getEquipCd(), stationCd);
		
		// 3. 동일 작업 스테이션에 동시 작업이 불가능하므로 현재 피킹 중인 작업이 있는지 체크
		this.checkWorkStationPicking(batch, stationCd, true);
		
		// 4. 상품으로 해당 작업 스테이션 내에 존재하는 (이미 셀에 매핑된) 주문 조회
		JobInstance job = this.findWorkingOrderJob(batch, comCd, skuCd, stationCd);
		
		// 5. 작업 스테이션 내 이미 매핑된 작업이 없다면 신규 주문이 존재하는지 체크하여 존재한다면 작업 데이터 생성
		if(job == null) {
			// 5.1 작업 스테이션 내 신규 매핑할 주문이 존재하는 지 체크
			job = this.findWaitingOrderJob(batch, comCd, skuCd, stationCd);
			
			// 5.2 작업 스테이션 내 신규 주문이 존재한다면 적치할 셀 조회
			if(job != null) {
				// 5.2.1 다음 매핑 셀 조회
				String nextWorkingCellCd = this.findNextWorkingCellCd(batch, stationCd, true);
				
				// 5.2.2 주문 - 셀 자동 매핑 모드라면
				if(this.isCellMappingAutoMode(batch)) {
					// 주문 - 셀 매핑 자동 처리
					this.assignJobToCell(batch, job, stationCd, nextWorkingCellCd);
				// 5.2.3 주문 - 셀 매핑 사용자 작업 
				} else {
					job.setStatus("N");
				}
				
				// 5.2.4 작업 리턴
				return job;
				
			// 5.3 작업 스테이션 내 신규 주문이 존재하지 않으면 다른 존에 매핑된 작업인 지 체크
			} else {
				// 5.3.1 중분류 처리 조건인지 판단 - 작업 스테이션이 구분되어 있고 중분류를 하는 설정이라면 중분류 처리
				if(this.isUseMiddleAssorting(batch)) {
					if(!PdasServiceUtil.isValidStationCode(stationCd)) {
						throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("WORK_ZONE_NOT_VALID_MIDDLE_CLASS","중분류 처리할 작업 존[{0}]이 유효하지 않습니다.",ValueUtil.toList(stationCd)));
					}
					
					JobInstance maJob = this.findMiddleAssortJob(batch, stationCd, comCd, skuCd);
					if(maJob != null) {
						// 5.3.2 커스텀 서비스 호출
						this.customService.doCustomService(domainId, CUSTOM_PDAS_MIDDLE_ASSORT_JOB, ValueUtil.newMap("batch,job", batch, maJob));
						// 5.3.3 작업 리턴
						return maJob;
					}
				}
			}
		}
		
		// 6. 그래도 없다면 다른 존에 매핑된 주문의 상품이거나 처리가 완료되었거나 주문에 포함된 상품이 아니다.
		if(job == null) {
			// 중분류 처리를 한다면 이미 앞에서 처리했으므로 스킵
			if(!this.isUseMiddleAssorting(batch)) {
				// case 1 랙 별 스테이션 구분이 되어있다면 다른 존에 매핑된 주문인 지 체크
				if(PdasServiceUtil.isValidStationCode(stationCd)) {
					if(this.checkSkuBelongsToOtherStation(batch, comCd, skuCd, stationCd)) {
						throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("PRODUCT_ORDER_MAPPED_OTHER_ZONE"));
					}
				}
			}
			
			// case 2 상품 분류가 이미 완료된 상황
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("PRODUCT_CLASSIFIED"));

		// 7. 주문 - 셀 수동 매핑인 경우
		} else {
			// 7.1 커스텀 서비스 호출
			this.customService.doCustomService(domainId, CUSTOM_PDAS_FIND_JOB, ValueUtil.newMap("batch,job", batch, job));
			// 7.2 표시기 사용시 신규 주문이 아니면 표시기 점등
			if(this.isUseIndicator(batch) && ValueUtil.isNotEmpty(job.getIndCd())) {
				RuntimeIndServiceUtil.indOnByJob(false, batch, job);
			}
			// 7.3 작업 리턴
			return job;
		}
	}
	
	/**
	 * 주문과 셀 매핑
	 * 
	 * @param batch
	 * @param job
	 * @param stationCd
	 * @param cellCd
	 * @return
	 */
	@Override
	public JobInstance assignJobToCell(JobBatch batch, JobInstance job, String stationCd, String cellCd) {
		Long domainId = batch.getDomainId();
		
		// 1. 셀 조회 (With Lock)
		Cell cell = this.findCellToWork(domainId, job.getEquipCd(), stationCd, cellCd, true);
		
		// 2. 셀에 classCd가 매핑되어 있는지 체크
		if(ValueUtil.isNotEmpty(cell.getClassCd())) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("CELL_WORK_ANOTHER_ORDER"));
		}
		
		// 3. 셀에 분류 코드 설정, 작업에 셀 코드 설정
		cell.setClassCd(job.getClassCd());
		this.queryManager.update(cell, "classCd", "updaterId", "updatedAt");

		// 4. 작업에 매핑 정보 설정 
		job.setSubEquipCd(cellCd);
		job.setIndCd(cell.getIndCd());
		job.setColorCd(cell.getIndColorCd());
		
		// 5. 주문, 작업 정보에 분류 코드로 동일 주문 모두 업데이트
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,classCd,cellCd,indCd,colorCd", domainId, batch.getId(), job.getClassCd(), cellCd, cell.getIndCd(), cell.getIndColorCd());
		String sql = "update orders set sub_equip_cd = :cellCd where domain_id = :domainId and batch_id = :batchId and class_cd = :classCd and (sub_equip_cd is null or sub_equip_cd = '')";
		this.queryManager.executeBySql(sql, params);
		sql = "update job_instances set sub_equip_cd = :cellCd, ind_cd = :indCd, color_cd = :colorCd where domain_id = :domainId and batch_id = :batchId and class_cd = :classCd and (sub_equip_cd is null or sub_equip_cd = '')";
		this.queryManager.executeBySql(sql, params);
		
		// 6. 커스텀 서비스 호출
		this.customService.doCustomService(domainId, CUSTOM_PDAS_ASSIGN_JOB, ValueUtil.newMap("batch,job,cell", batch, job, cell));
		
		// 7. 표시기 사용 여부
		boolean useIndicator = this.isUseIndicator(batch);
		
		// 8. 주문 - 박스 선 매핑 모드인 경우
		if(this.isBoxMappingPreMode(batch)) {
			// 표시기에 박스 매핑 표시 점등 ...
			if(useIndicator) {
				this.serviceDispatcher.getIndicationService(batch).displayForBoxMapping(batch, job.getIndCd());
			}
			
		// 9. 주문 - 박스 후 매핑 모드인 경우
		} else {
			if(useIndicator) {
				// 표시기에 작업 수량 표시 점등
				RuntimeIndServiceUtil.indOnByJob(false, batch, job);
			}
		}
		
		// 10. 작업 리턴
		return job;
	}
	
	@Override
	public JobInstance assignJobToCell(JobBatch batch, String jobInstanceId, String stationCd, String cellCd) {
		JobInstance job = AnyEntityUtil.findEntityBy(batch.getDomainId(), true, JobInstance.class, null, SysConstants.ENTITY_FIELD_ID, jobInstanceId);
		return this.assignJobToCell(batch, job, stationCd, cellCd);
	}
	
	@Override
	public JobInstance assignOrderToBox(JobBatch batch, String jobInstanceId, String stationCd, String cellCd, String boxId) {
		JobInstance job = AnyEntityUtil.findEntityBy(batch.getDomainId(), true, JobInstance.class, null, SysConstants.ENTITY_FIELD_ID, jobInstanceId);
		return this.assignOrderToBox(batch, job, stationCd, cellCd, boxId);
	}
	
	@Override
	public JobInstance assignOrderToBox(JobBatch batch, JobInstance job, String stationCd, String cellCd, String boxId) {
		Long domainId = batch.getDomainId();
		
		// 1. 셀 조회 (With Lock)
		Cell cell = this.findCellToWork(domainId, job.getEquipCd(), stationCd, cellCd, true);
		
		// 2. 유효성 체크 - 주문에 박스 ID가 이미 매핑되었는지, 박스 ID가 이미 사용된 것인지 체크
		this.boxService.isUsedBoxId(batch, boxId, true);
		this.checkOrderAlreadyHasBox(batch, job.getClassCd(), true);
		
		// 3. 주문, 작업에 박스 ID 매핑
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,classCd,cellCd", domainId, batch.getId(), job.getClassCd(), cellCd);
		String sql = "update orders set box_id = :boxId where domain_id = :domainId and batch_id = :batchId and class_cd = :classCd";
		this.queryManager.executeBySql(sql, params);
		this.queryManager.executeBySql(sql.replace("orders", "job_instances"), params);
		
		// 4. 피킹 처리
		job.setBoxId(boxId);
		
		// 5. 커스텀 서비스 호출
		this.customService.doCustomService(domainId, CUSTOM_PDAS_BOX_MAPPING, ValueUtil.newMap("batch,job,cell", batch, job, cell));
		
		// 6. 작업 리턴
		return job;
	}
	
	@Override
	public JobInstance middleAssortJob(JobBatch batch, String jobInstanceId) {
		Long domainId = batch.getDomainId();
		
		// 1. 중분류 작업 처리를 위해 작업 조회
		JobInstance job = AnyEntityUtil.findEntityBy(domainId, false, JobInstance.class, null, SysConstants.ENTITY_FIELD_ID, jobInstanceId);
		
		// 2. 작업 유효성 체크 - 작업 존재 여부 체크
		if(job == null) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NOT_EXIST_ORDER"));
		}
		
		// 3. 셀 조회 (With Lock)
		Cell cell = this.findCellToWork(domainId, batch.getEquipCd(), null, job.getSubEquipCd(), true);
		
		// 4. 작업 유효성 체크 - 작업 상태 체크
		if(ValueUtil.isNotEqual(job.getStatus(), LogisConstants.JOB_STATUS_WAIT)) {
			// throw ThrowUtil.newValidationErrorWithNoLog("작업을 처리할 수 있는 상태가 아닙니다.");
			return job;
		}
		
		// 5. 중분류 처리
		job.setBoxClassCd(cell.getStationCd());
		this.queryManager.update(job, "boxClassCd", "updatedAt");
		
		// 6. 클라이언트에서 처리하도록 작업 상태를 CM으로 변경
		job.setStatus("CM");
		return job;
	}
	
	@Override
	public JobInstance assortJob(JobBatch batch, String jobInstanceId, String cellCd, String stationCd) {
		Long domainId = batch.getDomainId();
		
		// 1. 작업 처리를 위해 셀 조회시 락을 걸어 조회하고 유효성 체크까지 처리
		Cell cell = this.findCellToWork(domainId, batch.getEquipCd(), stationCd, cellCd, true);
		
		// 2. 작업 유효성 체크
		JobInstance job = AnyEntityUtil.findEntityBy(domainId, true, JobInstance.class, null, SysConstants.ENTITY_FIELD_ID, jobInstanceId);
		
		// 3. 셀 코드와 작업의 셀 코드 일치 여부 체크
		if(ValueUtil.isNotEqual(job.getSubEquipCd(), cellCd)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("INCONSISTENT_CELL_NO_OPER_SCAN"));
		}
		
		// 4. 셀의 분류 코드 존재 여부 체크
		if(ValueUtil.isEmpty(cell.getClassCd())) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("CLASSIFICATION_CODE_NOT_MAPPED_CELL"));
		}
		
		// 5. 셀 분류 코드와 작업의 분류 코드 일치 여부 체크
		if(ValueUtil.isNotEqual(job.getClassCd(), cell.getClassCd())) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("INCONSISTENT_CLASS_CODE_TASK_CELL"));
		}

		// 6. 작업 처리
		job = this.confirmAssort(batch, job, cell, false);
		
		// 7. 작업 리턴
		return job;
	}
	
	@Override
	public JobInstance boxingJob(JobBatch batch, String jobInstanceId, String boxId, boolean boxReusable, String stationCd) {
		Long domainId = batch.getDomainId();
		
		// 1. 작업 정보 조회
		JobInstance job = AnyEntityUtil.findEntityBy(domainId, true, JobInstance.class, null, SysConstants.ENTITY_FIELD_ID, jobInstanceId);
		
		// 2. 상태 체크
		if(ValueUtil.isEqualIgnoreCase(job.getStatus(), LogisConstants.JOB_STATUS_BOXED)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("ORDER_ALREADY_COMPLETED"));
		}
		
		// 3. 셀 조회 - Lock 처리
		Cell cell = this.findCellToWork(domainId, job.getEquipCd(), stationCd, job.getSubEquipCd(), true);
		
		// 4. 박스 ID 체크 - 이미 사용한 박스 ID인지 체크
		this.boxService.isUsedBoxId(batch, boxId, true);
		
		// 5. 최종 주문 분류 완료 처리
		job.setBoxId(boxId);
		this.fullBoxing(batch, job, cell, false);
		
		// 6. 작업 리턴
		return job;
	}

}
