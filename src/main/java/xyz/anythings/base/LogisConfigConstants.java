package xyz.anythings.base;

/**
 * 물류 마스터 모듈 설정 관련 키 상수 정의
 *
 * @author shortstop
 */
public class LogisConfigConstants extends xyz.anythings.sys.ConfigConstants {
	
	/**********************************************************************
	 * 								1. 인터페이스 관련 설정
	 **********************************************************************/
	/**
	 * 상위 시스템 인터페이스 유형
	 */
	public static final String IF_IF_TYPE = "parent.system.if.iftype";
	/**
	 * 상위 시스템 DB Link 명
	 */
	public static final String IF_DBLINK = "parent.system.if.dblink.name";
	/**
	 * 상위 시스템 데이터소스 명
	 */
	public static final String IF_DS_NAME = "parent.system.if.datasource.name";
	/**
	 * 고객사 I/F 테이블 명
	 */
	public static final String IF_COMPANY_TABLE = "parent.system.if.company.table";
	/**
	 * 상품 I/F 테이블 명
	 */
	public static final String IF_SKU_TABLE = "parent.system.if.sku.table";
	/**
	 * 매장 I/F 테이블 명
	 */
	public static final String IF_SHOP_TABLE = "parent.system.if.shop.table";
	/**
	 * 박스 유형 I/F 테이블 명
	 */
	public static final String IF_BOXTYPE_TABLE = "parent.system.if.boxtype.table";
	/**
	 * 주문 I/F 테이블 명
	 */
	public static final String IF_ORDER_TABLE = "parent.system.if.order.table";
	/**
	 * 피킹 실적  I/F 테이블 명
	 */
	public static final String IF_PICK_RESULT_TABLE = "parent.system.if.pick-result.table";
	/**
	 * 박스 실적 I/F 테이블 명
	 */
	public static final String IF_BOX_RESULT_TABLE = "parent.system.if.box-result.table";
	/**
	 * 토털 피킹 I/F 테이블 명
	 */
	public static final String IF_TOTAL_PICKING_TABLE = "parent.system.if.totalpicking.table";
	/**
	 * 고객사 I/F 프로시져 명
	 */
	public static final String IF_COMPANY_PROCEDURE = "parent.system.if.company.procedure";
	/**
	 * 상품 I/F 프로시져 명
	 */
	public static final String IF_SKU_PROCEDURE = "parent.system.if.sku.procedure";
	/**
	 * 매장 I/F 프로시져 명
	 */
	public static final String IF_SHOP_PROCEDURE = "parent.system.if.shop.procedure";
	/**
	 * 박스 유형 I/F 프로시져 명
	 */
	public static final String IF_BOXTYPE_PROCEDURE = "parent.system.if.boxtype.procedure";
	/**
	 * 주문 I/F 프로시져 명
	 */
	public static final String IF_ORDER_PROCEDURE = "parent.system.if.order.procedure";
	/**
	 * 피킹 실적  I/F 프로시져 명
	 */
	public static final String IF_PICK_RESULT_PROCEDURE = "parent.system.if.pick-result.procedure";
	/**
	 * 박스 실적 I/F 프로시져 명
	 */
	public static final String IF_BOX_RESULT_PROCEDURE = "parent.system.if.box-result.procedure";
	/**
	 * 토털 피킹 I/F 프로시져 명
	 */
	public static final String IF_TOTAL_PICKING_PROCEDURE = "parent.system.if.totalpicking.procedure";
	/**
	 * 주문 수신유형 (datasource/procedure)
	 */
	public static final String IF_RECEIVE_ORDER_TYPE = "parent.system.if.receive.order.type";
	
	/**********************************************************************
	 * 								2. SKU 조회 관련 설정
	 **********************************************************************/
	/**
	 * 바코드 최대 길이 - 상품 스캔시 최대 입력 길이
	 */
	public static final String SKU_BARCODE_MAX_LENGTH = "job.cmm.sku.barcode.max.length";
	/**
	 * SKU 조회를 위한 코드 필드명 리스트
	 */
	public static final String SKU_CONDITION_FIELDS_TO_SEARCH = "job.cmm.sku.search.condition.fields";
	/**
	 * SKU 조회를 위한 조회 필드명 리스트
	 */
	public static final String SKU_SELECT_FIELDS_TO_SEARCH = "job.cmm.sku.search.select.fields";
	/**
	 * SKU 중량 단위 - g/kg
	 */
	public static final String SKU_WEIGHT_UNIT = "job.cmm.sku.weight.unit";
	
