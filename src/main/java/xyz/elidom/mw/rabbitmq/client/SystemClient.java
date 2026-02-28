package xyz.elidom.mw.rabbitmq.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Component;

import xyz.elidom.mw.rabbitmq.connection.ConnectionCreater;
import xyz.elidom.mw.rabbitmq.message.MessageProperties;
import xyz.elidom.mw.rabbitmq.message.api.IMwMsgObject;
import xyz.elidom.mw.rabbitmq.model.SystemQueueNameModel;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.BeanUtil;

/**
 * 시스템 클라이언트
 *  
 * @author yang
 */
@Component
public class SystemClient extends CreateMessageReceiver implements IClient {
	
	/**
	 * 시스템 클라이언트 타입
	 */
	private String clientType = "system";
	/**
	 * Virtual Host - 
	 */
	private Map<String, Map<String, ClientTemplate>> vHostMap = new HashMap<String, Map<String, ClientTemplate>>();
	
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
	}
	
	/**
	 * virtual host에 시스템 큐 추가
	 * 
	 * @param vHost
	 * @param queueList
	 */
	public void addVirtualHost(String vHost, List<SystemQueueNameModel> queueList) {
		if(!this.vHostMap.containsKey(vHost)) {
			this.vHostMap.put(vHost, new HashMap<String, ClientTemplate>());
		}
		
		for(SystemQueueNameModel model : queueList) {
			this.addSystemQueue(vHost, model.getQueueName());
		}
	}
	
	/**
	 * virtual host에 시스템 큐 추가
	 * 
	 * @param vHost
	 * @param queueName
	 */
	public void addSystemQueue(String vHost, String queueName) {
		if(this.vHostMap.containsKey(vHost)) {
			Map<String, ClientTemplate> vHostQueueMap = this.vHostMap.get(vHost);
			
			BeanUtil.get(BrokerSiteAdmin.class).createSystemQueue(vHost, queueName);
			SimpleMessageListenerContainer container = super.CreateMessageListener(vHost, queueName, this.clientType);
			RabbitTemplate template = ConnectionCreater.CreateMessageSender(container.getConnectionFactory());
			
			vHostQueueMap.put(queueName, new ClientTemplate(container, template));
			this.mqProperties.addSystemQueue(new SystemQueueNameModel(vHost, queueName));
		}
	}
	
	/**
	 * virtual host에 시스템 큐 삭제
	 * 
	 * @param vHost
	 * @param queueName
	 */
	public void removeSystemQueue(String vHost, String queueName) {
		if(this.vHostMap.containsKey(vHost)) {
			Map<String, ClientTemplate> vHostQueueMap = this.vHostMap.get(vHost);
			
			if(vHostQueueMap.containsKey(queueName)) {
				vHostQueueMap.get(queueName).container.destroy();
				vHostQueueMap.remove(queueName);
			}
			
			this.mqProperties.removeSystemQueue(queueName);
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
			Map<String, ClientTemplate> clientMap = this.vHostMap.get(vHost);
			
			for(String queueName : clientMap.keySet()) {
				clientMap.get(queueName).container.destroy();
				this.mqProperties.removeSystemQueue(queueName);
			}
			
			this.vHostMap.remove(vHost);
		}
	}
	
	/**
	 * 메시지 publish
	 *  
	 * @param vHost 사이트 코드
	 * @param sendQueueName 전송 큐 이름
	 * @param destId 목적지 라우팅 키 
	 * @param message 메시지 
	 * @return 
	 */
	public boolean sendMessage(String vHost, String sendQueueName, String destId, String message) {
		if(!this.vHostMap.containsKey(vHost) || !this.vHostMap.get(vHost).containsKey(sendQueueName)) {
			return false;
		} else {
			// AMQP To MQTT 전송시 Routekey의 '/'를 '.'로 변환
			if(destId.contains(SysConstants.SLASH)) {
				destId = destId.replaceAll(SysConstants.SLASH, SysConstants.DOT);
			}
			
			// 전송
			this.vHostMap.get(vHost).get(sendQueueName).template.convertAndSend(this.mqProperties.getBrokerExchange(), destId, message);
			return true;
		}
	}
	
	/**
	 * 메시지 publish
	 *  
	 * @param message 메시지 오브젝트
	 * @return 
	 */
	public boolean sendMessage(IMwMsgObject message) {
		MessageProperties header = message.getProperties();
		String vHost = header.getSite();
		String sendQueueName = header.getSourceId();
		
		if(!this.vHostMap.containsKey(vHost) || !this.vHostMap.get(vHost).containsKey(sendQueueName)) {
			return false;
		} else {
			String destId = header.getDestId();
			
			// AMQP To MQTT 전송시 Routekey의 '/'를 '.'로 변환
			if(destId.contains(SysConstants.SLASH)) {
				destId = destId.replaceAll(SysConstants.SLASH, SysConstants.DOT);
			}
			
			// 전송
			this.vHostMap.get(vHost).get(sendQueueName).template.convertAndSend(this.mqProperties.getBrokerExchange(), destId, message.toJsonString());
			return true;
		}
	}
	
	/**
	 * 시스템 클라이언트 리스너 및 퍼블리셔 저장 모델 클래스
	 * 
	 * @author yang
	 */
	private class ClientTemplate {
		public SimpleMessageListenerContainer container;
		public RabbitTemplate template;
		
		public ClientTemplate(SimpleMessageListenerContainer container, RabbitTemplate template) {
			this.container = container;
			this.template = template;
		}
	}
}
