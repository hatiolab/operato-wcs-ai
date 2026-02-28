package xyz.elidom.mw.rabbitmq.service;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.mw.rabbitmq.client.BrokerSiteAdmin;
import xyz.elidom.mw.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.mw.rabbitmq.model.PublishTestModel;
import xyz.elidom.mw.rabbitmq.model.PublishTestResultModel;
import xyz.elidom.mw.rabbitmq.service.model.Node;
import xyz.elidom.mw.rabbitmq.service.model.Policy;
import xyz.elidom.mw.rabbitmq.service.model.QueueSearch;
import xyz.elidom.mw.rabbitmq.service.model.VirtualHost;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 브로커 관리 서비스
 *  
 * @author yang
 */
@Component
public class BrokerAdminService {

	/**
	 * RabbitMQ 프로퍼티
	 */
	@Autowired
	private RabbitmqProperties mqProperties;
	/**
	 * BrokerSiteAdmin
	 */
	@Autowired
	private BrokerSiteAdmin siteAdmin;
	
	/**
	 * Virtual Host 리스트 조회
	 * 
	 * @return
	 */
	public List<VirtualHost> getVhostsList() {
		VirtualHost[] vArr = this.restGetService(BrokerAdminUrls.VIRTUAL_HOST_LIST_ALL, VirtualHost[].class, null);
		return Arrays.stream(vArr).collect(Collectors.toList());
	}
	
	/**
	 * Virtual Host 삭제
	 * 
	 * @param vhost
	 */
	public void removeVhost(String vhost) {
		Map<String, Object> params = ValueUtil.newMap("vhost", vhost);
		this.restDeleteService(BrokerAdminUrls.VIRTUAL_HOST_REMVOE, params);
	}
	
	/**
	 * Virtual Host 등록
	 * 
	 * @param vhost
	 */
	public void addVhost(String vhost, boolean useTrace) {
		// 1. 사이트 생성
		Map<String, Object> params = ValueUtil.newMap("vhost", vhost);
		this.restPutDefParamService(BrokerAdminUrls.VIRTUAL_HOST_ADD, SysConstants.EMPTY_STRING, params);
		
		// 2. policy 생성 
		// 2.1 HA-CLUSTER
		params.put("name", "ha-mode");
		Map<String, Object> haDefinition = ValueUtil.newMap("ha-mode,queue-master-locator", "all", "client-local");
		
		Policy haPolicy = new Policy();
		haPolicy.setApplyTo("queues");
		haPolicy.setPattern(".*");
		haPolicy.setPriority(0);
		haPolicy.setDefinition(haDefinition);
		
		this.restPutDefParamService(BrokerAdminUrls.POLICY_ADD, FormatUtil.toUnderScoreJsonString(haPolicy), params);
		
		// 2.2 MQTT DEFAULT
		params.put("name", "mqtt");
		Map<String,Object> mqDefinition = ValueUtil.newMap(
				"dead-letter-exchange,dead-letter-routing-key,message-ttl,ha-mode,queue-master-locator"
				, "messages.dead","dead", 10000, "all", "client-local");
		
		Policy mqPolicy = new Policy();
		mqPolicy.setApplyTo("queues");
		mqPolicy.setPattern("^mqtt\\-subscription\\-");
		mqPolicy.setPriority(1);
		mqPolicy.setDefinition(mqDefinition);
		
		this.restPutDefParamService(BrokerAdminUrls.POLICY_ADD, FormatUtil.toUnderScoreJsonString(mqPolicy), params);
		
		// 2.3 TRACE DEFAULT
		params.put("name", "trace");
		Map<String,Object> definitionTraceMap = ValueUtil.newMap("ha-mode,message-ttl,queue-master-locator", "all", 10000, "client-local");
		
		Policy tracePolicy = new Policy();
		tracePolicy.setApplyTo("queues");
		tracePolicy.setPattern("trace_*");
		tracePolicy.setPriority(1);
		tracePolicy.setDefinition(definitionTraceMap);
		
		this.restPutDefParamService(BrokerAdminUrls.POLICY_ADD, FormatUtil.toUnderScoreJsonString(tracePolicy), params);
		
		// 3. site admin 생성 
		this.siteAdmin.addVirtualHost(vhost);
		
		// 3.1 dead message exchage 생성 
		this.siteAdmin.setExchage(vhost, "messages.dead", null);
		
		// 3.2 trace queue 생성 
		this.siteAdmin.setQueue(vhost, "trace_dead", null);
		this.siteAdmin.setQueue(vhost, "trace_publish", null);
		this.siteAdmin.setQueue(vhost, "trace_deliver", null);

		// 3.3 exchage = queue routing 
		this.siteAdmin.setBinding(vhost, "trace_dead", "dead", "messages.dead", null);
		this.siteAdmin.setBinding(vhost, "trace_publish", "publish.#", "amq.rabbitmq.trace", null);
		this.siteAdmin.setBinding(vhost, "trace_deliver", "deliver.#", "amq.rabbitmq.trace", null);
		
		// 4 Trace On
		this.setVirtualHostTrace(useTrace, vhost);
	}
	
