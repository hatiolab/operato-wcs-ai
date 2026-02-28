package xyz.elidom.mw.rabbitmq.client.event;

import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationEvent;

/**
 * Dead 메시지 이벤트
 * Dead 메시지 : 메시지를 publish했는데 가져갈 대상이 없어서 큐에 그대로 남아있는 메시지가 일정 시간이 지나면 Dead 메시지로 넘어온다.
 * 
 * 
 * @author yang
 */
public class DeadTraceEvent extends ApplicationEvent {
	private static final long serialVersionUID = -5915649162035414207L;
	/**
	 * Dead 메시지
	 */
	private final Message message;
	
	public DeadTraceEvent(Object source, Message message) {
		super(source);
	    this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
}	