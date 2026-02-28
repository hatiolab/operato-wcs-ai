package xyz.elidom.mw.rabbitmq.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedTransferQueue;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import xyz.elidom.mw.rabbitmq.client.event.PublishTraceEvent;
import xyz.elidom.mw.rabbitmq.logger.TraceLogger;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.BeanUtil;

/**
 * Publish 메시지 수신 클라이언트
 *   
 * @author yang
 */
@Component
public class TracePublish extends CreateMessageReceiver implements ApplicationListener<PublishTraceEvent>, IClient {

	private String traceMode = "trace_pub";
	private Map<String, SimpleMessageListenerContainer> vHostMap = new HashMap<String, SimpleMessageListenerContainer>();
	private LinkedTransferQueue<Message> linkedQueue = new LinkedTransferQueue<Message>();
	
	/**
	 * 트래이스 메시지 write 를 위해 write 메니저 준비 
	 */
	@Override
	public void initLogger() {
		BeanUtil.get(TraceLogger.class).startPub(this.traceMode, this.linkedQueue);
	}
	
	/**
	 * 메시지 수신 이벤트 처리
	 *  
	 * @param event
	 */
	@Override
	public void onApplicationEvent(PublishTraceEvent event) {
		this.linkedQueue.add(event.getMessage());
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