	/**
	 * Virtual Host에 메시지 트레이스 설정
	 * 
	 * @param isUseTrace
	 * @param vHost
	 */
	public void setVirtualHostTrace(boolean isUseTrace, String vHost) {
		// 1 cluster list get
		Node[] clusters = this.getClusterNodeList();
		
		// 2 vhost trace on 
		String traceOption = isUseTrace ? "{\"tracing\":true}" : "{\"tracing\":false}";
		
		for(Node nodeInfo : clusters) {
			String nodeName = nodeInfo.getName();
			String[] nodeNameSplits = nodeName.split("@")[0].split("-");
			Map<String, Object> params = ValueUtil.newMap("address,port,vhost", nodeNameSplits[1].replaceAll("_", SysConstants.DOT), nodeNameSplits[2], vHost);
			this.restPutService(BrokerAdminUrls.VIRTUAL_HOST_TRACE, traceOption, params);
		}
	}
	
	/**
	 * Virtual Host의 Queue 리스트 조회
	 * 
	 * @param vhost
	 * @param page
	 * @param pageSize
	 * @param queueName
	 * @return
	 */
	public QueueSearch getQueueList(String vhost, int page, int pageSize, String queueName) {
		String searchUrl = ValueUtil.isEmpty(vhost) ? BrokerAdminUrls.QUEUE_SEARCH : BrokerAdminUrls.QUEUE_SEARCH_VHOST;
		Map<String, Object> params = ValueUtil.newMap(SysConstants.EMPTY_STRING);
		if(ValueUtil.isNotEmpty(vhost)) {
			params.put("vhost", vhost);
		}
		
		if(ValueUtil.isEqualIgnoreCase(queueName, "all")) {
			queueName = SysConstants.EMPTY_STRING;
		}

		params.put("page", page);
		params.put("size", pageSize);
		params.put("name", queueName);
		params.put("sort_col", "name");
		params.put("is_sort_asc", false);
		
		// http://{address}:{port}/api/queues?page={page}1&page_size={size}&name={name}&use_regex=true&sort={sort_col}&sort_reverse={is_sort_asc}&pagination=true
		// {size=50, is_sort_asc=true, name=, page=1, sort_col=name}
		QueueSearch result = this.restGetService(searchUrl, QueueSearch.class, params);
		return result;
	}
	
	/**
	 * Message Publish 
	 * @param model
	 */
	public boolean exchangePublishMessage(String vhost, String queueName, String message) {
		try {
			String url = BrokerAdminUrls.PUB_USE_EXCHANGE;
			PublishTestModel model = new PublishTestModel(vhost, this.mqProperties.getBrokerExchange(), queueName, message);
			Map<String,Object> params = ValueUtil.newMap("vhost,exchange", vhost, this.mqProperties.getBrokerExchange());
			PublishTestResultModel res = this.restPostService(url, model, params, PublishTestResultModel.class);
			return res.isRouted();
		}catch(Exception e) {
			throw new ElidomRuntimeException("Message Send Test Error..", e);
		}
	}
	
	/**
	 * Purge Queue Message
	 * 
	 * @param vhost
	 * @param queueName
	 * @param is_system_queue
	 */
	public void purgeQueue(String vhost, String queueName, boolean is_system_queue) {
		queueName = is_system_queue ? queueName : ("mqtt-subscription-" + queueName + "qos1");
		this.siteAdmin.purgeQueue(vhost, queueName);
	}
	
