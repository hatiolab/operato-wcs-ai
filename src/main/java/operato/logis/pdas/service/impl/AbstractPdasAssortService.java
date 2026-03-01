package operato.logis.pdas.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import operato.logis.pdas.query.store.PdasPickQueryStore;
import operato.logis.pdas.service.util.PdasServiceUtil;
import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.WorkCell;
import xyz.anythings.base.event.ICategorizeEvent;
import xyz.anythings.base.event.IClassifyErrorEvent;
import xyz.anythings.base.event.IClassifyInEvent;
import xyz.anythings.base.event.IClassifyOutEvent;
import xyz.anythings.base.event.IClassifyRunEvent;
import xyz.anythings.base.event.classfy.ClassifyEndEvent;
import xyz.anythings.base.event.classfy.ClassifyErrorEvent;
import xyz.anythings.base.event.classfy.ClassifyRunEvent;
import xyz.anythings.base.event.device.DeviceEvent;
import xyz.anythings.base.model.Category;
import xyz.anythings.base.service.api.IAssortService;
import xyz.anythings.base.service.api.IBoxingService;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.gw.entity.Indicator;
import xyz.anythings.gw.service.IndConfigProfileService;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.exception.ElidomException;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * IAssortService P-DAS 구현
 *  
 * @author shortstop
 */
public abstract class AbstractPdasAssortService extends AbstractLogisService implements IAssortService {

	/**
	 * 분류 작업 조회 커스텀 서비스
	 */
	public static final String CUSTOM_PDAS_FIND_JOB = "diy-pdas-find-job";
	/**
	 * 분류 작업을 셀에 매핑하는 커스텀 서비스
	 */
	public static final String CUSTOM_PDAS_ASSIGN_JOB = "diy-pdas-assign-job";
	/**
	 * 중분류 처리 커스텀 서비스
	 */
	public static final String CUSTOM_PDAS_MIDDLE_ASSORT_JOB = "diy-pdas-middle-assort-job";
	/**
	 * 주문 - 박스 매핑 커스텀 서비스
	 */
	public static final String CUSTOM_PDAS_BOX_MAPPING = "diy-pdas-box-mapping";
	/**
	 * 분류 작업 처리 커스텀 서비스
	 */
	public static final String CUSTOM_PDAS_ASSORT_JOB = "diy-pdas-assort-job";
	/**
	 * 박싱 작업 처리 커스텀 서비스
	 */
	public static final String CUSTOM_PDAS_BOX_JOB = "diy-pdas-box-job";
	
	/**
	 * 표시기 설정 프로파일 서비스
	 */
	@Autowired
	protected IndConfigProfileService indConfigSetService;
	/**
	 * 피킹 쿼리 스토어
	 */
	@Autowired
	protected PdasPickQueryStore pdasPickQueryStore;
	/**
	 * 박스 서비스
	 */
	@Autowired
	protected PdasBoxingService boxService;
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;
	
	/**
	 * 표시기 사용 여부
	 * 
	 * @param batch
	 * @return
	 */
	public boolean isUseIndicator(JobBatch batch) {
		return ValueUtil.toBoolean(BatchJobConfigUtil.getConfigValue(batch, "pdas.use.indicator", "false"));
	}
	
	/**
	 * 중분류 사용 여부
	 * 
	 * @param batch
	 * @return
	 */
	public boolean isUseMiddleAssorting(JobBatch batch) {
		return ValueUtil.toBoolean(BatchJobConfigUtil.getConfigValue(batch, "pdas.use.middle-assort", "false"));
	}

	/**
	 * 자동 셀 매핑 여부
	 * 
	 * @param batch
	 * @return
	 */
	public boolean isCellMappingAutoMode(JobBatch batch) {
		return ValueUtil.toBoolean(BatchJobConfigUtil.getConfigValue(batch, "pdas.cell-mapping-mode.auto", "false"));
	}
	
	/**
	 * 박스 선 매핑 여부
	 * 
	 * @param batch
	 * @return
	 */
	public boolean isBoxMappingPreMode(JobBatch batch) {
		return ValueUtil.toBoolean(BatchJobConfigUtil.getConfigValue(batch, "pdas.box-mapping-mode.pre", "false"));
	}
	
