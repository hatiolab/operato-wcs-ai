package xyz.elidom.mw.rabbitmq.service.model.elastic;

/**
 * 엘라스틱 publish 메시지 조회 결과 모델
 * 
 * @author yang
 */
public class SelectTracePubChartResultBucket {
	
	private Long from;
	private Long to;
	private Long doc_count;
	
	public Long getFrom() {
		return from;
	}
	
	public void setFrom(Long from) {
		this.from = from;
	}
	
	public Long getTo() {
		return to;
	}
	
	public void setTo(Long to) {
		this.to = to;
	}
	
	public Long getDoc_count() {
		return doc_count;
	}
	
	public void setDoc_count(Long doc_count) {
		this.doc_count = doc_count;
	}
}