	/**********************************************************************
	 * 								3. 각종 스캔 코드 유효성 체크
	 **********************************************************************/
	/**
	 * 서버 사이드에서 상품 유효성 체크 여부
	 */
	public static final String VALIDATION_SKUCD_ENABLED = "job.cmm.sku.skucd.validation.enabled";
	/**
	 * 서버 사이드에서 상품 유효성 체크를 위한 룰
	 */
	public static final String VALIDATION_RULE_SKUCD = "job.cmm.server.validate.sku_cd.rule";
	/**
	 * 서버 사이드에서 박스 ID 유효성 체크를 위한 룰
	 */
	public static final String VALIDATION_RULE_BOXID = "job.cmm.server.validate.box_id.rule";
	/**
	 * 서버 사이드에서 로케이션 코드 유효성 체크를 위한 룰
	 */
	public static final String VALIDATION_RULE_CELLCD = "job.cmm.server.validate.cell_cd.rule";
	/**
	 * 서버 사이드에서 표시기 코드 유효성 체크를 위한 룰
	 */
	public static final String VALIDATION_RULE_INDCD = "job.cmm.server.validate.ind_cd.rule";
	/**
	 * 서버 사이드에서 랙 코드 유효성 체크를 위한 룰
	 */
	public static final String VALIDATION_RULE_RACKCD = "job.cmm.server.validate.rack_cd.rule";
	/**
	 * 서버 사이드에서 송장번호 유효성 체크를 위한 룰
	 */
	public static final String VALIDATION_RULE_INVNO = "job.cmm.server.validate.invoice_no.rule";
	
	/**********************************************************************
	 * 								4. 주문 설정
	 **********************************************************************/
	/**
	 * 주문 테이블의 분류 코드(class_cd)와 매핑할 주문 필드명
	 */
	public static final String ORDER_CLASS_CD_FIELD = "job.cmm.order.class_cd.field";
	/**
	 * 주문 취소시 데이터 삭제 여부
	 */
	public static final String ORDER_DELETE_WHEN_ORDER_CANCEL = "job.cmm.order.delete.when.order_cancel";
	
	/**********************************************************************
	 * 								5. 작업 지시 설정
	 **********************************************************************/
	/**
	 * 작업지시 시점에 게이트웨이 리부팅 할 지 여부
	 */
	public static final String GW_REBOOT_WHEN_BATCH_START_ENABLED = "job.cmm.reboot.enabled.when.batch.start";
	/**
	 * 작업지시 시점에 표시기에 할당 셀 표시 활성화 여부
	 */
	public static final String ASSIGNED_CELL_INDICATION_WHEN_BATCH_START_ENABLED = "job.cmm.assigned-cell.indicator.enabled.when.batch.start";
	/**
	 * 작업지시 시점에 배치에 걸린 모든 셀의 표시기에 셀 코드 표시 활성화 여부
	 */
	public static final String CELL_CD_INDICATION_WHEN_BATCH_START_ENABLED = "job.cmm.cell_cd.indicator.enabled.when.batch.start";
	/**
	 * 작업지시 시점에 표시기에 할당 셀 표시시 대기 시간 (ms)
	 */
	public static final String WAIT_DURATION_ASSIGNED_CELL_INDICATION_WHEN_BATCH_START = "job.cmm.assigned-cell.wait.duration.when.batch.start";
	/**
	 * 배치 마감시에 풀 박스 처리 안 된 모든 셀에 대해서 풀 박스 처리할 것인지 여부
	 */
	public static final String BATCH_FULLBOX_WHEN_CLOSING_ENABLED = "job.cmm.batch-fullbox.when.closing.enabled";
	
	/**
	 * 게이트웨이 리부팅 시에 표시기 상태 자동 복원할 지 여부
	 */
	public static final String INDICATOR_RESTORE_WHEN_GW_REBOOT = "job.cmm.indicators.restore.when.gw.reboot";
	