	/**
	 * 버킷 사용 여부
	 * 
	 * @param batch
	 * @return
	 */
	public boolean isUseBucket(JobBatch batch) {
		return ValueUtil.toBoolean(BatchJobConfigUtil.getConfigValue(batch, "pdas.use.bucket", "false"));
	}
	
	/**
	 * 싱글 피스 피킹 모드
	 * 
	 * @param batch
	 * @return
	 */
	public boolean isPiecePickingMode(JobBatch batch) {
		// 중분류 사용하는 경우는 JobInstance를 1pcs당 처리
		return this.isUseMiddleAssorting(batch);
	}
	
	@Override
	public String getJobType() {
		return LogisConstants.JOB_TYPE_PDAS;
	}

	@Override
	public JobConfigSet getJobConfigSet(String batchId) {
		return this.serviceDispatcher.getConfigSetService().getConfigSet(batchId);
	}
	
	@Override
	public IBoxingService getBoxingService(Object... params) {
		return this.boxService;
	}

	@Override
	public String checkInput(JobBatch batch, String inputId, Object... params) {
		// inputId를 체크하여 어떤 코드 인지 (상품 코드, 상품 바코드, 박스 ID, 랙 코드, 셀 코드 등)를 찾아서 리턴 
		if(BatchJobConfigUtil.isBoxIdValid(batch, inputId)) {
			return LogisCodeConstants.INPUT_TYPE_BOX_ID;
			
		} else if(BatchJobConfigUtil.isSkuCdValid(batch, inputId)) {
			return LogisCodeConstants.INPUT_TYPE_SKU_CD;
		
		} else if(BatchJobConfigUtil.isRackCdValid(batch, inputId)) {
			return LogisCodeConstants.INPUT_TYPE_RACK_CD;
			
		} else if(BatchJobConfigUtil.isCellCdValid(batch, inputId)) {
			return LogisCodeConstants.INPUT_TYPE_CELL_CD;
			
		} else if(BatchJobConfigUtil.isIndCdValid(batch, inputId)) {
			return LogisCodeConstants.INPUT_TYPE_IND_CD;
			
		} else {
			// 스캔한 정보가 어떤 투입 유형인지 구분할 수 없습니다.
			String msg = MessageUtil.getMessage("CANT_DISTINGUISH_WHAT_INPUT_TYPE", "Can't distinguish what type of input the scanned information is.");
			throw new ElidomRuntimeException(msg);
		}
	}

	@Override
	@EventListener(classes = ClassifyRunEvent.class, condition = "#exeEvent.jobType == 'PDAS'")
	public Object classify(IClassifyRunEvent exeEvent) {
		String classifyAction = exeEvent.getClassifyAction();
		JobInstance job = exeEvent.getJobInstance();
		
		try {
			switch(classifyAction) {
				// 확정 처리
				case LogisCodeConstants.CLASSIFICATION_ACTION_CONFIRM :
					this.confirmAssort(exeEvent);
					break;
					
				// 작업 취소
				case LogisCodeConstants.CLASSIFICATION_ACTION_CANCEL :
					this.cancelAssort(exeEvent);
					break;
					
				// 수량 조정 처리
				case LogisCodeConstants.CLASSIFICATION_ACTION_MODIFY :
					this.splitAssort(exeEvent);
					break;
					
				// 풀 박스
				case LogisCodeConstants.CLASSIFICATION_ACTION_FULL :
					if(exeEvent instanceof IClassifyOutEvent) {
						this.fullBoxing((IClassifyOutEvent)exeEvent);
					}
					break;
			}
		} catch (Throwable th) {
			IClassifyErrorEvent errorEvent = new ClassifyErrorEvent(exeEvent, exeEvent.getEventStep(), th);
			this.handleClassifyException(errorEvent);
			return exeEvent;
		}
		
		exeEvent.setExecuted(true);
		return job;
	}

	@Override
	public Object output(IClassifyOutEvent outputEvent) {
		return this.fullBoxing(outputEvent);
	}

