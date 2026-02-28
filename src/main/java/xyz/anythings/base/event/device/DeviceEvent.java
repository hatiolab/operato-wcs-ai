package xyz.anythings.base.event.device;

import xyz.anythings.base.event.IDeviceEvent;
import xyz.anythings.sys.event.model.SysEvent;

/**
 * 장비에 전달할 이벤트
 * 
 * @author shortstop
 */
public class DeviceEvent extends SysEvent implements IDeviceEvent {
	
	/**
	 * 명령어
	 */
	private String command;
	/**
	 * 전달할 메시지 
	 */
	private String message;
	/**
	 * 작업 스테이션 코드
	 */
	private String stageCd;
	/**
	 * 작업 유형 - DAS, RTN, DPS, ...
	 */
	private String jobType;
	/**
	 * 설비 유형 
	 */
	private String equipType;
	/**
	 * 설비 코드
	 */
	private String equipCd;
	/**
	 * 작업 스테이션
	 */
	private String stationCd;
	/**
	 * 사이드 코드
	 */
	private String sideCd;
	/**
	 * 장비 유형
	 */
	private String deviceType;
	/**
	 * 장비 ID
	 */
	private String deviceId;
	/**
	 * 전송할 데이터
	 */
	private Object sendData;
	
	public DeviceEvent() {
	}
	
	/**
	 * 생성자 1
	 * 
	 * @param domainId
	 */
	public DeviceEvent(Long domainId) {
		this.domainId = domainId;
	}
	
	/**
	 * 생성자 2
	 * 
	 * @param domainId
	 * @param deviceType
	 * @param stageCd
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @param sideCd
	 * @param jobType
	 * @param command
	 * @param message
	 */
	public DeviceEvent(Long domainId, String deviceType, String stageCd, String equipType, String equipCd, String stationCd, String sideCd, String jobType, String command, String message) {
		this.domainId = domainId;
		this.deviceType = deviceType;
		this.stageCd = stageCd;
		this.equipType = equipType;
		this.equipCd = equipCd;
		this.stationCd = stationCd;
		this.sideCd = sideCd;
		this.jobType = jobType;
		this.command = command;
		this.message = message;
	}
	
	/**
	 * 생성자 3
	 * 
	 * @param domainId
	 * @param deviceType
	 * @param deviceId
	 * @param jobType
	 * @param command
	 * @param message
	 */
	public DeviceEvent(Long domainId, String deviceType, String deviceId, String jobType, String command, String message) {
		this.domainId = domainId;
		this.deviceType = deviceType;
		this.deviceId = deviceId;
		this.jobType = jobType;
		this.command = command;
		this.message = message;
	}
	
	@Override
	public String getCommand() {
		return this.command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public Object getSendData() {
		return this.sendData;
	}
	
	public void setSendData(Object sendData) {
		this.sendData = sendData;
	}

	@Override
	public String getStageCd() {
		return this.stageCd;
	}
	
	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

	@Override
	public String getJobType() {
		return this.jobType;
	}
	
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	@Override
	public String getEquipType() {
		return this.equipType;
	}
	
	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}

	@Override
	public String getEquipCd() {
		return this.equipCd;
	}
	
	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}

	@Override
	public String getStationCd() {
		return this.stationCd;
	}
	
	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
	}
	
	@Override
	public String getSideCd() {
		return this.sideCd;
	}
	
	public void setSideCd(String sideCd) {
		this.sideCd = sideCd;
	}

	@Override
	public String getDeviceType() {
		return this.deviceType;
	}
	
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	@Override
	public String getDeviceId() {
		return this.deviceId;
	}
	
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
}