	/**********************************************************************
	 * 								6. 투입 모드 설정
	 **********************************************************************/
	/**
	 * 투입시 투입 범위
	 */
	public static final String INPUT_WORK_SCOPE = "job.cmm.input.work_scope";
	/**
	 * 투입시 표시기 점등 모드 (all : 전체 점등, qty : 수량 기반 점등)
	 */
	public static final String INPUT_IND_ON_MODE = "job.cmm.input.ind_on.mode";
	/**
	 * 완박스 투입 활성화 여부
	 */
	public static final String INPUT_MODE_BOX_ENABLED = "job.cmm.input.mode.box.enabled";
	/**
	 * 완박스 투입시 표시기 점등 모드 - 하나씩 점등 / 전체 점등
	 */
	public static final String INPUT_BOX_IND_ON_MODE = "job.cmm.input.box.ind_on.mode";
	/**
	 * 번들 투입 활성화 여부
	 */
	public static final String INPUT_MODE_BUNDLE_ENABLED = "job.cmm.input.mode.bundle.enabled";
	/**
	 * 단품 투입 활성화 여부
	 */
	public static final String INPUT_MODE_SINGLE_ENABLED = "job.cmm.input.mode.single.enabled";
	/**
	 * 단품 투입시 표시기 점등 모드 - 하나씩 점등 / 전체 점등
	 */
	public static final String INPUT_SINGLE_IND_ON_MODE = "job.cmm.input.single.ind_on.mode";
	
	/**********************************************************************
	 * 								7. 피킹 설정
	 **********************************************************************/
	/**
	 * 확정 취소 기능 활성화 여부
	 */
	public static final String PICK_CANCEL_ENABLED = "job.cmm.pick.cancel.enabled";
	/**
	 * 표시기에서 분류 작업 취소시에 '취소' 상태로 관리할 지 여부
	 */
	public static final String PICK_CANCEL_STATUS_ENABLED = "job.cmm.pick.cancel.status.enabled";
	/**
	 * 상품 투입시 취소된 상품을 조회할 지 여부
	 */
	public static final String PICK_INCLUDE_CANCALLED_ENABLED = "job.cmm.pick.include.cancelled.enabled";
	/**
	 * 확정 실적 보고 여부
	 */
	public static final String PICK_RESULT_REPORT_ENABLED = "job.cmm.pick.result.report.enabled";
	/**
	 * 풀 박스 기능 지원 여부
	 */
	public static final String PICK_FULLBOX_ENABLED = "job.cmm.pick.fullbox.enabled";
	
	/**********************************************************************
	 * 								8. 검수 / 라벨 설정
	 **********************************************************************/
	/**
	 * 출고 검수 활성화 여부
	 */
	public static final String INSPECTION_ENABLED = "job.cmm.inspection.enabled";
	/**
	 * 중량 체크 여부
	 */
	public static final String INSPECTION_WEIGHT_ENABLED = "job.cmm.insepction.weight.enabled";
	/**
	 * 출고 검수 후 액션
	 */
	public static final String INSPECTION_ACTION = "job.cmm.inspection.action";
	/**
	 * 라벨 발행시 한 번에 동일 라벨을 몇 장 발행할 지 설정
	 */
	public static final String LABEL_PRINT_COUNT = "job.cmm.label.print.count";
	/**
	 * 송장 라벨 발행 방법 (S: 라벨 자체 발행, I: 인터페이스)
	 */
	public static final String LABEL_PRINT_METHOD = "job.cmm.label.print.method";
	/**
	 * 송장 라벨을 자체 출력시 출력 템플릿 명
	 */
	public static final String LABEL_TEMPLATE = "job.cmm.label.template";

	/**********************************************************************
	 * 								9. 박스 설정
	 **********************************************************************/
	/**
	 * 박스 처리 후 액션
	 */
	public static final String BOX_ACTION = "job.cmm.box.action";
	/**
	 * 박스 ID 유일성 보장 범위
	 */
	public static final String BOX_ID_UNIQE_SCOPE = "job.cmm.box.box_id.unique.scope";
	/**
	 * 박스 취소 기능 활성화 여부
	 */
	public static final String BOX_CANCEL_ENABLED = "job.cmm.box.cancel.enabled";
	/**
	 * 옵션에 따라 송장 번호를 다르게 부여
	 */
	public static final String BOX_INVOICE_NO_RULE = "job.cmm.box.invoice-no.rule";
	/**
	 * 주문 필드 중에 박스 처리시에 출고 분류 코드로 사용할 필드 명
	 */
	public static final String BOX_OUT_CLASS_FIELD = "job.cmm.box.out.class.field";
	/**
	 * 박스 - 셀 매핑 선 매핑 여부
	 */
	public static final String BOX_CELL_MAPPING_POINT = "job.cmm.box.cell.mapping.point";
	/**
	 * 박스 실적 보고 여부
	 */
	public static final String BOX_RESULT_REPORT_ENABLED = "job.cmm.box.result.report.enabled";
	/**
	 * 박스 실적 전송 시점
	 */
	public static final String BOX_RESULT_REPORT_POINT = "job.cmm.box.result.report.point";
	/**
	 * 박스 중량 관리 여부
	 */
	public static final String BOX_WEIGHT_ENABLED = "job.cmm.box.weight.enabled";
	/**
	 * 거래명세서 템플릿
	 */
	public static final String BOX_TRADE_STATEMENT_TEMPLATE = "job.cmm.trade-statement.template";
	
