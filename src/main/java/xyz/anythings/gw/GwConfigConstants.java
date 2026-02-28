package xyz.anythings.gw;

import xyz.elidom.sys.SysConfigConstants;

/**
 * 미들웨어 모듈 설정 키 관련 상수 정의
 *
 * @author shortstop
 */
public class GwConfigConstants extends SysConfigConstants {
	
	/**
	 * 미들웨어 메시지 로깅 활성화 여부
	 */
	public static final String MW_LOG_RCV_MSG_ENABLED = "mw.log.receive.msg.enabled";
	/**
	 * 미들웨어 설비 이벤트 로깅할 지 여부
	 */
	public static final String MW_LOG_EQUIP_STATUS_ENABLED = "mw.log.equip.status.enabled";

	/**********************************************************************
	 * 								1. 전체 설정 
	 **********************************************************************/
	/**
	 * 표시 확정 이후 표시기 처리 이력 표시를 위한 세그먼트의 첫번째 숫자와 매핑되는 역할
	 * (T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
	 */
	public static final String IND_SEGMENT1_MAPPING_ROLE = "ind.show.segment1.mapping.role";
	/**
	 * 표시 확정 이후 표시기 처리 이력 표시를 위한 세그먼트의 두번째 숫자와 매핑되는 역할
	 * (T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
	 */
	public static final String IND_SEGMENT2_MAPPING_ROLE = "ind.show.segment2.mapping.role";
	/**
	 * 표시 확정 이후 표시기 처리 이력 표시를 위한 세그먼트의 세번째 숫자와 매핑되는 역할
	 * (T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
	 */
	public static final String IND_SEGMENT3_MAPPING_ROLE = "ind.show.segment3.mapping.role";	
	/**
	 * 표시기 표시 자리 (L:Left, R:Right)
	 */
	public static final String IND_NUMBER_ALIGNMENT = "ind.show.number.alignment";
	/**
	 * 표시기 버튼 점등 모드 (B:깜빡임, S:정지)
	 */
	public static final String IND_BUTTON_ON_MODE = "ind.show.button.on.mode";
	/**
	 * 표시기 버튼 깜빡임 주기 (100ms)
	 */
	public static final String IND_BUTTON_BLINK_INTERVAL = "ind.show.button.blink.interval";
	/**
	 * 표시기 Full Box 터치시 버튼 깜빡임 여부 (true / false)
	 */
	public static final String IND_FULLBOX_BUTTON_BLINK = "ind.show.fullbox.button.blink";
	/**
	 * 표시기 최대 릴레이 번호
	 */
	public static final String IND_RELAY_MAX_NO = "ind.show.relay.max.no";
	/**
	 * 표시기 표시 모드 - 0: default (or undefined) 1: 박스입수, 낱개수량을 계산하여 박스 수량/낱개 수량 형태로 표시 2: 누적 수량/낱개 수량(반품) 3: 작업 잔량/낱개 수량
	 */
	public static final String IND_SHOW_VIEW_TYPE = "ind.show.view-type";
	
	/**
	 * 표시기 점등 전 표시 문자
	 */
	public static final String IND_SHOW_STRING_BEFORE_ON = "ind.action.show.string.before.on";
	/**
	 * 표시기 점등 전 문자 표시 시간
	 */
	public static final String IND_SHOW_STRING_DELAY_BEFORE_ON = "ind.action.show.string.delay.before.on";
	/**
	 * 표시기 점등 전 딜레이 (100ms)
	 */
	public static final String IND_DELAY_BEFORE_ON = "ind.action.delay.before.on";
	/**
	 * 표시기 취소 버튼 터치시 소등까지 딜레이 (100ms)
	 */
	public static final String IND_DELAY_CANCEL_BUTTON_OFF = "ind.action.delay.cancel.button.off";
	/**
	 * 표시기가 이미 소등된 경우 소등 요청을 받았을 때 ACK를 응답할 지 여부
	 */
	public static final String IND_SEND_OFF_ACK_ALREADY_OFF = "ind.action.send.off.ack.already.off";
	/**
	 * 표시기 상태 보고 주기.
	 */
	public static final String IND_HEALTH_PERIOD = "ind.action.status.report.interval";
	
	/**
	 * LED를 사용할 지 여부 설정
	 */
	public static final String IND_LED_USE_ENABLED = "ind.led.use.enabled";
	/**
	 * LED를 사용할 랙 
	 */
	public static final String IND_LED_USE_ENABLED_RACKS = "ind.led.use.enabled.racks";
	/**
	 * LED 점등 모드 (B:깜빡임, S:정지)
	 */
	public static final String IND_LED_ON_MODE = "ind.led.on.mode";
	/**
	 * LED 깜빡임 주기 (100ms)
	 */
	public static final String IND_LED_BLINK_INTERVAL = "ind.led.blink.interval";
	/**
	 * LED 바 밝기 정도 (1~10)
	 */
	public static final String IND_LED_BRIGHTNESS = "ind.led.brightness";
	
	/**
	 * 표시기 점등 세그먼트 역할 - 첫번째/두번째/세번째 세그먼트 역할 -> R(릴레이 순서)/B(Box)/P(PCS)/S(문자열)
	 */
	public static final String IND_SEGMENT_ROLE_ON = "ind.job.segment.roles.on";
	/**
	 * 재고 실사 기본 표시기 색상
	 */
	public static final String IND_DEFAULT_COLOR_STOCKTAKING = "ind.job.color.stocktaking";
	/**
	 * 표시기 색상 로테이션 순서
	 */
	public static final String IND_COLOR_ROTATION_SEQ = "ind.job.color.rotation.seq";
	
	/**
	 * 표시기 버튼 사용 여부
	 */
	public static final String IND_BUTTONS_ENABLE = "ind.buttons.enable";
	/**
	 * 표시기 최신 버전 정보 설정.
	 */
	public static final String IND_LATEST_RELEASE_VERSION = "ind.latest.release.version";
	/**
	 * Gateway 최신 버전 정보 설정.
	 */
	public static final String GW_LATEST_RELEASE_VERSION = "ind.gw.latest.release.version";
	/**
	 * 재고 조정시 LED 타입
	 */
	public static final String IND_STOCK_ADJUSTMENT_LED_COLOR = "ind.stock.show.adjustment.led.color";
	
	// 아래 코드는 사용 안 함
	/**
	 * 표시기 교체 시 교체 메시지 사용 여부 
	 */
	public static final String IND_ALTER_MESSAGE_ENABLED = "ind.alter.message.enabled";
	/**
	 * 표시기 연속 full 요청 blocking 시간 (초)
	 */
	public static final String IND_MPI_BLOCK_SEC_CONTINOUS_FULL_REQ = "ind.block.sec.continous.full.request";
	/**
	 * 기본 업무 기본 표시기 색상
	 */
	public static final String IND_DEFAULT_COLOR_DEFAULT = "ind.job.color.default";
	/**
	 * DAS 업무 기본 표시기 색상
	 */
	public static final String IND_DEFAULT_COLOR_DAS = "ind.job.color.das";
	/**
	 * DPS 업무 기본 표시기 색상
	 */
	public static final String IND_DEFAULT_COLOR_DPS = "ind.job.color.dps";
	/**
	 * 반품 업무 기본 표시기 색상
	 */
	public static final String IND_DEFAULT_COLOR_RTN = "ind.job.color.rtn";
	
}
