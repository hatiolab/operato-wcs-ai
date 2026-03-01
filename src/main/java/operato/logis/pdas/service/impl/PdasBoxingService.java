package operato.logis.pdas.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.logis.pdas.service.util.PdasServiceUtil;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.WorkCell;
import xyz.anythings.base.service.api.IBoxingService;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

/**
 * P-DAS 출고용 박스 처리 서비스
 * 
 * @author shortstop
 */
@Component("pdasBoxingService")
public class PdasBoxingService extends AbstractLogisService implements IBoxingService {

	@Override
	public String getJobType() {
		return LogisConstants.JOB_TYPE_PDAS;
	}

	@Override
	public JobConfigSet getJobConfigSet(String batchId) {
		return BatchJobConfigUtil.getConfigSetService().getConfigSet(batchId);
	}

	/**
	 * 버킷을 사용하는지 여부 (false인 경우 박스를 사용)
	 * 
	 * @param batch
	 * @return
	 */
	public boolean isUseBucket(JobBatch batch) {
		return ValueUtil.toBoolean(BatchJobConfigUtil.getConfigValue(batch, "pdas.use.bucket", "false"));
	}

	@Override
	public BoxPack fullBoxing(JobBatch batch, WorkCell workCell, List<JobInstance> jobList, Object... params) {

		// 1. 작업 추출, 셀 추출, 박스 ID 추출
		JobInstance firstJob = jobList.get(0);
		Cell cell = (Cell)params[0];
		String boxId = ValueUtil.isNotEmpty(firstJob.getBoxId()) ? firstJob.getBoxId() : (ValueUtil.isNotEmpty(params) ? ValueUtil.toString(params[1]) : null);

		// 2. 작업 박싱 완료 처리
		String currentTime = DateUtil.currentTimeStr();
		for(JobInstance job : jobList) {
			job.setStatus(LogisConstants.JOB_STATUS_BOXED);
			job.setBoxedAt(currentTime);
			if(ValueUtil.isNotEmpty(boxId)) {
				job.setBoxId(boxId);
			}
		}
		this.queryManager.updateBatch(jobList, "status", "boxedAt", "boxId");
		
		// 4. 셀을 비워줌
		cell.setClassCd(null);
		this.queryManager.update(cell, "classCd", "updaterId", "updatedAt");

		// 5. BoxPack
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,boxId", batch.getDomainId(), batch.getId(), boxId);
		List<BoxPack> boxPacks = this.queryManager.selectList(BoxPack.class, condition);
		return ValueUtil.isNotEmpty(boxPacks) ? boxPacks.get(0) : null;
	}

	@Override
	public boolean isUsedBoxId(JobBatch batch, String boxId, boolean exceptionWhenBoxIdUsed) {
		Long domainId = batch.getDomainId();
		String sql = "select id from job_instances where domain_id = :domainId and box_id = :boxId";
		Map<String, Object> qParams = ValueUtil.newMap("domainId,boxId", domainId, boxId);
		
		// 버킷을 사용하는 경우
		if(this.isUseBucket(batch)) {
			sql += " and batch_id = :batchId and status in (:statuses)";
			qParams.put("batchId", batch.getId());
			qParams.put("statuses", LogisConstants.JOB_STATUS_WIPFB);
		}
		
		// 박스 ID 체크 - 이미 사용한 박스 ID인지 체크
		boolean alreadyUsed = this.queryManager.selectSizeBySql(sql, qParams) > 0;
		if(exceptionWhenBoxIdUsed && alreadyUsed) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("BOX_ALREADY_USED"));
		}
		
		// 사용 여부
		return alreadyUsed;
	}
	
	/**********************************************************************************************
	/*									Protected Method										  *
	/*********************************************************************************************/
	
	/**
	 * 작업 처리를 위한 셀 조회 - 락 처리 && 유효성 체크
	 * 
	 * @param domainId
	 * @param equipCd
	 * @param stationCd
	 * @param cellCd
	 * @return
	 */
	protected Cell findCellToWork(Long domainId, String equipCd, String stationCd, String cellCd) {
		// 1. 셀에 락 걸고 조회
		Cell cell = AnyEntityUtil.findEntityByCodeWithLock(domainId, true, Cell.class, "cellCd", cellCd);
		
		// 2. 셀 유효성 체크 - 호기 범위 내 셀인지 체크
		if(ValueUtil.isNotEqual(equipCd, cell.getEquipCd())) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("CELL_NOT_WORK_RANGE_RACK"));
		}
		
		// 3. 셀 유효성 체크 - 스테이션 범위 내 셀인지 체크
		if(PdasServiceUtil.isValidStationCode(stationCd) && ValueUtil.isNotEqual(stationCd, cell.getStationCd())) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("CELL_NOT_WORK_SCOPE_STATION"));
		}
		
		
		// 4. 셀 유효성 체크 - 호기 범위 내 셀인지 체크
		if(cell.getActiveFlag() == null || !cell.getActiveFlag()) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("CELL_NOT_ACTIVE"));
		}
		
		// 5. 이상 없다면 리턴
		return cell;
	}
	
	/**********************************************************************************************
	/*									지원하지 않는 메소드 											  *
	/*********************************************************************************************/

	@Override
	public Object assignBoxToCell(JobBatch batch, String cellCd, String boxId, Object... params) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public Object resetBoxToCell(JobBatch batch, String cellCd, Object... params) {
		throw ThrowUtil.newNotSupportedMethod();
	}
	
	@Override
	public BoxPack partialFullboxing(JobBatch batch, WorkCell workCell, List<JobInstance> jobList, Integer fullboxQty, Object... params) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public List<BoxPack> batchBoxing(JobBatch batch) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public BoxPack cancelFullboxing(BoxPack box) {
		throw ThrowUtil.newNotSupportedMethod();
	}

}