	@Override
	public void handleClassifyException(IClassifyErrorEvent errorEvent) {
		// 1. 예외 정보 추출 
		Throwable th = errorEvent.getException();
		// 2. 디바이스 정보 추출
		String device = errorEvent.getClassifyRunEvent().getClassifyDevice();
		// 3. 표시기로 부터 온 요청에서 에러가 발생했는지 체크
		boolean isIndicatorDevice = !ValueUtil.isEqualIgnoreCase(device, Indicator.class.getSimpleName());

		// 4. 모바일 알람 이벤트 전송
		if(th != null) {
			String cellCd = (errorEvent.getWorkCell() != null) ? errorEvent.getWorkCell().getCellCd() : (errorEvent.getJobInstance() != null ? errorEvent.getJobInstance().getSubEquipCd() : null);
			Cell c = ValueUtil.isNotEmpty(cellCd) ? 
				AnyEntityUtil.findEntityBy(errorEvent.getDomainId(), false, Cell.class, "stationCd", "cellCd", cellCd) : null;
			
			String errMsg = (th.getCause() == null) ? th.getMessage() : th.getCause().getMessage();
			JobBatch batch = errorEvent.getJobBatch();
			String[] deviceTypeList = (isIndicatorDevice) ? new String[] { "pda" } : null;
			
			if(deviceTypeList != null) {
				for(String deviceType : deviceTypeList) {
					DeviceEvent event = new DeviceEvent(batch.getDomainId(), deviceType, batch.getStageCd(), batch.getEquipType(), batch.getEquipCd(), (c != null ? c.getStationCd() : null), null, batch.getJobType(), "error", errMsg);
					this.eventPublisher.publishEvent(event);
				}
			}
		}

		// 5. 예외 발생
		throw (th instanceof ElidomException) ? (ElidomException)th : new ElidomRuntimeException(th);
	}
	
	@Override
	public void confirmAssort(IClassifyRunEvent exeEvent) {
		
		// 1. 이벤트로 부터 작업에 필요한 데이터 추출
		JobBatch batch = exeEvent.getJobBatch();
		JobInstance job = exeEvent.getJobInstance();
		Cell cell = this.findCellToWork(batch.getDomainId(), null, null, job.getSubEquipCd(), true);
		
		// 2. 확정 처리
		this.confirmAssort(batch, job, cell, true);
		
		// 3. 표시기에서 처리한 경우 PDA에 메시지 전송
		ClassifyEndEvent classifyEndEvent = new ClassifyEndEvent(exeEvent);
		classifyEndEvent.setResult(job);
		classifyEndEvent.setPayload(new Object[] { cell, job });
		this.eventPublisher.publishEvent(classifyEndEvent);
	}
	
	@Override
	public void cancelAssort(IClassifyRunEvent exeEvent) {
		// 1. 이벤트로 부터 작업에 필요한 데이터 추출
		JobBatch batch = exeEvent.getJobBatch();
		JobInstance job = exeEvent.getJobInstance();
		Cell cell = this.findCellToWork(batch.getDomainId(), null, null, job.getSubEquipCd(), true);
		
		// 2. 표시기에서 취소한 경우 PDA에 메시지 전송
		ClassifyEndEvent classifyEndEvent = new ClassifyEndEvent(exeEvent);
		job.setStatus(LogisConstants.JOB_STATUS_CANCEL);
		classifyEndEvent.setResult(job);
		classifyEndEvent.setPayload(new Object[] { cell, job });
		this.eventPublisher.publishEvent(classifyEndEvent);
	}
	
	/**
	 * 표시기로 부터 Fullbox 요청
	 */
	@Override
	public BoxPack fullBoxing(IClassifyOutEvent outEvent) {
		Long domainId = outEvent.getDomainId();
		
		// 1. 풀 박스 처리해야 할 작업 리스트 조회
		JobBatch batch = outEvent.getJobBatch();
		
		// 2. 설정에 따라 Fullbox를 지원할 지 여부 판단하여 지원 안 하는 경우 스킵
		String settingKey = outEvent.getStageCd() + ".pdas.fullbox.support";
		if(!ValueUtil.toBoolean(SettingUtil.getValue(domainId, settingKey, "true"))) return null;
		
		// 3. 작업 셀 코드 추출 & 셀 조회 - Lock 처리
		String cellCd = outEvent.getCellCd();
		Cell cell = this.findCellToWork(domainId, null, null, cellCd, true);
		
		// 4. 작업 배치가 null이면 조회
		if(batch == null) {
			String sql = "select * from job_batches where domain_id = :domainId and status = :status and equip_cd in (select distinct equip_cd from cells where domain_id = :domainId and cell_cd = :cellCd)";
			Map<String, Object> bParams = ValueUtil.newMap("domainId,cellCd,status", domainId, cellCd, JobBatch.STATUS_RUNNING);
			batch = this.queryManager.selectBySql(sql, bParams, JobBatch.class);
			
			if(batch == null) {
				throw new ElidomRuntimeException(MessageUtil.getMessage("NOT_FOUND_RUNNING_BATCH"));
			}
		}
		
		// 5. 셀 번호로 부터 완료된 작업 리스트 조회
		JobInstance job = outEvent.getJobInstance();

		// 6. 풀 박스 처리
		BoxPack boxPack = this.fullBoxing(batch, job, cell, true);
		
		// 7. 이벤트 처리
		outEvent.setResult(job);
		outEvent.setExecuted(true);
		
		// 8. PDA로 메시지 전달
		ClassifyEndEvent classifyEndEvent = new ClassifyEndEvent(outEvent);
		classifyEndEvent.setResult(job);
		classifyEndEvent.setPayload(new Object[] { cell, job });
		this.eventPublisher.publishEvent(classifyEndEvent);
		
		// 9. 박스 리턴
		return boxPack;
	}
	
