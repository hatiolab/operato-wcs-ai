package xyz.elidom.rabbitmq.client.event;

import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationEvent;

/**
 * 메시지 dead event
 * @author yang
 *
 */
public class DeadTraceEvent extends ApplicationEvent {
	private static final long serialVersionUID = -5915649162035414207L;
	private final Message message;
	
	public DeadTraceEvent(Object source, Message message) {
		super(source);
	    this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
}	