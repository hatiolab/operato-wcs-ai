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
 * 스테이지 범위 내 표시기 설정 값 조회 유틸리티
 * 설정 항목 리스트
 * 
 * ind.action.delay.before.on					표시기가 점등되기 전 지연되는 시간입니다. (100ms 단위)
 * ind.action.delay.cancel.button.off			표시기의 취소 버튼을 눌렀을 때 표시기가 소등되기까지의 지연 시간입니다. (100ms)
 * ind.action.send.off.ack.already.off			표시기가 이미 소등된 상태에서 소등 요청을 받았을 때 응답 메시지를 보낼 지 여부입니다.
 * ind.action.show.string.before.on				다음 작업을 점등하기 전에 표시될 문자열입니다.
 * ind.action.show.string.delay.before.on		점등 전에 문자를 표시할 시간입니다. (100ms 단위)
 * ind.action.status.report.interval			표시기의 상태 보고 주기입니다. (초 단위)
 * ind.alter.message.enabled					표시기 교체 시 교체 메시지 사용 여부
 * ind.block.sec.continous.full.request			표시기 연속 full 요청 blocking 시간 (초)
 * ind.job.color.das							DAS 작업에서 표시기 버튼의 기본 색상입니다.
 * ind.job.color.dps							DPS 작업에서 표시기 버튼의 기본 색상입니다.
 * ind.job.color.rotation.seq					표시기 버튼 색상의 로테이션 순서입니다.
 * ind.job.color.rtn							반품 작업에서 표시기 버튼의 기본 색상입니다.
 * ind.job.color.stocktaking					재고 실사 작업에서 표시기 버튼의 기본 색상입니다.
 * ind.job.segment.roles.on						작업 점등 시 각 세그먼트가 나타낼 정보입니다 - 첫번째/두번째/세번째 세그먼트 역할 -> R(릴레이 순서)/B(Box)/P(PCS)
 * ind.show.segment1.mapping.role				표시 세그먼트의 첫번째 숫자와 매핑되는 역할 (T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
 * ind.show.segment2.mapping.role				표시 세그먼트의 두번째 숫자와 매핑되는 역할
 * ind.show.segment3.mapping.role				표시 세그먼트의 세번째 숫자와 매핑되는 역할
 * ind.show.relay.max.no						최대 릴레이 번호 
 * ind.show.button.blink.interval				표시기의 버튼이 깜빡이는 주기입니다. (100ms 단위)
 * ind.show.button.on.mode						표시기의 버튼이 점등되는 방식입니다. (B: 깜빡임, S: 항상 켜짐)
 * ind.show.fullbox.button.blink				Full Box 처리 후, 표시기가 Full Box 상태로 점등됐을 때 버튼이 깜빡일지 여부입니다.
 * ind.show.view-type							표시기 자체적으로 표시 형식을 변경하는 모드
 * ind.show.number.alignment					표시기 숫자의 정렬 방향입니다. (R / L)
 * ind.led.blink.interval						LED 바가 깜박이는 주기입니다. (100ms 단위)
 * ind.led.brightness							LED 바의 밝기입니다. (1~10)
 * ind.led.on.mode								LED 바가 점등되는 방식입니다. (B: 깜빡임, S: 항상 켜짐)
 * ind.led.use.enabled							LED 바를 사용할지 여부입니다.
 * ind.buttons.enable							표시기 버튼 사용 여부
 * 
 * @author shortstop
 */
public class StageIndConfigUtil {

	/**
	 * 설정 프로파일 서비스
	 */
	public static IIndConfigProfileService CONFIG_SET_SVC;
	
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
	 * 스테이지 범위 내에 설정 내용을 키로 조회해서 리턴
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param key
	 * @param exceptionWhenEmptyValue
	 * @return
	 */
	public static String getConfigValue(Long domainId, String stageCd, String key, boolean exceptionWhenEmptyValue) {
		IIndConfigProfileService configSvc = getConfigSetService();
		
		// 1. 작업 유형에 따른 설정값 조회
		String value = configSvc.getStageConfigValue(domainId, stageCd, key);
		
		// 2. 설정값이 없다면 exceptionWhenEmptyValue에 따라 예외 처리
		if(exceptionWhenEmptyValue) {
			throw ThrowUtil.newJobConfigNotSet(key);
		}
		
		return value;
	}
	
	/**
	 * 스테이지 범위 내에 설정 내용을 키로 조회해서 리턴
	 *  
	 * @param domainId
	 * @param stageCd
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getConfigValue(Long domainId, String stageCd, String key, String defaultValue) {
		IIndConfigProfileService configSvc = getConfigSetService();
		
		// 1. 작업 유형에 따른 설정값 조회
		String value = configSvc.getStageConfigValue(domainId, stageCd, key);
		// 2. 조회 값이 없으면 기본값 조회
		return ValueUtil.isEmpty(value) ? defaultValue : value;
	}

//	/**
//	 * 표시기가 점등되기 전 지연되는 시간입니다. (100ms 단위)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static int getIndOnDelayTime(String stageCd) {
//		// ind.action.delay.before.on
//		String intVal = getConfigValue(stageCd, MwConfigConstants.IND_DELAY_BEFORE_ON, true);
//		return ValueUtil.toInteger(intVal);
//	}
//
//	/**
//	 * 표시기의 취소 버튼을 눌렀을 때 표시기가 소등되기까지의 지연 시간입니다. (100ms)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static int getIndOnDelayTimeCancelPushed(String stageCd) {
//		// ind.action.delay.cancel.button.off
//		String intVal = getConfigValue(stageCd, MwConfigConstants.IND_DELAY_CANCEL_BUTTON_OFF, true);
//		return ValueUtil.toInteger(intVal);
//	}
//
//	/**
//	 * 표시기가 이미 소등된 상태에서 소등 요청을 받았을 때 응답 메시지를 보낼 지 여부입니다.
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static boolean isNoackWhenAlreadyOffEnabled(String stageCd) {
//		// ind.action.send.off.ack.already.off
//		String boolVal = getConfigValue(stageCd, MwConfigConstants.IND_SEND_OFF_ACK_ALREADY_OFF, true);
//		return ValueUtil.toBoolean(boolVal);
//	}
//	
//	/**
//	 * 다음 작업을 점등하기 전에 표시될 문자열입니다.
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String getDisplayStringBeforeIndOn(String stageCd) {
//		// ind.action.show.string.before.on
//		return getConfigValue(stageCd, MwConfigConstants.IND_SHOW_STRING_BEFORE_ON, true);
//	}
//
//	/**
//	 * 점등 전에 문자를 표시할 시간입니다. (100ms 단위)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static int getDisplayIntervalBeforeIndOn(String stageCd) {
//		// ind.action.show.string.delay.before.on
//		String intVal = getConfigValue(stageCd, MwConfigConstants.IND_SHOW_STRING_DELAY_BEFORE_ON, true);
//		return ValueUtil.toInteger(intVal);
//	}
//
//	/**
//	 * 표시기의 상태 보고 주기입니다. (초 단위)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static int getIndStateReportInterval(String stageCd) {
//		// ind.action.status.report.interval
//		String intVal = getConfigValue(stageCd, MwConfigConstants.IND_HEALTH_PERIOD, true);
//		return ValueUtil.toInteger(intVal);
//	}
//
//	/**
//	 * 표시기 버튼 색상의 로테이션 순서입니다.
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String[] getIndButtonColorForRotation(String stageCd) {
//		// ind.job.color.rotation.seq
//		String strVal = getConfigValue(stageCd, MwConfigConstants.IND_COLOR_ROTATION_SEQ, true);
//		return strVal.split(MwConstants.COMMA);
//	}
//	
//	/**
//	 * 재고 실사 작업에서 표시기 버튼의 기본 색상입니다.
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String getIndColorForStocktaking(String stageCd) {
//		// ind.job.color.stocktaking
//		return getConfigValue(stageCd, MwConfigConstants.IND_DEFAULT_COLOR_STOCKTAKING, true);
//	}
//	
//	/**
//	 * 세그먼트 사용 개수
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static int getSegmentCount(String stageCd) {
//		return getSegmentRoles(stageCd).length;
//	}
//	
//	/**
//	 * 작업 점등 시 각 세그먼트가 나타낼 정보입니다 - 첫번째/두번째/세번째 세그먼트 역할 -> R(릴레이 순서)/B(Box)/P(PCS)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String[] getSegmentRoles(String stageCd) {
//		// ind.job.segment.roles.on
//		String strVal = getConfigValue(stageCd, MwConfigConstants.IND_SEGMENT_ROLE_ON, true);
//		return strVal.split(MwConstants.COMMA);
//	}
//	
//	/**
//	 * 표시 세그먼트의 첫 번째 숫자와 매핑되는 역할 (T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String getSegment1RoleForDisplay(String stageCd) {
//		// ind.show.segment1.mapping.role
//		return getConfigValue(stageCd, MwConfigConstants.IND_SEGMENT1_MAPPING_ROLE, true);
//	}
//
//	/**
//	 * 표시 세그먼트의 두 번째 숫자와 매핑되는 역할 (T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String getSegment2RoleForDisplay(String stageCd) {
//		// ind.show.segment2.mapping.role
//		return getConfigValue(stageCd, MwConfigConstants.IND_SEGMENT2_MAPPING_ROLE, true);
//	}
//
//	/**
//	 * 표시 세그먼트의 세 번째 숫자와 매핑되는 역할 (T: 총 수량, R: 총 남은 수량, F: 총 처리한 수량, P: 방금 전 처리한 낱개 수량, B: 방금 전 처리한 박스 수량)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String getSegment3RoleForDisplay(String stageCd) {
//		// ind.show.segment3.mapping.role
//		return getConfigValue(stageCd, MwConfigConstants.IND_SEGMENT3_MAPPING_ROLE, true);
//	}
//	
//	/**
//	 * 최대 릴레이 번호
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static int getMaxRelayNo(String stageCd) {
//		// ind.show.relay.max.no
//		String intVal = getConfigValue(stageCd, MwConfigConstants.IND_RELAY_MAX_NO, true);
//		return ValueUtil.isEmpty(intVal) ? -1 : ValueUtil.toInteger(intVal);
//	}
//	
//	/**
//	 * 표시기의 버튼이 깜빡이는 주기입니다. (100ms 단위)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static int getButtonBlinkInterval(String stageCd) {
//		// ind.show.button.blink.interval
//		String intVal = getConfigValue(stageCd, MwConfigConstants.IND_BUTTON_BLINK_INTERVAL, true);
//		return ValueUtil.toInteger(intVal);
//	}
//	
//	/**
//	 * 표시기의 버튼이 점등되는 방식입니다. (B: 깜빡임, S: 항상 켜짐)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String getButtonOnMode(String stageCd) {
//		// ind.show.button.on.mode
//		return getConfigValue(stageCd, MwConfigConstants.IND_BUTTON_ON_MODE, true);
//	}
//	
//	/**
//	 * 표시기 Full Box 터치시 버튼 깜빡임 여부
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static boolean isFullButtonBlink(String stageCd) {
//		// ind.show.fullbox.button.blink
//		String boolVal = getConfigValue(stageCd, MwConfigConstants.IND_FULLBOX_BUTTON_BLINK, true);
//		return ValueUtil.toBoolean(boolVal);
//	}
//	
//	/**
//	 * 표시기 자체적으로 표시 형식을 변경하는 모드
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String getShowViewType(String stageCd) {
//		// ind.show.view-type
//		return getConfigValue(stageCd, MwConfigConstants.IND_SHOW_VIEW_TYPE, true);
//	}
//	
//	/**
//	 * 표시기 숫자의 정렬 방향입니다. (R / L)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String getShowNumberAlignment(String stageCd) {
//		// ind.show.number.alignment
//		return getConfigValue(stageCd, MwConfigConstants.IND_NUMBER_ALIGNMENT, true);
//	}
//	
//	/**
//	 * LED 바가 깜박이는 주기입니다. (100ms 단위)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static int getLedBlinkInterval(String stageCd) {
//		// ind.led.blink.interval
//		String intVal = getConfigValue(stageCd, MwConfigConstants.IND_LED_BLINK_INTERVAL, true);
//		return ValueUtil.toInteger(intVal);
//	}
//
//	/**
//	 * LED 바의 밝기입니다. (1~10)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static int getLedBrightness(String stageCd) {
//		// ind.led.brightness
//		
//		String intVal = getConfigValue(stageCd, MwConfigConstants.IND_LED_BRIGHTNESS, true);
//		return ValueUtil.toInteger(intVal);
//	}
//	
//	/**
//	 * LED 바를 깜빡이게 할 지 여부
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static boolean isLedBlink(String stageCd) {
//		// ind.show.fullbox.button.blink
//		String boolVal = getConfigValue(stageCd, MwConfigConstants.IND_FULLBOX_BUTTON_BLINK, true);
//		return ValueUtil.toBoolean(boolVal);
//	}
//	
//	/**
//	 * LED 바를 사용할지 여부입니다.
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static boolean isUseLed(String stageCd) {
//		// ind.led.use.enabled			
//		String boolVal = getConfigValue(stageCd, MwConfigConstants.IND_LED_USE_ENABLED, true);
//		return ValueUtil.toBoolean(boolVal);
//	}
//
//	/**
//	 * LED 바를 사용할 호기 리스트 (콤마로 구분)
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String[] getRackOfUsingLed(String stageCd) {
//		// ind.led.use.enabled.racks
//		String strVal = getConfigValue(stageCd, MwConfigConstants.IND_LED_USE_ENABLED_RACKS, true);
//		return ValueUtil.isEmpty(strVal) ? null : strVal.split(MwConstants.COMMA);
//	}
//		
//	/**
//	 * 표시기 버튼 사용 여부
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static boolean isUseButton(String stageCd) {
//		// ind.buttons.enable
//		String boolVal = getConfigValue(stageCd, MwConfigConstants.IND_BUTTONS_ENABLE, true);
//		return ValueUtil.toBoolean(boolVal);
//	}
//	
//	/**
//	 * 표시기 최신 버전 정보 설정
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String getIndLatestReleaseVersion(String stageCd) {
//		// ind.latest.release.version
//		return getConfigValue(stageCd, MwConfigConstants.IND_LATEST_RELEASE_VERSION, true);
//	}
//	
//	/**
//	 * Gateway 최신 버전 정보 설정
//	 * 
//	 * @param batch
//	 * @return
//	 */
//	public static String getGwLatestReleaseVersion(String stageCd) {
//		// ind.gw.latest.release.version
//		return getConfigValue(stageCd, MwConfigConstants.GW_LATEST_RELEASE_VERSION, true);
//	}

	/**
	 * 스테이지 범위 내에서 다음 표시기 버튼 색상을 추출
	 *
	 * @param domainId
	 * @param stageCd
	 * @param currentColor
	 * @return
	 */
	public static String getNextIndColor(Long domainId, String stageCd, String currentColor) {
		String[] colorRotations = getIndColorRotations(domainId, stageCd);

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
	 * 스테이지 범위 내에서 표시기 색상 로테이션 값 배열
	 *
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static String[] getIndColorRotations(Long domainId, String stageCd) {
		return getIndColorRotationSeq(domainId, stageCd).split(SysConstants.COMMA);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 색상 로테이션 값 리턴
	 *
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static String getIndColorRotationSeq(Long domainId, String stageCd) {
		return getConfigValue(domainId, stageCd, GwConfigConstants.IND_COLOR_ROTATION_SEQ, BatchIndConfigUtil.DEFAULT_ROTATION_SEQ);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 점등을 위한 세그먼트 역할
	 *
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static String[] getIndSegmentRolesOn(Long domainId, String stageCd) {
		String value = getConfigValue(domainId, stageCd, GwConfigConstants.IND_SEGMENT_ROLE_ON, null);
		return ValueUtil.isEmpty(value) ? null : value.split(SysConstants.COMMA);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 세그먼트 사용 개수
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static int getIndSegmentCount(Long domainId, String stageCd) {
		return getIndSegmentRolesOn(domainId, stageCd).length;
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 넘버 표시 정렬
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static String getIndNumberAlignment(Long domainId, String stageCd) {
		return getConfigValue(domainId, stageCd, GwConfigConstants.IND_NUMBER_ALIGNMENT, BatchIndConfigUtil.IND_NUMBER_ALIGNMENT_LEFT);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 버튼 점등 모드 (B : Blink, S : Static)
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static String getIndButtonOnMode(Long domainId, String stageCd) {
		return getConfigValue(domainId, stageCd, GwConfigConstants.IND_BUTTON_ON_MODE, BatchIndConfigUtil.IND_BUTTON_MODE_BLINK);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 버튼 깜빡임 주기 (100ms)
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static Integer getIndButtonBlinkInterval(Long domainId, String stageCd) {
		String value = getConfigValue(domainId, stageCd, GwConfigConstants.IND_BUTTON_BLINK_INTERVAL, BatchIndConfigUtil.DEFAULT_IND_BUTTON_BLINK_INTERVAL);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 점등 전 표시 문자
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static String getIndShowStringBeforeOn(Long domainId, String stageCd) {
		return getConfigValue(domainId, stageCd, GwConfigConstants.IND_SHOW_STRING_BEFORE_ON, null);
	}
	
	/**
	 * 스테이지 범위 내에서 점등 전 문자 표시 시간 (100ms)
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static Integer getIndShowStringDelayBeforeOn(Long domainId, String stageCd) {
		String value = getConfigValue(domainId, stageCd, GwConfigConstants.IND_SHOW_STRING_DELAY_BEFORE_ON, BatchIndConfigUtil.DEFAULT_IND_SHOW_STRING_DELAY_BEFORE_ON);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 점등 전 딜레이 (1sec)
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static Integer getIndDelayBeforeOn(Long domainId, String stageCd) {
		String value = getConfigValue(domainId, stageCd, GwConfigConstants.IND_DELAY_BEFORE_ON, BatchIndConfigUtil.DEFAULT_IND_DELAY_BEFORE_ON);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 취소 버튼 터치시 소등까지 딜레이 (100ms)
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static Integer getIndDelayCancelButtonOff(Long domainId, String stageCd) {
		String value = getConfigValue(domainId, stageCd, GwConfigConstants.IND_DELAY_CANCEL_BUTTON_OFF, BatchIndConfigUtil.DEFAULT_IND_DELAY_CANCEL_BUTTON_OFF);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 풀 박스 터치시 버튼 깜빡임 여부 (true / false)
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static Boolean isIndFullboxButtonBlink(Long domainId, String stageCd) {
		String value = getConfigValue(domainId, stageCd, GwConfigConstants.IND_FULLBOX_BUTTON_BLINK, AnyConstants.FALSE_STRING);
		return ValueUtil.toBoolean(value);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기가 이미 소등된 상태에서 소등 요청을 받았을 때 ACK를 응답할 지 여부 (true / false)
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static Boolean isIndSendOffAckAlreadyOff(Long domainId, String stageCd) {
		String value = getConfigValue(domainId, stageCd, GwConfigConstants.IND_SEND_OFF_ACK_ALREADY_OFF, AnyConstants.FALSE_STRING);
		return ValueUtil.toBoolean(value);
	}
	
	/**
	 * 스테이지 범위 내에서 LED 점등 모드 (B: 깜빡임, S: 정지)
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static String getIndLedOnMode(Long domainId, String stageCd) {
		return getConfigValue(domainId, stageCd, GwConfigConstants.IND_LED_ON_MODE, BatchIndConfigUtil.IND_BUTTON_MODE_STOP);
	}
	
	/**
	 * 스테이지 범위 내에서 LED 깜빡임 주기
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static Integer getIndLedBlinkInterval(Long domainId, String stageCd) {
		String value = getConfigValue(domainId, stageCd, GwConfigConstants.IND_LED_BLINK_INTERVAL, BatchIndConfigUtil.DEFAULT_IND_LED_BLINK_INTERVAL);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 스테이지 범위 내에서 LED 바 밝기 정도 (1~10)
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static Integer getIndLedBrightness(Long domainId, String stageCd) {
		String value = getConfigValue(domainId, stageCd, GwConfigConstants.IND_LED_BRIGHTNESS, BatchIndConfigUtil.DEFAULT_IND_LED_BRIGHTNESS);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 View Type
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static String getIndDisplayViewType(Long domainId, String stageCd) {
		return getConfigValue(domainId, stageCd, GwConfigConstants.IND_SHOW_VIEW_TYPE, BatchIndConfigUtil.DEFAULT_IND_SHOW_VIEW_TYPE);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 상태 보고 주기 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static Integer getIndHealthPeriod(Long domainId, String stageCd) {
		String value = getConfigValue(domainId, stageCd, GwConfigConstants.IND_HEALTH_PERIOD, BatchIndConfigUtil.DEFAULT_IND_HEALTH_PERIOD);
		return ValueUtil.toInteger(value);
	}
	
	/**
	 * 스테이지 범위 내에서 표시기 점등 정보로 부터 indOnInfo에 relaySeq, boxQty, eaQty 등을 설정한다.
	 *
	 * @param domainId
	 * @param stageCd
	 * @param indOnReq
	 * @param indOnInfo
	 */
	public static void setIndOnQty(Long domainId, String stageCd, IndOnPickReq indOnReq, IndicatorOnInformation indOnInfo) {
		setIndOnQty(indOnInfo, domainId, stageCd, indOnReq.getInputSeq(), indOnReq.getBoxInQty(), indOnReq.getPickQty());
	}

	/**
	 * 스테이지 범위 내에서 표시기 점등 옵션으로 표시기 점등 정보 indOnInfo에 relaySeq, boxQty, eaQty 등을 설정한다.
	 *
	 * @param indOnInfo
	 * @param domainId
	 * @param stageCd
	 * @param relaySeq
	 * @param boxInQty
	 * @param pickQty
	 */
	public static void setIndOnQty(IndicatorOnInformation indOnInfo, Long domainId, String stageCd, Integer relaySeq, Integer boxInQty, Integer pickQty) {
		String[] onSegments = getIndSegmentRolesOn(domainId, stageCd);
		indOnInfo.setSegRole(onSegments);
		
		for(String segment : onSegments) {
			// 세그먼트가 릴레이 번호라면
			if(ValueUtil.isEqualIgnoreCase(segment, BatchIndConfigUtil.IND_SEGMENT_ROLE_RELAY_SEQ)) {
				indOnInfo.setOrgRelay(fitRelaySeq(domainId, stageCd, relaySeq));

			// 세그먼트가 박스 수량이라면
			} else if(ValueUtil.isEqualIgnoreCase(segment, BatchIndConfigUtil.IND_SEGMENT_ROLE_BOX)) {
				indOnInfo.setOrgBoxQty(0);
				indOnInfo.setOrgBoxinQty(boxInQty);

			// 세그먼트가 낱개 수량이라면
			} else if(ValueUtil.isEqualIgnoreCase(segment, BatchIndConfigUtil.IND_SEGMENT_ROLE_PCS)) {
				indOnInfo.setOrgEaQty(pickQty);
			}
		}
	}
		
	/**
	 * 세그먼트 개수에 따라 릴레이 시퀀스 자리수를 변경한다.
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param relaySeq
	 * @return
	 */
	public static Integer fitRelaySeq(Long domainId, String stageCd, Integer relaySeq) {
		String value = getConfigValue(domainId, stageCd, GwConfigConstants.IND_RELAY_MAX_NO, BatchIndConfigUtil.DEFAULT_IND_RELAY_MAX_NO);
		int relayMaxNo = ValueUtil.toInteger(value);
		return (relaySeq > relayMaxNo) ? 1 : relaySeq;
	}
	
	/**
	 * 스테이지 코드 범위 내에서 작업 유형별 표시기 표현 형식 - 0 : 기본, 1 : 박스 / 낱개, 2 : 누적수량 / 낱개
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public static String getIndViewType(Long domainId, String stageCd) {
		return getConfigValue(domainId, stageCd, GwConfigConstants.IND_SHOW_VIEW_TYPE, BatchIndConfigUtil.DEFAULT_IND_SHOW_VIEW_TYPE);		
	}
	
	/**
	 * Gateway 펌웨어 최신 릴리즈 버전
	 *
	 * @param gateway
	 * @return
	 */
	public static String getGwLatestReleaseVersion(Gateway gateway) {
		String version = gateway.getVersion();
		
		if(ValueUtil.isEmpty(version)) {
			return getConfigValue(gateway.getDomainId(), gateway.getStageCd(), GwConfigConstants.GW_LATEST_RELEASE_VERSION, BatchIndConfigUtil.DEFAULT_GW_LATEST_RELEASE_VERSION);
		} else {
			return version;
		}
	}

	/**
	 * Indicator 펌웨어 최신 릴리즈 버전
	 *
	 * @param gateway
	 * @return
	 */
	public static String getIndLatestReleaseVersion(Gateway gateway) {
		return getConfigValue(gateway.getDomainId(), gateway.getStageCd(), GwConfigConstants.IND_LATEST_RELEASE_VERSION, BatchIndConfigUtil.DEFAULT_IND_LATEST_RELEASE_VERSION);
	}
	
	/**
	 * 게이트웨이 부트시에 게이트웨이에 내려 줄 부트 설정을 생성하여 리턴 
	 * 
	 * @param gateway
	 * @return
	 */
	public static GatewayInitResIndConfig getGatewayBootConfig(Gateway gateway) {
		Long domainId = gateway.getDomainId();
		String stageCd = gateway.getStageCd();
		
		GatewayInitResIndConfig config = new GatewayInitResIndConfig();
		config.setAlignment(getIndNumberAlignment(domainId, stageCd));
		config.setSegRole(getIndSegmentRolesOn(domainId, stageCd));
		config.setBtnMode(getIndButtonOnMode(domainId, stageCd));
		config.setBtnIntvl(getIndButtonBlinkInterval(domainId, stageCd));
		config.setBfOnMsg(getIndShowStringBeforeOn(domainId, stageCd));
		config.setBfOnMsgT(getIndShowStringDelayBeforeOn(domainId, stageCd));
		config.setBfOnDelay(getIndDelayBeforeOn(domainId, stageCd));
		config.setCnclDelay(getIndDelayCancelButtonOff(domainId, stageCd));
		config.setBlinkIfFull(isIndFullboxButtonBlink(domainId, stageCd));
		config.setOffUseRes(isIndSendOffAckAlreadyOff(domainId, stageCd));
		config.setLedBarMode(getIndLedOnMode(domainId, stageCd));
		config.setLedBarIntvl(getIndLedBlinkInterval(domainId, stageCd));
		config.setLedBarBrtns(getIndLedBrightness(domainId, stageCd));
		config.setViewType(getIndViewType(domainId, stageCd));
		
		return config;
	}

}
