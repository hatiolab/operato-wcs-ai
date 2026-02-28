package xyz.anythings.base.service.util;

import xyz.anythings.base.LogisConfigConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.service.impl.JobConfigProfileService;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * 스테이지 범위 내 작업 설정 값 조회 유틸리티
 * 설정 항목 리스트
 * 
 * - parent.system.if.iftype					상위 시스템 인터페이스 유형 (datasource / dblink / if-table)
 * - parent.system.if.dblink.name				상위 시스템 I/F를 위한 DB Link 명
 * - parent.system.if.datasource.name			상위 시스템 데이터소스 명
 * - parent.system.if.company.table				상위 시스템 고객사 I/F 테이블 명
 * - parent.system.if.sku.table					상위 시스템 SKU I/F 테이블 명
 * - parent.system.if.shop.table				상위 시스템 매장 I/F 테이블 명
 * - parent.system.if.boxtype.table				상위 시스템 박스 유형 I/F 테이블 명 
 * - parent.system.if.order.table				상위 시스템 주문 I/F 테이블 명
 * - parent.system.if.pick-result.table			상위 시스템 피킹 실적 I/F 테이블 명
 * - parent.system.if.box-result.table			상위 시스템 박스 실적 I/F 테이블 명
 * - parent.system.if.totalpicking.table		상위 시스템 토털 피킹 I/F 테이블 명
 * - parent.system.if.company.procedure			상위 시스템 고객사 I/F 프로시져 명
 * - parent.system.if.sku.procedure				상위 시스템 SKU I/F 프로시져 명
 * - parent.system.if.shop.procedure			상위 시스템 매장 I/F 프로시져 명
 * - parent.system.if.boxtype.procedure			상위 시스템 박스 유형 I/F 프로시져 명
 * - parent.system.if.order.procedure			상위 시스템 주문 I/F 프로시져 명
 * - parent.system.if.pick-result.procedure		상위 시스템 피킹 실적 I/F 프로시져 명
 * - parent.system.if.box-result.procedure		상위 시스템 박스 실적 I/F 프로시져 명
 * - parent.system.if.totalpicking.procedure	상위 시스템 토털 피킹 I/F 프로시져 명
 * 
 * - cmm.equip.status.report.ignore.flag		설비 이벤트 무시할 지 여부
 * - cmm.receive.logging.enabled				미들웨어 접수 메시지를 로깅할 지 여부
 * - cmm.ind.stock.adjustment.color				재고 실사시 버튼 색상
 * - cmm.ind.button.default.color				표시기 버튼 기본 색상
 * 
 * - cmm.sku.barcode.max.length					스테이지 공통 - 바코드 최대 길이
 * - cmm.sku.search.condition.fields			스테이지 공통 - 상품 조회를 위한 코드 필드명 리스트
 * - cmm.box.out.class.field					스테이지 공통 - 주문 필드 중에 박스 처리시에 출고 분류 코드로 사용할 필드 명
 * - cmm.cell.mapping.target.field				스테이지 공통 - 기본 셀 매핑 대상 필드 명
 * - cmm.indicator.type  						스테이지 공통 - 표시기 유형 (통신 프로토콜 기준)
 * - cmm.order.delete.when.order_cancel			스테이지 공통 - 주문 취소시 데이터 삭제 여부
 * - cmm.order.ordergroup.field					스테이지 공통 - 주문 그룹 필드로 사용할 주문 테이블의 필드명
 * 
 * - das.sku.barcode.max.length					DAS 바코드 최대 길이
 * - das.sku.search.condition.fields			DAS 상품 조회를 위한 코드 필드명 리스트
 * - das.box.out.class.field					DAS 주문 필드 중에 박스 처리시에 출고 분류 코드로 사용할 필드 명
 * - das.cell.mapping.target.field				DAS 기본 셀 매핑 대상 필드 명
 * - das.indicator.type  						DAS 표시기 유형 (통신 프로토콜 기준)
 * - das.order.delete.when.order_cancel			DAS 주문 취소시 데이터 삭제 여부
 * - das.order.ordergroup.field					DAS 주문 그룹 필드로 사용할 주문 테이블의 필드명
 * 
 * - rtn.sku.barcode.max.length					반품 바코드 최대 길이
 * - rtn.sku.search.condition.fields			반품 상품 조회를 위한 코드 필드명 리스트
 * - rtn.box.out.class.field					반품 주문 필드 중에 박스 처리시에 출고 분류 코드로 사용할 필드 명
 * - rtn.cell.mapping.target.field				반품 기본 셀 매핑 대상 필드 명
 * - rtn.indicator.type  						반품 표시기 유형 (통신 프로토콜 기준)
 * - rtn.order.delete.when.order_cancel			반품 주문 취소시 데이터 삭제 여부
 * - rtn.order.ordergroup.field					반품 주문 그룹 필드로 사용할 주문 테이블의 필드명
 * 
 * - dps.sku.barcode.max.length					DPS 바코드 최대 길이
 * - dps.sku.search.condition.fields			DPS 상품 조회를 위한 코드 필드명 리스트
 * - dps.box.out.class.field					DPS 주문 필드 중에 박스 처리시에 출고 분류 코드로 사용할 필드 명
 * - dps.cell.mapping.target.field				DPS 기본 셀 매핑 대상 필드 명
 * - dps.indicator.type  						DPS 표시기 유형 (통신 프로토콜 기준)
 * - dps.order.delete.when.order_cancel			DPS 주문 취소시 데이터 삭제 여부
 * - dps.order.ordergroup.field					DPS 주문 그룹 필드로 사용할 주문 테이블의 필드명
 *
 * - dps.batch.split-by-rack.enabled			DPS 배치 분리 여부
 * - dps.sku.popula.rank.calc.days				DPS SKU 물량 Rank 선정 기준 데이터 범위 (일자)
 * - dps.station.wait-pool.count				DPS 작업 스테이션에 대기할 박스 최대 개수 (-1이면 무한대)
 * - dps.supple.recommend-cell.enabled			DPS 추천 로케이션 사용 여부
 * - dps.job-assign.scheduling.enabled			DPS 작업 스케줄러 사용 여부
 * 
 * @author shortstop
 */
