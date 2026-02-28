package xyz.elidom.mw.rabbitmq.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import xyz.anythings.sys.event.model.MwQueueManageEvent;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.mw.rabbitmq.client.SystemClient;
import xyz.elidom.mw.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.mw.rabbitmq.event.model.IQueueNameModel;
import xyz.elidom.mw.rabbitmq.service.BrokerAdminService;
import xyz.elidom.mw.rabbitmq.service.ServiceUtil;
import xyz.elidom.mw.rabbitmq.service.model.Queue;
import xyz.elidom.mw.rabbitmq.service.model.QueueItem;
import xyz.elidom.mw.rabbitmq.service.model.QueueSearch;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 메시지 큐 관리 컨트롤러
 * 
 * @author yang
 */
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/rabbitmq/queue")
@ServiceDesc(description = "Mq Message Queue Manager Service API")
public class MessageQueueController extends AbstractRestService {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(MessageQueueController.class);
	/**
	 * BrokerAdmin 서비스
	 */
	@Autowired
	private BrokerAdminService adminService;
	/**
	 * RabbitMQ 프로퍼티
	 */
	@Autowired
	private RabbitmqProperties properties;
	/**
	 * 시스템 큐 추가 URL
	 */
	private String addSystemQueueUrl = "http://{addr}:{port}/rest/rabbitmq/queue/addSystemQueue?vhost={vhost}&queue={queue}";
	/**
	 * 시스템 큐 제거 URL
	 */
	private String removeSystemQueueUrl = "http://{addr}:{port}/rest/rabbitmq/queue/removeSystemQueue?vhost={vhost}&queue={queue}";
	
	@Override
	protected Class<?> entityClass() {
		return QueueItem.class;
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
		page =  page == null ? 1 : page.intValue();
		limit = (limit == null) ? ValueUtil.toInteger(SettingUtil.getValue(SysConfigConstants.SCREEN_PAGE_LIMIT, "50")) : limit.intValue();
		
		Filter[] querys = new Filter[0];
		String vhost = SysConstants.EMPTY_STRING;
		String queueName = SysConstants.EMPTY_STRING;
		
		if (ValueUtil.isNotEmpty(query)) {
			querys = FormatUtil.jsonToObject(query, Filter[].class);
		}
		
		for(Filter filter : querys) {
			if(filter.getName().equalsIgnoreCase("site_code")) {
				vhost = filter.getValue().toString();
			} else if (filter.getName().equalsIgnoreCase("queue_name")) {
				queueName = filter.getValue().toString();
			}
		}
		
		// 큐 리스트 조회 
		QueueSearch queues = this.adminService.getQueueList(vhost, page , limit, queueName);
		List<QueueItem> items = new ArrayList<QueueItem>();
		
		// 필터 조건 적용 
		for(Queue queue : queues.getItems()) {
			QueueItem item = new QueueItem();
			String siteCode = queue.getVhost();
			String itemQueueName = queue.getName().replaceAll("mqtt-subscription-", SysConstants.EMPTY_STRING).replaceAll("qos1", SysConstants.EMPTY_STRING);			
			boolean isSystemQueue = (this.properties.isSystemQueue(itemQueueName) || itemQueueName.startsWith("trace")) ? true : false;
			
			item.setId(itemQueueName);
			item.setSiteCode(siteCode);
			item.setClient(queue.getConsumers());
			item.setQueueName(itemQueueName);
			item.setIsSystemQueue(isSystemQueue);
			item.setMessageCount(queue.getMessageReady());
			item.setMessageBytes(ServiceUtil.valueToByteString(queue.getMessagesReadyDetails()));
			items.add(item);
		}
		
		// 페이지 리턴값 생성 
		Page<QueueItem> result = new Page<QueueItem>();
		result.setTotalSize(queues.getTotalCount());
		result.setList(items);
		return result;
	}
	
	/**
	 * 큐 메시지 비우기
	 * 
	 * @param vhost
	 * @param queue
	 * @param isSystemQueue
	 * @return
	 */
	@RequestMapping(value="/{vhost}/{queue}/{is_system_queue}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Queue Message purge")
	public Boolean purgeQueue(@PathVariable("vhost") String vhost,
			@PathVariable("queue") String queue,
			@PathVariable("is_system_queue") boolean isSystemQueue) {
		queue = queue.replaceAll("\\.", SysConstants.SLASH);
		this.adminService.purgeQueue(vhost, queue, isSystemQueue);
		return true;
	}
	
	/**
	 * 큐 추가 삭제
	 * 
	 * @param list
	 * @return
	 */
	@RequestMapping(value="/update_multiple", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<QueueItem> list) {
		for(QueueItem item : list) {
			if(item.getCudFlag_().equalsIgnoreCase(SysConstants.CUD_FLAG_DELETE)) {
				this.adminService.deleteQueue(item.getSiteCode(), item.getQueueName(), item.getIsSystemQueue());
			} else if(item.getCudFlag_().equalsIgnoreCase(SysConstants.CUD_FLAG_CREATE)) {
				this.adminService.createQueue(item.getSiteCode(), item.getQueueName(), false);
			}
		}
		
		return true;
	}
	

