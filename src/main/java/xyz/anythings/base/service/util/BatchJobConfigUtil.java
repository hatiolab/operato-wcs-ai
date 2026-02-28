package xyz.anythings.base.service.util;

import xyz.anythings.base.LogisConfigConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.service.impl.JobConfigProfileService;
import xyz.anythings.sys.AnyConstants;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * 작업 배치 범위 내 작업 설정 값 조회 유틸리티
 * 작업 설정 리스트
 *  - job.cmm.sku.barcode.max.length								상품 바코드 최대 길이
 *  - job.cmm.sku.search.condition.fields							SKU 조회를 위한 코드 필드명 리스트
 *  - job.cmm.sku.search.select.fields								SKU 조회를 위한 조회 필드명 리스트
 *  - job.cmm.sku.skucd.validation.enabled							서버 사이드에서 상품 유효성 체크 여부
 *  - job.cmm.sku.weight.unit										상품 중량 정보를 g으로 사용할 지 kg으로 사용할 지 여부
 *  - job.cmm.server.validate.sku_cd.rule							서버 사이드에서 상품 유효성 체크를 위한 룰
 *  - job.cmm.server.validate.box_id.rule							서버 사이드에서 박스 ID 유효성 체크를 위한 룰
 *  - job.cmm.server.validate.cell_cd.rule							서버 사이드에서 로케이션 코드 유효성 체크를 위한 룰
 *  - job.cmm.server.validate.ind_cd.rule							서버 사이드에서 표시기 코드 유효성 체크를 위한 룰
 *  - job.cmm.server.validate.rack_cd.rule							서버 사이드에서 랙 코드 유효성 체크를 위한 룰
 *  - job.cmm.server.validate.invoice_no.rule						서버 사이드에서 송장번호 유효성 체크를 위한 룰
 *  - job.cmm.box.cell.mapping.point								박스 셀 매핑 시점
 *  - job.cmm.box.result.report.enabled								박스 실적 보고 여부 
 *  - job.cmm.box.result.report.point								박스 실적 전송 시점
 *  - job.cmm.box.cancel.enabled									박스 취소 기능 활성화 여부 
 *  - job.cmm.box.weight.enabled									박스 중량 관리 여부
 *  - job.cmm.box.out.class.field									주문 필드 중에 박스 처리시에 출고 분류 코드로 사용할 필드 명
 *  - job.cmm.box.action											박스 처리 후 액션
 *  - job.cmm.box.invoice-no.rule									옵션에 따라 송장 번호를 다르게 부여 
 *  - job.cmm.box.box_id.unique.scope								박스 ID 유일성 보장 범위
 *  - job.cmm.pick.cancel.enabled									확정 취소 기능 활성화 여부 
 *  - job.cmm.pick.result.report.enabled							확정 실적 보고 여부 
 *  - job.cmm.pick.cancel.status.enabled							표시기에서 분류 작업 취소시에 '취소' 상태로 관리할 지 여부
 *  - job.cmm.pick.include.cancelled.enabled						상품 투입시 취소된 상품을 조회할 지 여부
 *  - job.cmm.pick.fullbox.enabled									피킹 시 풀 박스 기능을 사용할 지 여부
 *  - job.cmm.label.print.count										라벨 발행시 한 번에 동일 라벨을 몇 장 발행할 지 설정 
 *  - job.cmm.label.print.method									송장 라벨 발행 방법 
 *  - job.cmm.label.template										송장 라벨을 자체 출력시 송장 라벨 출력 템플릿 설정 MPS 매니저 > 개발자 > 커스텀 템플릿에 등록된 송장 라벨 명칭을 설정하면 됨
 *  - job.cmm.device.list											사용 디바이스 리스트
 *  - job.cmm.device.side.enabled									디바이스의 작업 위치 (앞,뒤,앞/뒤,전체 등) 정보를 사용할 지 여부
 *  - job.cmm.device.station.enabled								디바이스의 작업 영역 정보를 사용할 지 여부
 *  - job.cmm.inspection.enabled									출고 검수 활성화 여부
 *  - job.cmm.insepction.weight.enabled								중량 검수 활성화 여부
 *  - job.cmm.inspection.action										출고 검수 후 액션. 아래 출고 검수 활성화 시에만 의미가 있음.
 *  - job.cmm.input.work_scope										투입시 투입 범위 (station : 작업 존 별 투입, rack : 호기별 투입, batch : 배치별 투입, batch_group : 배치 그룹별 투입)
 *  - job.cmm.input.ind_on.mode										투입시 표시기 점등 모드 (all : 전체 상품 점등, qty : 투입 수량 만큼 점등) 
 *  - job.cmm.input.mode.single.enabled								단품 투입 활성화 여부
 *  - job.cmm.input.single.ind_on.mode								job.cmm.input.mode.single.enabled이 true인 경우에 단품 투입시 하나씩 표시기에 점등할 것인지 전체 표시기에 점등할 것인지 설정
 *  - job.cmm.input.mode.box.enabled								완박스 투입 활성화 여부
 *  - job.cmm.input.box.ind_on.mode									job.cmm.input.mode.box.enabled이 true인 경우에 완박스 투입시 하나씩 표시기에 점등할 것인지 전체 표시기에 점등할 것인지 설정
 *  - job.cmm.input.mode.bundle.enabled								번들 투입 활성화 여부
 *  - job.cmm.order.delete.when.order_cancel						주문 취소시 데이터 삭제 여부
 *  - job.cmm.assigned-cell.indicator.enabled.when.batch.start		작업지시 시점에 표시기에 할당 셀 표시 활성화 여부
 *  - job.cmm.assigned-cell.wait.duration.when.batch.start			작업지시 시점에 표시기에 할당 셀 표시시 대기 시간 (초)
 *  - job.cmm.trade-statement.template								거래명세서 템플릿 이름을 설정
 *  - job.cmm.reboot.enabled.when.batch.start						작업배치 시에 게이트웨이 리부팅 할 지 여부								
 *	
 * @author shortstop
 */