public class StageJobConfigUtil {
	
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
	 * 스테이지 범위 내에 설정 내용을 키로 조회해서 리턴
	 *  
	 * @param stageCd
	 * @param jobType
	 * @param key
	 * @param exceptionWhenEmptyValue
	 * @return
	 */
	public static String getConfigValue(String stageCd, String jobType, String key, boolean exceptionWhenEmptyValue) {
		JobConfigProfileService configSvc = getConfigSetService();
		String value = null;
		
		// 1. 작업 유형에 따른 설정값 조회
		if(ValueUtil.isNotEmpty(jobType)) {
			String jobTypeKey = key.replace("cmm.", jobType.toLowerCase() + LogisConstants.DOT);
			value = configSvc.getStageConfigValue(Domain.currentDomainId(), stageCd, jobTypeKey);
		}
		
		// 2. 1값이 없다면 공통 설정값 조회
		if(ValueUtil.isEmpty(value)) {
			value = configSvc.getStageConfigValue(Domain.currentDomainId(), stageCd, key);
		}
		
		// 3. 설정값이 없다면 exceptionWhenEmptyValue에 따라 예외 처리
		if(ValueUtil.isEmpty(value) && exceptionWhenEmptyValue) {
			JobConfigSet confSet = configSvc.getStageConfigSet(Domain.currentDomainId(), stageCd);
			if(confSet != null) {
				throw ThrowUtil.newJobConfigNotSet(confSet.getConfSetCd(), key);
			} else {
				throw ThrowUtil.newJobConfigNotSet(key);
			}
		}
		
		return value;
	}
	
