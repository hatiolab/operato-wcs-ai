package operato.logis.dpc.service.util;

import xyz.anythings.base.service.util.StageJobConfigUtil;

/**
 * DPC 관련 Stage 공통 설정 프로파일
 * 
 * @author shortstop
 */
public class DpcStageJobConfigUtil extends StageJobConfigUtil {
	
//	/**
//	 * SKU 물량 Rank 선정 기준 데이터 범위 (일자)
//	 * 
//	 * @param stageCd 스테이지 코드
//	 * @return
//	 */
//	public static String getSkuRankingTargetDays(String stageCd) {
//		// dpc.sku.popula.rank.calc.days
//		return getConfigValue(stageCd, DpcConstants.MODULE_DEFAULT_JOB_TYPE, DpcConfigConstants.DPC_SKU_POPULAR_RANK_CALC_DAYS, true);
//	}
//
//	/**
//	 * DPC 배치 분리 여부
//	 * 
//	 * @param stageCd 스테이지 코드
//	 * @return
//	 */
//	public static boolean isSeparatedBatchByRack(String stageCd) {
//		// dpc.batch.split-by-rack.enabled
//		String boolVal = getConfigValue(stageCd, DpcConstants.MODULE_DEFAULT_JOB_TYPE, DpcConfigConstants.DPC_BATCH_SPLIT_BY_RACK_ENABLED, true);
//		return ValueUtil.toBoolean(boolVal);
//	}
//	
//	/**
//	 * DPC 작업 스테이션에 대기할 박스 최대 개수 (-1이면 무한대)
//	 * 
//	 * @param stageCd 스테이지 코드
//	 * @return
//	 */
//	public static int getStationWaitPoolCount(String stageCd) {
//		// dpc.station.wait-pool.count
//		String intVal = getConfigValue(stageCd, DpcConstants.MODULE_DEFAULT_JOB_TYPE, DpcConfigConstants.DPC_STATION_WAIT_POOL_COUNT, true);
//		return ValueUtil.toInteger(intVal);
//	}
//	
//	/**
//	 * DPC 추천 로케이션 사용 여부
//	 * 
//	 * @param stageCd 스테이지 코드
//	 * @return
//	 */
//	public static boolean isRecommendCellEnabled(String stageCd) {
//		// dpc.supple.recommend-cell.enabled
//		String boolVal = getConfigValue(stageCd, DpcConstants.MODULE_DEFAULT_JOB_TYPE, DpcConfigConstants.DPC_SUPPLE_RECOMMEND_CELL_ENABLED, true);
//		return ValueUtil.toBoolean(boolVal);
//	}
//	
//	/**
//	 * DPC 투입 박스 유형
//	 * 
//	 * @param stageCd 스테이지 코드
//	 * @return
//	 */
//	public static String getInputBoxType(String stageCd) {
//		// dpc.input.box.type
//		return getConfigValue(stageCd, DpcConstants.MODULE_DEFAULT_JOB_TYPE, DpcConfigConstants.DPC_INPUT_BOX_TYPE, true);
//	}
//	
//	/**
//	 * DPC 단포 대상 분류 여부
//	 * 
//	 * @param stageCd 스테이지 코드
//	 * @return
//	 */
//	public static boolean isSingleSkuNpcsClassEnabled(String stageCd) {
//		// dpc.pick.1sku.npcs.enabled
//		String boolVal = getConfigValue(stageCd, DpcConstants.MODULE_DEFAULT_JOB_TYPE, DpcConfigConstants.DPC_PICK_1SKU_NPCS_ENABLED, true);
//		return ValueUtil.toBoolean(boolVal);
//	}
//	
//	/**
//	 * DPC 피킹과 동시에 검수 처리할 것인지 여부
//	 * 
//	 * @param stageCd 스테이지 코드
//	 * @return
//	 */
//	public static boolean isPickingWithInspectionEnabled(String stageCd) {
//		// dpc.pick.with-inspection.enabled
//		String boolVal = getConfigValue(stageCd, DpcConstants.MODULE_DEFAULT_JOB_TYPE, DpcConfigConstants.DPC_PICK_WITH_INSPECTION_ENABLED, true);
//		return ValueUtil.toBoolean(boolVal);
//	}
//	
//	/**
//	 * DPC 작업 할당 스케줄러 사용 여부
//	 * 
//	 * @param stageCd 스테이지 코드
//	 * @return
//	 */
//	public static boolean isJobAssignSchedulingEnabled(String stageCd) {
//		// dpc.job-assign.scheduling.enabled
//		String boolVal = getConfigValue(stageCd, DpcConstants.MODULE_DEFAULT_JOB_TYPE, DpcConfigConstants.DPC_JOB_ASSIGN_SCHEDULING_ENABLE, true);
//		return ValueUtil.toBoolean(boolVal);
//	}
//	
//	
//	/**
//	 * DPC 박스에 할당할 대상 필드 (매장, 상품, 주문번호 …)
//	 * 
//	 * @param stageCd 스테이지 코드
//	 * @return
//	 */
//	public static String getBoxMappingTargetField(String stageCd) {
//		// dpc.box.mapping.field
//		return getConfigValue(stageCd, DpcConstants.MODULE_DEFAULT_JOB_TYPE, DpcConfigConstants.DPC_BOX_MAPPING_FIELD, true);
//	}

}
