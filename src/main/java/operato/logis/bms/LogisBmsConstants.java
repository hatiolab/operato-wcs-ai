package operato.logis.bms;

import xyz.anythings.sys.AnyConstants;

/**
 * BMS 마스터 모듈 상수 정의
 * 
 * @author shortstop
 */
public class LogisBmsConstants extends AnyConstants {

	// 상품이 존재하지 않는 경우 추천 할 박스 유형 
	public static final String NOT_EXIST_SKU_MASTER_BOX_TYPE = "ER1";
	// 추천 가능한 박스 유형이 없는 경우 (길이 값 초과) 추천 할 박스 유형 
	public static final String NOT_EXIST_ENABLE_BOX_TYPE = "ER2";
	// 박스 추천 기준 : 부피 
	public static final String SPLIT_BY_VOLUME = "VOLUME";
}