	@Override
	public boolean checkStationJobsEnd(JobInstance job, String stationCd) {
		// 작업 스테이션에서 작업이 끝났는지 체크 ...
		String sql = this.pdasPickQueryStore.getSearchPickingJobListQuery();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,stationCd,statuses", job.getDomainId(), job.getBatchId(), stationCd, LogisConstants.JOB_STATUS_WIP);
		return this.queryManager.selectSizeBySql(sql, params) == 0;
	}
	
	@Override
	public boolean checkCellAssortEnd(JobInstance job, boolean finalEndCheck) {
		// finalEndCheck가 false이면 모든 작업이 FINISH / CANCEL, true이면 모두 BOXED / EXAMED
		List<String> statuses = finalEndCheck ? LogisConstants.JOB_STATUS_BER : LogisConstants.JOB_STATUS_FC;
		Map<String, Object> qParams = ValueUtil.newMap("domainId,batchId,classCd,statuses", job.getDomainId(), job.getBatchId(), job.getClassCd(), statuses);
		String sql = this.pdasPickQueryStore.getCheckOrderFinalEndQuery();
		return (this.queryManager.selectSizeBySql(sql, qParams) == 0);
	}
	
	/**********************************************************************************************
	/*									Protected Method										  *
	/*********************************************************************************************/
	
	/**
	 * 분류 처리
	 * 
	 * @param batch
	 * @param job
	 * @param cell
	 * @return
	 */
	protected JobInstance confirmAssort(JobBatch batch, JobInstance job, Cell cell, boolean fromIndicator) {
		Long domainId = batch.getDomainId();
		
		// 1. 작업 처리 가능 상태 체크
		if(!LogisConstants.JOB_STATUS_WP.contains(job.getStatus())) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NOT_STATE_HANDLE_WORK"));
		}
		
		// 2. 작업 처리
		if(job.getPickQty() > job.getPickedQty()) {
			job.setPickedQty(job.getPickedQty() + 1);
		}
		
		String status = (job.getPickedQty() == job.getPickQty()) ? LogisConstants.JOB_STATUS_FINISH : LogisConstants.JOB_STATUS_PICKING;
		job.setStatus(status);
		job.setPickingQty(0);
		String currentTimeStr = DateUtil.currentTimeStr();
		job.setPickEndedAt((job.getPickedQty() == job.getPickQty()) ? currentTimeStr : null);
		if(ValueUtil.isEmpty(job.getPickStartedAt())) {
			job.setInputAt(currentTimeStr);
			job.setPickStartedAt(currentTimeStr);
		}
		this.queryManager.update(job, "subEquipCd", "pickingQty", "pickedQty", "inputAt", "pickStartedAt", "pickEndedAt", "status", "updaterId", "updatedAt");
		
