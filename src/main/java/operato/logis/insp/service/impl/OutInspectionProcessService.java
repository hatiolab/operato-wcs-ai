package operato.logis.insp.service.impl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import operato.logis.insp.model.OutInspection;
import xyz.anythings.base.LogisCodeConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.BoxItem;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.event.rest.DeviceProcessRestEvent;
import xyz.anythings.base.service.impl.AbstractLogisService;
import xyz.anythings.sys.model.BaseResponse;
import xyz.anythings.sys.service.ICustomService;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 출고 검수용 트랜잭션 이벤트 처리 서비스 
 * 
 * @author shortstop
 */
@Component
public class OutInspectionProcessService extends AbstractLogisService {
	/**
	 * 출고 검수 서비스
	 */
	@Autowired
	private OutInspectionService outInspectionService;
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	private ICustomService customService;
	
	/*****************************************************************************************************
	 *											출 고 검 수 A P I
	 *****************************************************************************************************
	
	/**
	 * 출고 검수를 위한 검수 정보 조회 - 박스 ID
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/find_by_box', 'out')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void findByBox(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		JobBatch batch = this.findJobBatchByParams(event.getDomainId(), params);
		
		// 3. 검수 정보 조회
		OutInspection inspection = this.findInspectionByParams(batch, params, true);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inspection));
		event.setExecuted(true);
	}
	
	/**
	 * 출고 검수를 위한 검수 정보 조회 - 송장 번호
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/find_by_invoice', 'out')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void findByInvoice(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		JobBatch batch = this.findJobBatchByParams(event.getDomainId(), params);
		
		// 3. 검수 정보 조회
		OutInspection inspection = this.findInspectionByParams(batch, params, true);
		
		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inspection));
		event.setExecuted(true);
	}
	
	/**
	 * 출고 검수를 위한 검수 정보 조회 - 주문 번호
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/find_by_order', 'out')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void findByOrder(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		JobBatch batch = this.findJobBatchByParams(event.getDomainId(), params);
		
		// 3. 검수 정보 조회
		OutInspection inspection = this.findInspectionByParams(batch, params, true);

		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inspection));
		event.setExecuted(true);
	}
	
	/**
	 * 출고 검수를 위한 검수 정보 조회 - 분류 코드
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/find_by_class_cd', 'out')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void findByClassCd(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 추출
		Map<String, Object> params = event.getRequestParams();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		JobBatch batch = this.findJobBatchByParams(event.getDomainId(), params);
		
		// 3. 검수 정보 조회
		OutInspection inspection = this.findInspectionByParams(batch, params, true);

		// 4. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inspection));
		event.setExecuted(true);
	}
	
	/**
	 * 송장 (박스) 분할
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/split_box', 'out')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void splitBox(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String printerId = params.get("printerId").toString();
		String inspItems = params.get("inspItems").toString();
		
		// 2. 분할할 InspectionItem 정보 파싱
		Gson gson = new Gson();
		Type type = new TypeToken<List<BoxItem>>(){}.getType();
		List<BoxItem> dpsInspItems = gson.fromJson(inspItems, type);
		
		// 3. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		JobBatch batch = this.findJobBatchByParams(event.getDomainId(), params);
		
		// 4. 박스 정보 조회
		OutInspection inspection = this.findInspectionByParams(batch, params, false);
		
		if(inspection == null) {
			inspection = this.findInspectionByParams(null, params, true);
		}
		
		// 5. 송장 분할
		BoxPack splitBox = this.outInspectionService.splitBox(batch, inspection, dpsInspItems, printerId);
		
		// 6. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, splitBox));
		event.setExecuted(true);
	}
	
	/**
	 * 출고 검수 완료
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/inspection/finish', 'out')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void finishInspection(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String printerId = params.get("printerId").toString();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		JobBatch batch = this.findJobBatchByParams(event.getDomainId(), params);
		
		// 3. 검수 정보 조회
		OutInspection inspection = this.findInspectionByParams(batch, params, false);
		
		if(inspection == null) {
			inspection = this.findInspectionByParams(null, params, true);
		}
		
		// 4. 검수 처리
		this.outInspectionService.finishInspection(batch, inspection, printerId);
		
		// 5. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, inspection));
		event.setExecuted(true);
	}
	
	/**
	 * 송장 출력
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/print_invoice', 'out')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void printInvoiceLabel(DeviceProcessRestEvent event) {
		
		// 1. 파라미터
		Map<String, Object> params = event.getRequestParams();
		String printerId = params.get("printerId").toString();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회
		JobBatch batch = this.findJobBatchByParams(event.getDomainId(), params);
		
		// 3. 박스 조회
		OutInspection inspection = this.findInspectionByParams(batch, params, false);
		
		if(inspection == null) {
			inspection = this.findInspectionByParams(null, params, true);
		}
		
		// 4. 송장 발행
		String customServiceName = "diy-" + batch.getJobType() + "-print-invoice";
		Object result = this.customService.doCustomService(event.getDomainId(), customServiceName.toLowerCase(), ValueUtil.newMap("batch,box,printerId", batch, inspection, printerId));
		Integer printedCount = 1;
		
		if(result == null) {
			printedCount = this.outInspectionService.printInvoiceLabel(batch, inspection, printerId);
		} else {
			printedCount = result instanceof Integer ? (Integer)result : 1;
		}
		
		// 5. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, printedCount));
		event.setExecuted(true);
	}
	
	/**
	 * 거래명세서 출력
	 * 
	 * @param event
	 */
	@EventListener(classes=DeviceProcessRestEvent.class, condition = "#event.checkCondition('/print_trade_statement', 'out')")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void printTradeStatement(DeviceProcessRestEvent event) {
		
		// 1. 파라미터 
		Map<String, Object> params = event.getRequestParams();
		String printerId = params.get("printerId").toString();
		
		// 2. 설비 코드로 현재 진행 중인 작업 배치 및 설비 정보 조회 
		JobBatch batch = this.findJobBatchByParams(event.getDomainId(), params);
		
		// 3. 박스 조회
		OutInspection inspection = this.findInspectionByParams(batch, params, false);
		
		if(inspection == null) {
			inspection = this.findInspectionByParams(null, params, true);
		}
		
		// 4. 거래명세서 발행
		Integer printedCount = this.outInspectionService.printTradeStatement(batch, inspection, printerId);
		
		// 5. 이벤트 처리 결과 셋팅
		event.setReturnResult(new BaseResponse(true, LogisConstants.OK_STRING, printedCount));
		event.setExecuted(true);
	}
	
