package xyz.elidom.rabbitmq.service.model.elastic;

import java.util.List;
/**
 * 엘라스틱 subscribe 메시지 조회 결과 상세 모델 
 * @author yang
 *
 */
public class SelectTraceSubResultHits {
	
	private int total;
	private List<SelectTraceSubResultSource> hits;
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public List<SelectTraceSubResultSource> getHits() {
		return hits;
	}
	public void setHits(List<SelectTraceSubResultSource> hits) {
		this.hits = hits;
	}
	
}
