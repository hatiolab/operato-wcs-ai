package xyz.elidom.mw.rabbitmq.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * RabbitMQ로 부터 수신한 메시지에서 헤더 정보를 정의
 *  
 * @author shortstop
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReceiveMessage {
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
