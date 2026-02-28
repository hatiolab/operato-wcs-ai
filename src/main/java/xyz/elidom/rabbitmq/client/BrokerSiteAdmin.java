package xyz.elidom.rabbitmq.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.elidom.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.rabbitmq.connection.ConnectionCreater;
import xyz.elidom.util.ValueUtil;

/**
 * 브로커 사이트 관리 매니저 
 * @author yang
 *
 */
@Component
public class BrokerSiteAdmin implements IClient{
	
	@Autowired
	private RabbitmqProperties mqProperties;
	
	// 사용중인 브로커 어드민 map
	HashMap<String, RabbitAdmin> vHostMap = new HashMap<String, RabbitAdmin>();
	
	/**
	 * 사이트 리스너 추가 
	 * @param vHost 사이트 코드 
	 */
	@Override
	public void addVirtualHost(String vHost) {
		if(vHostMap.containsKey(vHost) == true) return;

		// 브로커 연결 
		CachingConnectionFactory connectionFactory = 
				ConnectionCreater.CreateConnectionFactory(mqProperties.getBrokerAddress(), mqProperties.getBrokerPort()
						                                , mqProperties.getBrokerAdminId(), mqProperties.getBrokerAdminPw(), vHost);

		// 브로커 어드민 wrap
		RabbitAdmin admin = new RabbitAdmin(connectionFactory);
		vHostMap.put(vHost, admin);
	}

	/**
	 * 사이트 리스터 삭제 
	 * @param vHost 사이트 코드 
	 */
	@Override
	public void removeVirtualHost(String vHost) {
		if(vHostMap.containsKey(vHost) == false) return;
		
		vHostMap.remove(vHost);
	}
	
	/**
	 * 시스템 큐 생성 
	 * @param vHost 사이트 코드 
	 * @param queueName 큐 이름 
	 * @return
	 */
	public boolean createSystemQueue(String vHost, String queueName) {
		if(vHostMap.containsKey(vHost) == false) return false;
		
		// 큐 생성 
		this.setQueue(vHost, queueName
				, ValueUtil.newMap("x-dead-letter-exchange,x-dead-letter-routing-key,x-message-ttl,x-queue-master-locator"
								 , "messages.dead", "dead", 10000, "client-local"));
		
		
		// 큐, 익스체인지 바인딩 
		this.setSystemQueueBinding(vHost, queueName, queueName);
		return true;
	}
	
	/**
	 * 시스템 큐에 라우팅 키 바인딩 
	 * @param vHost
	 * @param queueName
	 * @param routingKey
	 */
	private void setSystemQueueBinding(String vHost, String queueName, String routingKey) {
		this.setBinding(vHost, queueName, queueName, mqProperties.getBrokerExchange(), null);
		
		if(ValueUtil.isEqual(queueName, queueName.replaceAll("/", ".")) == false) {
			this.setBinding(vHost, queueName, queueName.replaceAll("/", "."), mqProperties.getBrokerExchange(), null);
		}
	}
	
	/**
	 * 큐 , 익스체인지 바인딩 생성 
	 * @param vHost 사이트 코드 
	 * @param queueName 큐 이름 
	 * @param routeKey 라우팅 키 
	 * @param exchangeName 익스체인지 이름
	 * @param arguments 상세 설정 
	 */
	public void setBinding(String vHost, String queueName, String routeKey,  String exchangeName, Map<String, Object> arguments) {
		//destination, destinationType, exchange, routingKey, Map<String, Object> arguments
		Binding binding =  new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routeKey, arguments);
		
		vHostMap.get(vHost).declareBinding(binding);
	}
	
	/**
	 * 큐 생성 
	 * @param vHost 사이트 코드 
	 * @param queueName 큐 이름 
	 * @param arguments 상세 설정 
	 */
	public void setQueue(String vHost, String queueName, Map<String, Object> arguments) {
		if(vHostMap.containsKey(vHost) == false) return ;
		
		//name, durable, exclusive, autoDelete, arguments)
		Queue queue = new Queue(queueName, true, false, false, arguments);
		queue.setAdminsThatShouldDeclare(vHostMap.get(vHost));
		queue.setShouldDeclare(true);
		
		vHostMap.get(vHost).declareQueue(queue);  
	}
	
	
	/**
	 * 익스체인지 생성 
	 * @param vHost 사이트 코드 
	 * @param exchangeName 익스체인지 명 
	 * @param arguments 상세 설정 
	 */
	public void setExchage(String vHost, String exchangeName, Map<String, Object> arguments) {
		if(vHostMap.containsKey(vHost) == false) return ;
		
		// name , durable , autoDelete, arguments
		DirectExchange exchange = new DirectExchange(exchangeName, true, false, arguments);
		vHostMap.get(vHost).declareExchange(exchange);
	}
	/**
	 * 큐 대기 메시지 삭제 
	 * @param vHost 사이트 코드 
	 * @param queueName 큐 이름
	 */
	public void purgeQueue(String vHost, String queueName) {
		if(vHostMap.containsKey(vHost) == false) return ;
		vHostMap.get(vHost).purgeQueue(queueName, true);
	}
	/**
	 * 큐 삭제 
	 * @param vHost 사이트 코드 
	 * @param queueName 큐 이름 
	 * @return
	 */
	public boolean deleteQueue(String vHost, String queueName) {
		if(vHostMap.containsKey(vHost) == false) return false;
		return vHostMap.get(vHost).deleteQueue(queueName);
	}
	
	/**
	 * 큐 생성 
	 * @param vHost 사이트 코드 
	 * @param queueName 큐 이름 
	 * @param is_system_queue 시스템 큐 유무 
	 */
	public void createQueue(String vHost, String queueName, boolean is_system_queue) {
		this.setQueue(vHost, queueName, null);
		
		if(is_system_queue) {
			this.setSystemQueueBinding(vHost, queueName, queueName);
			return;
		}
		
		String routeKey = "";
		String[] routeKeys = queueName.split("/");
		
		for(int i = 0 ; i < routeKeys.length ; i++) {
			if(i == 0 ) routeKey = routeKeys[0].replaceAll("mqtt-subscription-", "");
			else routeKey+="."+ routeKeys[i].replaceAll("qos1", "");
			
			this.setBinding(vHost, queueName, routeKey, this.mqProperties.getBrokerExchange(), null);
		}
	}
	
	
	@Override
	public void initLogger() {
	}
}