	/**
	 * API 호출 파라미터로 부터 박스 ID, 주문 번호, 송장 번호 등으로 진행 중인 작업 배치를 조회
	 * 
	 * @param domainId
	 * @param params
	 * @return
	 */
	private JobBatch findJobBatchByParams(Long domainId, Map<String, Object> params) {
		boolean boxIdContains = params.containsKey("boxId");
		boolean invoiceIdContains = params.containsKey("invoiceId");
		boolean orderNoContains = params.containsKey("orderNo");
		boolean classCdContains = params.containsKey("classCd");
				
		StringBuffer sql = new StringBuffer()
			.append("select * from job_batches where domain_id = :domainId")
			.append(" #if($equipType)")
			.append(" and equip_type = :equipType")
			.append(" #end")
			.append(" and status = 'RUN'")
			.append(" and id in (")
			.append("		select")
			.append("			distinct(batch_id) as batch_id")
			.append("		from")
			.append("			job_instances")
			.append("		where")
			.append("			domain_id = :domainId ");
						
			if(boxIdContains) {
				sql.append(" and box_id = :boxId");
			} else if(invoiceIdContains) {
				sql.append(" and invoice_id = :invoiceId");
			} else if(orderNoContains) {
				sql.append(" and order_no = :orderNo");
			} else if(classCdContains) {
				sql.append(" and class_cd = :classCd");
			}
			
		sql.append("			)");
		
		params.put("domainId", domainId);
		List<JobBatch> batches = this.queryManager.selectListBySql(sql.toString(), params, JobBatch.class, 0, 0);
		
		if(ValueUtil.isEmpty(batches)) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("ORDER_NOT_FOUND"));
		}
		
		return batches.get(0);
	}
	
	/**
	 * API 호출 파라미터로 부터 송장 번호, 주문 번호, 분류 코드, 박스 ID 등의 정보를 추출하여 검수 정보 조회
	 * 
	 * @param batch
	 * @param params
	 * @param exceptionWhenEmpty
	 * @return
	 */
	private OutInspection findInspectionByParams(JobBatch batch, Map<String, Object> params, boolean exceptionWhenEmpty) {
		String invoiceId = params.containsKey("invoiceId") ? params.get("invoiceId").toString() : null;
		String orderNo = params.containsKey("orderNo") ? params.get("orderNo").toString() : null;
		String classCd = params.containsKey("classCd") ? params.get("classCd").toString() : null;
		String boxId = params.containsKey("boxId") ? params.get("boxId").toString() : null;
		String boxType = params.containsKey("boxType") ? params.get("boxType").toString() : LogisCodeConstants.BOX_TYPE_BOX;
		boolean reprintMode = params.containsKey("reprintMode") ? ValueUtil.toBoolean(params.get("reprintMode")) : false;
		OutInspection inspection = this.outInspectionService.findInspection(batch, invoiceId, orderNo, boxId, classCd, boxType, reprintMode, exceptionWhenEmpty);
		
		if(inspection == null) {
			throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("ORDER_NOT_FOUND"));
		}
		
		return inspection;
	}

}
