package xyz.elidom.rabbitmq.service.model.elastic;

import xyz.elidom.rabbitmq.entity.TracePublish;

/**
 * 엘라스틱 publish 메시지 조회 결과 row  모델 
 * @author yang
 *
 */
public class SelectTracePubResultSource {

	private TracePublish _source;

	public TracePublish get_source() {
		return _source;
	}

	public void set_source(TracePublish _source) {
		this._source = _source;
	}
}