	/**
	 * 상위 시스템 인터페이스 유형 (datasource / dblink / if-table)
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfType(String stageCd) {
		// parent.system.if.iftype
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_IF_TYPE, true);
	}
	
	/**
	 * 상위 시스템 I/F를 위한 DB Link 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfDbLinkName(String stageCd) {
		// parent.system.if.dblink.name
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_DBLINK, true);
	}
	
	/**
	 * 상위 시스템 데이터소스 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfDatasource(String stageCd) {
		// parent.system.if.datasource.name
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_DS_NAME, true);
	}
	
	/**
	 * 상위 시스템 고객사 I/F 테이블 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfCompanyTable(String stageCd) {
		// parent.system.if.company.table
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_COMPANY_TABLE, true);
	}
	
	/**
	 * 상위 시스템 SKU I/F 테이블 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfSkuTable(String stageCd) {
		// parent.system.if.sku.table
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_SKU_TABLE, true);
	}
	
	/**
	 * 상위 시스템 매장 I/F 테이블 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfShopTable(String stageCd) {
		// parent.system.if.shop.table
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_SHOP_TABLE, true);
	}
	
	/**
	 * 상위 시스템 박스 유형 I/F 테이블 명 
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfBoxTypeTable(String stageCd) {
		// parent.system.if.boxtype.table
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_BOXTYPE_TABLE, true);
	}
	
	/**
	 * 상위 시스템 주문 I/F 테이블 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfOrderTable(String stageCd) {
		// parent.system.if.order.table
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_ORDER_TABLE, true);
	}
	
	/**
	 * 상위 시스템 피킹 실적 I/F 테이블 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfPickingResultTable(String stageCd) {
		// parent.system.if.pick-result.table
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_PICK_RESULT_TABLE, true);
	}
	
	/**
	 * 상위 시스템 박스 실적 I/F 테이블 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfBoxResultTable(String stageCd) {
		// parent.system.if.box-result.table
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_BOX_RESULT_TABLE, true);
	}
	
	/**
	 * 상위 시스템 토털 피킹 I/F 테이블 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfTotalPickingTable(String stageCd) {
		// parent.system.if.totalpicking.table
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_TOTAL_PICKING_TABLE, true);
	}
	
	/**
	 * 상위 시스템 고객사 I/F 프로시져 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfCompanyProcedure(String stageCd) {
		// parent.system.if.company.procedure
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_COMPANY_PROCEDURE, true);
	}
		
	/**
	 * 상위 시스템 SKU I/F 프로시져 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfSkuProcedure(String stageCd) {
		// parent.system.if.sku.procedure
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_SKU_PROCEDURE, true);
	}
	
	/**
	 * 상위 시스템 매장 I/F 프로시져 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfShopProcedure(String stageCd) {
		// parent.system.if.shop.procedure
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_SHOP_PROCEDURE, true);
	}
	
	/**
	 * 상위 시스템 박스 유형 I/F 프로시져 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfBoxTypeProcedure(String stageCd) {
		// parent.system.if.boxtype.procedure
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_BOXTYPE_PROCEDURE, true);
	}
	
	/**
	 * 상위 시스템 주문 I/F 프로시져 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfOrderProcedure(String stageCd) {
		// parent.system.if.order.procedure
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_ORDER_PROCEDURE, true);
	}
	
	/**
	 * 상위 시스템 피킹 실적 I/F 프로시져 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfPickingResultProcedure(String stageCd) {
		// parent.system.if.pick-result.procedure
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_PICK_RESULT_PROCEDURE, true);
	}
	
	/**
	 * 상위 시스템 박스 실적 I/F 프로시져 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfBoxResultProcedure(String stageCd) {
		// parent.system.if.box-result.procedure
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_BOX_RESULT_PROCEDURE, true);
	}
	
	/**
	 * 상위 시스템 토털 피킹 I/F 프로시져 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getParentIfTotalPickingProcedure(String stageCd) {
		// parent.system.if.totalpicking.procedure
		return getConfigValue(stageCd, null, LogisConfigConstants.IF_TOTAL_PICKING_PROCEDURE, true);
	}

	/**
	 * 설비 이벤트 무시할 지 여부
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static boolean isIgnoreEquipStatusReport(String stageCd) {
		// cmm.equip.status.report.ignore.flag
		String boolVal = getConfigValue(stageCd, null, "cmm.equip.status.report.ignore.flag", true);
		return ValueUtil.toBoolean(boolVal);
	}

	/**
	 * 미들웨어 접수 메시지를 로깅할 지 여부
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String isLogMwMessage(String stageCd) {
		// cmm.receive.logging.enabled
		return getConfigValue(stageCd, null, "cmm.receive.logging.enabled", true);
	}
	
	/**
	 * 재고 실사시 버튼 색상
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getStockAdjustColor(String stageCd) {
		// cmm.ind.stock.adjustment.color
		return getConfigValue(stageCd, null, "cmm.ind.stock.adjustment.color", true);
	}
	
	/**
	 * 표시기 버튼 기본 색상
	 * 
	 * @param stageCd 스테이지 코드
	 * @return
	 */
	public static String getIndButtonDefaultColor(String stageCd) {
		// cmm.ind.button.default.color
		return getConfigValue(stageCd, null, "cmm.ind.button.default.color", true);
	}
	