public class BatchJobConfigUtil {
	
	/**
	 * 설정 프로파일 서비스
	 */
	public static JobConfigProfileService CONFIG_SET_SVC;
	
	/**
	 * 설정 프로파일 서비스 리턴
	 * 
	 * @return
	 */
	public static JobConfigProfileService getConfigSetService() {
		if(CONFIG_SET_SVC == null) {
			CONFIG_SET_SVC = BeanUtil.get(JobConfigProfileService.class);
		}
		
		return CONFIG_SET_SVC;
	}
	
	/**
	 * 작업 배치 범위 내에 설정 내용을 키로 조회해서 리턴
	 *  
	 * @param batch
	 * @param key
	 * @param exceptionWhenEmptyValue
	 * @return
	 */
	public static String getConfigValue(JobBatch batch, String key, boolean exceptionWhenEmptyValue) {
		String jobType = batch.getJobType().toLowerCase();
		JobConfigProfileService configSvc = getConfigSetService();
		
		// 1. 작업 유형에 따른 설정값 조회
		String jobTypeKey = key.replace("job.cmm", jobType);
		String value = configSvc.getConfigValue(batch, jobTypeKey);
		
		// 2. 1값이 없다면 공통 설정값 조회
		if(ValueUtil.isEmpty(value)) {
			jobTypeKey = key.replace("job.cmm", "cmm");
			value = configSvc.getConfigValue(batch, jobTypeKey);
		}
		
		// 3. 2값이 없다면
		if(ValueUtil.isEmpty(value)) {
			jobTypeKey = key.replace("job.", LogisConstants.EMPTY_STRING);
			value = configSvc.getConfigValue(batch, jobTypeKey);
		}
		
		// 4. 3값까지 없다면 그대로 조회
		if(ValueUtil.isEmpty(value)) {
			value = configSvc.getConfigValue(batch, key);
		}
		
		// 5. 설정값이 없다면 exceptionWhenEmptyValue에 따라 예외 처리
		if(ValueUtil.isEmpty(value) && exceptionWhenEmptyValue) {
			throw ThrowUtil.newJobConfigNotSet(key);
		}
		
		// 6. 값 리턴
		return value;
	}
	
