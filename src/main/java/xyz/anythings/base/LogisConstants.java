package xyz.anythings.base;

import java.util.List;

import xyz.anythings.sys.AnyConstants;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 물류 마스터 모듈 상수 정의
 * 
 * @author shortstop
 */
public class LogisConstants extends AnyConstants {

	/**
	 * 사이드 F : 앞
	 */
	public static final String SIDE_FRONT = "F";
	/**
	 * 사이드 R : 뒤
	 */
	public static final String SIDE_REAR = "R";
	/**
	 * 사이드 T : 전체
	 */
	public static final String SIDE_TOTAL = "T";
	
	/**
	 * 장비 유형 ALL
	 */
	public static final String DEVICE_ALL = "all";
	/**
	 * 장비 유형 Kiosk
	 */
	public static final String DEVICE_KIOSK = "kiosk";
	/**
	 * 장비 유형 Tablet
	 */
	public static final String DEVICE_TABLET = "tablet";
	/**
	 * 장비 유형 PDA
	 */
	public static final String DEVICE_PDA = "pda";
	/**
	 * 장비 유형 표시기
	 */
	public static final String DEVICE_INDICATOR = "Indicator";

	/**
	 * 분류 작업 유형 : Rack
	 */
	public static final String EQUIP_TYPE_RACK = "Rack";
	/**
	 * 분류 작업 유형 : Sorter 
	 */
	public static final String EQUIP_TYPE_SORTER = "Sorter";
	/**
	 * 분류 작업 유형 : MobileCart
	 */
	public static final String EQUIP_TYPE_MOBILE_CART = "MobileCart";
	
	/**
	 * 분류 작업 유형 필드 : JOB_TYPE
	 */
	public static final String JOB_TYPE = "JOB_TYPE";
	/**
	 * 분류 작업 유형 : DAS
	 */
	public static final String JOB_TYPE_DAS = "DAS";
	/**
	 * 분류 작업 유형 : RTN 
	 */
	public static final String JOB_TYPE_RTN = "RTN";
	/**
	 * 분류 작업 유형 : DPS
	 */
	public static final String JOB_TYPE_DPS = "DPS";
	/**
	 * 분류 작업 유형 : PDAS
	 */
	public static final String JOB_TYPE_PDAS = "PDAS";
	/**
	 * 분류 작업 유형 : QPS
	 */
	public static final String JOB_TYPE_QPS = "QPS";
	/**
	 * 분류 작업 유형 : SPS (단포)
	 */
	public static final String JOB_TYPE_SPS = "SPS";
	/**
	 * 분류 작업 유형 : OPS (오더 피킹)
	 */
	public static final String JOB_TYPE_OPS = "OPS";
	/**
	 * 분류 작업 유형 : DPC (Digital Picking Cart)
	 */
	public static final String JOB_TYPE_DPC = "DPC";
	
	/**
	 * 박스요청 대기 상태 : Box Waiting
	 */
	public static final String JOB_STATUS_BOX_WAIT = "BW";
	
	/**
	 * 작업 대기 상태 : Waiting
	 */
	public static final String JOB_STATUS_WAIT = "W";
	/**
	 * 작업 투입 상태 : Input
	 */
	public static final String JOB_STATUS_INPUT = "I";
	/**
	 * 작업 피킹 상태 : Picking
	 */
	public static final String JOB_STATUS_PICKING = "P";
	/**
	 * 작업 완료 상태 : Finished
	 */
	public static final String JOB_STATUS_FINISH = "F";
	/**
	 * 박싱 완료 상태 : Boxed
	 */
	public static final String JOB_STATUS_BOXED = "B";
	/**
	 * 검수 완료 상태 : Examinated
	 */
	public static final String JOB_STATUS_EXAMINATED = "E";
	/**
	 * 출고 완료 상태 : Final Out
	 */
	public static final String JOB_STATUS_FINAL_OUT = "O";
	/**
	 * 실적 보고 완료 상태 : Reported
	 */
	public static final String JOB_STATUS_REPORTED = "R";
	/**
	 * 작업 취소 상태 : Canceled
	 */
	public static final String JOB_STATUS_CANCEL = "C";
	/**
	 * 주문 취소 상태 : Deleted
	 */
	public static final String JOB_STATUS_DELETED = "D";
	
