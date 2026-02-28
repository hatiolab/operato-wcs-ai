package xyz.elidom.rabbitmq.service.model.elastic;

import xyz.elidom.rabbitmq.entity.TraceDeliver;
/**
 * 엘라스틱 subscribe 메시지 조회 결과 row  모델 
 * @author yang
 *
 */
public class SelectTraceSubResultSource {

	private TraceDeliver _source;

	public TraceDeliver get_source() {
		return _source;
	}

	public void set_source(TraceDeliver _source) {
		this._source = _source;
	}
}
