package xyz.anythings.gw.service.mq.model.device;

import java.util.Map;

import xyz.elidom.util.FormatUtil;

/**
 * 장비 이벤트를 생성하는 팩토리 
 * 
 * @author shortstop
 */
public class DeviceCommandFactory {

	/**
	 * 장비 통신을 위한 이벤트 모델 생성 
	 * 
	 * @param eventMessage
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static DeviceCommand createDeviceCommand(String eventMessage) {
		Map data = FormatUtil.jsonToObject(eventMessage, Map.class);
		String deviceType = (String)data.get("device_type");
		String equipType = (String)data.get("equip_type");
		String equipCd = (String)data.get("equip_cd");
		String stationCd = data.containsKey("station_cd") ? (String)data.get("station_cd") : null;
		String sideCd = data.containsKey("side_cd") ? (String)data.get("side_cd") : null;
		String jobType = (data.containsKey("job_type")) ? (String)data.get("job_type") : null;
		String message = (data.containsKey("message")) ? (String)data.get("message") : null;
		String command = data.containsKey("command") ? (String)data.get("command") : null;
		return new DeviceCommand(deviceType, equipType, equipCd, stationCd, sideCd, jobType, command, message);
	}
	
	/**
	 * 장비 통신을 위한 이벤트 모델 생성 
	 * 
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @param sideCd
	 * @param jobType
	 * @param command
	 * @param messageObj
	 * @return
	 */
	public static DeviceCommand createDeviceCommand(String deviceType, String equipType, String equipCd, String stationCd, String sideCd, String jobType, String command, Object messageObj) {
		String message = (messageObj instanceof String) ? messageObj.toString() : FormatUtil.toUnderScoreJsonString(messageObj);
		return new DeviceCommand(deviceType, equipType, equipCd, stationCd, sideCd, jobType, command, message);
	}
	
	/**
	 * 장비 통신을 위한 이벤트 모델 생성
	 * 
	 * @param deviceType
	 * @param equipType
	 * @param equipCd
	 * @param stationCd
	 * @param sideCd
	 * @param jobType
	 * @param messageObj
	 * @return
	 */
	public static DeviceCommand createDeviceRefreshCommand(String deviceType, String equipType, String equipCd, String stationCd, String sideCd, String jobType, Object messageObj) {
		return createDeviceCommand(deviceType, equipType, equipCd, stationCd, sideCd, jobType, DeviceCommand.COMMAND_REFRESH, messageObj);
	}
	
}
