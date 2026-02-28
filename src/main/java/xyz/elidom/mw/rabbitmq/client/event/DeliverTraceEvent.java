package xyz.elidom.mw.rabbitmq.client.event;

import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationEvent;

/**
 * Deliver 메시지 이벤트
 * Deliver 메시지 : 메시지 Producer가 생성하여 큐에 저장된 메시지를 Consumer에게 전달한 메시지  
 * 
 * @author yang
 */
public class DeliverTraceEvent extends ApplicationEvent {
	private static final long serialVersionUID = 882259546452914811L;
	/**
	 * Deliver 메시지
	 */
	private final Message message;
	
	public DeliverTraceEvent(Object source, Message message) {
		super(source);
	    this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
}