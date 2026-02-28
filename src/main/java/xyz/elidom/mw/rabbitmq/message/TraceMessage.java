package xyz.elidom.mw.rabbitmq.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * 미들웨어 통해서 주고 받는 메시지 로깅을 위한 메시지 정의
 *  
 * @author shortstop
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TraceMessage {
	/**
	 * 메시지 프로퍼티
	 */
	private MessageProperties properties;

	public MessageProperties getProperties() {
		return properties;
	}

	public void setProperties(MessageProperties properties) {
		this.properties = properties;
	}
}