	/**
	 * 작업 배치 범위 내에 설정 내용을 키로 조회해서 리턴
	 *  
	 * @param batch
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getConfigValue(JobBatch batch, String key, String defaultValue) {
		// 1. 기본값 조회
		String value = getConfigValue(batch, key, false);
		
		// 2. 설정값이 없다면 defaultValue 리턴
		return ValueUtil.isEmpty(value) ? defaultValue : value;
	}
	
	/**
	 * 작업 배치 설정 중에 최대 바코드 사이즈
	 * 
	 * @param batch
	 * @return
	 */
	public static int getMaxBarcodeSize(JobBatch batch) {
		// job.cmm.sku.barcode.max.length
		String intVal = getConfigValue(batch, LogisConfigConstants.SKU_BARCODE_MAX_LENGTH, true);
		return ValueUtil.toInteger(intVal);
	}
	
	/**
	 * 상품 조회를 위한 코드 필드명 리스트
	 * 
	 * @param batch
	 * @return
	 */
	public static String[] getSkuSearchConditionFields(JobBatch batch) {
		// job.cmm.sku.search.condition.fields
		String strVal = getConfigValue(batch, LogisConfigConstants.SKU_CONDITION_FIELDS_TO_SEARCH, true);
		return strVal.split(LogisConstants.COMMA);
	}
	
	/**
	 * 상품 조회를 위한 상품 조회 필드명 리스트
	 * 
	 * @param batch
	 * @return
	 */
	public static String getSkuSearchSelectFields(JobBatch batch) {
		// job.cmm.sku.search.select.fields
		return getConfigValue(batch, LogisConfigConstants.SKU_SELECT_FIELDS_TO_SEARCH, true);
	}
	
	/**
	 * 서버 사이드에서 상품 코드 유효성 체크 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isNeedCheckSkucdValidation(JobBatch batch) {
		// job.cmm.sku.skucd.validation.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.VALIDATION_SKUCD_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 상품 중량 정보를 g으로 사용할 지 kg으로 사용할 지 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static String getSkuWeightUnit(JobBatch batch) {
		// job.cmm.sku.weight.unit
		return getConfigValue(batch, LogisConfigConstants.SKU_WEIGHT_UNIT, true);
	}

	/**
	 * 서버 사이드에서 상품 유효성 체크를 위한 룰
	 * 
	 * @param batch
	 * @return
	 */
	public static String getSkuCdValidationRule(JobBatch batch) {
		// job.cmm.server.validate.sku_cd.rule
		return getConfigValue(batch, LogisConfigConstants.VALIDATION_RULE_SKUCD, true);
	}
	
	/**
	 * 상품 코드가 유효한 지 체크
	 * 
	 * @param batch
	 * @param skuCd
	 * @return
	 */
	public static boolean isSkuCdValid(JobBatch batch, String skuCd) {
		String skuCdRule = getSkuCdValidationRule(batch);
		return AnyValueUtil.checkValidateByRegExpr(skuCdRule, skuCd);
	}
	
	/**
	 * 서버 사이드에서 박스 ID 유효성 체크를 위한 룰
	 * 
	 * @param batch
	 * @return
	 */
	public static String getBoxIdValidationRule(JobBatch batch) {
		// job.cmm.server.validate.box_id.rule
		return getConfigValue(batch, LogisConfigConstants.VALIDATION_RULE_BOXID, true);
	}
	
	/**
	 * 박스 ID가 유효한 지 체크
	 * 
	 * @param batch
	 * @param boxId
	 * @return
	 */
	public static boolean isBoxIdValid(JobBatch batch, String boxId) {
		String boxIdRule = getBoxIdValidationRule(batch);
		return AnyValueUtil.checkValidateByRegExpr(boxIdRule, boxId);
	}
	
	/**
	 * 서버 사이드에서 셀 코드 유효성 체크를 위한 룰
	 * 
	 * @param batch
	 * @return
	 */
	public static String getCellCdValidationRule(JobBatch batch) {
		// job.cmm.server.validate.cell_cd.rule
		return getConfigValue(batch, LogisConfigConstants.VALIDATION_RULE_CELLCD, true);
	}
	
