package xyz.elidom.mw.rabbitmq.logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedTransferQueue;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import xyz.elidom.mw.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.mw.rabbitmq.entity.TraceError;
import xyz.elidom.mw.rabbitmq.model.trace.ITraceModel;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ThreadUtil;

/**
 * 트레이스 메시지 로그 처리 클래스
 * 
 * @author yang
 */
@Component
public class TraceLogger {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(TraceLogger.class);
	/**
	 * RabbitMQ 프로퍼티
	 */
	@Autowired
	private RabbitmqProperties mqProperties;

	/**
	 * 퍼블리쉬 메시지 트레이스 처리 시작
	 * 
	 * @param traceMode
	 * @param linkedQueue
	 */
	@Async("mwTracePool")
	public void startPub(String traceMode, LinkedTransferQueue<Message> linkedQueue) {
		this.start(traceMode,linkedQueue);
	}
	/**
	 * 서브스크라이브 메시지 트레이스 처리 시작
	 * 
	 * @param traceMode
	 * @param linkedQueue
	 */
	@Async("mwTracePool")
	public void startSub(String traceMode, LinkedTransferQueue<Message> linkedQueue) {
		this.start(traceMode,linkedQueue);
	}
	/**
	 * dead 메시지 트레이스 처리 시작
	 * 
	 * @param traceMode
	 * @param linkedQueue
	 */
	@Async("mwTracePool")
	public void startDead(String traceMode, LinkedTransferQueue<Message> linkedQueue) {
		this.start(traceMode,linkedQueue);
	}
	
	/**
	 * 각 타입에 대해 큐를 소비하면서 트레이스 메시지 기록
	 * 
	 * @param traceMode 
	 * @param linkedQueue 
	 */
	private void start(String traceMode, LinkedTransferQueue<Message> linkedQueue) {
		IQueryManager queryManager = BeanUtil.get(IQueryManager.class);
		TraceWriteManager writeManager = new TraceWriteManager(this.logger, this.mqProperties.getTraceType(), queryManager, this.mqProperties.getTraceElasticAddress(), this.mqProperties.getTraceElasticPort(), traceMode, this.mqProperties.getTraceFileRoot());
		
		// 한번에 처리할 최대 트레이스 수 
		int maxPageRow = 1000;
		while(true) {
			try {
				// 현재 큐에 누적된 메시지 수 
				int queueSize = linkedQueue.size();
				
				if(queueSize == 0) {
					ThreadUtil.sleep(1000);
				} else {
					// 최대 사이즈 만큼 끊기 
					int readCnt = (queueSize >= maxPageRow) ? maxPageRow : queueSize;					
					List<Message> messageList = new ArrayList<Message>();

					// 메시지 poll 
					for(int i = 0 ; i < readCnt ; i++) {
						messageList.add(linkedQueue.poll());
					}
					
					List<ITraceModel> logList = new ArrayList<ITraceModel>();
					List<ITraceModel> errorList = new ArrayList<ITraceModel>();
					
					// 메시지 변환 
					for(Message message : messageList) {
						MessageProperties messageProperties = message.getMessageProperties();
						ITraceModel logMessage = null;
						
						try {
							if(traceMode.equals("trace_pub")) {
								logMessage = TraceMessageConverter.convertPublishMessage(messageProperties, message.getBody());
								
							} else if(traceMode.equals("trace_sub")) {
								logMessage = TraceMessageConverter.convertDeliverMessage(messageProperties, message.getBody());
								
							} else if(traceMode.equals("trace_dead")) {
								logMessage = TraceMessageConverter.convertDeadMessage(messageProperties, message.getBody());
							}
							
							logList.add(logMessage);
							
						} catch(Exception e) {
							this.logger.error("Trace message parsing error", e);
							
							TraceError model = new TraceError();
							model.setId(UUID.randomUUID().toString());
							model.setErrDate(new Date());
							model.setErrDateLong(model.getErrDate().getTime());
							model.setTraceType(traceMode);
							model.setMessageProp(messageProperties.toString());
							model.setMessageBody(new String(message.getBody()));
							model.setErrTrace(ExceptionUtils.getStackTrace(e));
							errorList.add(model);
						}
					}
					
					try {
						// 메시지 기록 
						writeManager.write(logList);
					} catch(Exception e) {
						logger.error("Trace message writing error : " + traceMode, e);
						
						TraceError model = new TraceError();
						model.setId(UUID.randomUUID().toString());
						model.setErrDate(new Date());
						model.setErrDateLong(model.getErrDate().getTime());
						model.setTraceType(traceMode);
						model.setMessageProp("DB Insert Error Message Count : " + readCnt);
						model.setMessageBody(FormatUtil.toJsonString(logList));
						model.setErrTrace(ExceptionUtils.getStackTrace(e));
						errorList.add(model);
					}
					
					if(errorList.size() > 0) {
						writeManager.writeError(errorList);
					}
				}
				
			} catch(Exception e) {
				logger.error(traceMode + " Error : ", e);
			}
		}
	}
}
