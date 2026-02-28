package xyz.elidom.mw.rabbitmq.client;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import xyz.elidom.mw.rabbitmq.client.event.DeadTraceEvent;
import xyz.elidom.mw.rabbitmq.client.event.DeliverTraceEvent;
import xyz.elidom.mw.rabbitmq.client.event.PublishTraceEvent;
import xyz.elidom.mw.rabbitmq.client.event.SystemMessageReceiveEvent;
import xyz.elidom.mw.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.mw.rabbitmq.connection.ConnectionCreater;

/**
 * 메시지 수신 이벤트 처리 생성
 * 
 * @author yang
 */
public class CreateMessageReceiver implements ApplicationEventPublisherAware {
	
	/**
	 * RabbitMQ 모듈 프로퍼티
	 */
	@Autowired
	protected RabbitmqProperties mqProperties;
	/**
	 * 이벤트 퍼블리셔
	 */
	protected ApplicationEventPublisher publisher;

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}
	
	/**
	 * 메시지 리스너 생성
	 * 
	 * @param vHost 사이트 코드
	 * @param queueName 큐 이름
	 * @param clientType 리스너 타입
	 * @return
	 */
	public SimpleMessageListenerContainer CreateMessageListener(String vHost, String queueName, String clientType) {
		
		// 1. 연결 
		CachingConnectionFactory connectionFactory = ConnectionCreater.CreateConnectionFactory(
														  this.mqProperties.getBrokerAddress(), 
														  this.mqProperties.getBrokerPort(), 
														  this.mqProperties.getBrokerAdminId(), 
														  this.mqProperties.getBrokerAdminPw(), 
														  vHost);
		// 2. 리스너 타입별 이벤트 생성
		int traceConsumeCnt = this.mqProperties.getTraceConsumeCnt();
		
		// 2.1 리스너 타입이 trace_pub인 경우 
        if(clientType.equalsIgnoreCase("trace_pub")) {
    		SimpleMessageListenerContainer container = ConnectionCreater.CreateMessageListener(connectionFactory, "trace_publish", traceConsumeCnt);
        		
    		// 2.1.1 publish 이벤트 리스너 생성 
            container.setMessageListener(new MessageListener() {
            	public void onMessage(Message message) {
            		PublishTraceEvent event = new PublishTraceEvent(this, message);
            		publisher.publishEvent(event);
                }
            });
            
            container.start();
            return container;
        		
        // 2.2 리스너 타입이 trace_sub인 경우
        } else if (clientType.equalsIgnoreCase("trace_sub")) {
    		SimpleMessageListenerContainer container = ConnectionCreater.CreateMessageListener(connectionFactory, "trace_deliver", traceConsumeCnt);
        		
	    	// 2.2.1 subscribe 이벤트 생성.
            container.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {                	
	                String destRouteKey = message.getMessageProperties().getReceivedRoutingKey();
	                
	                // 2.2.2 라우팅 키에 따라 필터링
	                if(destRouteKey.equals("deliver.trace_dead")) {
	                	DeadTraceEvent event = new DeadTraceEvent(this, message);
	                	publisher.publishEvent(event);
	                	return;
	                	
	                } else if(destRouteKey.equals("deliver.trace_deliver")) {
	                	return;
	                	
	                } else if(destRouteKey.equals("deliver.trace_publish")) {
	                	return;
	                	
	                } else if(destRouteKey.equals("trace_dead")) {
	                	return;
	                }
	            	
	                // 2.2.3 traceType이 db이고 redelivered 라면 스킵
	                if(!mqProperties.getTraceType().equalsIgnoreCase("db") || !message.getMessageProperties().isRedelivered()) {
		                DeliverTraceEvent event = new DeliverTraceEvent(this, message);
		                publisher.publishEvent(event);
	                }
                }
            });
            
            container.start();
            return container;
            
        // 2.3 리스너 타입이 trace_dead인 경우
        } else if (clientType.equalsIgnoreCase("trace_dead")) {
    		SimpleMessageListenerContainer container = ConnectionCreater.CreateMessageListener(connectionFactory, "trace_dead", traceConsumeCnt);
        		
    		// 2.3.1 dead 메시지 이벤트 생성, 사이트 코드 관리를 위해 subscribe에서 구분 처리 
    		container.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                	// site_code 처리를 위해 trace_deliver 에서 별도 처리 함.
                	// DeadTraceEvent event = new DeadTraceEvent(this, message);
                	// publisher.publishEvent(event);
                }
            });
    		
            container.start();
            return container;
            
        // 2.4 리스너 타입이 system인 경우
        } else if (clientType.equalsIgnoreCase("system")) {
    		SimpleMessageListenerContainer container = ConnectionCreater.CreateMessageListener(connectionFactory, queueName, this.mqProperties.getSystemConsumeCnt());
        		
    		// 2.4.1 시스템 메시지 이벤트 생성 
            container.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                	SystemMessageReceiveEvent event = new SystemMessageReceiveEvent(this, message, vHost);
                	try {
    	                publisher.publishEvent(event);
                	} catch(Exception e) {
                	}
                }
            });
            
            container.start();
            return container;
        }
        
        return null;
	}
}