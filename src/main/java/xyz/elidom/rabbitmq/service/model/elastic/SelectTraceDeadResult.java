package xyz.elidom.rabbitmq.service.model.elastic;

/**
 * 엘라스틱 dead 메시지 조회 결과 모델 
 * @author yang
 *
 */
public class SelectTraceDeadResult {

	private Boolean timed_out;
	private SelectTraceDeadResultHits hits;
	
	public Boolean getTimed_out() {
		return timed_out;
	}
	public void setTimed_out(Boolean timed_out) {
		this.timed_out = timed_out;
	}
	public SelectTraceDeadResultHits getHits() {
		return hits;
	}
	public void setHits(SelectTraceDeadResultHits hits) {
		this.hits = hits;
	}
}
