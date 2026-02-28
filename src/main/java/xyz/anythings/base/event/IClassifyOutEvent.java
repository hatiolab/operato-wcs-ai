package xyz.anythings.base.event;

/**
 * 소분류 Out 이벤트
 * 
 * @author shortstop
 */
public interface IClassifyOutEvent extends IClassifyRunEvent {

	/**
	 * 박스 ID
	 * 
	 * @return
	 */
	public String getBoxId();
	
	/**
	 * 박스 ID 설정
	 * 
	 * @param boxId
	 */
	public void setBoxId(String boxId);
}