	/**
	 *  셀 코드가 유효한 지 체크
	 * 
	 * @param batch
	 * @param cellCd
	 * @return
	 */
	public static boolean isCellCdValid(JobBatch batch, String cellCd) {
		String cellCdRule = getCellCdValidationRule(batch);
		return AnyValueUtil.checkValidateByRegExpr(cellCdRule, cellCd);
	}
	
	/**
	 * 서버 사이드에서 표시기 코드 유효성 체크를 위한 룰
	 * 
	 * @param batch
	 * @return
	 */
	public static String getIndCdValidationRule(JobBatch batch) {
		// job.cmm.server.validate.ind_cd.rule
		return getConfigValue(batch, LogisConfigConstants.VALIDATION_RULE_INDCD, true);
	}
	
	/**
	 *  표시기 코드가 유효한 지 체크
	 * 
	 * @param batch
	 * @param indCd
	 * @return
	 */
	public static boolean isIndCdValid(JobBatch batch, String indCd) {
		String indCdRule = getIndCdValidationRule(batch);
		return AnyValueUtil.checkValidateByRegExpr(indCdRule, indCd);
	}
	
	/**
	 * 서버 사이드에서 랙 코드 유효성 체크를 위한 룰
	 * 
	 * @param batch
	 * @return
	 */
	public static String getRackCdValidationRule(JobBatch batch) {
		// 	job.cmm.server.validate.rack_cd.rule
		return getConfigValue(batch, LogisConfigConstants.VALIDATION_RULE_RACKCD, true);
	}
	
	/**
	 *  랙 코드가 유효한 지 체크
	 * 
	 * @param batch
	 * @param rackCd
	 * @return
	 */
	public static boolean isRackCdValid(JobBatch batch, String rackCd) {
		String rackCdRule = getIndCdValidationRule(batch);
		return AnyValueUtil.checkValidateByRegExpr(rackCdRule, rackCd);
	}
	
	/**
	 * 서버 사이드에서 송장번호 유효성 체크를 위한 룰
	 * 
	 * @param batch
	 * @return
	 */
	public static String getInvoiceNoValidationRule(JobBatch batch) {
		// job.cmm.server.validate.invoice_no.rule
		return getConfigValue(batch, LogisConfigConstants.VALIDATION_RULE_INVNO, LogisConstants.FALSE_STRING);
	}
	
	/**
	 *  송장 번호가 유효한 지 체크
	 * 
	 * @param invoiceNo
	 * @param rackCd
	 * @return
	 */
	public static boolean isInvoiceNoValid(JobBatch batch, String invoiceNo) {
		String invoiceNoRule = getInvoiceNoValidationRule(batch);
		return AnyValueUtil.checkValidateByRegExpr(invoiceNoRule, invoiceNo);
	}
	
	/**
	 * 박스 - 셀 매핑을 분류 처리 전에 하는지 즉 선 매핑 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isPreviousBoxCellMapping(JobBatch batch) {
		String boolVal = getConfigValue(batch, LogisConfigConstants.BOX_CELL_MAPPING_POINT, LogisConstants.TRUE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 박스 실적 보고 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isBoxResultReportEnabled(JobBatch batch) {
		// job.cmm.box.result.report.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.BOX_RESULT_REPORT_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 박스 실적 전송 시점 - F: Fullbox 시, I: Inspection 시
	 * 
	 * @param batch
	 * @return
	 */
	public static String getBoxResultSendPoint(JobBatch batch) {
		// job.cmm.box.result.report.point
		return getConfigValue(batch, LogisConfigConstants.BOX_RESULT_REPORT_POINT, true);
	}
	
	/**
	 * 박스 취소 기능 활성화 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isBoxResultCancelEnabled(JobBatch batch) {
		// job.cmm.box.cancel.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.BOX_CANCEL_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 박스 중량 측정 활성화 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isBoxWeightMeasureEnabled(JobBatch batch) {
		// job.cmm.box.weight.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.BOX_WEIGHT_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 주문 필드 중에 박스 처리시에 출고 분류 코드로 사용할 필드 명 - 기본은 class_cd
	 * 
	 * @param batch
	 * @return
	 */
	public static String getBoxOutClassField(JobBatch batch) {
		// job.cmm.box.out.class.field
		return getConfigValue(batch, LogisConfigConstants.BOX_OUT_CLASS_FIELD, true);
	}
		
