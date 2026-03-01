package operato.logis.insp.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import operato.logis.insp.model.OutInspection;
import operato.logis.insp.service.api.IOutInspectionService;
import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxItem;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.TrayBox;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.anythings.sys.event.model.PrintEvent;
import xyz.anythings.sys.service.ICustomService;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.DateUtil;

/**
 * 출고 수량 검수 서비스
 * 
 * @author shortstop
 */
@Component("outInspectionService")
public class OutInspectionService extends AbstractLogisService implements IOutInspectionService {

	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;
	
	@Override
	public OutInspection findInspection(JobBatch batch, String invoiceId, String orderNo, String boxId, String classCd, String boxType, boolean reprintMode, boolean exceptionWhenEmpty) {

		OutInspection inspection = null;
		
		if(ValueUtil.isNotEmpty(invoiceId)) {
			inspection = this.findInspectionByInput(batch, "invoiceId", invoiceId, reprintMode, false);
		}
		
		if(inspection == null && ValueUtil.isNotEmpty(orderNo)) {
			inspection = this.findInspectionByInput(batch, "orderNo", orderNo, reprintMode, false);
		}
		
		if(inspection == null && ValueUtil.isNotEmpty(boxId) && ValueUtil.isEqualIgnoreCase(boxType, LogisCodeConstants.BOX_TYPE_BOX)) {
			inspection = this.findInspectionByInput(batch, "box", boxId, reprintMode, false);
		}
		
		if(inspection == null && ValueUtil.isNotEmpty(boxId) && ValueUtil.isEqualIgnoreCase(boxType, LogisCodeConstants.BOX_TYPE_TRAY)) {
			inspection = this.findInspectionByInput(batch, "tray", boxId, reprintMode, false);
		}
		
		if(inspection == null && ValueUtil.isNotEmpty(classCd)) {
			inspection = this.findInspectionByInput(batch, "classCd", classCd, reprintMode, exceptionWhenEmpty);
		}
		
		return inspection;
	}
	
	@Override
	public OutInspection findInspectionByInput(JobBatch batch, String inputType, String inputId, boolean reprintMode, boolean exceptionWhenEmpty) {
		Long domainId = batch == null ? Domain.currentDomainId() : batch.getDomainId();
		Map<String, Object> params = ValueUtil.newMap("domainId", domainId);
		if(batch != null) {
			params.put("batchId", batch.getId());
		}
		
		String boxType = LogisCodeConstants.BOX_TYPE_BOX;
		if(ValueUtil.isEqualIgnoreCase(inputType, LogisCodeConstants.BOX_TYPE_TRAY)) {
			params.put("boxId", inputId);
			boxType = LogisCodeConstants.BOX_TYPE_TRAY;
			
		} else if(ValueUtil.isEqualIgnoreCase(inputType, LogisCodeConstants.BOX_TYPE_BOX)) {
			params.put("boxId", inputId);
			
		} else if(ValueUtil.isEqualIgnoreCase(inputType, "orderNo")) {
			params.put("orderNo", inputId);
			
		} else if(ValueUtil.isEqualIgnoreCase(inputType, "classCd")) {
			params.put("classCd", inputId);
			
		} else if(ValueUtil.isEqualIgnoreCase(inputType, "invoiceId")) {
			params.put("invoiceId", inputId);
		}
		
		// 검수 정보 조회
		OutInspection inspection = this.findInspection(domainId, reprintMode, params, inputId, boxType.toLowerCase(), exceptionWhenEmpty);
		
		if(inspection != null) {
			// 검수 상품 정보 조회
			this.searchInpsectionItems(inspection, params);
			
			// 검수 조회 후 커스텀 서비스 처리
			Map<String, Object> parameters = ValueUtil.newMap("batch,box", batch, inspection);
			String diySvcName = "diy-" + batch.getJobType().toLowerCase() + "-before-inspection";
			this.customService.doCustomService(domainId, diySvcName, parameters);
		}
		
		// 검수 정보 리턴
		return inspection;
	}