	/**
	 * 스테이지 공통 - 바코드 최대 길이
	 * 
	 * @param stageCd 스테이지 코드
	 * @param jobType 작업 유형
	 * @return
	 */
	public static String getSkuBarcdMaxLength(String stageCd, String jobType) {
		// cmm.sku.barcode.max.length
		return getConfigValue(stageCd, jobType, "cmm.sku.barcode.max.length", true);
	}
	
    /**
     * 스테이지 공통 - 상품 조회를 위한 상품 조회 필드명 리스트
     * 
     * @param batch
     * @return
     */
	public static String getSkuSearchSelectFields(String stageCd, String jobType) {
	    // cmm.sku.search.select.fields
	    return getConfigValue(stageCd, jobType, "cmm.sku.search.select.fields", true);
	}
	
	/**
	 * 스테이지 공통 - 상품 조회를 위한 코드 필드명 리스트
	 * 
	 * @param stageCd 스테이지 코드
	 * @param jobType 작업 유형
	 * @return
	 */
	public static String getSearchSkuFields(String stageCd, String jobType) {
		// cmm.sku.search.condition.fields
		return getConfigValue(stageCd, jobType, "cmm.sku.search.condition.fields", true);
	}
	
	/**
	 * 스테이지 공통 - 주문 필드 중에 분류 코드로 사용할 필드 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @param jobType 작업 유형
	 * @return
	 */
	public static String getPickingClassCodeField(String stageCd, String jobType) {
		// cmm.order.class_cd.field
		return getConfigValue(stageCd, jobType, "cmm.order.class_cd.field", true);
	}
	
	/**
	 * 스테이지 공통 - 주문 필드 중에 박스 처리시에 출고 분류 코드로 사용할 필드 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @param jobType 작업 유형
	 * @return
	 */
	public static String getBoxOutClassCodeField(String stageCd, String jobType) {
		// cmm.box.out.class.field
		return getConfigValue(stageCd, jobType, "cmm.box.out.class.field", true);
	}
	
	/**
	 * @deprecated getPickingClassCodeField로 대체
	 * 스테이지 공통 - 기본 셀 매핑 대상 필드 명
	 * 
	 * @param stageCd 스테이지 코드
	 * @param jobType 작업 유형
	 * @return
	 */
	public static String getCellMappingTargetField(String stageCd, String jobType) {
		// cmm.cell.mapping.target.field
		return getConfigValue(stageCd, jobType, "cmm.cell.mapping.target.field", true);
	}
	
	/**
	 * 스테이지 공통 - 표시기 유형 (통신 프로토콜 기준)
	 * 
	 * @param stageCd 스테이지 코드
	 * @param jobType 작업 유형
	 * @return
	 */
	public static String getIndicatorType(String stageCd, String jobType) {
		// cmm.indicator.type
		return getConfigValue(stageCd, jobType, "cmm.indicator.type", true);
	}
	
	/**
	 * 스테이지 공통 - 주문 취소시 데이터 삭제 여부
	 * 
	 * @param stageCd 스테이지 코드
	 * @param jobType 작업 유형
	 * @return
	 */
	public static boolean isOrderDeleteWhenOrderCancel(String stageCd, String jobType) {
		// cmm.order.delete.when.order_cancel
		String boolVal = getConfigValue(stageCd, jobType, "cmm.order.delete.when.order_cancel", true);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 스테이지 공통 - 주문 그룹 필드로 사용할 주문 테이블의 필드명
	 * 
	 * @param stageCd 스테이지 코드
	 * @param jobType 작업 유형
	 * @return
	 */
	public static String getOrderGroupField(String stageCd, String jobType) {
		// cmm.order.ordergroup.field
		return getConfigValue(stageCd, jobType, "cmm.order.ordergroup.field", true);
	}

}