	/**
	 * 큐 삭제
	 * 
	 * @param vhost
	 * @param queueName
	 * @param is_system_queue
	 * @return
	 */
	public boolean deleteQueue(String vhost, String queueName, boolean is_system_queue) {
		queueName = is_system_queue ? queueName : ("mqtt-subscription-" + queueName + "qos1");
		return this.siteAdmin.deleteQueue(vhost, queueName);
	}

	/**
	 * 큐 생성
	 * 
	 * @param vhost
	 * @param queueName
	 * @param is_system_queue
	 */
	public void createQueue(String vhost, String queueName, boolean is_system_queue) {
		queueName = is_system_queue ? queueName : ("mqtt-subscription-" + queueName + "qos1");
		this.siteAdmin.createQueue(vhost, queueName, is_system_queue);
	}
	
	/**
	 * 클러스터 리스트 조회
	 * 
	 * @return
	 */
	public Node[] getClusterNodeList() {
		String clusterInfos = this.restGetService(BrokerAdminUrls.CLUSTER_LIST_ALL, String.class, null);
		return FormatUtil.underScoreJsonToObject(clusterInfos, Node[].class);
	}
	
	/**
	 * 기본 파라미터로 REST PUT 호출
	 * 
	 * @param httpUrl
	 * @param requestStr
	 * @param params
	 */
	private void restPutDefParamService(String httpUrl, String requestStr, Map<String, Object> params) {
		params = this.setDefaultParamsMap(params);
		this.restPutService(httpUrl, requestStr, params);
	}
	
	/**
	 * REST PUT 호출
	 * 
	 * @param httpUrl
	 * @param requestStr
	 * @param params
	 */
	private void restPutService(String httpUrl, String requestStr, Map<String,Object> params) {
		RestTemplate rest = this.getBasicAuthRestTemplate();
		rest.put(httpUrl, requestStr, params);
	}
	
	/**
	 * REST POST 호출
	 * 
	 * @param httpUrl
	 * @param requestStr
	 * @param params
	 */
	private <T> T restPostService(String httpUrl, PublishTestModel message, Map<String,Object> params, Class<T> responseType) {
		params = this.setDefaultParamsMap(params);
		RestTemplate rest = this.getBasicAuthRestTemplate();
		String msgStr = FormatUtil.toJsonString(message, true);
		return rest.postForObject(httpUrl, msgStr, responseType, params);
	}
	
	
	/**
	 * REST DELETE 호출
	 * 
	 * @param httpUrl
	 * @param params
	 */
	private void restDeleteService(String httpUrl, Map<String,Object> params) {
		params = this.setDefaultParamsMap(params);
		RestTemplate rest = this.getBasicAuthRestTemplate();
		
		rest.delete(httpUrl, params);
	}
	
	/**
	 * REST GET 호출 
	 * 
	 * @param <T>
	 * @param httpUrl
	 * @param inputType
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> T restGetService(String httpUrl , Class<T> inputType, Map<String, Object> params) {
		params = this.setDefaultParamsMap(params);
		RestTemplate rest = this.getBasicAuthRestTemplate();
		String str = rest.getForObject(httpUrl, String.class, params);
		
		if(String.class.equals(inputType)) {
			return (T) str;
		}
		
		return FormatUtil.underScoreJsonToObject(str, inputType);
	}
	
	/**
	 * 기본 REST Template 생성 & 리턴
	 * 
	 * @return
	 */
	private RestTemplate getBasicAuthRestTemplate() {
		RestTemplate rest = new RestTemplate();
		rest.getInterceptors().add(new BasicAuthenticationInterceptor(this.mqProperties.getBrokerAdminId(), this.mqProperties.getBrokerAdminPw()));
		rest.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		return rest;
	}
	
	/**
	 * 기본 파라미터 맵 생성
	 * 
	 * @param params
	 * @return
	 */
	private Map<String, Object> setDefaultParamsMap(Map<String, Object> params) {
		if(params == null) {
			params = ValueUtil.newMap(SysConstants.EMPTY_STRING);
		}
		
		params.put("address", this.mqProperties.getBrokerAddress());
		params.put("port", this.mqProperties.getBrokerApiPort());
		return params;
	}
}