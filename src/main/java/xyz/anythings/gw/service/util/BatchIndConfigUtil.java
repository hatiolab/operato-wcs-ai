package xyz.anythings.gw.service.util;

import xyz.anythings.gw.GwConfigConstants;
import xyz.anythings.gw.entity.Gateway;
import xyz.anythings.gw.service.api.IIndConfigProfileService;
import xyz.anythings.gw.service.model.IndOnPickReq;
import xyz.anythings.gw.service.mq.model.GatewayInitResIndConfig;
import xyz.anythings.gw.service.mq.model.IndicatorOnInformation;
import xyz.anythings.sys.AnyConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * 작업 배치 범위 내 표시기 설정 값 조회 유틸리티
 * 표시기 설정 리스트
 *  - ind.action.delay.before.on				표시기가 점등되기 전 지연되는 시간입니다. (100ms 단위)
 *  - ind.action.delay.cancel.button.off		표시기의 취소 버튼을 눌렀을 때 표시기가 소등되기까지의 지연 시간입니다. (100ms)
 *  - ind.action.send.off.ack.already.off		표시기가 이미 소등된 상태에서 소등 요청을 받았을 때 응답 메시지를 보낼 지 여부입니다.
 *  - ind.action.show.string.before.on			다음 작업을 점등하기 전에 표시될 문자열입니다.
 *  - ind.action.show.string.delay.before.on	점등 전에 문자를 표시할 시간입니다. (100ms 단위)
 *  - ind.action.status.report.interval			표시기의 상태 보고 주기입니다. (초 단위)
 *  - ind.job.color.das							DAS 업무 기본 표시기 색상
 *  - ind.job.color.dps							DPS 업무 기본 표시기 색상
 *  - ind.job.color.rtn							반품 업무 기본 표시기 색상
 *  - ind.job.color.rotation.seq				표시기 버튼 색상의 로테이션 순서입니다.
 *  - ind.job.color.stocktaking					재고 실사 작업에서 표시기 버튼의 기본 색상입니다.
 *  - ind.job.segment.roles.on					작업 점등 시 각 세그먼트가 나타낼 정보입니다 - 첫번째/두번째/세번째 세그먼트 역할 -> R(릴레이 순서)/B(Box)/P(PCS)
 *  - ind.show.segment1.mapping.role			표시 세그먼트의 첫번째 숫자와 매핑되는 역할 (T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
 *  - ind.show.segment2.mapping.role			표시 세그먼트의 두번째 숫자와 매핑되는 역할
 *  - ind.show.segment3.mapping.role			표시 세그먼트의 세번째 숫자와 매핑되는 역할
 *  - ind.show.relay.max.no						최대 릴레이 번호 
 *  - ind.show.button.blink.interval			표시기의 버튼이 깜빡이는 주기입니다. (100ms 단위)
 *  - ind.show.button.on.mode					표시기의 버튼이 점등되는 방식입니다. (B: 깜빡임, S: 항상 켜짐)
 *  - ind.show.fullbox.button.blink				Full Box 처리 후, 표시기가 Full Box 상태로 점등됐을 때 버튼이 깜빡일지 여부입니다.
 *  - ind.show.view-type						표시기 자체적으로 표시 형식을 변경하는 모드
 *  - ind.show.number.alignment					표시기 숫자의 정렬 방향입니다. (R / L)
 *  - ind.led.blink.interval					LED 바가 깜박이는 주기입니다. (100ms 단위)
 *  - ind.led.brightness						LED 바의 밝기입니다. (1~10)
 *  - ind.led.on.mode							LED 바가 점등되는 방식입니다. (B: 깜빡임, S: 항상 켜짐)
 *  - ind.led.use.enabled						LED 바를 사용할지 여부입니다.
 *  - ind.led.use.enabled.racks					LED 바를 사용할 호기 리스트 (콤마로 구분)
 *  - ind.buttons.enable						표시기 버튼 사용 여부
 *  - ind.latest.release.version				표시기 최신 버전 정보 설정
 *  - ind.gw.latest.release.version				Gateway 최신 버전 정보 설정
 * 
 * @author shortstop
 */
public class BatchIndConfigUtil {
	
	/**
	 * 설정 프로파일 서비스
	 */
	public static IIndConfigProfileService CONFIG_SET_SVC;
	
	/**
	 * 기본 색상 로테이션 순서
	 */
	public static String DEFAULT_ROTATION_SEQ = "R" + SysConstants.COMMA + "B" + SysConstants.COMMA + "G" + SysConstants.COMMA + "Y";
	
	/**
	 * 표시기 수량 표시 단위 - 박스 & 낱개
	 */
	public static final String IND_DISPLAY_QTY_UNIT_BOX = "B";
	/**
	 * 표시기 수량 표시 단위 - 낱개
	 */
	public static final String IND_DISPLAY_QTY_UNIT_PCS = "P";
	
	/**
	 * 표시기 세그먼트 : R (릴레이 번호) 
	 */
	public static final String IND_SEGMENT_ROLE_RELAY_SEQ = "R";
	/**
	 * 표시기 세그먼트 : B (Box) 
	 */
	public static final String IND_SEGMENT_ROLE_BOX = "B";
	/**
	 * 표시기 세그먼트 : P (PCS) 
	 */
	public static final String IND_SEGMENT_ROLE_PCS = "P";
	/**
	 * 표시기 세그먼트 : S (문자열 표시) 
	 */
	public static final String IND_SEGMENT_ROLE_STR = "S";
	/**
	 * 표시기 점등을 위한 세그먼트 기본 값
	 */
	public static final String[] DEFAULT_SEGMENT_ROLES_ON = {  IND_SEGMENT_ROLE_PCS  };
	
	/**
	 * 표시기 숫자 정렬 방식 (L:Left, R:Rear)
	 */
	public static final String IND_NUMBER_ALIGNMENT_LEFT = "L";
	/**
	 * 표시기 숫자 정렬 방식 (L:Left, R:Rear)
	 */
	public static final String IND_NUMBER_ALIGNMENT_RIGHT = "R";
	
	/**
	 * 표시기 버튼 점등 모드 (B:깜빡임, S:정지)
	 */
	public static final String IND_BUTTON_MODE_BLINK = "B";
	/**
	 * 표시기 버튼 점등 모드 (B:깜빡임, S:정지)
	 */
	public static final String IND_BUTTON_MODE_STOP = "S";
	
	/**
	 * 표시기 버튼 깜빡임 주기 기본값 (300ms)
	 */
	public static final String DEFAULT_IND_BUTTON_BLINK_INTERVAL = "300";
	/**
	 * 점등 전 문자 표시 시간 기본값 (100ms)
	 */
	public static final String DEFAULT_IND_SHOW_STRING_DELAY_BEFORE_ON = "100";
	/**
	 * 표시기 점등 전 딜레이 기본값 (1sec)
	 */
	public static final String DEFAULT_IND_DELAY_BEFORE_ON = "1";
	/**
	 * 표시기 취소 버튼 터치시 소등까지 딜레이 기본값 (100ms)
	 */
	public static final String DEFAULT_IND_DELAY_CANCEL_BUTTON_OFF = "100";
	/**
	 *  LED 깜빡임 주기 기본값 (100ms)
	 */
	public static final String DEFAULT_IND_LED_BLINK_INTERVAL = "100";
	/**
	 * LED 바 밝기 정도 (1~10) 기본값 1
	 */
	public static final String DEFAULT_IND_LED_BRIGHTNESS = "1";
	/**
	 * 표시기 View Type 기본값 0
	 */
	public static final String DEFAULT_IND_SHOW_VIEW_TYPE = "0";
	/**
	 * 표시기 상태 보고 주기 기본값 300
	 */
	public static final String DEFAULT_IND_HEALTH_PERIOD = "300";
	/**
	 * 릴레이 시퀀스 최재 자리수 기본값 99 
	 */
	public static final String DEFAULT_IND_RELAY_MAX_NO = "99";
	/**
	 * 게이트웨이 펌웨어 최신 릴리즈 버전 기본값
	 */
	public static final String DEFAULT_GW_LATEST_RELEASE_VERSION = "1.0.0";
	/**
	 * 표시기 펌웨어 최신 릴리즈 버전 기본값
	 */
	public static final String DEFAULT_IND_LATEST_RELEASE_VERSION = "1.0.0";
	/**
	 * 기본 업무 표시기 색상
	 */
	public static final String DEFAULT_JOB_COLOR = "R";
	
	/**
	 * DAS/RTN 분류 처리 후에 표시할 내용 중 사용 안 함 표시 (해당 세그먼트에 표시되는 내용이 없음)
	 * (N: 사용 안 함, T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
	 */
	public static final String IND_DISP_SEGMENT_MAPPING_ROLE_NONE = "N";
	/**
	 * DAS/RTN 분류 처리 후에 표시할 내용 중 분류 처리 정보에 대한 총 주문 수량 - T
	 * (N: 사용 안 함, T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
	 */
	public static final String IND_DISP_SEGMENT_MAPPING_ROLE_TOTAL_DONE = "T";
	/**
	 * DAS/RTN 분류 처리 후에 표시할 내용 중 분류 처리 정보에 대한 총 분류할 (남은) 수량 - R
	 * (N: 사용 안 함, T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
	 */
	public static final String IND_DISP_SEGMENT_MAPPING_ROLE_TOTAL_REMAIN = "R";
	/**
	 * DAS/RTN 분류 처리 후에 표시할 내용 중 분류 처리 정보에 대한 총 처리한 수량 - F
	 * (N: 사용 안 함, T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
	 */
	public static final String IND_DISP_SEGMENT_MAPPING_ROLE_TOTAL_FINISHED = "F";
	/**
	 * DAS/RTN 분류 처리 후에 표시할 내용 중 분류 처리 정보에 대한 방금 전 처리한 낱개 수량 - P
	 * (N: 사용 안 함, T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
	 */
	public static final String IND_DISP_SEGMENT_MAPPING_ROLE_PREVIOUS_PICKED_PCS = "P";
	/**
	 * DAS/RTN 분류 처리 후에 표시할 내용 중 분류 처리 정보에 대한 방금 전 처리한 박스 수량 - B
	 * (N: 사용 안 함, T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
	 */
	public static final String IND_DISP_SEGMENT_MAPPING_ROLE_PREVIOUS_PICKED_BOX = "B";
	
	/**
	 * 설정 프로파일 서비스 리턴
	 * 
	 * @return
	 */
	public static IIndConfigProfileService getConfigSetService() {
		if(CONFIG_SET_SVC == null) {
			CONFIG_SET_SVC = BeanUtil.get(IIndConfigProfileService.class);
		}
		
		return CONFIG_SET_SVC;
	}
	
	/**
	 * 작업 배치 범위 내에 설정 내용을 키로 조회해서 리턴
	 *  
	 * @param batch
	 * @param key
	 * @param exceptionWhenEmptyValue
	 * @return
	 */
	public static String getConfigValue(String batchId, String key, boolean exceptionWhenEmptyValue) {
		IIndConfigProfileService configSvc = getConfigSetService();
		
		// 1. 작업 유형에 따른 설정값 조회
		String value = configSvc.getConfigValue(batchId, key);
		
		// 2. 설정값이 없다면 exceptionWhenEmptyValue에 따라 예외 처리
		if(ValueUtil.isEmpty(value) && exceptionWhenEmptyValue) {
			throw ThrowUtil.newJobConfigNotSet(key);
		}
		
		return value;
	}
	
	/**
	 * 작업 배치 범위 내에 설정 내용을 키로 조회해서 리턴
	 *  
	 * @param batch
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getConfigValue(String batchId, String key, String defaultValue) {
		IIndConfigProfileService configSvc = getConfigSetService();
		
		// 1. 작업 유형에 따른 설정값 조회
		String value = configSvc.getConfigValue(batchId, key);
		// 2. 조회 값이 없으면 기본값 조회
		return ValueUtil.isEmpty(value) ? defaultValue : value;
	}

	/**
	 * 표시기 버튼 사용 여부
	 * 
	 * @param batch
	 * @return
	 */
	public static boolean isUseButton(String batchId) {
		String boolVal = getConfigValue(batchId, GwConfigConstants.IND_BUTTONS_ENABLE, true);
		return ValueUtil.toBoolean(boolVal);
	}
	
	/**
	 * 기본 작업 표시기 색상 리턴
	 * 
	 * @param batchId
	 * @return
	 */
	public static String getDefaultJobColor(String batchId) {
		return getConfigValue(batchId, GwConfigConstants.IND_DEFAULT_COLOR_DEFAULT, DEFAULT_JOB_COLOR);
	}
	
	/**
	 * DAS 작업 표시기 색상 리턴
	 * 
	 * @param batchId
	 * @return
	 */
	public static String getDasJobColor(String batchId) {
		String color = getConfigValue(batchId, GwConfigConstants.IND_DEFAULT_COLOR_DAS, null);
		return ValueUtil.isNotEmpty(color) ? color : getDefaultJobColor(batchId);
	}
	
	/**
	 * DPS 작업 표시기 색상 리턴
	 * 
	 * @param batchId
	 * @return
	 */
	public static String getDpsJobColor(String batchId) {
		String color = getConfigValue(batchId, GwConfigConstants.IND_DEFAULT_COLOR_DPS, null);
		return ValueUtil.isNotEmpty(color) ? color : getDefaultJobColor(batchId);
	}
	
	/**
	 * 반품 작업 표시기 색상 리턴
	 * 
	 * @param batchId
	 * @return
	 */
	public static String getRtnJobColor(String batchId) {
		String color = getConfigValue(batchId, GwConfigConstants.IND_DEFAULT_COLOR_RTN, null);
		return ValueUtil.isNotEmpty(color) ? color : getDefaultJobColor(batchId);
	}

	/**
	 * 작업 배치 범위 내에서 다음 표시기 버튼 색상을 추출
	 *
	 * @param batch
	 * @param currentColor
	 * @return
	 */
	public static String getNextIndColor(String batchId, String currentColor) {
		String[] colorRotations = getIndColorRotations(batchId);

		if(ValueUtil.isEmpty(currentColor)) {
			return colorRotations[0];
		}

		int currentIdx = 0;
		for(int i = 0 ; i < colorRotations.length ; i++) {
			if(ValueUtil.isEqualIgnoreCase(colorRotations[i], currentColor)) {
				currentIdx = i;
				break;
			}
		}

		currentIdx = (currentIdx == (colorRotations.length - 1)) ? 0 : (currentIdx + 1);
		return colorRotations[currentIdx];
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 색상 로테이션 값 배열
	 *
	 * @param batchId
	 * @return
	 */
	public static String[] getIndColorRotations(String batchId) {
		return getIndColorRotationSeq(batchId).split(SysConstants.COMMA);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 색상 로테이션 값 리턴
	 *
	 * @param batchId
	 * @return
	 */
	public static String getIndColorRotationSeq(String batchId) {
		return getConfigValue(batchId, GwConfigConstants.IND_COLOR_ROTATION_SEQ, DEFAULT_ROTATION_SEQ);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 점등을 위한 세그먼트 역할
	 *
	 * @param batchId
	 * @return
	 */
	public static String[] getIndSegmentRolesOn(String batchId) {
		String value = getConfigValue(batchId, GwConfigConstants.IND_SEGMENT_ROLE_ON, true);
		return ValueUtil.isEmpty(value) ? null : value.split(SysConstants.COMMA);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 세그먼트 사용 개수
	 * 
	 * @param batchId
	 * @return
	 */
	public static int getIndSegmentCount(String batchId) {
		return getIndSegmentRolesOn(batchId).length;
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 넘버 표시 정렬
	 * 
	 * @param batchId
	 * @return
	 */
	public static String getIndNumberAlignment(String batchId) {
		return getConfigValue(batchId, GwConfigConstants.IND_NUMBER_ALIGNMENT, IND_NUMBER_ALIGNMENT_LEFT);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 버튼 점등 모드 (B : Blink, S : Static)
	 * 
	 * @param batchId
	 * @return
	 */
	public static String getIndButtonOnMode(String batchId) {
		return getConfigValue(batchId, GwConfigConstants.IND_BUTTON_ON_MODE, IND_BUTTON_MODE_BLINK);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 버튼 깜빡임 주기 (300ms)
	 * 
	 * @param batchId
	 * @return
	 */
	public static Integer getIndButtonBlinkInterval(String batchId) {
		String value = getConfigValue(batchId, GwConfigConstants.IND_BUTTON_BLINK_INTERVAL, DEFAULT_IND_BUTTON_BLINK_INTERVAL);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 점등 전 표시 문자
	 * 
	 * @param batchId
	 * @return
	 */
	public static String getIndShowStringBeforeOn(String batchId) {
		return getConfigValue(batchId, GwConfigConstants.IND_SHOW_STRING_BEFORE_ON, null);
	}
	
	/**
	 * 작업 배치 범위 내에서 점등 전 문자 표시 시간 (100ms)
	 * 
	 * @param batchId
	 * @return
	 */
	public static Integer getIndShowStringDelayBeforeOn(String batchId) {
		String value = getConfigValue(batchId, GwConfigConstants.IND_SHOW_STRING_DELAY_BEFORE_ON, DEFAULT_IND_SHOW_STRING_DELAY_BEFORE_ON);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 점등 전 딜레이 (1sec)
	 * 
	 * @param batchId
	 * @return
	 */
	public static Integer getIndDelayBeforeOn(String batchId) {
		String value = getConfigValue(batchId, GwConfigConstants.IND_DELAY_BEFORE_ON, DEFAULT_IND_DELAY_BEFORE_ON);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 취소 버튼 터치시 소등까지 딜레이 (100ms)
	 * 
	 * @param batchId
	 * @return
	 */
	public static Integer getIndDelayCancelButtonOff(String batchId) {
		String value = getConfigValue(batchId, GwConfigConstants.IND_DELAY_CANCEL_BUTTON_OFF, DEFAULT_IND_DELAY_CANCEL_BUTTON_OFF);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 풀 박스 터치시 버튼 깜빡임 여부 (true / false)
	 * 
	 * @param batchId
	 * @return
	 */
	public static Boolean isIndFullboxButtonBlink(String batchId) {
		String value = getConfigValue(batchId, GwConfigConstants.IND_FULLBOX_BUTTON_BLINK, AnyConstants.FALSE_STRING);
		return ValueUtil.toBoolean(value);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기가 이미 소등된 상태에서 소등 요청을 받았을 때 ACK를 응답할 지 여부 (true / false)
	 * 
	 * @param batchId
	 * @return
	 */
	public static Boolean isIndSendOffAckAlreadyOff(String batchId) {
		String value = getConfigValue(batchId, GwConfigConstants.IND_SEND_OFF_ACK_ALREADY_OFF, AnyConstants.FALSE_STRING);
		return ValueUtil.toBoolean(value);
	}
	
	/**
	 * 작업 배치 범위 내에서 LED 점등 모드 (B: 깜빡임, S: 정지)
	 * 
	 * @param batchId
	 * @return
	 */
	public static String getIndLedOnMode(String batchId) {
		return getConfigValue(batchId, GwConfigConstants.IND_LED_ON_MODE, IND_BUTTON_MODE_STOP);
	}
	
	/**
	 * 작업 배치 범위 내에서 LED 깜빡임 주기
	 * 
	 * @param batchId
	 * @return
	 */
	public static Integer getIndLedBlinkInterval(String batchId) {
		String value = getConfigValue(batchId, GwConfigConstants.IND_LED_BLINK_INTERVAL, DEFAULT_IND_LED_BLINK_INTERVAL);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 작업 배치 범위 내에서 LED 바 밝기 정도 (1~10)
	 * 
	 * @param batchId
	 * @return
	 */
	public static Integer getIndLedBrightness(String batchId) {
		String value = getConfigValue(batchId, GwConfigConstants.IND_LED_BRIGHTNESS, DEFAULT_IND_LED_BRIGHTNESS);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 View Type
	 * 
	 * @param batchId
	 * @return
	 */
	public static String getIndDisplayViewType(String batchId) {
		return getConfigValue(batchId, GwConfigConstants.IND_SHOW_VIEW_TYPE, DEFAULT_IND_SHOW_VIEW_TYPE);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 상태 보고 주기 
	 * 
	 * @param batchId
	 * @return
	 */
	public static Integer getIndHealthPeriod(String batchId) {
		String value = getConfigValue(batchId, GwConfigConstants.IND_HEALTH_PERIOD, DEFAULT_IND_HEALTH_PERIOD);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 점등 정보로 부터 indOnInfo에 relaySeq, boxQty, eaQty 등을 설정한다.
	 *
	 * @param batchId
	 * @param indOnReq
	 * @param indOnInfo
	 */
	public static void setIndOnQty(String batchId, IndOnPickReq indOnReq, IndicatorOnInformation indOnInfo) {
		setIndOnQty(indOnInfo, batchId, indOnReq.getInputSeq(), indOnReq.getBoxInQty(), indOnReq.getPickQty());
	}

	/**
	 * 작업 배치 범위 내에서 표시기 점등 옵션으로 표시기 점등 정보 indOnInfo에 relaySeq, boxQty, eaQty 등을 설정한다.
	 *
	 * @param indOnInfo
	 * @param batchId
	 * @param relaySeq
	 * @param boxInQty
	 * @param pickQty
	 */
	public static void setIndOnQty(IndicatorOnInformation indOnInfo, String batchId, Integer relaySeq, Integer boxInQty, Integer pickQty) {
		String[] onSegments = getIndSegmentRolesOn(batchId);
		indOnInfo.setSegRole(onSegments);
		
		for(String segment : onSegments) {
			// 세그먼트가 릴레이 번호라면
			if(ValueUtil.isEqualIgnoreCase(segment, IND_SEGMENT_ROLE_RELAY_SEQ)) {
				indOnInfo.setOrgRelay(fitRelaySeq(batchId, relaySeq));

			// 세그먼트가 박스 수량이라면
			} else if(ValueUtil.isEqualIgnoreCase(segment, IND_SEGMENT_ROLE_BOX)) {
				indOnInfo.setOrgBoxQty(0);
				indOnInfo.setOrgBoxinQty(boxInQty);

			// 세그먼트가 낱개 수량이라면
			} else if(ValueUtil.isEqualIgnoreCase(segment, IND_SEGMENT_ROLE_PCS)) {
				indOnInfo.setOrgEaQty(pickQty);
			}
		}
	}
	
	/**
	 * 릴레이 시퀀스 자리수가 최대 릴레이 값이 넘었다면 1로 리셋하고 그렇지 않으면 릴레이 값을 리턴한다.
	 * 
	 * @param batchId
	 * @param relaySeq
	 * @return
	 */
	public static Integer fitRelaySeq(String batchId, Integer relaySeq) {
		String value = getConfigValue(batchId, GwConfigConstants.IND_RELAY_MAX_NO, DEFAULT_IND_RELAY_MAX_NO);
		int relayMaxNo = ValueUtil.toInteger(value);
		return (relaySeq > relayMaxNo) ? 1 : relaySeq;
	}
	
	/**
	 * 작업 배치 범위 내에서 작업 유형별 표시기 표현 형식 - 0 : 기본, 1 : 박스 / 낱개, 2 : 누적수량 / 낱개
	 * 
	 * @param batchId
	 * @return
	 */
	public static String getIndViewType(String batchId) {
		return getConfigValue(batchId, GwConfigConstants.IND_SHOW_VIEW_TYPE, DEFAULT_IND_SHOW_VIEW_TYPE);
	}
	
	/**
	 * 작업 배치 범위 내에서 라우터 펌웨어 최신 릴리즈 버전
	 *
	 * @param batchId
	 * @param gateway
	 * @return
	 */
	public static String getGwLatestReleaseVersion(String batchId, Gateway gateway) {
		String version = gateway.getVersion();
		
		if(ValueUtil.isEmpty(version)) {
			return getConfigValue(batchId, GwConfigConstants.GW_LATEST_RELEASE_VERSION, DEFAULT_GW_LATEST_RELEASE_VERSION);
		} else {
			return version;
		}
	}

	/**
	 * 작업 배치 범위 내에서 표시기 펌웨어 최신 릴리즈 버전
	 *
	 * @param batchId
	 * @return
	 */
	public static String getIndLatestReleaseVersion(String batchId) {
		return getConfigValue(batchId, GwConfigConstants.IND_LATEST_RELEASE_VERSION, DEFAULT_IND_LATEST_RELEASE_VERSION);
	}
	
	/**
	 * 작업 배치 범위 내에서 표시기 점등 정보 indOnInfo에 relaySeq, boxQty, eaQty 등을 설정한다.
	 *
	 * @param indOnInfo
	 * @param relaySeq
	 * @param boxQty
	 * @param eaQty
	 */
	public static void setIndOnQty(IndicatorOnInformation indOnInfo, Integer relaySeq, Integer boxQty, Integer eaQty) {
		indOnInfo.setOrgRelay(relaySeq);
		indOnInfo.setOrgBoxQty(boxQty);
		indOnInfo.setOrgEaQty(eaQty);
	}

	/**
	 * 게이트웨이 부트시에 게이트웨이에 내려 줄 부트 설정을 생성하여 리턴 
	 * 
	 * @param batchId
	 * @param gateway
	 * @return
	 */
	public static GatewayInitResIndConfig getGatewayBootConfig(String batchId, Gateway gateway) {
		GatewayInitResIndConfig config = new GatewayInitResIndConfig();
		config.setAlignment(getIndNumberAlignment(batchId));
		config.setSegRole(getIndSegmentRolesOn(batchId));
		config.setBtnMode(getIndButtonOnMode(batchId));
		config.setBtnIntvl(getIndButtonBlinkInterval(batchId));
		config.setBfOnMsg(getIndShowStringBeforeOn(batchId));
		config.setBfOnMsgT(getIndShowStringDelayBeforeOn(batchId));
		config.setBfOnDelay(getIndDelayBeforeOn(batchId));
		config.setCnclDelay(getIndDelayCancelButtonOff(batchId));
		config.setBlinkIfFull(isIndFullboxButtonBlink(batchId));
		config.setOffUseRes(isIndSendOffAckAlreadyOff(batchId));
		config.setLedBarMode(getIndLedOnMode(batchId));
		config.setLedBarIntvl(getIndLedBlinkInterval(batchId));
		config.setLedBarBrtns(getIndLedBrightness(batchId));
		config.setViewType(getIndViewType(batchId));
		return config;
	}

}
