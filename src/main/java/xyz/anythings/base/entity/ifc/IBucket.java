package xyz.anythings.base.entity.ifc;

/**
 * Tray or Box Interface
 * 
 * @author yang
 */
public interface IBucket {
	
	/**
	 * PK ID 정보
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * 버킷 유형 : tray (플라스틱 박스) / box (종이 박스)
	 * 
	 * @return
	 */
	public String getBucketType();
	
	/**
	 * TRAY_CD or BOX_ID
	 * 
	 * @return
	 */
	public String getBucketCd();
	
	/**
	 * TRAY_TYPE or BOX_TYPE_CD
	 * 
	 * @return
	 */
	public String getBucketTypeCd();
	
	/**
	 * TRAY_COLOR or BOX_COLOR
	 * 
	 * @return
	 */
	public String getBucketColor();
	
	/**
	 * 버킷 상태 - 투입, 진행 중, 대기 중 .... 
	 * 
	 * @return
	 */
	public String getStatus();
	
}