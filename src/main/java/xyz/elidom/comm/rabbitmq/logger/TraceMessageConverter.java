package xyz.elidom.comm.rabbitmq.logger;

import java.util.Date;
import java.util.List;
import java.util.Map;

import xyz.elidom.rabbitmq.entity.TraceDead;
import xyz.elidom.rabbitmq.entity.TraceDeliver;
import xyz.elidom.rabbitmq.entity.TracePublish;
import xyz.elidom.rabbitmq.message.MessageProperties;
import xyz.elidom.rabbitmq.message.TraceMessage;
import xyz.elidom.rabbitmq.model.trace.ITraceModel;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * rabbit mq 메시지 트레이스 모델 변환 
 * @author yang
 *
 */
public class TraceMessageConverter {
	
	/**
	 * 퍼블리시 메시지 변환 
	 * @param messageProperties amqp 프로퍼티 
	 * @param body 메시지 바디 
	 * @return
	 * @throws Exception
	 */
	public static ITraceModel convertPublishMessage(org.springframework.amqp.core.MessageProperties messageProperties, byte[] body) throws Exception{
		
		String bodyString = new String(body);
		
		TraceMessage message = FormatUtil.underScoreJsonToObject(bodyString, TraceMessage.class);
		MessageProperties properties = message.getProperties();
		
		String routedQueueStr = messageProperties.getHeaders().get("routed_queues").toString();
		routedQueueStr = routedQueueStr.substring(1, routedQueueStr.length() -1).trim();
		
		String[] routedQueueArr = ValueUtil.isEmpty(routedQueueStr) ? new String[0] : routedQueueStr.split(",");
		String routedQueues = "";
		
		for(String rQueue : routedQueueArr) {
	        routedQueues += rQueue.replaceAll("mqtt-subscription-", "").replaceAll("qos1", "").replaceAll("^\\s+","") + "\n";
		}
		
		TracePublish model = new TracePublish(); 
		
		model.setId(properties.getId());
		model.setSourceId(properties.getSourceId());
		model.setDestId(properties.getDestId());
		model.setIsReply(properties.getIsReply());
		
		model.setRoutedCount(routedQueueArr.length);
		model.setRoutedQueues(routedQueues);
		
		model.setBody(bodyString);
		
		model.setPubTimeLong(properties.getTime());
		model.setPubTime(new Date(properties.getTime()));
		model.setLogTime(new Date());
		model.setLogTimeLong(model.getLogTime().getTime());
		model.setSite(messageProperties.getHeaders().get("vhost").toString());
		
		
		model.setAction(message.getBody().getAction());
		model.setEquipId(message.getBody().getEquipId());
		
		return model;
	}

	/**
	 * 서브스크라이브 메시지 변환 
	 * @param messageProperties amqp 프로퍼티 
	 * @param body 메시지 바디 
	 * @return
	 * @throws Exception
	 */
	public static ITraceModel convertDeliverMessage(org.springframework.amqp.core.MessageProperties messageProperties, byte[] body) throws Exception{
		String destRouteKey = messageProperties.getReceivedRoutingKey();
		
		String bodyString = new String(body);
		TraceMessage message = FormatUtil.underScoreJsonToObject(bodyString, TraceMessage.class);
		MessageProperties properties = message.getProperties();
		
		TraceDeliver model = new TraceDeliver();
		
		model.setId(properties.getId());
		model.setSourceId(properties.getSourceId());
		
		model.setDestId(destRouteKey.replace("mqtt-subscription-", "").replaceAll("qos1", "").replaceAll("deliver.", ""));
		model.setLogTime(new Date());
		model.setLogTimeLong(model.getLogTime().getTime());
		model.setSite(messageProperties.getHeaders().get("vhost").toString());
		
		return model;
	}

	/**
	 * dead 메시지 변환 
	 * @param messageProperties amqp 프로퍼티 
	 * @param body 메시지 바디 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static ITraceModel convertDeadMessage(org.springframework.amqp.core.MessageProperties messageProperties, byte[] body) throws Exception{
		String bodyString = new String(body);
		TraceMessage message = FormatUtil.underScoreJsonToObject(bodyString, TraceMessage.class);
		MessageProperties properties = message.getProperties();
		
		Map<String,Object> propHeader = (Map<String, Object>) messageProperties.getHeaders().get("properties");
		Map<String,Object> deadHeader = (Map<String, Object>) propHeader.get("headers");
		
		String deathRouteKey = deadHeader.get("x-first-death-queue").toString().replaceAll("deliver.", "");
		
		TraceDead model = new TraceDead();
		
		model.setId(properties.getId());
		model.setSourceId(properties.getSourceId());
		model.setDestId(deathRouteKey.replace("mqtt-subscription-", "").replaceAll("qos1", ""));
		
		List<Map<String,Object>> deathInfoList = (List<Map<String,Object>>)deadHeader.get("x-death");
//		[{reason=expired, count=1, exchange=amq.direct, time=Sat Mar 10 17:27:03 KST 2018, routing-keys=[miss], queue=miss}]
		
		model.setDeadTime((Date)deathInfoList.get(0).get("time"));
		model.setDeadTimeLong(model.getDeadTime().getTime());
		model.setLogTime(new Date());
		model.setLogTimeLong(model.getLogTime().getTime());
		
		/*
		MessageProperties [
		                   headers={x-first-death-exchange=amq.direct, 
		                   x-death=[{reason=expired, count=1, exchange=amq.direct, time=Sat Mar 10 17:43:18 KST 2018, routing-keys=[miss], queue=miss}], 
		                   x-first-death-reason=expired, 
		                   x-first-death-queue=miss}, 
		                   timestamp=null, 
		                   messageId=null, 
		                   userId=null, 
		                   receivedUserId=null, 
		                   appId=null, 
		                   clusterId=null, 
		                   type=null, 
		                   correlationId=null, 
		                   correlationIdString=null, 
		                   replyTo=null, 
		                   contentType=text/plain, 
		                   contentEncoding=UTF-8, 
		                   contentLength=0, 
		                   deliveryMode=PERSISTENT, receivedDeliveryMode=null, expiration=null, priority=0, 
		                   redelivered=false, 
		                   receivedExchange=messages.dead, 
		                   receivedRoutingKey=dead, receivedDelay=null, 
		                   deliveryTag=28, 
		                   messageCount=0, 
		                   consumerTag=amq.ctag-YccsmuCXcYuM4ulR8vR3Kw, 
		                   consumerQueue=trace_dead]
				*/
		model.setSite(messageProperties.getHeaders().get("vhost").toString());
		
		return model;
	}
}