		// 3. 최종 주문 분류 완료이면 ...
		if(ValueUtil.isEqualIgnoreCase(status, LogisConstants.JOB_STATUS_FINISH) && this.checkCellAssortEnd(job, false)) {
			job.setStatus(LogisConstants.JOB_STATUS_BOXED);
			
			// 3.1 주문 - 박스 선 매핑 모드이면 바로 박싱 완료 처리
			if(this.isBoxMappingPreMode(batch)) {
				this.fullBoxing(batch, job, cell, fromIndicator);
				return job;
				
			// 3.2 주문 - 박스 후 매핑 모드이면 박스 매핑 표시
			} else {
				if(this.isUseIndicator(batch)) {
					this.serviceDispatcher.getIndicationService(batch).displayForBoxMapping(batch, job.getIndCd());
				}
			}
		
		// 4. 최종 주문 분류 완료가 아니면 ...
		} else {
			// 4.1 작업 배치 진행율 업데이트
			this.updateBatchResult(batch);
			
			// 4.2 커스텀 서비스 호출
			this.customService.doCustomService(domainId, CUSTOM_PDAS_ASSORT_JOB, ValueUtil.newMap("batch,job", batch,job));
			
			// 4.3 PDA로 처리했을 때만 표시기 사용시 수량 표시
			//if(this.isUseIndicator(batch) && !fromIndicator) {
			if(this.isUseIndicator(batch)) {
				String sql = "select sum(pick_qty) as pick_qty, sum(picked_qty) as picked_qty from job_instances where domain_id = :domainId and batch_id = :batchId and class_cd = :classCd";
				JobInstance qtyJob = this.queryManager.selectBySql(sql, ValueUtil.newMap("domainId,batchId,classCd", domainId, batch.getId(), job.getClassCd()), JobInstance.class);
				String displayStr = StringUtils.leftPad("" + qtyJob.getPickQty(), 3) + StringUtils.leftPad("" + qtyJob.getPickedQty(), 3);
				this.serviceDispatcher.getIndicationService(batch).displayForString(domainId, batch.getId(), batch.getStageCd(), batch.getJobType(), job.getIndCd(), displayStr);
			}
		}
		
