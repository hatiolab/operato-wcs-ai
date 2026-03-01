package operato.logis.das;

import xyz.anythings.base.LogisConfigConstants;

/**
 * DAS 용 설정용 상수 정의
 * 
 * @author shortstop
 */
public class DasConfigConstants extends LogisConfigConstants {

	/**
	 * 셀 - 박스 매핑 시점 (P: 주문 가공시)
	 */
	public static final String DAS_CELL_BOX_MAPPING_POINT_PREPROCESS = "P";
	/**
	 * 셀 - 박스 매핑 시점 (B: 분류 처리 전 박스 매핑)
	 */
	public static final String DAS_CELL_BOX_MAPPING_POINT_BEFORE_ASSORTING = "B";
	/**
	 * 셀 - 박스 매핑 시점 (A: 분류 시, 풀 박스 바로 전)
	 */
	public static final String DAS_CELL_BOX_MAPPING_POINT_AFTER_ASSORTING = "A";
	/**
	 * 셀 - 박스 매핑 시점 (N: 수동 박스 매핑이 필요 없는 자동 매핑 시)
	 */
	public static final String DAS_CELL_BOX_MAPPING_POINT_NONE = "N";
	
	/**
	 * 셀 - 분류 코드 (주문, 상품, 매장) 매핑 시점 (P: 주문 가공시)
	 */
	public static final String DAS_CELL_CLASSCD_MAPPING_POINT_PREPROCESS = "P";
	/**
	 * 셀 - 분류 코드 (주문, 상품, 매장) 매핑 시점 (S: 작업 지시 이후 작업자가 수동 매핑)
	 */
	public static final String DAS_CELL_CLASSCD_MAPPING_POINT_AFTER_START = "S";
	
	/**
	 * 다음 작업 처리 방식 (relay)
	 */
	public static final String DAS_NEXT_JOB_PROCESS_METHOD_RELAY = "relay";
	/**
	 * 다음 작업 처리 방식 (event)
	 */
	public static final String DAS_NEXT_JOB_PROCESS_METHOD_EVENT = "event";
	
	/**
	 * DAS 중분류 화면에서 표시 수량 (fix : 고정)
	 */
	public static final String DAS_CATEGORIZATION_QTY_MODE_FIX = "fix";
	/**
	 * DAS 중분류 화면에서 표시 수량 (filter : 처리 수량 제외 계산)
	 */
	public static final String DAS_CATEGORIZATION_QTY_MODE_FILTER = "filter";
	
	/**
	 * DAS 주문 가공 정보 조회시 소팅할 기본 필드 명
	 */
	public static final String DAS_DEFAULT_PREPROCESS_SORT_FIELD = "total_pcs";

}