	/**********************************************************************
	 * 								10. 디바이스 설정
	 **********************************************************************/
	/**
	 * 작업 장비에서 장비 리스트
	 */
	public static final String DEVICE_DEVICE_LIST = "job.cmm.device.list";
	/**
	 * 작업 장비에서 작업 위치 (앞,뒤,앞/뒤,전체 등) 정보를 사용할 지 여부
	 */
	public static final String DEVICE_SIDE_ENABLED = "job.cmm.device.side.enabled";
	/**
	 * 작업 장비에서 작업 스테이션 정보를 사용할 지 여부
	 */
	public static final String DEVICE_STATION_ENABLED = "job.cmm.device.station.enabled";
	
	/**********************************************************************
	 * 								11. DAS 설정 
	 **********************************************************************/
	/**
	 * DAS 셀에 할당할 대상 필드 (매장, 상품, 주문 번호…)
	 */
	public static final String DAS_PREPROCESS_CELL_MAPPING_FIELD = "job.das.preprocess.cell.mapping.field";
	/**
	 * DAS 셀에 할당할 대상 필드 (매장, 상품, 주문 번호…)
	 */
	public static final String DAS_CELL_MAPPING_TARGET_FIELD = "job.das.cell.mapping.target.field";
	/**
	 * DAS 설정 - 셀과 박스 ID를 매핑하는 시점 - (P: 주문 가공시, B: 분류 처리 전 박스 매핑, A: 분류 처리 후 풀 박스 전 박스 매핑, N: 수동 박스 매핑이 필요 없음 - 자동 매핑 시)
	 */
	public static final String DAS_CELL_BOXID_MAPPING_POINT = "job.das.cell-boxid.mapping.point";
	/**
	 * DAS 설정 - 셀과 분류 코드(매장, 상품, 주문 번호…)를 매핑하는 시점 - (P: 주문 가공시, S: 작업 지시 이후 작업자 수동 매핑)
	 */
	public static final String DAS_CELL_CLASSCD_MAPPING_POINT = "job.das.cell-classcd.mapping.point";
	/**
	 * DAS 설정 - KIOSK 중분류 화면에서 표시 수량을 주문 수량으로 표시 할 것인지 분류 처리한 수량을 제외하고 표시 할 것인지 (fix/filter)
	 */
	public static final String DAS_MIDDLEASSORT_DISPLAY_QTY_MODE = "job.das.middleassort.display.qty.mode";
	/**
	 * DAS 설정 - DAS KIOSK 중분류 화면에서 호기 정렬 옵션
	 */
	public static final String DAS_MIDDLEASSORT_RACK_SORT_ASEND = "job.das.middleassort.rack.sort.ascending";
	/**
	 * DAS 설정 - 키오스크에서 상품 투입시에 상품 중량 체크 여부
	 */
	public static final String DAS_INPUT_CHECK_WEIGHT_ENABLED = "job.das.input.check.weight.enabled"; 
	/**
	 * DAS 설정 - 표시기에 표시할 릴레이 번호 최대 번호 (최대 번호 이후 다시 1로)
	 */
	public static final String DAS_RELAY_JOB_MAX_NO = "job.das.pick.relay.max.no";
	/**
	 * DAS 설정 - 주문 가공시 셀 매핑을 하기 위해 주문 가공 조회시 소팅을 하기 위한 필드명
	 */
	public static final String DAS_PREPROCESS_SORT_FIELD_FOR_CELLMAPPING = "job.das.preprocess.sort.field.for.cellmapping";
	/**
	 * DAS 설정 - 셀 매핑 시 주문 가공 정보 소팅 필드를 오름차순 정렬을 할 것인지 여부
	 */
	public static final String DAS_PREPROCESS_SORT_ASC_FOR_CELLMAPPING = "job.das.preprocess.sort.ascend.for.cellmapping";
	/**
	 * DAS 설정 - 표시기 최종 End 사용 여부  
	 */
	public static final String DAS_PICK_FINAL_END_ENABLED = "job.das.pick.fianl.end.enabled";

