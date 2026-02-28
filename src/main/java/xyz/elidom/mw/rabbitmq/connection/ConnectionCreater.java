package xyz.elidom.mw.rabbitmq.connection;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.util.backoff.FixedBackOff;

/**
 * 브로커 연결 처리 클래스
 * 
 * @author yang
 */
public class ConnectionCreater {
	/**
	 * 브로커 연결
	 * 
	 * @param brokerAddress 브로커 주소  
	 * @param brokerPort    브로커 포트 
	 * @param brokerAdminId 브로커 관리 아이디 
	 * @param brokerAdminPw 브포커 관리 비밀번호 
	 * @param vHost 사이트 코드 
	 * @return
	 */
	public static CachingConnectionFactory CreateConnectionFactory(String brokerAddress, int brokerPort, String brokerAdminId, String brokerAdminPw, String vHost) {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost(brokerAddress);
		connectionFactory.setPort(brokerPort);
		connectionFactory.setUsername(brokerAdminId);
		connectionFactory.setPassword(brokerAdminPw);
		connectionFactory.setVirtualHost(vHost);
		
		// connectionFactory.getRabbitConnectionFactory().setAutomaticRecoveryEnabled(true);
		// connectionFactory.getRabbitConnectionFactory().setNetworkRecoveryInterval(1000);		
		// connectionFactory.setAutomaticRecoveryEnabled(true); 
		// connectionFactory.setNetworkRecoveryInterval(1000);
		
		return connectionFactory;
	}
	
	/**
	 * 브로커 메시지 리스너 연결
	 * 
	 * @param connectionFactory 연결 처리자 
	 * @param queueName  메시지 큐 이름 
	 * @param consumeCnt 리스너 쓰레드 수 
	 * @return
	 */
	public static SimpleMessageListenerContainer CreateMessageListener(CachingConnectionFactory connectionFactory, String queueName, int consumeCnt) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setRecoveryBackOff(new FixedBackOff(3000, 20));
		container.setPrefetchCount(256);
	    container.setConnectionFactory(connectionFactory);
	    container.setAcknowledgeMode(AcknowledgeMode.AUTO);
	    container.setConcurrentConsumers(consumeCnt);
	    container.setQueueNames(queueName);
	    
	    // container.setTaskExecutor(taskExecutor);
	    // container.setMissingQueuesFatal(false);
	    // container.setRecoveryInterval(1000);
	    // container.setAutoDeclare(true);
	    // ExponentialBackOff eee = new ExponentialBackOff();
		// eee.setMaxElapsedTime(1000);
		// eee.setMaxInterval(1000);
		// eee.setMultiplier(10);
	    // 복구 시도 1초 단위 10번 max
	    // container.setRecoveryBackOff(eee);
	    // container.setAdviceChain(adviceChain);
	    // container.setErrorHandler(new ConditionalRejectingErrorHandler() {
		// });
		
		container.afterPropertiesSet();
		container.setAutoStartup(true);
		container.initialize();
		
		// ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        // backOffPolicy.setInitialInterval(15000);
        // backOffPolicy.setMultiplier(100);
        // backOffPolicy.setMaxInterval(604800);
        // return RetryInterceptorBuilder.stateless().backOffPolicy(backOffPolicy).maxAttempts(5).recoverer(new RejectAndDontRequeueRecoverer()).build();

	    return container;
	}
	
	/**
	 * rabbitmq 메시지 발송 생성
	 * 
	 * @param connectionFactory 연결 처리자 
	 * @return
	 */
	public static RabbitTemplate CreateMessageSender(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		return template;
	}
}