	/**
	 * 박스 처리 후 액션 - P : 라벨 출력, T : 거래명세서 출력, N : 액션 없음 
	 * 
	 * @param batch
	 * @return
	 */
	public static String getBoxingAction(JobBatch batch) {
		// job.cmm.box.action
		return getConfigValue(batch, LogisConfigConstants.BOX_ACTION, true);
	}
	
	/**
	 * 옵션에 따라 송장 번호를 다르게 부여 - BOX_ID : 박스 ID와 송장 번호 동일, BOX_ID_ONLY : 박스 ID, ... 
	 * 
	 * @param batch
	 * @return
	 */
	public static String getInvoiceNoRule(JobBatch batch) {
		// job.cmm.box.invoice-no.rule
		return getConfigValue(batch, LogisConfigConstants.BOX_INVOICE_NO_RULE, true);
	}
	
	/**
	 * 박스 ID 유일성 보장 범위 - G : 도메인 전체 유일, D : 날자별 유일, B : 배치 내 유일
	 * 
	 * @param batch
	 * @param defaultValue
	 * @return
	 */
	public static String getBoxIdUniqueScope(JobBatch batch, String defaultValue) {
		// job.cmm.box.box_id.unique.scope
		String val = getBoxIdUniqueScope(batch, false);
		return ValueUtil.isEmpty(val) ? defaultValue : val;
	}
	
	/**
	 * 박스 ID 유일성 보장 범위 - G : 도메인 전체 유일, D : 날자별 유일, B : 배치 내 유일
	 * 
	 * @param batch
	 * @param withExeption
	 * @return
	 */
	public static String getBoxIdUniqueScope(JobBatch batch, boolean withExeption) {
		// job.cmm.box.box_id.unique.scope
		return getConfigValue(batch, LogisConfigConstants.BOX_ID_UNIQE_SCOPE, withExeption);
	}
	
	/**
	 * 확정 취소 기능 활성화 여부 
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isUndoPickedEnabled(JobBatch batch) {
		// job.cmm.pick.cancel.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.PICK_CANCEL_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 확정 실적 보고 여부 
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isPickResultEnabled(JobBatch batch) {
		// job.cmm.pick.result.report.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.PICK_RESULT_REPORT_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 표시기에서 분류 작업 취소시에 '취소' 상태로 관리할 지 여부 
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isPickCancelStatusEnabled(JobBatch batch) {
		// job.cmm.pick.cancel.status.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.PICK_CANCEL_STATUS_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}

	/**
	 * 상품 투입시 취소된 상품을 조회할 지 여부 
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isCanceledSkuInputEnabled(JobBatch batch) {
		// job.cmm.pick.include.cancelled.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.PICK_INCLUDE_CANCALLED_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}

	/**
	 * 라벨 발행시 한 번에 동일 라벨을 몇 장 발행할 지 설정 
	 * 
	 * @param batch
	 * @return
	 */
	public static int getLabelPrintCountAtOnce(JobBatch batch) {
		// job.cmm.label.print.count
		String intVal = getConfigValue(batch, LogisConfigConstants.LABEL_PRINT_COUNT, "1");
		return ValueUtil.toInteger(intVal);
	}

	/**
	 * 송장 라벨 발행 방법 - S: 송장라벨 자체발행, I: 인터페이스로 발행, N: 발행안 함
	 * 
	 * @param batch
	 * @return
	 */
	public static String getInvoiceIssueMethod(JobBatch batch) {
		// job.cmm.label.print.method
		return getConfigValue(batch, LogisConfigConstants.LABEL_PRINT_METHOD, true);
	}

	/**
	 * 송장 라벨을 자체 출력시 송장 라벨 출력 템플릿 설정 MPS 매니저 > 개발자 > 커스텀 템플릿에 등록된 송장 라벨 명칭을 설정하면 됨
	 * 
	 * @param batch
	 * @return
	 */
	public static String getInvoiceLabelTemplate(JobBatch batch) {
		// job.cmm.label.template
		return getConfigValue(batch, LogisConfigConstants.LABEL_TEMPLATE, true);
	}
	
