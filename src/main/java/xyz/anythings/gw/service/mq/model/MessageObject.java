package xyz.anythings.gw.service.mq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import xyz.elidom.rabbitmq.message.MessageProperties;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class MessageObject {
	private MessageProperties properties;
	private IMessageBody body;
	
	public MessageProperties getProperties() {
		return properties;
	}
	public void setProperties(MessageProperties properties) {
		this.properties = properties;
	}
	public IMessageBody getBody() {
		return body;
	}
	public void setBody(IMessageBody body) {
		this.body = body;
	}
}
