package xyz.anythings.gw;

import xyz.elidom.sys.SysConstants;

/**
 * 미들웨어 모듈 상수 정의 
 * 
 * @author shortstop
 */
public class GwConstants extends SysConstants {		
	
	/**
	 * Indicator의 버튼에 대한 작업 수행 - 확정 처리
	 */
	public static final String IND_BIZ_FLAG_OK = "ok";
	/**
	 * Indicator의 버튼에 대한 작업 수행 - 수정 처리 
	 */
	public static final String IND_BIZ_FLAG_MODIFY = "modify";
	/**
	 * Indicator의 Full 기능 버튼을 눌러서 처리
	 */
	public static final String IND_BIZ_FLAG_FULL = "full";
	/**
	 * Indicator의 수량을 조절하여 Full 처리
	 */
	public static final String IND_BIZ_FLAG_FULL_MODIFY = "full_modify";
	/**
	 * Indicator의 Full 문자가 깜빡이는 시점에 버튼 확정 처리
	 */
	public static final String IND_BIZ_FLAG_CONFIRM_FULL = "confirm-full";	
	/**
	 * Indicator의 End 기능 버튼을 눌러서 처리
	 */
	public static final String IND_BIZ_FLAG_END = "end";
	/**
	 * Indicator의 Cancel 기능 버튼을 눌러서 처리
	 */
	public static final String IND_BIZ_FLAG_CANCEL = "cancel";
	/**
	 * 피킹 작업 타입
	 */
	public static final String IND_ACTION_TYPE_PICK = "pick";
	/**
	 * 검수 작업 타입
	 */
	public static final String IND_ACTION_TYPE_INSPECT = "inspect";	
	/**
	 * STOCK 작업 타입
	 */
	public static final String IND_ACTION_TYPE_STOCK = "stock";
	/**
	 * 박스 매핑 안 됨 표시 작업 타입
	 */
	public static final String IND_ACTION_TYPE_NOBOX = "nobox";
	/**
	 * Fullbox시 박스 매핑 안 됨 에러 표시 작업 타입
	 */
	public static final String IND_ACTION_TYPE_ERRBOX = "errbox";
	/**
	 * Display 작업 타입
	 */
	public static final String IND_ACTION_TYPE_DISPLAY = "display";
	/**
	 * 문자열 표시 타입
	 */
	public static final String IND_ACTION_TYPE_STR_SHOW = "strshow";
	
	/**
	 * 표시기 왼쪽 세그먼트 - L
	 */
	public static final String IND_LEFT_SEGMENT = "L";
	/**
	 * 표시기 오른쪽 세그먼트 - R
	 */
	public static final String IND_RIGHT_SEGMENT = "R";
	
	/**
	 * 처리해야 할 표시기 모드 
	 */
	public static final String IND_ON_TODO_MODE = "todo";
	/**
	 * 처리한 표시기 모드
	 */
	public static final String IND_ON_DONE_MODE = "done";
	
	/**
	 * Gateway Name Separator
	 */
	public static final String GW_NAME_SEPARATOR = "/";
	
	/**
	 * 장비 타입 - 게이트웨이
	 */
	public static final String EQUIP_GATEWAY = "gw";
	/**
	 * 장비 타입 - 표시기
	 */
	public static final String EQUIP_INDICATOR = "ind";
	/**
	 * 장비 상태 - Offline
	 */
	public static final String EQUIP_STATUS_OFFLINE = "offline";
	/**
	 * 해당 없음 상수
	 */
	public static final String NOT_AVAILABLE = "_na_";
	/**
	 * 문자열 - all
	 */
	public static final String ALL_STRING = "all";
	/**
	 * 문자열 - ALL
	 */
	public static final String ALL_CAP_STRING = "ALL";
	
}