	/**********************************************************************
	 * 								12. 반품 설정
	 **********************************************************************/
	/**
	 * 반품 설정 - 셀 - 박스 매핑 시점 (P: 주문 가공시, A: 분류 시)
	 */
	public static final String RTN_CELL_BOXID_MAPPING_POINT = "job.rtn.cell-boxid.mapping.point";
	/**
	 * 반품 설정 - 셀에 할당할 대상 필드 (매장, 상품, 주문번호…)
	 */
	public static final String RTN_PREPROCESS_CELL_MAPPING_FIELD = "job.rtn.preprocess.cell.mapping.field";
	
	/**********************************************************************
	 * 								13. DPS 설정
	 **********************************************************************/
	/**
	 * DPS 설정 - DPS 박스별 할당할 대상 필드 (매장, 상품, 주문번호…)
	 */
	public static final String DPS_BOX_MAPPING_FIELD = "dps.box.mapping.field";
	/**
	 * DPS 설정 - 방면 분류를 위한 주문 매핑 필드 (주문의 box_class_cd에 복사할 필드명)
	 */
	public static final String DPS_BOX_OUT_CLASS_FIELD = "dps.box.out.class.field";
	/**
	 * DPS 설정 - 투입 박스 유형 (box / tray)
	 */
	public static final String DPS_INPUT_BOX_TYPE = "dps.input.box.type";
	/**
	 * DPS 설정 - 호기별로 배치 분리 처리 여부
	 */
	public static final String DPS_BATCH_SPLIT_BY_RACK_ENABLED = "dps.batch.split-by-rack.enabled";
	/**
	 * DPS 설정 - 박스 투입시 박스 타입 split 기준 ( 1,2 )
	 */
	public static final String DPS_INPUT_BOX_TYPE_SPLIT_INDEX = "dps.input.box.type.split.index";
	/**
	 * DPS 설정 - 피킹과 동시에 검수 처리할 것인지 여부
	 */
	public static final String DPS_PICK_WITH_INSPECTION_ENABLED = "dps.pick.with-inspection.enabled";
	/**
	 * DPS 설정 - 완박스 바로 출고 대상 분류 여부
	 */
	public static final String DPS_PICK_1BOX_ENABLED = "dps.pick.1box.enabled";
	/**
	 * DPS 설정 - 단수 대상 분류 여부
	 */
	public static final String DPS_PICK_1SKU_1PCS_ENABLED = "dps.pick.1sku.1pcs.enabled";
	/**
	 * DPS 설정 - 단포 대상 분류 여부
	 */
	public static final String DPS_PICK_1SKU_NPCS_ENABLED = "dps.pick.1sku.npcs.enabled";
	/**
	 * DPS 설정 - SKU 물량 Rank 선정 기준 데이터 범위 (일자)
	 */
	public static final String DPS_SKU_POPULAR_RANK_CALC_DAYS = "dps.sku.popula.rank.calc.days";
	/**
	 * DPS 설정 - 작업 스테이션에 대기할 박스 최대 개수 (-1이면 무한대)
	 */
	public static final String DPS_STATION_WAIT_POOL_COUNT = "dps.station.wait-pool.count";
	/**
	 * DPS 설정 - 추천 로케이션 사용 여부
	 */
	public static final String DPS_SUPPLE_RECOMMEND_CELL_ENABLED = "dps.supple.recommend-cell.enabled";
	/**
	 * DPS 설정 - 작업 할당 스케줄러 사용 여부
	 */
	public static final String DPS_JOB_ASSIGN_SCHEDULING_ENABLE = "dps.job-assign.scheduling.enabled";
	/**
	 * DPS 설정 - 박스 요청 프로세스 사용 여부
	 */
	public static final String DPS_PREPROCESS_BOX_REQUEST_ENABLED = "dps.preprocess.box-request.enabled";
	/**
	 * DPS 설정 - 시리얼 번호가 유니크해야 하는지 여부
	 */
	public static final String DPS_SERIAL_MUST_BE_UNIQUE = "dps.serial.must.be.unique";
}
