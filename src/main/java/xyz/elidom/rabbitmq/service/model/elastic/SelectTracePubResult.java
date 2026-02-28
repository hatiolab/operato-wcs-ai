package xyz.elidom.rabbitmq.service.model.elastic;

/**
 * 엘라스틱 publish 메시지 조회 결과 모델 
 * @author yang
 *
 */
public class SelectTracePubResult {

	private Boolean timed_out;
	private SelectTracePubResultHits hits;
	
	public Boolean getTimed_out() {
		return timed_out;
	}
	public void setTimed_out(Boolean timed_out) {
		this.timed_out = timed_out;
	}
	public SelectTracePubResultHits getHits() {
		return hits;
	}
	public void setHits(SelectTracePubResultHits hits) {
		this.hits = hits;
	}
}
