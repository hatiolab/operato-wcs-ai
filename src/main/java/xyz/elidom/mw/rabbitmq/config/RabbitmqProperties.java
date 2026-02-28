package xyz.elidom.mw.rabbitmq.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import xyz.elidom.mw.rabbitmq.model.SystemQueueNameModel;

/**
 * Rabbitmq 관련 properties 셋팅
 *  
 * @author yang
 */
@Component
public class RabbitmqProperties {
	
	/**
	 * 웹 애플리케이션 서버 포트
	 */
	@Value("${server.port:80}")
	private int managerPort;
	
	/**
	 * 브로커 관련 설정 
	 */	
	// RabbitMQ 모듈 사용 여부 
	@Value("${mq.module.use:false}")
	private boolean useMqModule;
	
	// 브로커 주소 (def : localhost)
	@Value("${mq.broker.address:localhost}")
	private String brokerAddress;
	
	// 브로커 포트 (def : 5672 : amqp 기본)
	@Value("${mq.broker.port:5672}")
	private int brokerPort;	
	
	// 브로커 web api 포트 (def : 15672)
	@Value("${mq.broker.api.port:15672}")
	private int brokerApiPort;	
	
	// 브로커 관리자 아이디
	@Value("${mq.broker.user.id:admin}")
	private String brokerAdminId;	
	
	// 브로커 관리자 비밀번호
	@Value("${mq.broker.user.pw:admin}")
	private String brokerAdminPw;
	
	// 브로커 기본 exchange 설정 
	@Value("${mq.broker.exchange.default:amq.direct}")
	private String brokerExchange;		

	/**
	 * 브로커 메시지 트레이스 관련 설정 
	 */
	@Value("${mq.trace.use:false}")
	private boolean traceUse;
	
	// 트레이스 기록 대상 설정 (db, file, elastic)
	@Value("${mq.trace.type:db}")
	private String traceType;	
	
	// 트레이스 타입이 file 일때 root path
	@Value("${mq.trace.file.root:./traces}")
	private String traceFileRoot;	
	
	// 트레이스 메시지 보관 일수 
	@Value("${mq.trace.keep.date:5}")
	private String traceKeepDate;
	
	// 트레이스 메시지 삭제 처리 서버 설정 (true인 서버에서만 삭제 실행)
	@Value("${mq.trace.delete.main:false}")
	private boolean deleteMain;
	
	// 트레이스 메시지 삭제 시간 
	@Value("${mq.trace.delete.time:01}")
	private String traceDelTime;
	
	// 트레이스 메시지 리스터 숫자 
	@Value("${mq.trace.consume.count:3}")
	private int traceConsumeCnt;	
	
	// 엘라스틱 주소 
	@Value("${mq.trace.elastic.address:localhost}")
	private String traceElasticAddress;
	
	// 엘라스틱 포트 
	@Value("${mq.trace.elastic.port:0}")
	private int traceElasticPort;	

	// 리스너 숫자 
	@Value("${mq.system.receive.queue.consume.count:3}")
	private int systemConsumeCnt;
	
	// 도메인의 사이트 코드로 큐를 구성할 지 여부  
	// @Value("${mq.quename.use.domain:true}")
	// private boolean useDomainQueue;
	
	/**
	 * 시스템 메시지 리스너 설정 
	 */
	private List<SystemQueueNameModel> systemQueueList = new ArrayList<SystemQueueNameModel>();
	/**
	 * 애플리케이션 초기화 시 초기화 할 Virtual Host 리스트
	 */
	private List<String> appInitVHosts;	
	
	public List<SystemQueueNameModel> getSystemQueueList() {
		return systemQueueList;
	}
	
	public void addSystemQueue(SystemQueueNameModel model) {
		this.systemQueueList.add(model);
	}
	
	public void removeSystemQueue(String queueName) {
		List<SystemQueueNameModel> result = this.systemQueueList.stream().filter(a -> a.getQueueName().equals(queueName)).collect(Collectors.toList());
		this.systemQueueList.removeAll(result);
	}
	
