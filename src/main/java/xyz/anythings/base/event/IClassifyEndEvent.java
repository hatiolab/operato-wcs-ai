package xyz.anythings.base.event;

/**
 * 소분류 처리 완료 이벤트
 * 
 * @author shortstop
 */
public interface IClassifyEndEvent {

	/**
	 * 소분류 처리 이벤트
	 * 
	 * @return
	 */
	public IClassifyEvent getClassifyEvent();
}
