package xyz.elidom.mw.rabbitmq.model.trace;

/**
 * 트레이스 모델 인터페이스 
 * 엘라스틱을 위한 jsonString 변환 메쏘드 공통
 * 
 * @author yang
 */
public interface ITraceModel {
	
	/**
	 * Object를 JSON 문자열로 변환
	 * 
	 * @return
	 */
	public String toJsonString();
}