	@SuppressWarnings("unchecked")
	@RequestMapping(value="/{queue}/send", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Message Publish")
	public Boolean sendMessage(@PathVariable("queue") String queue, @RequestBody Map<String,Object> body) {
		
		String msgString = body.get("message").toString();
		
		if(ValueUtil.isEmpty(msgString)) {
			throw new ElidomRuntimeException("Message is Not Found!");
		}
		
		Map<String,Object> msgObj = FormatUtil.jsonToObject(msgString, Map.class);
		
		if(ValueUtil.isEmpty(msgObj.get("properties"))) {
			throw new ElidomRuntimeException("Message Properties is Not Found!");
		}
		
		Map<String,Object> prop = (Map<String,Object>)msgObj.get("properties");
		
		if(ValueUtil.isEmpty(prop.get("dest_id"))) {
			throw new ElidomRuntimeException("Message Properties dest_id is Not Found!");
		}
		
		return this.adminService.exchangePublishMessage(Domain.currentDomain().getMwSiteCd(), prop.get("dest_id").toString().replaceAll(SysConstants.SLASH, SysConstants.DOT), msgString);
	}
	
	
	
	
	/**
	 * Virtual Host에 시스템 큐 추가
	 * 
	 * @param vhost
	 * @return
	 */
	@RequestMapping(value = "/addSystemQueue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Add Virtual Host Listen System Queue")
	public boolean addSystemQueue(@RequestParam(name="vhost") String vhost, @RequestParam(name="queue") String queue) {
		BeanUtil.get(SystemClient.class).addSystemQueue(vhost, queue);
		return true;
	}
	

	/**
	 * Virtual Host에 시스템 큐 제거
	 * 
	 * @param vhost
	 * @return
	 */
	@RequestMapping(value = "/removeSystemQueue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Remove Virtual Host Listen System Queue")
	public boolean removeSystemQueue(@RequestParam(name="vhost") String vhost, @RequestParam(name="queue") String queue) {
		BeanUtil.get(SystemClient.class).removeSystemQueue(vhost, queue);
		return true;
	}
	
	/**
	 * 큐 관리 이벤트 처리자
	 * 
	 * @param event
	 */
	@Order(Ordered.LOWEST_PRECEDENCE)
	@EventListener(condition = "#root.args[0].isExecuted() == false")
	public void getRabbitMqVhostQueueList(MwQueueManageEvent event) {
		List<IQueueNameModel> modelList = event.getModelList();
		
		for(IQueueNameModel model : modelList) {
			String cudFlag = model.getCudFlag_();
			String siteCd = model.getDomainSite();
			
			if(cudFlag.equalsIgnoreCase(SysConstants.CUD_FLAG_CREATE)) {
				this.requestAddSystemQueue(siteCd, model.getQueueName());
				
			} else if(cudFlag.equalsIgnoreCase(SysConstants.CUD_FLAG_UPDATE)) {
				this.requestRemoveSystemQueue(siteCd, model.getBefQueueName());
				this.adminService.deleteQueue(siteCd, model.getBefQueueName(), true);
				this.requestAddSystemQueue(siteCd, model.getQueueName());
				
			} else if(cudFlag.equalsIgnoreCase(SysConstants.CUD_FLAG_DELETE)) {
				this.requestRemoveSystemQueue(siteCd, model.getQueueName());
				this.adminService.deleteQueue(siteCd, model.getQueueName(), true);
			}
		}
		
		event.setExecuted(true);
	}
	
	/**
	 * RabbitMQ 서버에 Virtual Host에 큐 생성 요청
	 * 
	 * @param vHost
	 * @param queueName
	 */
	private void requestAddSystemQueue(String vHost, String queueName) {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(3000);
		factory.setConnectionRequestTimeout(3000);
		
		RestTemplate rest = new RestTemplate(factory);
		String managerAddr = SettingUtil.getValue("mq.manager.system.addresses", "localhost");
		String[] managerAddrs = managerAddr.split(SysConstants.COMMA);
		
		for(String addr : managerAddrs) {
			try {
				Map<String, Object> params = ValueUtil.newMap("addr,port,vhost,queue", addr, this.properties.getManagerPort(), vHost, queueName);
				boolean restRes = (ValueUtil.isEqualIgnoreCase(addr, "localhost") || ValueUtil.isEqualIgnoreCase(addr, "127.0.0.1")) ?
								this.addSystemQueue(vHost, queueName) : 
								rest.getForObject(this.addSystemQueueUrl, Boolean.class, params);
				logger.info(String.format("Add System Queue : %s , addr : %s , result %s", queueName, addr, restRes));
				
			} catch(Exception e) {
				logger.info(String.format("Add System Queue : %s , addr : %s , result %s \n%s", queueName, addr, "err", e.getMessage()));
			}
		}
	}
	
	/**
	 * RabbitMQ 서버에 Virtual Host에 큐 삭제 요청
	 * 
	 * @param vHost
	 * @param queueName
	 */
	private void requestRemoveSystemQueue(String vHost, String queueName) {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(3000);
		factory.setConnectionRequestTimeout(3000);
		
		RestTemplate rest = new RestTemplate(factory);
		String managerAddr = SettingUtil.getValue("mq.manager.system.addresses", "localhost");
		String[] managerAddrs = managerAddr.split(SysConstants.COMMA);

		for(String addr : managerAddrs) {
			try {
				Map<String, Object> params = ValueUtil.newMap("addr,port,vhost,queue", addr, this.properties.getManagerPort(), vHost, queueName);
				// TODO JWT 인증 문제 해결 필요
				boolean restRes = (ValueUtil.isEqualIgnoreCase(addr, "localhost") || ValueUtil.isEqualIgnoreCase(addr, "127.0.0.1")) ?
								this.removeSystemQueue(vHost, queueName) : 
								rest.getForObject(this.removeSystemQueueUrl, Boolean.class, params);
				logger.info(String.format("Remove System Queue : %s , addr : %s , result %s", queueName, addr, restRes));
				
			} catch(Exception e) {
				logger.info(String.format("Remove System Queue : %s , addr : %s , result %s \n%s", queueName, addr, "err", e.getMessage()));
			}
		}
	}
}