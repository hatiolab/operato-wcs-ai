package xyz.elidom.rabbitmq.service.model.elastic;

import java.util.List;

/**
 * 엘라스틱 dead 메시지 조회 결과 상세 모델 
 * @author yang
 *
 */
public class SelectTraceDeadResultHits {
	
	private int total;
	private List<SelectTraceDeadResultSource> hits;
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public List<SelectTraceDeadResultSource> getHits() {
		return hits;
	}
	public void setHits(List<SelectTraceDeadResultSource> hits) {
		this.hits = hits;
	}
	
}
