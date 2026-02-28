package xyz.elidom.rabbitmq.model;

import java.util.List;

import xyz.elidom.rabbitmq.entity.TracePublish;

/**
 * 메시지 트레이스 뷰어 상세 화면 모델 
 * @author yang
 *
 */
public class TraceDetailView {
	private TracePublish detail;
	private List<TraceDeliverResult> list;
	
	public TracePublish getDetail() {
		return detail;
	}
	public void setDetail(TracePublish detail) {
		this.detail = detail;
	}
	public List<TraceDeliverResult> getList() {
		return list;
	}
	public void setList(List<TraceDeliverResult> list) {
		this.list = list;
	}
}
