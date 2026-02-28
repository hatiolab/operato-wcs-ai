package xyz.elidom.mw.rabbitmq.client;

import java.util.HashMap;
import java.util.concurrent.LinkedTransferQueue;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import xyz.elidom.mw.rabbitmq.client.event.DeadTraceEvent;
import xyz.elidom.mw.rabbitmq.logger.TraceLogger;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.BeanUtil;

/**
 * Dead 메시지 수신 클라이언트
 *  
 * @author yang
 */
@Component
public class TraceDead extends CreateMessageReceiver implements ApplicationListener<DeadTraceEvent>, IClient {

	private String traceMode = "trace_dead";
	private HashMap<String, SimpleMessageListenerContainer> vHostMap = new HashMap<String, SimpleMessageListenerContainer>();
	private LinkedTransferQueue<Message> linkedQueue = new LinkedTransferQueue<Message>();
	
	/** 
	 * 메시지 수신 이벤트 처리 
	 * 
	 * @param event
	 */
	@Override
	public void onApplicationEvent(DeadTraceEvent event) {
		this.linkedQueue.add(event.getMessage());
	}
	
	/**
	 * 트래이스 메시지 write 를 위해 write 메니저 준비 
	 */
	@Override
	public void initLogger() {
		BeanUtil.get(TraceLogger.class).startDead(this.traceMode, linkedQueue);
	}

	/**
	 * 사이트 리스너 추가
	 *  
	 * @param vHost 사이트 코드 
	 */
	@Override
	public void addVirtualHost(String vHost) {
		if(!this.vHostMap.containsKey(vHost)) {
			SimpleMessageListenerContainer container = super.CreateMessageListener(vHost, SysConstants.EMPTY_STRING, this.traceMode);
			this.vHostMap.put(vHost, container);	
		}
	}
	
	/**
	 * 사이트 리스너 삭제
	 *  
	 * @param vHost 사이트 코드 
	 */
	@Override
	public void removeVirtualHost(String vHost) {
		if(this.vHostMap.containsKey(vHost)) {
			SimpleMessageListenerContainer container = this.vHostMap.remove(vHost);
			container.destroy();
		}
	}
}