package xyz.elidom.mw.rabbitmq.client.event;

import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationEvent;

/**
 * Publish 메시지 이벤트
 * Publish 메시지 : 메시지 Producer가 생성하여 브로커에 전달한 메시지
 * 
 * @author yang
 */
public class PublishTraceEvent extends ApplicationEvent {
	private static final long serialVersionUID = -5637200314112693717L;
	/**
	 * Publish 메시지
	 */
	private final Message message;
	
	public PublishTraceEvent(Object source, Message message) {
		super(source);
	    this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
}