	@Override
	public OutInspection findInspectionByTrayCd(JobBatch batch, String trayCd, boolean reprintMode, boolean exceptionWhenEmpty) {
		Long domainId = batch.getDomainId();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,boxId", domainId, batch.getId(), trayCd);
		OutInspection inspection = this.findInspection(domainId, reprintMode, params, trayCd, LogisCodeConstants.BOX_TYPE_TRAY.toLowerCase(), exceptionWhenEmpty);
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public OutInspection findInspectionByBoxId(JobBatch batch, String boxId, boolean exceptionWhenEmpty) {
		Long domainId = batch.getDomainId();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,boxId", domainId, batch.getId(), boxId);
		OutInspection inspection = this.findInspection(domainId, false, params, boxId, LogisCodeConstants.BOX_TYPE_BOX.toLowerCase(), exceptionWhenEmpty);
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public OutInspection findInspectionByInvoiceId(JobBatch batch, String invoiceId, String boxType, boolean exceptionWhenEmpty) {
		Long domainId = batch.getDomainId();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,invoiceId", domainId, batch.getId(), invoiceId);
		OutInspection inspection = this.findInspection(domainId, false, params, invoiceId, boxType, exceptionWhenEmpty);
		return this.searchInpsectionItems(inspection, params);
	}
	
	@Override
	public OutInspection findInspectionByOrderNo(JobBatch batch, String orderNo, String boxType, boolean exceptionWhenEmpty) {
		Long domainId = batch.getDomainId();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,orderNo", domainId, batch.getId(), orderNo);
		OutInspection inspection = this.findInspection(domainId, false, params, orderNo, boxType, exceptionWhenEmpty);
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public OutInspection findInspectionByClassCd(JobBatch batch, String classCd, String boxType, boolean exceptionWhenEmpty) {
		Long domainId = batch.getDomainId();
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,classCd", domainId, batch.getId(), classCd);
		OutInspection inspection = this.findInspection(domainId, false, params, classCd, boxType, exceptionWhenEmpty);
		return this.searchInpsectionItems(inspection, params);
	}

	@Override
	public List<OutInspection> searchInspectionList(JobBatch batch, String classCd, String boxType, boolean exceptionWhenEmpty) {
		
		Map<String, Object> params = ValueUtil.newMap("domainId,batchId,classCd", batch.getDomainId(), batch.getId(), classCd);
		return this.searchInpsectionList(false, params, classCd, boxType, exceptionWhenEmpty);
	}
	
	@Override
	public void finishInspection(JobBatch batch, OutInspection inspection, String printerId, Object... params) {
		
		// 1. 박스 상태 업데이트
		Long domainId = batch.getDomainId();
		inspection.setStatus(LogisConstants.JOB_STATUS_EXAMINATED);
		inspection.setManualInspStatus(LogisConstants.PASS_STATUS);
		inspection.setManualInspectedAt(DateUtil.currentTimeStr());
		
		// 2. 작업 정보 검수 완료 처리
		Map<String, Object> updateParams = ValueUtil.newMap("domainId,batchId,classCd,status,manualInspStatus,updaterId,nowStr,now",
				domainId,
				batch.getId(),
				inspection.getClassCd(),
				LogisConstants.JOB_STATUS_EXAMINATED,
				LogisConstants.PASS_STATUS,
				User.currentUser().getId(),
				inspection.getManualInspectedAt(),
				new Date()
			);
		
		String sql = "update job_instances set picking_qty = 0, picked_qty = pick_qty, inspected_qty = pick_qty, status = :status, manual_insp_status = :manualInspStatus, manual_inspected_at = :nowStr, updater_id = :updaterId, updated_at = :now where domain_id = :domainId and batch_id = :batchId and class_cd = :classCd";
		this.queryManager.executeBySql(sql, updateParams);
		
		// 3. 박스 유형이 트레이라면
		if(ValueUtil.isEqualIgnoreCase(inspection.getBoxType(), LogisCodeConstants.BOX_TYPE_TRAY)) {
			// 3.1 트레이 조회 후
			TrayBox condition = new TrayBox();
			condition.setTrayCd(inspection.getBoxId());
			TrayBox tray = this.queryManager.selectByCondition(TrayBox.class, condition);
			
			// 3.1 트레이 상태 변경
			if(tray != null) {
				tray.setStatus(LogisConstants.JOB_STATUS_WAIT);
				this.queryManager.update(tray, "status", "updaterId", "updatedAt");
			}
		}
		
		// 4. 검수 완료 후 커스텀 서비스 처리
		Map<String, Object> parameters = ValueUtil.newMap("batch,box,printerId", batch, inspection, printerId);
		String diySvcName = "diy-" + batch.getJobType().toLowerCase() + "-after-inspection";
		this.customService.doCustomService(domainId, diySvcName, parameters);
	}

	@Override
	public BoxPack splitBox(JobBatch batch, OutInspection inspection, List<BoxItem> inspectionItems, String printerId, Object... params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int printInvoiceLabel(JobBatch batch, OutInspection inspection, String printerId, Object... params) {
		
		String labelTemplate = BatchJobConfigUtil.getInvoiceLabelTemplate(batch);
		PrintEvent printEvent = new PrintEvent(batch.getDomainId(), batch.getJobType(), printerId, labelTemplate, ValueUtil.newMap("batch,box", batch, inspection));
		printEvent.setPrintType("barcode");
		printEvent.setSyncMode(false);
		this.eventPublisher.publishEvent(printEvent);
		return 1;
	}

	@Override
	public int printTradeStatement(JobBatch batch, OutInspection inspection, String printerId, Object... params) {
		
		String labelTemplate = BatchJobConfigUtil.getTradeStatmentTemplate(batch);
		PrintEvent printEvent = new PrintEvent(batch.getDomainId(), batch.getJobType(), printerId, labelTemplate, ValueUtil.newMap("batch,box", batch, inspection));
		printEvent.setPrintType("normal");
		printEvent.setSyncMode(false);
		this.eventPublisher.publishEvent(printEvent);
		return 1;
	}

	/**
	 * 검수 정보 조회
	 * 
	 * @param domainId
	 * @param reprintMode
	 * @param params
	 * @param orderInfo
	 * @param boxType
	 * @param exceptionWhenEmpty
	 * @return
	 */
	private OutInspection findInspection(Long domainId, boolean reprintMode, Map<String, Object> params, String orderInfo, String boxType, boolean exceptionWhenEmpty) {
		
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		Iterator<String> keyIter = params.keySet().iterator();
		while(keyIter.hasNext()) {
			String key = keyIter.next();
			condition.addFilter(key, params.get(key));
		}
		
		if(ValueUtil.isEqualIgnoreCase(boxType, LogisCodeConstants.BOX_TYPE_TRAY)) {
			String oper = reprintMode ? "in" : "notin";
			condition.addFilter("status", oper, ValueUtil.toList(LogisConstants.JOB_STATUS_EXAMINATED, LogisConstants.JOB_STATUS_FINAL_OUT, LogisConstants.JOB_STATUS_REPORTED));
		} else {
			if(reprintMode) {
				condition.addFilter("status", SysConstants.IN, ValueUtil.toList(LogisConstants.JOB_STATUS_EXAMINATED, LogisConstants.JOB_STATUS_FINAL_OUT, LogisConstants.JOB_STATUS_REPORTED));
			}
		}
		
		BoxPack boxPack = null;
		
		if(reprintMode) {
			// 재발행 모드는 마지막 검수한 주문을 찾는다.
			condition.addOrder("manualInspectedAt", false);
			condition.setPageSize(1);
			condition.setPageIndex(1);
			List<BoxPack> boxPackList = this.queryManager.selectList(BoxPack.class, condition);
			boxPack = boxPackList.isEmpty() ? null : boxPackList.get(0);
			
		} else {
			// 검수 모드는 박스가 반드시 하나만 나와야 한다.
			boxPack = this.queryManager.selectByCondition(BoxPack.class, condition);
		}
		
		if(boxPack == null) {
			if(exceptionWhenEmpty) {
				throw ThrowUtil.newNotFoundRecord("terms.label.inspection", ValueUtil.toString(orderInfo));
			} else {
				return null;
			}
		} else {
			OutInspection inspection = ValueUtil.populate(boxPack, new OutInspection());
			inspection.setBoxType(boxType);
			return inspection;
		}
	}
	
	/**
	 * 검수 리스트 조회
	 * 
	 * @param reprintMode
	 * @param params
	 * @param boxType
	 * @param orderInfo
	 * @param exceptionWhenEmpty
	 * @return
	 */
	private List<OutInspection> searchInpsectionList(boolean reprintMode, Map<String, Object> params, String boxType, String orderInfo, boolean exceptionWhenEmpty) {
		
		Query condition = new Query();
		Iterator<String> keyIter = params.keySet().iterator();
		while(keyIter.hasNext()) {
			String key = keyIter.next();
			condition.addFilter(key, params.get(key));
		}
		
		if(reprintMode) {
			condition.addFilter("status", SysConstants.IN, ValueUtil.toList(LogisConstants.JOB_STATUS_EXAMINATED, LogisConstants.JOB_STATUS_FINAL_OUT, LogisConstants.JOB_STATUS_REPORTED));
		}
		List<BoxPack> boxPackList = this.queryManager.selectList(BoxPack.class, condition);
		
		if(ValueUtil.isEmpty(boxPackList)) {
			if(exceptionWhenEmpty) {
				throw ThrowUtil.newNotFoundRecord("terms.label.inspection", ValueUtil.toString(orderInfo));
			} else {
				return null;
			}
		} else {
			List<OutInspection> inspectionList = new ArrayList<OutInspection>();
			for(BoxPack boxPack : boxPackList) {
				OutInspection inspection = ValueUtil.populate(boxPack, new OutInspection());
				inspection.setBoxType(boxType);
				inspectionList.add(inspection);
			}
			
			return inspectionList;
		}
	}
	
	/**
	 * 검수 항목 조회 처리 ...
	 * 
	 * @param inspection
	 * @param params
	 * @return
	 */
	private OutInspection searchInpsectionItems(OutInspection inspection, Map<String, Object> params) {
		
		if(inspection == null) {
			return null;
		}
		
		Map<String, Object> condition = ValueUtil.newMap("boxPackId", inspection.getId());
		List<BoxItem> boxItems = this.queryManager.selectList(BoxItem.class, condition);
		
		if(ValueUtil.isNotEmpty(boxItems)) {
			List<BoxItem> items = new ArrayList<BoxItem>(boxItems.size());
			
			for(BoxItem item : boxItems) {
				items.add(ValueUtil.populate(item, new BoxItem()));
			}
			
			inspection.setItems(items);
		}
		
		return inspection;
	}

}
