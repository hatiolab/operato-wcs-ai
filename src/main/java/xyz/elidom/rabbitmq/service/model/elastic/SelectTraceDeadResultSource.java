package xyz.elidom.rabbitmq.service.model.elastic;

import xyz.elidom.rabbitmq.entity.TraceDead;

/**
 * 엘라스틱 dead 메시지 조회 결과 row  모델 
 * @author yang
 *
 */
public class SelectTraceDeadResultSource {

	private TraceDead _source;

	public TraceDead get_source() {
		return _source;
	}

	public void set_source(TraceDead _source) {
		this._source = _source;
	}
}
