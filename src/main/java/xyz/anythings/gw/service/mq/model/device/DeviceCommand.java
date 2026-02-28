package xyz.anythings.gw.service.mq.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import xyz.anythings.gw.GwConstants;
import xyz.anythings.gw.service.mq.model.Action;
import xyz.anythings.gw.service.mq.model.IMessageBody;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.FormatUtil;

/**
 * 장비 관련 미들웨어 이벤트 
 * 
 * @author shortstop
 */
@JsonTypeName(Action.Values.DeviceCommand)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DeviceCommand implements IMessageBody {
	
	/**
	 * 목적지 타입 - kiosk
	 */
	public static final String DEST_KIOSK = "kiosk";
	/**
	 * 목적지 타입 - mobile
	 */
	public static final String DEST_MOBILE = "mobile";
	
	/**
	 * 장비 타입 - kiosk
	 */
	public static final String EQUIP_KIOSK = "kiosk";
	/**
	 * 장비 타입 - tablet
	 */	
	public static final String EQUIP_TABLET = "tablet";
	/**
	 * 장비 타입 - pda
	 */	
	public static final String EQUIP_PDA = "pda";
	
	/**
	 * 새로고침  
	 */
	public static final String COMMAND_REFRESH = "refresh";
	/**
	 * 새로고침 (리스트 새로고침)  
	 */
	public static final String COMMAND_REFRESH_LIST = "refresh-list";
	/**
	 * 새로고침 (리스트 상세 새로고침)
	 */
	public static final String COMMAND_REFRESH_DETAILS = "refresh-details";
	/**
	 * 명령 유형 (설정 데이터 전달) - setting
	 */
	public static final String COMMAND_SETTING = "setting";
	/**
	 * 명령 유형 (장비 업데이트 명령) - update
	 */
	public static final String COMMAND_UPDATE = "update";
	/**
	 * 명령 유형 (데이터 전달) - data
	 */
	public static final String COMMAND_DATA = "data";
	/**
	 * 명령 유형 (알림 메시지) - info
	 */
	public static final String COMMAND_INFO = "info";
	/**
	 * 명령 유형 (에러 메시지) - error
	 */
	public static final String COMMAND_ERROR = "error";
	
	/**
	 * 액션
	 */
	private String action = Action.Values.DeviceCommand;
	/**
	 * 장비 유형 
	 */
	protected String deviceType;
	/**
	 * 설비 타입 
	 */
	protected String equipType;
	/**
	 * 설비 코드
	 */
	protected String equipCd;
	/**
	 * 작업 스테이션
	 */
	protected String stationCd;
	/**
	 * 호기 사이드 코드
	 */
	protected String sideCd;
	/**
	 * 장비 패스
	 */
	protected String equipPath;
	/**
	 * 작업 타입 - DAS, DPS, RTN 
	 */
	protected String jobType;
	/**
	 * 커맨드 
	 */
	protected String command;
	/**
	 * Message
	 */
	protected String message;
	
	/**
	 * 장비 관련 미들웨어 이벤트 생성자 1
	 */
	public DeviceCommand() {
	}
	
	/**
	 * 장비 관련 미들웨어 이벤트 생성자 2
	 * 
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @param sideCd
	 * @param jobType
	 * @param command
	 * @param message
	 */
	public DeviceCommand(String deviceType, String equipType, String equipCd, String stationCd, String sideCd, String jobType, String command, String message) {
		this.deviceType = deviceType;
		this.equipType = equipType;
		this.equipCd = equipCd;
		this.stationCd = stationCd;
		this.sideCd = sideCd;
		this.jobType = jobType;
		this.command = command;
		this.message = message;
	}
	
	@Override
	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public String getAction() {
		return this.action;
	}

	public String getEquipType() {
		return equipType;
	}

	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}
	
	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}
	
	public String getStationCd() {
		return stationCd;
	}

	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
	}
	
	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getEquipPath() {
		if(ValueUtil.isEmpty(this.equipPath)) {
			this.equipPath = this.deviceType + SysConstants.DASH + this.equipType + SysConstants.DASH + this.equipCd;
			
			if(ValueUtil.isNotEmpty(this.stationCd)) {
				this.equipPath = this.equipPath + SysConstants.DASH + this.stationCd;
			} else {
				this.equipPath = this.equipPath + SysConstants.DASH + GwConstants.ALL_STRING;
			}
			
			if(ValueUtil.isNotEmpty(this.sideCd)) {
				this.equipPath = this.equipPath + SysConstants.DASH + this.sideCd;
			} else {
				this.equipPath = this.equipPath + SysConstants.DASH + GwConstants.ALL_STRING;
			}
		}
		
		return this.equipPath;
	}
	
	public void setEquipPath(String equipPath) {
		this.equipPath = equipPath;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String toString() {
		return FormatUtil.toUnderScoreJsonString(this);
	}
	
}
