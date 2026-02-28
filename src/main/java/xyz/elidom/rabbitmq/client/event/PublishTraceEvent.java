package xyz.elidom.rabbitmq.client.event;

import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationEvent;

/**
 * 메시지 publish event
 * @author yang
 *
 */
public class PublishTraceEvent extends ApplicationEvent {
	private static final long serialVersionUID = -5637200314112693717L;
	private final Message message;
	
	public PublishTraceEvent(Object source, Message message) {
		super(source);
	    this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
}