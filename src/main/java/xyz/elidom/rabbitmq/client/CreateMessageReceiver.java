package xyz.elidom.rabbitmq.client;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import xyz.elidom.rabbitmq.client.event.DeadTraceEvent;
import xyz.elidom.rabbitmq.client.event.DeliverTraceEvent;
import xyz.elidom.rabbitmq.client.event.PublishTraceEvent;
import xyz.elidom.rabbitmq.client.event.SystemMessageReceiveEvent;
import xyz.elidom.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.rabbitmq.connection.ConnectionCreater;

/**
 * 메시지 수신 이벤트 처리 생성 
 * @author yang
 *
 */
public class CreateMessageReceiver implements ApplicationEventPublisherAware {
	
	@Autowired
	private RabbitmqProperties mqProperties;
	
	private ApplicationEventPublisher publisher;

	/**
	 * 메시지 리스너 생성 
	 * @param vHost 사이트 코드 
	 * @param clientType 리스너 타입 
	 * @return
	 */
	public SimpleMessageListenerContainer CreateMessageListener(String vHost, String queueName, String clientType) {
		
		// 1. 연결 
		CachingConnectionFactory connectionFactory = 
				ConnectionCreater.CreateConnectionFactory(mqProperties.getBrokerAddress(), mqProperties.getBrokerPort()
                                                         , mqProperties.getBrokerAdminId(), mqProperties.getBrokerAdminPw(), vHost);
		
		// 2. 리스너 타입별 이벤트 생성 
        if(clientType.equalsIgnoreCase("trace_pub")) {
    		SimpleMessageListenerContainer container = ConnectionCreater.CreateMessageListener(connectionFactory, "trace_publish", mqProperties.getTraceConsumeCnt());
        		
        		// 2.1 publish 이벤트 생성 
            container.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                		PublishTraceEvent event = new PublishTraceEvent(this, message);
	                	publisher.publishEvent(event);
                }
            });
            container.start();
            return container;
        		
        } else if (clientType.equalsIgnoreCase("trace_sub")) {
    		SimpleMessageListenerContainer container = ConnectionCreater.CreateMessageListener(connectionFactory, "trace_deliver", mqProperties.getTraceConsumeCnt());
        		
	    		// 2.2 subscribe 이벤트 생성.
            container.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                	
	                String destRouteKey = message.getMessageProperties().getReceivedRoutingKey();
	                
	                //2.2.1 라우팅 키에 따라 필요 없는 정보 skip
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
	            		
	            		// db  일때 redeliver 메시지에 대해서 skip !!!!!!!!!!
	            		if(mqProperties.getTraceType().equalsIgnoreCase("db")) {
	    	                if(message.getMessageProperties().isRedelivered()) return;
	    	                //message.getMessageProperties().isRedelivered() == true
	            		}
	            		
                		DeliverTraceEvent event = new DeliverTraceEvent(this, message);
	                	publisher.publishEvent(event);
                }
            });
            container.start();
            return container;
            
        } else if (clientType.equalsIgnoreCase("trace_dead")) {
    		SimpleMessageListenerContainer container = ConnectionCreater.CreateMessageListener(connectionFactory, "trace_dead", mqProperties.getTraceConsumeCnt());
        		
	    		// 2.3 dead 메시지 이벤트 생성 
	    		//     사이트 코드 관리를 위해 subscribe 에서 구분 처리 함. 
            container.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                		// site_code 처리를 위해 trace_deliver 에서 별도 처리 함.
//                	DeadTraceEvent event = new DeadTraceEvent(this, message);
//	                	publisher.publishEvent(event);
                }
            });
            container.start();
            return container;
        } else if (clientType.equalsIgnoreCase("system")) {
    		SimpleMessageListenerContainer container = ConnectionCreater.CreateMessageListener(connectionFactory, queueName, mqProperties.getSystemConsumeCnt());
        		
	    		// 2.4 시스템 메시지 이벤트 생성 
            container.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                	/**
                	 * TODO : 나중에 고민 좀 해볼것.....
                	 * 사이트 관리에서 사이트 추가 밑 삭제 시 어떻게 해야 할까.......
                	 * 플러그인이 사용하는 메시지. skip
                	 * 
                	if(ValueUtil.isEqualIgnoreCase(message.getMessageProperties().getMessageId(), "system"))
                	{
                		return;
                	}
                	 */
                	SystemMessageReceiveEvent event = new SystemMessageReceiveEvent(this, message, vHost);
                	try {
    	                publisher.publishEvent(event);
                	}catch(Exception e) {
                	}
                }
            });
            container.start();
            return container;
        }
        return null;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}
}
