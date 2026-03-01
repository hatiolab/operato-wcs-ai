package operato.logis.dpc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.logis.dpc.service.util.DpcBatchJobConfigUtil;
import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.WorkCell;
import xyz.anythings.base.event.box.UndoBoxingEvent;
import xyz.anythings.base.event.classfy.ClassifyOutEvent;
import xyz.anythings.base.service.api.IBoxingService;
import xyz.anythings.base.service.api.IIndicationService;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.gw.entity.Indicator;
import xyz.anythings.sys.event.model.SysEvent;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * DPC 박스 처리 서비스
 * 
 * @author shortstop
 */
@Component("dpcBoxingService")
public class DpcBoxingService extends AbstractLogisService implements IBoxingService {

	/**
	 * 커스텀 서비스 - 박스 매핑 전 처리
	 */
	private static final String DIY_DPC_PRE_BOXMAPPING = "diy-dpc-pre-boxmapping";
	/**
	 * 커스텀 서비스 - 박스 매핑 후 처리
	 */
	private static final String DIY_DPC_POST_BOXMAPPING = "diy-dpc-post-boxmapping";
	/**
	 * 커스텀 서비스 - 풀 박스 전 처리
	 */
	private static final String DIY_DPC_PRE_FULLBOX = "diy-dpc-pre-fullbox";
	/**
	 * 커스텀 서비스 - 풀 박스 후 처리
	 */
	private static final String DIY_DPC_POST_FULLBOX = "diy-dpc-post-fullbox";
	
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;

	@Override
	public String getJobType() {
		return LogisConstants.JOB_TYPE_DPC;
	}

	@Override
	public JobConfigSet getJobConfigSet(String batchId) {
		return BatchJobConfigUtil.getConfigSetService().getConfigSet(batchId);
	}
	
	@Override
	public boolean isUsedBoxId(JobBatch batch, String boxId, boolean exceptionWhenBoxIdUsed) {
		Query condition = AnyOrmUtil.newConditionForExecution(batch.getDomainId());
		String boxIdUniqueScope = BatchJobConfigUtil.getBoxIdUniqueScope(batch, LogisConstants.BOX_ID_UNIQUE_SCOPE_GLOBAL);
		
		switch(boxIdUniqueScope) {
			case LogisConstants.BOX_ID_UNIQUE_SCOPE_GLOBAL :
				condition.addFilter("boxId", boxId);
				break;
				
			case LogisConstants.BOX_ID_UNIQUE_SCOPE_DAY :
				condition.addFilter("jobDate", batch.getJobDate());
				condition.addFilter("boxId", boxId);
				break;
				
			case LogisConstants.BOX_ID_UNIQUE_SCOPE_BATCH :
				condition.addFilter("batchId", batch.getId());
				condition.addFilter("boxId", boxId);
				break;
		}
		
		int count = this.queryManager.selectSize(JobInstance.class, condition);
		if(count > 0 && exceptionWhenBoxIdUsed) {
			throw new ElidomRuntimeException(MessageUtil.getMessage("BOX_ID_ALREADY_USED","박스 ID [{0}]는 이미 사용한 박스입니다.",ValueUtil.toList(boxId)));
		}
		
		return count > 0;
	}

