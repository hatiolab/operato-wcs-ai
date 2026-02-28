package xyz.anythings.base.event;

/**
 * 장비 이벤트
 * 
 * @author shortstop
 */
public interface IDeviceEvent {
	
	/**
	 * 도메인 ID
	 * 
	 * @return
	 */
	public Long getDomainId();
		
	/**
	 * 명령어
	 * 
	 * @return
	 */
	public String getCommand();

	/**
	 * 전달할 메시지
	 * 
	 * @return
	 */
	public String getMessage();
	
	/**
	 * 전송할 데이터
	 * 
	 * @return
	 */
	public Object getSendData();
	
	/**
	 * 스테이지 코드
	 * 
	 * @return
	 */
	public String getStageCd();

	/**
	 * 작업 유형
	 * 
	 * @return
	 */
	public String getJobType();
	
	/**
	 * 설비 유형
	 * 
	 * @return
	 */
	public String getEquipType();
	
	/**
	 * 설비 코드
	 * 
	 * @return
	 */
	public String getEquipCd();
	
	/**
	 * 작업 스테이션 코드
	 * 
	 * @return
	 */
	public String getStationCd();
	
	/**
	 * 장비 사이드 코드
	 * 
	 * @return
	 */
	public String getSideCd();
	
	/**
	 * 장비 유형
	 * 
	 * @return
	 */
	public String getDeviceType();
	
	/**
	 * 장비 ID
	 * 
	 * @return
	 */
	public String getDeviceId();
	
}
