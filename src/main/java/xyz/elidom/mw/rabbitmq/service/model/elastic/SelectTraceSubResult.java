package xyz.elidom.mw.rabbitmq.service.model.elastic;

/**
 * 엘라스틱 subscribe 메시지 조회 결과 모델
 * 
 * @author yang
 */
public class SelectTraceSubResult {

	private Boolean timed_out;
	private SelectTraceSubResultHits hits;
	
	public Boolean getTimed_out() {
		return timed_out;
	}
	
	public void setTimed_out(Boolean timed_out) {
		this.timed_out = timed_out;
	}
	
	public SelectTraceSubResultHits getHits() {
		return hits;
	}
	
	public void setHits(SelectTraceSubResultHits hits) {
		this.hits = hits;
	}
}