	/**
	 * 작업 상태 - WAIT, PICKING
	 */
	public static final List<String> JOB_STATUS_WP = ValueUtil.newStringList(JOB_STATUS_WAIT, JOB_STATUS_PICKING);
	/**
	 * 작업 상태 - WAIT, PICKING, FINISH
	 */
	public static final List<String> JOB_STATUS_WPF = ValueUtil.newStringList(JOB_STATUS_WAIT, JOB_STATUS_PICKING, JOB_STATUS_FINISH);
	/**
	 * 작업 상태 - PICKING, FINISH
	 */
	public static final List<String> JOB_STATUS_PF = ValueUtil.newStringList(JOB_STATUS_PICKING, JOB_STATUS_FINISH);
	/**
	 * 작업 상태 - WAIT, INPUT, CANCEL
	 */
	public static final List<String> JOB_STATUS_WIC = ValueUtil.newStringList(JOB_STATUS_WAIT, JOB_STATUS_INPUT, JOB_STATUS_CANCEL);
	/**
	 * 작업 상태 - WAIT, INPUT, PICKING, CANCEL
	 */
	public static final List<String> JOB_STATUS_WIPC = ValueUtil.newStringList(JOB_STATUS_WAIT, JOB_STATUS_INPUT, JOB_STATUS_PICKING, JOB_STATUS_CANCEL);
	/**
	 * 작업 상태 - WAIT, INPUT, PICKING
	 */
	public static final List<String> JOB_STATUS_WIP = ValueUtil.newStringList(JOB_STATUS_WAIT, JOB_STATUS_INPUT, JOB_STATUS_PICKING);
	/**
	 * 작업 상태 - INPUT, PICKING
	 */
	public static final List<String> JOB_STATUS_IP = ValueUtil.newStringList(JOB_STATUS_INPUT, JOB_STATUS_PICKING);
	/**
	 * 작업 상태 - INPUT, PICKING, FINISH
	 */
	public static final List<String> JOB_STATUS_IPF = ValueUtil.newStringList(JOB_STATUS_INPUT, JOB_STATUS_PICKING, JOB_STATUS_FINISH);
	/**
	 * 작업 상태 - INPUT, PICKING, CANCEL
	 */
	public static final List<String> JOB_STATUS_IPC = ValueUtil.newStringList(JOB_STATUS_INPUT, JOB_STATUS_PICKING, JOB_STATUS_CANCEL);
	/**
	 * 작업 상태 - WAIT, INPUT, PICKING, FINISH
	 */
	public static final List<String> JOB_STATUS_WIPF = ValueUtil.newStringList(JOB_STATUS_WAIT, JOB_STATUS_INPUT, JOB_STATUS_PICKING, JOB_STATUS_FINISH);
	/**
	 * 작업 상태 - WAIT, INPUT, PICKING, FINISH, BOXED
	 */
	public static final List<String> JOB_STATUS_WIPFB = ValueUtil.newStringList(JOB_STATUS_WAIT, JOB_STATUS_INPUT, JOB_STATUS_PICKING, JOB_STATUS_FINISH, JOB_STATUS_BOXED);
	/**
	 * 작업 상태 - WAIT, INPUT, PICKING, FINISH, CANCEL
	 */
	public static final List<String> JOB_STATUS_WIPFC = ValueUtil.newStringList(JOB_STATUS_WAIT, JOB_STATUS_INPUT, JOB_STATUS_PICKING, JOB_STATUS_FINISH, JOB_STATUS_CANCEL);
	/**
	 * 작업 상태 - FINISH, BOXED
	 */
	public static final List<String> JOB_STATUS_FB = ValueUtil.newStringList(JOB_STATUS_FINISH, JOB_STATUS_BOXED);
	/**
	 * 작업 상태 - FINISH, CANCEL
	 */
	public static final List<String> JOB_STATUS_FC = ValueUtil.newStringList(JOB_STATUS_FINISH, JOB_STATUS_CANCEL);
	/**
	 * 작업 상태 - BOXED, EXAMINED, REPORTED
	 */
	public static final List<String> JOB_STATUS_BER = ValueUtil.newStringList(JOB_STATUS_BOXED, JOB_STATUS_EXAMINATED, JOB_STATUS_REPORTED);
	/**
	 * 작업 상태 - BOXED, EXAMINED, REPORTED, CANCEL
	 */
	public static final List<String> JOB_STATUS_BERC = ValueUtil.newStringList(JOB_STATUS_BOXED, JOB_STATUS_EXAMINATED, JOB_STATUS_REPORTED, JOB_STATUS_CANCEL);
	/**
	 * 작업 상태 - FINISH, BOXED, EXAMINED, REPORTED
	 */
	public static final List<String> JOB_STATUS_FBER = ValueUtil.newStringList(JOB_STATUS_FINISH, JOB_STATUS_BOXED, JOB_STATUS_EXAMINATED, JOB_STATUS_REPORTED);
		
	/**
	 * 로케이션 단위의 작업 박싱 완료 상태 : BOXED
	 */
	public static final String CELL_JOB_STATUS_BOXED = "BOXED";
	/**
	 * 로케이션 단위의 작업 완료 후 Fullbox가 필요한 상태 : ENDING
	 */
	public static final String CELL_JOB_STATUS_ENDING = "ENDING";
	/**
	 * 로케이션 단위의 작업 최종 완료 상태 : ENDED
	 */
	public static final String CELL_JOB_STATUS_ENDED = "ENDED";
	
	
	/**
	 * 박스 ID 의 유일성 범위 : G: 도메인 전체 유일
	 */
	public static final String BOX_ID_UNIQUE_SCOPE_GLOBAL = "G";
	/**
	 * 박스 ID 의 유일성 범위 : D: 날자별 유일
	 */
	public static final String BOX_ID_UNIQUE_SCOPE_DAY = "D";
	/**
	 * 박스 ID 의 유일성 범위 : B: 배치 내 유일
	 */
	public static final String BOX_ID_UNIQUE_SCOPE_BATCH = "B";
	
