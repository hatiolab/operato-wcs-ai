package xyz.elidom.mw.rabbitmq.service.model.elastic;

import xyz.elidom.mw.rabbitmq.entity.TraceError;

/**
 * 엘라스틱 error 메시지 조회 결과 row 모델
 * 
 * @author yang
 */
public class SelectTraceErrorResultSource {

	private TraceError _source;

	public TraceError get_source() {
		return _source;
	}

	public void set_source(TraceError _source) {
		this._source = _source;
	}
}