	/**
	 * 사용 디바이스 리스트
	 * 
	 * @param batch
	 * @return
	 */
	public static String[] getDeviceList(JobBatch batch) {
		// job.cmm.device.list
		String strVal = getConfigValue(batch, LogisConfigConstants.DEVICE_DEVICE_LIST, true);
		return strVal.split(LogisConstants.COMMA);
	}
	
	/**
	 * 디바이스의 작업 위치 (앞,뒤,앞/뒤,전체 등) 정보를 사용할 지 여부
	 * TODO 장비 설정에 있어야 할 듯 -> 여기서 뺄 지 결정
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isUseCellSideAtDevice(JobBatch batch) {
		// job.cmm.device.side.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.DEVICE_SIDE_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}

	/**
	 * 디바이스의 작업 영역 정보를 사용할 지 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isUseStationAtDevice(JobBatch batch) {
		// job.cmm.device.station.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.DEVICE_STATION_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 출고 검수 활성화 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isInspectionEnabled(JobBatch batch) {
		// job.cmm.inspection.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.INSPECTION_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 중량 검수 활성화 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isWeightInspectionEnabled(JobBatch batch) {
		// job.cmm.insepction.weight.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.INSPECTION_WEIGHT_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}

	/**
	 * 출고 검수 후 액션. 아래 출고 검수 활성화 시에만 의미가 있음.
	 * 
	 * @param batch
	 * @return
	 */
	public static String getInspectionAction(JobBatch batch) {
		// job.cmm.inspection.action
		return getConfigValue(batch, LogisConfigConstants.INSPECTION_ACTION, true);
	}

	/**
	 * 투입시 투입 범위
	 * 	- station : 작업 존 별 투입
	 * 	- rack : 호기별 투입
	 * 	- batch : 배치별 투입
	 * 	- batch_group : 배치 그룹별 투입
	 * 
	 * @param batch
	 * @return
	 */
	public static String getInputWorkScope(JobBatch batch) {
		// job.cmm.input.work_scope
		return getConfigValue(batch, LogisConfigConstants.INPUT_WORK_SCOPE, true);
	}
	
	/**
	 * 투입시 표시기 점등 모드 (all : 전체 상품 점등, qty : 투입 수량 만큼 점등) : job.cmm.input.ind_on.mode
	 * 
	 * @param batch
	 * @return
	 */
	public static String getInputIndOnMode(JobBatch batch) {
		// job.cmm.input.ind_on.mode
		return getConfigValue(batch, LogisConfigConstants.INPUT_IND_ON_MODE, LogisConstants.ALL_STRING);
	}
	
	/**
	 * 투입시 표시기 전체 점등 모드 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isInputIndOnAllMode(JobBatch batch) {
		// job.cmm.input.ind_on.mode
		String modeVal = getInputIndOnMode(batch);
		return ValueUtil.isEqualIgnoreCase(modeVal, LogisConstants.ALL_STRING);
	}
	
	/**
	 * 단품 투입 활성화 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isSingleSkuInputEnabled(JobBatch batch) {
		// job.cmm.input.mode.single.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.INPUT_MODE_SINGLE_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 번들 투입 활성화 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isBundleInputEnabled(JobBatch batch) {
		// job.cmm.input.mode.bundle.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.INPUT_MODE_BUNDLE_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 완박스 투입 활성화 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isSingleBoxInputEnabled(JobBatch batch) {
		// job.cmm.input.mode.box.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.INPUT_MODE_BOX_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * job.cmm.input.mode.single.enabled이 true인 경우에 단품 투입시 하나씩 표시기에 점등할 것인지 전체 표시기에 점등할 것인지 설정
	 *  - single : 상품 하나 스캔시 하나 표시기 점등
	 *  - all : 상품 하나 스캔시 모든 셀의 표시기 점등
	 * 
	 * @param batch
	 * @return
	 */
	public static String getIndOnModeWhenSkuInput(JobBatch batch) {
		// job.cmm.input.single.ind_on.mode
		return getConfigValue(batch, LogisConfigConstants.INPUT_SINGLE_IND_ON_MODE, true);
	}
	
