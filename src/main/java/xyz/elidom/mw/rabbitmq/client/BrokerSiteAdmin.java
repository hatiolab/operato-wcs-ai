package xyz.elidom.mw.rabbitmq.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.elidom.mw.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.mw.rabbitmq.connection.ConnectionCreater;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.ValueUtil;

/**
 * 브로커 사이트 관리자
 * 
 * @author yang
 */
@Component
public class BrokerSiteAdmin implements IClient {
	
	/**
	 * Rabbitmq 모듈 프로퍼티
	 */
	@Autowired
	private RabbitmqProperties mqProperties;
	/**
	 * 사용 중인 브로커 관리 맵 (VirtualHost 명 - RabbitAdmin)
	 */
	private Map<String, RabbitAdmin> vHostMap = new HashMap<String, RabbitAdmin>();
	
	@Override
	public void initLogger() {
	}
	
	/**
	 * 사이트 리스너 추가
	 * 
	 * @param vHost 사이트 코드 
	 */
	@Override
	public void addVirtualHost(String vHost) {
		if(!this.vHostMap.containsKey(vHost)) {
			// 브로커 연결 
			CachingConnectionFactory connectionFactory = 
					ConnectionCreater.CreateConnectionFactory(this.mqProperties.getBrokerAddress(), 
															  this.mqProperties.getBrokerPort(), 
															  this.mqProperties.getBrokerAdminId(), 
															  this.mqProperties.getBrokerAdminPw(), vHost);
			// Virtual Host 명으로 RabbitAdmin 등록 
			this.vHostMap.put(vHost, new RabbitAdmin(connectionFactory));
		}
	}

	/**
	 * 사이트 리스터 삭제
	 * 
	 * @param vHost 사이트 코드 
	 */
	@Override
	public void removeVirtualHost(String vHost) {
		if(this.vHostMap.containsKey(vHost)) {
			this.vHostMap.remove(vHost);
		}
	}
	
	/**
	 * 시스템 큐 생성
	 * 
	 * @param vHost 사이트 코드 
	 * @param queueName 큐 이름 
	 * @return
	 */
	public boolean createSystemQueue(String vHost, String queueName) {
		if(!this.vHostMap.containsKey(vHost)) {
			return false;
			
		} else {
			// 파라미터 맵
			Map<String, Object> params = ValueUtil.newMap(
					"x-dead-letter-exchange,x-dead-letter-routing-key,x-message-ttl,x-queue-master-locator", 
					"messages.dead", 
					"dead", 
					10000, 
					"client-local");			
			// 큐 생성 
			this.setQueue(vHost, queueName, params);
			// 큐, 익스체인지 바인딩 
			this.setSystemQueueBinding(vHost, queueName, queueName);
			return true;
		}
	}
	
	/**
	 * 시스템 큐에 라우팅 키 바인딩
	 * 
	 * @param vHost 사이트 코드
	 * @param queueName 큐 이름
	 * @param routingKey 라우팅 키
	 */
	private void setSystemQueueBinding(String vHost, String queueName, String routingKey) {
		// 1. 브로커 기본 exchange 명 (amq.direct)
		String brokerExchange = this.mqProperties.getBrokerExchange();
		
		// 2. 바인딩 
		this.setBinding(vHost, queueName, queueName, brokerExchange, null);
		
		// 3. '/'를 '.'으로 변환하여 
		String newQueueName = queueName.replaceAll(SysConstants.SLASH, SysConstants.DOT);
		
		// 4. 원래 큐 이름과 변환 이름이 다르다면 바인딩 처리
		if(ValueUtil.isNotEqual(queueName, newQueueName)) {
			this.setBinding(vHost, queueName, newQueueName, brokerExchange, null);
		}
	}
	
	/**
	 * 큐, 익스체인지 바인딩 생성
	 * 
	 * @param vHost 사이트 코드 
	 * @param queueName 큐 이름 
	 * @param routeKey 라우팅 키 
	 * @param exchangeName 익스체인지 이름
	 * @param arguments 상세 설정 
	 */
	public void setBinding(String vHost, String queueName, String routeKey,  String exchangeName, Map<String, Object> arguments) {
		// destination, destinationType, exchange, routingKey, Map<String, Object> arguments
		Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routeKey, arguments);
		// 사이트 코드로 바인딩 정보 추가
		this.vHostMap.get(vHost).declareBinding(binding);
	}
	
	/**
	 * 큐 생성
	 * 
	 * @param vHost 사이트 코드 
	 * @param queueName 큐 이름 
	 * @param arguments 상세 설정 
	 */
	public void setQueue(String vHost, String queueName, Map<String, Object> arguments) {
		if(this.vHostMap.containsKey(vHost)) {
			// name, durable, exclusive, autoDelete, arguments
			Queue queue = new Queue(queueName, true, false, false, arguments);
			queue.setAdminsThatShouldDeclare(this.vHostMap.get(vHost));
			queue.setShouldDeclare(true);
			this.vHostMap.get(vHost).declareQueue(queue);
		}
	}
	
	/**
	 * 익스체인지 생성
	 * 
	 * @param vHost 사이트 코드 
	 * @param exchangeName 익스체인지 명 
	 * @param arguments 상세 설정 
	 */
	public void setExchage(String vHost, String exchangeName, Map<String, Object> arguments) {
		if(this.vHostMap.containsKey(vHost)) {
			// name, durable, autoDelete, arguments
			DirectExchange exchange = new DirectExchange(exchangeName, true, false, arguments);
			this.vHostMap.get(vHost).declareExchange(exchange);
		}
	}
	
	/**
	 * 큐 대기 메시지 삭제
	 * 
	 * @param vHost 사이트 코드 
	 * @param queueName 큐 이름
	 */
	public void purgeQueue(String vHost, String queueName) {
		if(this.vHostMap.containsKey(vHost)) {
			this.vHostMap.get(vHost).purgeQueue(queueName, true);
		}
	}
	
	/**
	 * 큐 삭제
	 * 
	 * @param vHost 사이트 코드 
	 * @param queueName 큐 이름 
	 * @return
	 */
	public boolean deleteQueue(String vHost, String queueName) {
		if(this.vHostMap.containsKey(vHost)) {
			return this.vHostMap.get(vHost).deleteQueue(queueName);
		} else {
			return false;
		}
	}
	
	/**
	 * 큐 생성
	 * 
	 * @param vHost 사이트 코드 
	 * @param queueName 큐 이름 
	 * @param isSystemQueue 시스템 큐 유무 
	 */
	public void createQueue(String vHost, String queueName, boolean isSystemQueue) {
		this.setQueue(vHost, queueName, null);
		
		if(isSystemQueue) {
			this.setSystemQueueBinding(vHost, queueName, queueName);
			
		} else {
			String routeKey = SysConstants.EMPTY_STRING;
			String[] routeKeys = queueName.split(SysConstants.SLASH);
			String brokerExchange = this.mqProperties.getBrokerExchange();
			
			for(int i = 0 ; i < routeKeys.length ; i++) {
				if(i == 0) {
					routeKey = routeKeys[0].replaceAll("mqtt-subscription-", SysConstants.EMPTY_STRING);
				} else {
					routeKey += SysConstants.DOT + routeKeys[i].replaceAll("qos1", SysConstants.EMPTY_STRING);
				}
				
				this.setBinding(vHost, queueName, routeKey, brokerExchange, null);
			}
		}
	}
}
