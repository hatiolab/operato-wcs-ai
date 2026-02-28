package xyz.elidom.rabbitmq.client.event;

import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationEvent;

/**
 * 메시지 deliver event
 * @author yang
 *
 */
public class DeliverTraceEvent extends ApplicationEvent {
	private static final long serialVersionUID = 882259546452914811L;
	private final Message message;
	
	public DeliverTraceEvent(Object source, Message message) {
		super(source);
	    this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
}