	/**
	 * 로케이션 단위의 작업 최종 완료, 완료 중 상태
	 */
	public static final List<String> CELL_JOB_STATUS_END_LIST = ValueUtil.newStringList(CELL_JOB_STATUS_ENDED, CELL_JOB_STATUS_ENDING);
		
	/**
	 * 색상 - RED
	 */
	public static final String COLOR_RED = "R";
	/**
	 * 색상 - GREEN
	 */
	public static final String COLOR_GREEN = "G";
	/**
	 * 색상 - BLUE
	 */
	public static final String COLOR_BLUE = "B";
	/**
	 * 색상 - YELLOW
	 */
	public static final String COLOR_YELLOW = "Y";
	/**
	 * 표시기 색상 리스트
	 */
	public static final List<String> MPI_COLOR_LIST = ValueUtil.newStringList(COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_YELLOW);
	
	/**
	 * 랙 사이드 코드 명 - RACK_SIDE
	 */
	public static final String CODE_NAME_RACK_SIDE = "RACK_SIDE";
	
	/**
	 * 설비 상태 정상
	 */
	public static final String EQUIP_STATUS_OK = "1";
	/**
	 * 설비 상태 고장
	 */
	public static final String EQUIP_STATUS_BREAK_DOWN = "2";

	/**
	 * 합 / 불 상태 합격 - P
	 */
	public static final String PASS_STATUS = "P";
	/**
	 * 합 / 불 상태 불합격 - F
	 */
	public static final String FAIL_STATUS = "F";
	
	/**
	 * 작업 유형이 DAS 작업 타입인지 체크
	 * 
	 * @param jobType
	 * @return
	 */
	public static boolean isDasJobType(String jobType) {
		return ValueUtil.isEqualIgnoreCase(JOB_TYPE_DAS, jobType);
	}
	
	/**
	 * 작업 유형이 반품 작업 타입인지 체크
	 * 
	 * @param jobType
	 * @return
	 */
	public static boolean isRtnJobType(String jobType) {
		return ValueUtil.isEqualIgnoreCase(JOB_TYPE_RTN, jobType);
	}
	
	/**
	 * 작업 유형이 DPS 작업 타입인지 체크
	 * 
	 * @param jobType
	 * @return
	 */
	public static boolean isDpsJobType(String jobType) {
		return ValueUtil.isEqualIgnoreCase(JOB_TYPE_DPS, jobType);
	}
	
	/**
	 * 작업 유형이 SNG 작업 타입인지 체크
	 * 
	 * @param jobType
	 * @return
	 */
	public static boolean isSngJobType(String jobType) {
		return ValueUtil.isEqualIgnoreCase(JOB_TYPE_SPS, jobType);
	}
	
	/**
	 * 작업 유형이 QPS 작업 타입인지 체크
	 * 
	 * @param jobType
	 * @return
	 */
	public static boolean isQpsJobType(String jobType) {
		return ValueUtil.isEqualIgnoreCase(JOB_TYPE_QPS, jobType);
	}
	
	/**
	 * B2B 작업 타입인지 체크
	 * 
	 * @param jobType
	 * @return
	 */
	public static boolean isB2BJobType(String jobType) {
		return !isB2CJobType(jobType);
	}
	
	/**
	 * B2C 작업 타입인지 체크
	 * 
	 * @param jobType
	 * @return
	 */
	public static boolean isB2CJobType(String jobType) {
		return isDpsJobType(jobType) || isQpsJobType(jobType);
	}
	
	/**
	 * 쿼리에서 sideCd가 ALL이라면 sideCd 조건은 없는 것과 같으므로 null로 바꾼다.
	 *
	 * @param domainId
	 * @param sideCd
	 * @return
	 */
	public static String checkSideCdForQuery(Long domainId, String sideCd) {
		if(isDeviceSideCdEnabled(domainId)) {
			if(ValueUtil.isNotEmpty(sideCd) && (ValueUtil.isEqualIgnoreCase(sideCd, AnyConstants.NULL_STRING) || ValueUtil.isEqualIgnoreCase(sideCd, SIDE_TOTAL) || ValueUtil.isEqualIgnoreCase(sideCd, AnyConstants.ALL_CAP_STRING))) {
				return null;
			} else if(ValueUtil.isEmpty(sideCd)) {
				return null;
			} else {
				return sideCd.toUpperCase();
			}
		} else {
			return null;
		}
	}
	
	/**
	 * 모바일 장비에서 SideCd 사용 여부
	 * 
	 * @param domainId
	 * @return
	 */
	public static boolean isDeviceSideCdEnabled(Long domainId) {
		return ValueUtil.toBoolean(SettingUtil.getValue(domainId, LogisConfigConstants.DEVICE_SIDE_ENABLED, AnyConstants.FALSE_STRING));
	}

}