		// 5. 작업 리턴
		return job;
	}
	
	/**
	 * PDA로 부터 받은 Fullbox 요청
	 * 
	 * @param batch
	 * @param job
	 * @param cell
	 * @param fromIndicator
	 * @return
	 */
	protected BoxPack fullBoxing(JobBatch batch, JobInstance job, Cell cell, boolean fromIndicator) {
		
		// 1. 주문 완료 처리하려는 주문이 분류 처리가 모두 완료되었는지 체크
		if (!this.checkCellAssortEnd(job, false)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NOT_COMPLETED_ORDER"));
		}
		
		// 2. 최종 주문 분류 완료 대상 작업 조회
		Map<String, Object> params = ValueUtil.newMap("subEquipCd,status,classCd", cell.getCellCd(), LogisConstants.JOB_STATUS_FINISH, job.getClassCd());
		List<JobInstance> jobList = this.serviceDispatcher.getJobStatusService(batch).searchJobList(batch, params);
		
		// 3. 풀 박스 대상 작업 체크
		if(ValueUtil.isEmpty(jobList)) {
			// 이미 처리된 항목입니다. --> 작업[job.getId()]은 이미 풀 박스가 완료되었습니다.
			throw new ElidomRuntimeException(MessageUtil.getMessage("ALREADY_BEEN_PROCEEDED", "Already been proceeded."));
		}
		
		// 4. 풀 박스 처리
		BoxPack boxPack = this.boxService.fullBoxing(batch, null, jobList, cell, job.getBoxId());
		
		// 5. 작업 배치 업데이트
		this.updateBatchResult(batch);
		
		// 6. 커스텀 서비스 호출
		this.customService.doCustomService(batch.getDomainId(), CUSTOM_PDAS_BOX_JOB, ValueUtil.newMap("batch,box,job", batch, boxPack, job));
		
		// 7. 표시기 사용시 END 표시
		if(this.isUseIndicator(batch)) {
			this.serviceDispatcher.getIndicationService(batch).indicatorOnForPickEnd(job, true);
		}
		
		// 8. 결과 리턴
		job.setStatus(LogisConstants.JOB_STATUS_BOXED);
		return boxPack;
	}
	
	/**
	 * 작업 처리를 위한 셀 조회 - 락 처리 && 유효성 체크
	 * 
	 * @param domainId
	 * @param equipCd
	 * @param stationCd
	 * @param cellCd
	 * @param withLock
	 * @return
	 */
	protected Cell findCellToWork(Long domainId, String equipCd, String stationCd, String cellCd, boolean withLock) {
		// 1. 셀 조회
		Cell cell = withLock ? AnyEntityUtil.findEntityByCodeWithLock(domainId, false, Cell.class, "cellCd", cellCd) : 
								AnyEntityUtil.findEntityByCode(domainId, false, Cell.class, "cellCd", cellCd);
		
		// 2. 셀 유효성 체크 - 셀이 존재하는지 체크
		if(cell == null) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("CELL_NO_CANNOT_FOUND"));
		}
		
		// 3. 셀 유효성 체크 - 호기 범위 내 셀인지 체크
		if(ValueUtil.isNotEmpty(equipCd) && ValueUtil.isNotEqual(equipCd, cell.getEquipCd())) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("CELL_NOT_WORK_RANGE_RACK"));
		}
		
		// 4. 셀 유효성 체크 - 스테이션 범위 내 셀인지 체크
		if(PdasServiceUtil.isValidStationCode(stationCd) && ValueUtil.isNotEqual(stationCd, cell.getStationCd())) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("CELL_NOT_WORK_SCOPE_STATION"));
		}
		
		
		// 5. 셀 유효성 체크 - 호기 범위 내 셀인지 체크
		if(cell.getActiveFlag() == null || !cell.getActiveFlag()) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("CELL_NOT_ACTIVE"));
		}
		
		// 6. 이상 없다면 리턴
		return cell;
	}
	
	/**
	 * 작업 배치 수량 및 진행율 업데이트
	 * 
	 * @param batch
	 */
	protected void updateBatchResult(JobBatch batch) {
		// 1. 작업 진행 정보 - 주문 수 / 분류 수 조회
		Map<String, Object> qParams = ValueUtil.newMap("domainId,batchId,statuses", batch.getDomainId(), batch.getId(), LogisConstants.JOB_STATUS_BER);
		String sql = this.pdasPickQueryStore.getBatchOrderResultQtyQuery();
		JobInstance result = this.queryManager.selectBySql(sql, qParams, JobInstance.class);
		int orderedQty = result.getPickQty();
		int resultPcs = result.getPickedQty();
		
		// 2. 배치 정보에 분류 수량, 진행율 업데이트
		batch.setResultPcs(resultPcs);
		batch.setResultOrderQty(orderedQty);
		batch.setResultBoxQty(orderedQty);
		float rate = (ValueUtil.toFloat(orderedQty) / ValueUtil.toFloat(batch.getParentOrderQty())) * 100.0f;
		rate = Math.round(rate * 100) / 100.0f;
		batch.setProgressRate(rate);
		this.queryManager.update(batch, "resultPcs", "resultOrderQty", "resultBoxQty", "progressRate");
	}
	
	/**********************************************************************************************
	/*									지원하지 않는 메소드 											  *
	/*********************************************************************************************/
	
	@Override
	public Object input(IClassifyInEvent inputEvent) {
		throw ThrowUtil.newNotSupportedMethod();
	}
	
	@Override
	public Object inputSkuSingle(IClassifyInEvent inputEvent) {
		throw ThrowUtil.newNotSupportedMethod();
	}
	
	@Override
	public Object boxCellMapping(JobBatch batch, String cellCd, String boxId) {
		throw ThrowUtil.newNotSupportedMethod();
	}
	
	@Override
	public Object classCellMapping(JobBatch batch, String cellCd, String boxId, Object ... params) {
		throw ThrowUtil.newNotSupportedMethod();
	}
	
	@Override
	public boolean finishAssortCell(JobInstance job, WorkCell workCell, boolean finalEndFlag) {
		throw ThrowUtil.newNotSupportedMethod();
	}
	
	@Override
	public boolean checkEndClassifyAll(JobBatch batch) {
		throw ThrowUtil.newNotSupportedMethod();
	}
	
	@Override
	public Category categorize(ICategorizeEvent event) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public Object inputSkuBundle(IClassifyInEvent inputEvent) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public Object inputSkuBox(IClassifyInEvent inputEvent) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public Object inputForInspection(IClassifyInEvent inputEvent) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public int splitAssort(IClassifyRunEvent exeEvent) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public int undoAssort(IClassifyRunEvent exeEvent) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public BoxPack partialFullboxing(IClassifyOutEvent outEvent) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public BoxPack cancelBoxing(Long domainId, BoxPack boxPack) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public JobInstance splitJob(JobBatch batch, JobInstance job, WorkCell workCell, int splitQty) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public JobInstance findLatestJobForBoxing(Long domainId, String batchId, String cellCd) {
		throw ThrowUtil.newNotSupportedMethod();
	}

}
