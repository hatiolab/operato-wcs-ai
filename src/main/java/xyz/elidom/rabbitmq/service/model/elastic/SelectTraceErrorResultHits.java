package xyz.elidom.rabbitmq.service.model.elastic;

import java.util.List;

/**
 * 엘라스틱 error 메시지 조회 결과 상세 모델 
 * @author yang
 *
 */
public class SelectTraceErrorResultHits {
	
	private int total;
	private List<SelectTraceErrorResultSource> hits;
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public List<SelectTraceErrorResultSource> getHits() {
		return hits;
	}
	public void setHits(List<SelectTraceErrorResultSource> hits) {
		this.hits = hits;
	}
	
}