	/**
	 * job.cmm.input.mode.box.enabled이 true인 경우에  완박스 투입시 하나씩 표시기에 점등할 것인지 전체 표시기에 점등할 것인지 설정
	 *  - single : 상품 하나 스캔시 하나 표시기 점등
	 *  - all : 상품 하나 스캔시 모든 셀의 표시기 점등
	 * 
	 * @param batch
	 * @return
	 */
	public static String getIndOnModeWhenSingleBoxInput(JobBatch batch) {
		// job.cmm.input.box.ind_on.mode
		return getConfigValue(batch, LogisConfigConstants.INPUT_BOX_IND_ON_MODE, true);
	}
	
	/**
	 * 주문 취소시 데이터 삭제 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isDeleteWhenOrderCancel(JobBatch batch) {
		// job.cmm.order.delete.when.order_cancel
		String boolVal = getConfigValue(batch, LogisConfigConstants.ORDER_DELETE_WHEN_ORDER_CANCEL, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 작업지시 시점에 게이트웨이 리부팅 활성화 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isGwRebootWhenInstruction(JobBatch batch) {
		// job.cmm.reboot.enabled.when.batch.start
		String boolVal = getConfigValue(batch, LogisConfigConstants.GW_REBOOT_WHEN_BATCH_START_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 작업지시 시점에 표시기에 할당 셀 표시 활성화 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isIndOnAssignedCellWhenInstruction(JobBatch batch) {
		// job.cmm.assigned-cell.indicator.enabled.when.batch.start
		String boolVal = getConfigValue(batch, LogisConfigConstants.ASSIGNED_CELL_INDICATION_WHEN_BATCH_START_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 작업지시 시점에 표시기에 해당 셀 코드 표시 활성화 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isIndOnCellCodeWhenInstruction(JobBatch batch) {
		// job.cmm.cell_cd.indicator.enabled.when.batch.start
		String boolVal = getConfigValue(batch, LogisConfigConstants.CELL_CD_INDICATION_WHEN_BATCH_START_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 작업지시 시점에 표시기에 할당 셀 표시시 대기 시간 (ms)
	 * 
	 * @param batch
	 * @return
	 */
	public static int getWaitDuarionIndOnAssignedCellWhenInstruction(JobBatch batch) {
		// job.cmm.assigned-cell.wait.duration.when.batch.start
		String duration = getConfigValue(batch, LogisConfigConstants.WAIT_DURATION_ASSIGNED_CELL_INDICATION_WHEN_BATCH_START, AnyConstants.ZERO_STRING);
		return ValueUtil.toInteger(duration);
	}

	/**
	 * 거래명세서 템플릿 이름을 설정
	 * 
	 * @param batch
	 * @return
	 */
	public static String getTradeStatmentTemplate(JobBatch batch) {
		// job.cmm.trade-statement.template
		return getConfigValue(batch, LogisConfigConstants.BOX_TRADE_STATEMENT_TEMPLATE, true);
	}
	
	/**
	 * 작업 배치 마감시에 Fullbox 안 된 것이 있으면 일괄 풀 박스 처리 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isBatchFullboxWhenClosingEnabled(JobBatch batch) {
		// job.cmm.batch-fullbox.when.closing.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.BATCH_FULLBOX_WHEN_CLOSING_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}

	/**
	 * 게이트웨이 리부팅 시에 표시기 상태 자동 복원할 지 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isIndicatorsRestoreWhenGwReboot(JobBatch batch) {
		// job.cmm.indicators.restore.when.gw.reboot
		String boolVal = getConfigValue(batch, LogisConfigConstants.INDICATOR_RESTORE_WHEN_GW_REBOOT, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}

	/**
	 * 피킹 작업 - 풀 박스 기능을 사용할 지 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isPickingFullboxEnabled(JobBatch batch) {
		// job.cmm.pick.fullbox.enabled
		String boolVal = getConfigValue(batch, LogisConfigConstants.PICK_FULLBOX_ENABLED, LogisConstants.FALSE_STRING);
		return ValueUtil.toBoolean(boolVal);
	}
}
