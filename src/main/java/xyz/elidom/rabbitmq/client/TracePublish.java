package xyz.elidom.rabbitmq.client;

import java.util.HashMap;
import java.util.concurrent.LinkedTransferQueue;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import xyz.elidom.rabbitmq.client.event.PublishTraceEvent;
import xyz.elidom.rabbitmq.logger.TraceLogger;
import xyz.elidom.util.BeanUtil;


/**
 * publish 메시지 수신 클라이언트  
 * @author yang
 *
 */
@Component
public class TracePublish extends CreateMessageReceiver implements ApplicationListener<PublishTraceEvent>, IClient {

	String traceMode = "trace_pub";
	HashMap<String, SimpleMessageListenerContainer> vHostMap = new HashMap<String, SimpleMessageListenerContainer>();

	private LinkedTransferQueue<Message> linkedQueue = new LinkedTransferQueue<Message>();

	
	/**
	 * init
	 * 트래이스 메시지 write 를 위해 write 메니저 준비 
	 */
	@Override
	public void initLogger() {
		BeanUtil.get(TraceLogger.class).StartPub(traceMode, linkedQueue);
	}
		
	
	/**
	 * 사이트 리스너 추가 
	 * @param vHost 사이트 코드 
	 */
	@Override
	public void addVirtualHost(String vHost) {
		if(vHostMap.containsKey(vHost) == true) return;
		
		SimpleMessageListenerContainer container = super.CreateMessageListener(vHost, "", traceMode);
		vHostMap.put(vHost, container);
	}
	
	/**
	 * 사이트 리스너 삭제  
	 * @param vHost 사이트 코드 
	 */
	@Override
	public void removeVirtualHost(String vHost) {
		if(vHostMap.containsKey(vHost) == false) return;
		
		vHostMap.get(vHost).destroy();
		vHostMap.remove(vHost);
	}

	/**
	 * 메시지 수신 이벤트 처리 
	 * @param event
	 */
	@Override
	public void onApplicationEvent(PublishTraceEvent event) {
		linkedQueue.add(event.getMessage());
	}
}
