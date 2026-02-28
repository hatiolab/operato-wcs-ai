package xyz.elidom.rabbitmq.service.model.elastic;

import java.util.List;
/**
 * 엘라스틱 publish 메시지 조회 결과 상세 모델 
 * @author yang
 *
 */
public class SelectTracePubResultHits {
	
	private int total;
	private List<SelectTracePubResultSource> hits;
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public List<SelectTracePubResultSource> getHits() {
		return hits;
	}
	public void setHits(List<SelectTracePubResultSource> hits) {
		this.hits = hits;
	}
	
}
