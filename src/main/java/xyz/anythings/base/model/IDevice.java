package xyz.anythings.base.model;

/**
 * 장비 인터페이스
 * 
 * @author shortstop
 */
public interface IDevice {

	/**
	 * 장비 ID
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * 장비 ID
	 * 
	 * @param id
	 */
	public void setId(String id);

	/**
	 * 스테이지 코드
	 * 
	 * @return
	 */
	public String getStageCd();

	/**
	 * 스테이지 코드
	 * 
	 * @param stageCd
	 */
	public void setStageCd(String stageCd);
	
	/**
	 * 장비가 소속된 설비 유형
	 * 
	 * @return
	 */
	public String getEquipType();

	/**
	 * 장비가 소속된 설비 유형
	 * 
	 * @param equipType
	 */
	public void setEquipType(String equipType);

	/**
	 * 장비가 소속된 설비 코드
	 * 
	 * @return
	 */
	public String getEquipCd();
	
	/**
	 * 장비가 소속된 설비 코드
	 * 
	 * @param equipCd
	 */
	public void setEquipCd(String equipCd);

	/**
	 * 장비 상태
	 * 
	 * @return
	 */
	public String getStatus();

	/**
	 * 장비 상태
	 * 
	 * @param status
	 */
	public void setStatus(String status);

}