	public boolean isSystemQueue(String queueName) {
		long count = this.systemQueueList.stream().filter(a -> a.getQueueName().equals(queueName)).count();
		return count > 0 ? true : false; 
	}
	
	public void setSystemQueueList(List<SystemQueueNameModel> systemQueueList) {
		this.systemQueueList = systemQueueList;
	}
	
	public List<String> getAppInitVHosts() {
		return appInitVHosts;
	}
	
	public void setAppInitVHosts(List<String> appInitVHosts) {
		this.appInitVHosts = appInitVHosts;
	}
	
	public boolean isUseMqModule() {
		return useMqModule;
	}
	
	public void setUseMqModule(boolean useMqModule) {
		this.useMqModule = useMqModule;
	}

	public int getManagerPort() {
		return managerPort;
	}

	public void setManagerPort(int managerPort) {
		this.managerPort = managerPort;
	}

	public String getBrokerAddress() {
		return brokerAddress;
	}

	public void setBrokerAddress(String brokerAddress) {
		this.brokerAddress = brokerAddress;
	}

	public int getBrokerPort() {
		return brokerPort;
	}

	public void setBrokerPort(int brokerPort) {
		this.brokerPort = brokerPort;
	}

	public int getBrokerApiPort() {
		return brokerApiPort;
	}

	public void setBrokerApiPort(int brokerApiPort) {
		this.brokerApiPort = brokerApiPort;
	}

	public String getBrokerAdminId() {
		return brokerAdminId;
	}

	public void setBrokerAdminId(String brokerAdminId) {
		this.brokerAdminId = brokerAdminId;
	}

	public String getBrokerAdminPw() {
		return brokerAdminPw;
	}

	public void setBrokerAdminPw(String brokerAdminPw) {
		this.brokerAdminPw = brokerAdminPw;
	}

	public String getBrokerExchange() {
		return brokerExchange;
	}

	public void setBrokerExchange(String brokerExchange) {
		this.brokerExchange = brokerExchange;
	}

	public boolean getTraceUse() {
		return traceUse;
	}

	public void setTraceUse(boolean traceUse) {
		this.traceUse = traceUse;
	}

	public String getTraceType() {
		return traceType;
	}

	public void setTraceType(String traceType) {
		this.traceType = traceType;
	}

	public String getTraceFileRoot() {
		return traceFileRoot;
	}

	public void setTraceFileRoot(String traceFileRoot) {
		this.traceFileRoot = traceFileRoot;
	}

	public String getTraceKeepDate() {
		return traceKeepDate;
	}

	public void setTraceKeepDate(String traceKeepDate) {
		this.traceKeepDate = traceKeepDate;
	}

	public boolean isDeleteMain() {
		return deleteMain;
	}

	public void setDeleteMain(boolean deleteMain) {
		this.deleteMain = deleteMain;
	}

	public String getTraceDelTime() {
		return traceDelTime;
	}

	public void setTraceDelTime(String traceDelTime) {
		this.traceDelTime = traceDelTime;
	}

	public int getTraceConsumeCnt() {
		return traceConsumeCnt;
	}

	public void setTraceConsumeCnt(int traceConsumeCnt) {
		this.traceConsumeCnt = traceConsumeCnt;
	}

	public String getTraceElasticAddress() {
		return traceElasticAddress;
	}

	public void setTraceElasticAddress(String traceElasticAddress) {
		this.traceElasticAddress = traceElasticAddress;
	}

	public int getTraceElasticPort() {
		return traceElasticPort;
	}

	public void setTraceElasticPort(int traceElasticPort) {
		this.traceElasticPort = traceElasticPort;
	}

	public int getSystemConsumeCnt() {
		return systemConsumeCnt;
	}

	public void setSystemConsumeCnt(int systemConsumeCnt) {
		this.systemConsumeCnt = systemConsumeCnt;
	}

	/*public boolean isUseDomainQueue() {
		return useDomainQueue;
	}

	public void setUseDomainQueue(boolean useDomainQueue) {
		this.useDomainQueue = useDomainQueue;
	}*/
}