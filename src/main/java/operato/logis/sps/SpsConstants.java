package operato.logis.sps;

import xyz.anythings.base.LogisConstants;

/**
 * 단포 분류 모듈 상수 정의
 * 
 * @author shortstop
 */
public class SpsConstants extends LogisConstants {

	/**
	 * 모듈의 기본 작업 타입
	 */
	public static final String MODULE_DEFAULT_JOB_TYPE = LogisConstants.JOB_TYPE_SPS;
	
	/**
	 * B2C 가공 ( 박스투입 맵핑 ) 기준 주문
	 */
	public static final String SPS_PREPROCESS_COL_ORDER = "ORDER";
	/**
	 * B2B 가공 ( 박스투입 맵핑 ) 기준 매장
	 */
	public static final String SPS_PREPROCESS_COL_SHOP = "SHOP";
	
	/**
	 * B2C 대상 분류 : 단포
	 */
	public static final String SPS_ORDER_TYPE_OT = "OT";
	/**
	 * B2C 랙 타입 : 단포
	 */
	public static final String SPS_RACK_TYPE_OT = "O";

}
