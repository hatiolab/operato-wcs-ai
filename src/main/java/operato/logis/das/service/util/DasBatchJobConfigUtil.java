package operato.logis.das.service.util;

import operato.logis.das.DasConfigConstants;
import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.service.util.BatchJobConfigUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 출고 관련 작업 배치 관련 설정 프로파일
 * 
 * 작업 설정 프로파일 컨셉
 * 1. 스테이지마다 기본 설정 프로파일이 존재하고 기본 설정 프로파일은 default_flag = true인 것이다.
 * 2. job.cmm으로 시작하는 항목은 모두 기본 설정 프로파일에 추가가 이미 되어 있다. -> 없으면 해당 설정 항목 조회시 에러 발생해야 함
 * 3. default_flag가 false인 설정 프로파일은 기본 설정 프로파일의 모든 값을 복사하여 가지고 있어서 조회시 자기가 가진 정보로 조회한다. 없으면 기본 설정을 찾는다.
 * 4. 작업 배치에서 조회할 내용이 아닌 설정은 (성격상 작업 배치가 결정되지 않은 시점에 필요한 설정) Setting 정보에 존재한다.
 * 
 * 출고 작업 설정 항목
 *  - job.das.cell-boxid.mapping.point			셀과 박스 ID를 매핑하는 시점									P: 주문 가공시, B : 분류 처리 전 박스 매핑, A: 분류 처리 후 풀 박스 전 박스 매핑, N: 수동 박스 매핑이 필요 없음 - 자동 매핑 시
 *  - job.das.cell-classcd.mapping.point		셀과 분류 코드를 매핑하는 시점									P: 주문 가공시, S : 작업 지시 이후 작업자 수동 매핑
 *  - job.das.input.check.weight.enabled		키오스크에서 상품 투입시에 상품 중량 체크 여부 						true
 *  - job.das.next-job.event.method				다음 작업 처리 방식 (relay / event)							relay
 *  - job.das.middleassort.display.qty.mode		DAS KIOSK 중분류 화면에서 표시 수량 (fix/filter)				filter
 *  - job.das.middleassort.rack.sort.ascending	DAS KIOSK 중분류 화면에서 호기 정렬 옵션 						false
 *  - job.das.pick.relay.max.no					표시기에 표시할 릴레이 번호 최대 번호 (최대 번호 이후 다시 1로)		99
 *  - job.das.preprocess.cell.mapping.field		셀에 할당할 대상 필드 (매장, 상품, 주문번호…) 					shop_cd
 *  - job.das.preprocess.sort.field.for.cellmapping		주문 가공 정보에서 셀 매핑을 위해 소팅하기 위한 ORDER_PREPROCESSES 테이블 필드명 	total_pcs
 * 
 * @author shortstop
 */
public class DasBatchJobConfigUtil extends BatchJobConfigUtil {

	/**
	 * 셀 - 박스 매핑 시점 (P: 주문 가공시, B: 분류 처리 전 박스 매핑, A: 분류 처리 후 풀 박스 전 박스 매핑, N: 수동 박스 매핑이 필요 없음 - 자동 매핑 시)
	 * 
	 * @param batch
	 * @return
	 */
	public static String getCellBoxMappingPoint(JobBatch batch) {
		// job.das.cell-boxid.mapping.point
		return getConfigValue(batch, DasConfigConstants.DAS_CELL_BOXID_MAPPING_POINT, DasConfigConstants.DAS_CELL_BOX_MAPPING_POINT_PREPROCESS);
	}
	
	/**
	 * 셀 - 분류 코드 (매장, 주문, 상품...) 매핑 시점 (P: 주문 가공시, S: 작업 지시 이후 작업자 수동 매핑)
	 * 
	 * @param batch
	 * @return
	 */
	public static String getCellClassCdMappingPoint(JobBatch batch) {
		// job.das.cell-classcd.mapping.point
		return getConfigValue(batch, DasConfigConstants.DAS_CELL_CLASSCD_MAPPING_POINT, DasConfigConstants.DAS_CELL_CLASSCD_MAPPING_POINT_PREPROCESS);
	}
	
	/**
	 * 셀에 할당할 대상 필드 (매장, 상품, 주문번호 …)
	 * 
	 * @param batch
	 * @return
	 */
	public static String getBoxMappingTargetField(JobBatch batch) {
		// job.das.cell.mapping.target.field
		return getConfigValue(batch, DasConfigConstants.DAS_CELL_MAPPING_TARGET_FIELD, true);
	}
	
	/**
	 * DAS KIOSK 중분류 화면에서 표시 수량 (fix/filter)
	 * 
	 * @param batch
	 * @return
	 */
	public static String getCategorizationQtyDisplayMode(JobBatch batch) {
		// job.das.middleassort.display.qty.mode
		return getConfigValue(batch, DasConfigConstants.DAS_MIDDLEASSORT_DISPLAY_QTY_MODE, DasConfigConstants.DAS_CATEGORIZATION_QTY_MODE_FIX);
	}
	
	/**
	 * DAS KIOSK 중분류 화면에서 표시 수량이 fix 인지 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isCategorizationDisplayFixedQtyMode(JobBatch batch) {
		String val = getCategorizationQtyDisplayMode(batch);
		return ValueUtil.isEqualIgnoreCase(DasConfigConstants.DAS_CATEGORIZATION_QTY_MODE_FIX, val);
	}
	
	/**
	 * DAS KIOSK 중분류 화면에서 호기 정렬을 오름차순으로 할 것 인지 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isCategorizationRackSortMode(JobBatch batch) {
		// job.das.middleassort.rack.sort.ascending
		String val = getConfigValue(batch, DasConfigConstants.DAS_MIDDLEASSORT_RACK_SORT_ASEND, LogisConstants.TRUE_STRING);
		return ValueUtil.isEqualIgnoreCase(val, LogisConstants.TRUE_STRING);
	}

	/**
	 * DAS 주문 가공시 셀 매핑을 하기 위해 주문 가공 정보로 부터 소팅을 하기 위한 필드명
	 * 
	 * @param batch
	 * @return
	 */
	public static String getPreprocessSortFieldForCellMapping(JobBatch batch) {
		// job.das.preprocess.sort.field.for.cellmapping
		return getConfigValue(batch, DasConfigConstants.DAS_PREPROCESS_SORT_FIELD_FOR_CELLMAPPING, DasConfigConstants.DAS_DEFAULT_PREPROCESS_SORT_FIELD);
	}
	
	/**
	 * DAS 셀 매핑 시 주문 가공 정보 소팅 필드를 오름차순 정렬을 할 것인지 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isPreprocessAscendingSortForCellMapping(JobBatch batch) {
		// job.das.preprocess.sort.ascend.for.cellmapping
		String val = getConfigValue(batch, DasConfigConstants.DAS_PREPROCESS_SORT_ASC_FOR_CELLMAPPING, LogisConstants.FALSE_STRING);
		return ValueUtil.isEqualIgnoreCase(val, LogisConstants.TRUE_STRING);
	}	
	
	/**
	 * DAS 표시기 최종 End 사용 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isPickFinalEndOn(JobBatch batch) {
		// job.das.pick.fianl.end.enabled
		String val = getConfigValue(batch, DasConfigConstants.DAS_PICK_FINAL_END_ENABLED, LogisConstants.TRUE_STRING);
		return ValueUtil.isEqualIgnoreCase(val, LogisConstants.TRUE_STRING);
	}

}