	@Override
	public Object assignBoxToCell(JobBatch batch, String cellCd, String boxId, Object... params) {
		// 1. boxId 사용 여부 체크
		this.isUsedBoxId(batch, boxId, true);
				
		// 2. 박스 매핑 커스텀 서비스 전 처리
		Long domainId = batch.getDomainId();
		Map<String, Object> checkParams = ValueUtil.newMap("batch,boxId,cellCd", batch, boxId, cellCd);
		this.customService.doCustomService(domainId, DIY_DPC_PRE_BOXMAPPING, checkParams);
		
		// 3. 셀 체크
		Cell cell = AnyEntityUtil.findEntityBy(domainId, false, Cell.class, null, "domainId,cellCd", domainId, cellCd);
		if(cell == null) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("CELL_DOES_NOT_EXIST_RACK","랙 [{0}]에 셀 [{1}]이 존재하지 않습니다.",ValueUtil.toList(batch.getEquipCd(), cellCd)));
		}
		
		// 4. 워크 셀 체크
		WorkCell workCell = AnyEntityUtil.findEntityBy(domainId, false, WorkCell.class, null, "batchId,cellCd", batch.getId(), cellCd);
		if(workCell == null) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("NOT_EXIST_BATCH_MAPPED_CELL","작업배치 [{0}]에 매핑된 셀이 존재하지 않습니다.",ValueUtil.toList(batch.getId())));
		}
		
		// 5. 셀 활성화 체크
		if(!cell.getActiveFlag()) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("CELL_NOT_ACTIVE"));
		}
		
		// 6. 셀에 박스 ID가 매핑되어 있는지 체크
		if(ValueUtil.isNotEmpty(workCell.getBoxId())) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("ALREADY_EXIST_BOX_MAPPED_CELL", "셀 [{0}]에 이미 매핑된 박스가 존재합니다.", ValueUtil.toList(cellCd)));
		}
		
		// 7. 워크 셀에 상태가 ENDED 인지 체크
		if(ValueUtil.isEqualIgnoreCase(workCell.getStatus(), LogisConstants.CELL_JOB_STATUS_ENDED)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("ORDER_ALREADY_COMPLETED", "주문은 이미 분류가 완료되었습니다."));
		}
		
		// 8. 셀에 박스 ID 매핑
		cell.setClassCd(boxId);
		this.queryManager.update(cell, "classCd");
		
		// 9. 워크 셀에 박스 ID 설정
		workCell.setIndCd(cell.getIndCd());
		workCell.setBoxId(boxId);
		this.queryManager.update(workCell, "indCd", "boxId");
		
		// 10. 진행 중인 카트 작업번호 조회
		String sql = "select distinct to_zone_cd from orders where domain_id = :domainId and batch_id = :batchId and equip_cd = :equipCd and to_zone_cd is not null and to_cell_cd = :cartJobStatus";
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,equipCd,cartJobStatus,cellCd,indCd,boxId", domainId, batch.getId(), cell.getEquipCd(), JobBatch.STATUS_RUNNING, cell.getCellCd(), cell.getIndCd(), boxId);
		String cartJobId = this.queryManager.selectBySql(sql, condition, String.class);
		
		// 11. 해당 카트에 진행 중인 작업이 없습니다.
		if(ValueUtil.isEmpty(cartJobId)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("DOES_NOT_PROCEED", "진행 중인 {0}(이)가 아닙니다..", ValueUtil.toList("Cart")));
		}
		
		// 12. 주문, 작업에 박스 매핑
		condition.put("cartJobId", cartJobId);
		sql = "update orders set box_id = :boxId where domain_id = :domainId and batch_id = :batchId and equip_cd = :equipCd and sub_equip_cd = :cellCd and to_zone_cd = :cartJobId";
		this.queryManager.executeBySql(sql, condition);
		sql = "update job_instances set input_seq = 0, ind_cd = :indCd, box_id = :boxId where domain_id = :domainId and batch_id = :batchId and equip_cd = :equipCd and sub_equip_cd = :cellCd and box_class_cd = :cartJobId";
		this.queryManager.executeBySql(sql, condition);
		
		// 13. 표시기에 박스 ID 표시
		if(DpcBatchJobConfigUtil.isUseIndicator(batch)) {
			IIndicationService indSvc = this.serviceDispatcher.getIndicationService(batch);
			String dispBoxId = boxId.length() > 6 ? boxId.substring(boxId.length() - 6) : boxId;
			indSvc.displayForString(domainId, batch.getId(), batch.getStageCd(), batch.getJobType(), cell.getIndCd(), dispBoxId);
		}
		
		// 14. 박스 매핑 커스텀 서비스 전 처리
		checkParams.put("workCell", workCell);
		this.customService.doCustomService(domainId, DIY_DPC_POST_BOXMAPPING, checkParams);
		
		// 15. 클라이언트에 할당 정보 리턴
		return workCell;
	}

	@Override
	public Object resetBoxToCell(JobBatch batch, String cellCd, Object... params) {
		Long domainId = batch.getDomainId();
		
		// 1. Cell 조회 후 class_cd를 클리어
		Cell cell = AnyEntityUtil.findEntityBy(domainId, true, Cell.class, null, "domainId,cellCd", domainId, cellCd);
		cell.setClassCd(null);
		this.queryManager.update(cell, "classCd");
		
		// 2. 진행 중인 카트 작업번호 조회
		String sql = "select distinct to_zone_cd from orders where domain_id = :domainId and batch_id = :batchId and equip_cd = :equipCd and to_zone_cd is not null and to_cell_cd = :cartJobStatus";
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,equipCd,cartJobStatus", domainId, batch.getId(), cell.getEquipCd(), JobBatch.STATUS_RUNNING);
		String cartJobId = this.queryManager.selectBySql(sql, condition, String.class);
		
		// 3. 카트 작업 번호/ cellCd 의 작업이 진행중 인지 확인 
		Map<String, Object> conditions = ValueUtil.newMap("domainId,batchId,equipCd,cellCd,cartJobId", domainId, batch.getId(), cell.getEquipCd(), cellCd, cartJobId);
		sql = "select count(1) from job_instances where domain_id = :domainId and batch_id = :batchId and equip_cd = :equipCd and sub_equip_cd = :cellCd and box_class_cd = :cartJobId and COALESCE(picked_qty,0) > 0 ";
		if(this.queryManager.selectBySql(sql, conditions, Integer.class) > 0) {
			throw ThrowUtil.newValidationErrorWithNoLog(true, "MPS_NOT_ALLOWED_CANCEL_AFTER_START_JOB"); // 분류 작업시작 이후여서 취소가 불가능합니다.
		}
		
		// 4. Cell의 box_id 클리어
		sql = "update work_cells set box_id = null, ind_cd = null where domain_id = :domainId and cell_cd = :cellCd";
		this.queryManager.executeBySql(sql, conditions);
		
		// 5. 주문의 box_id 클리어
		sql = "update orders set box_id = null where domain_id = :domainId and batch_id = :batchId and equip_cd = :equipCd and sub_equip_cd = :cellCd and to_zone_cd = :cartJobId";
		this.queryManager.executeBySql(sql, conditions);
		
		// 6. 작업의 box_id 클리어
		sql = "update job_instances set input_seq = 0, ind_cd = null, box_id = null where domain_id = :domainId and batch_id = :batchId and equip_cd = :equipCd and sub_equip_cd = :cellCd and box_class_cd = :cartJobId";
		this.queryManager.executeBySql(sql, conditions);
		
		// 7. 표시기에 박스 매핑 표시
		if(DpcBatchJobConfigUtil.isUseIndicator(batch)) {
			IIndicationService indSvc = this.serviceDispatcher.getIndicationService(batch);
			indSvc.displayForNoBoxError(batch, cell.getIndCd());
		}
		
		// 8. 셀 정보 리턴
		return cell;
	}

	@Override
	public BoxPack fullBoxing(JobBatch batch, WorkCell workCell, List<JobInstance> jobList, Object... params) {
		// 1. 작업 리스트 존재 여부 체크
		if(ValueUtil.isEmpty(jobList)) {
			throw ThrowUtil.newValidationErrorWithNoLog(true, "NO_JOBS_FOR_BOXING");
		}
		
		// 2. 셀 - 박스 매핑이 안 되어 있는 경우 표시기 에러 표시
		if(ValueUtil.isEmpty(workCell.getBoxId())) {
			if(DpcBatchJobConfigUtil.isUseIndicator(batch)) {
				this.serviceDispatcher.getIndicationService(batch).displayForNoBoxError(batch, jobList.get(0).getIndCd());
			}
			
			throw ThrowUtil.newValidationErrorWithNoLog(true, "CELL_HAS_NO_MAPPED_BOX");
		}
		
		// 3. 커스텀 서비스 전 처리 호출 - 커스텀 서비스에서 박스 ID를 리턴하면 리턴 박스 ID를 새로운 박스 ID로 사용
		Map<String, Object> customParams = ValueUtil.newMap("batch,workCell,jobList", batch, workCell, jobList);
		Long domainId = batch.getDomainId();
		Object objBoxId = this.customService.doCustomService(domainId, DIY_DPC_PRE_FULLBOX, customParams);
		String boxId = ValueUtil.isNotEmpty(objBoxId) ? objBoxId.toString() : workCell.getBoxId();
		String nowStr = DateUtil.currentTimeStr();
		
		// 4. 작업 정보 업데이트 
		for(JobInstance job : jobList) {
			job.setBoxId(boxId);
			job.setBoxedAt(nowStr);
			if(ValueUtil.isEmpty(job.getPickEndedAt())) {
				job.setPickEndedAt(nowStr);
			}
			job.setStatus(LogisConstants.JOB_STATUS_BOXED);
		}
		this.queryManager.updateBatch(jobList, "boxId", "boxedAt", "pickEndedAt", "status", "updatedAt");
		
		// 5. 박스 정보 생성
		Map<String, Object> condition = ValueUtil.newMap("domainId,batchId,boxId", domainId, batch.getId(), boxId);
		BoxPack boxPack = this.queryManager.selectByCondition(BoxPack.class, condition);
		
		// 6. 커스텀 서비스 후 처리 구현 (ex: 송장 출력, 실적 전송)
		customParams.put("box", boxPack);
		this.customService.doCustomService(domainId, DIY_DPC_POST_FULLBOX, customParams);
		
		// 7. WorkCell 업데이트
		if(ValueUtil.isNotEmpty(workCell.getBoxId())) {
			workCell.setBoxId(null);
			this.queryManager.update(workCell, "boxId");
		}
		
		// 8. 박스 리턴
		return boxPack;
	}

	@Override
	public BoxPack partialFullboxing(JobBatch batch, WorkCell workCell, List<JobInstance> jobList, Integer fullboxQty, Object... params) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public List<BoxPack> batchBoxing(JobBatch batch) {
		// 1. 작업 셀 리스트 조회
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId", batch.getDomainId(), batch.getId());
		String sql = "select * from work_cells where domain_id = :domainId and batch_id = :batchId and (status is null or status != 'ENDED') and active_flag = true order by cell_cd";
		List<WorkCell> cellList = this.queryManager.selectListBySql(sql, params, WorkCell.class, 0, 0);

		// 2. 작업 셀 정보가 없으면 리턴
		if(ValueUtil.isEmpty(cellList)) {
			return new ArrayList<BoxPack>(1);
		}
		
		// 3. 박스 팩 리스트 생성
		List<BoxPack> boxPacks = new ArrayList<BoxPack>();
		
		// 4. 배치 내 박스 ID가 null이고 상태가 F(피킹 완료)인 작업 데이터를 셀 별로 모두 조회
		params.put("status", LogisConstants.JOB_STATUS_FINISH);
		sql = "select * from job_instances where domain_id = :domainId and batch_id = :batchId and status = :status and sub_equip_cd = :cellCd and (box_id is null or box_id = '') order by id";
		
		for(WorkCell cell : cellList) {
			params.put("cellCd", cell.getCellCd());
			List<JobInstance> jobList = this.queryManager.selectListBySql(sql, params, JobInstance.class, 1, 1);
			
			if(ValueUtil.isNotEmpty(jobList)) {
				// 5. 셀 별 작업이 있다면 풀 박스 처리
				JobInstance job = jobList.get(0);
				ClassifyOutEvent outEvent = new ClassifyOutEvent(SysEvent.EVENT_STEP_ALONE, Indicator.class.getSimpleName(), LogisCodeConstants.CLASSIFICATION_ACTION_FULL, job, job.getPickedQty(), job.getPickedQty());
				outEvent.setWorkCell(cell);
				this.eventPublisher.publishEvent(outEvent);
				
				if(outEvent.getResult() != null) {
					boxPacks.add((BoxPack)outEvent.getResult());
				}
			}
		}
		
		// 5. 결과 리턴 
		return boxPacks;
	}

	@Override
	public BoxPack cancelFullboxing(BoxPack box) {
		UndoBoxingEvent event = new UndoBoxingEvent(SysEvent.EVENT_STEP_ALONE, box);
		this.eventPublisher.publishEvent(event);
		return box;
	}

}
