package xyz.elidom.rabbitmq.service.model.elastic;

/**
 * 엘라스틱 error 메시지 조회 결과 모델 
 * @author yang
 *
 */
public class SelectTraceErrorResult {

	private Boolean timed_out;
	private SelectTraceErrorResultHits hits;
	
	public Boolean getTimed_out() {
		return timed_out;
	}
	public void setTimed_out(Boolean timed_out) {
		this.timed_out = timed_out;
	}
	public SelectTraceErrorResultHits getHits() {
		return hits;
	}
	public void setHits(SelectTraceErrorResultHits hits) {
		this.hits = hits;
	}
}
