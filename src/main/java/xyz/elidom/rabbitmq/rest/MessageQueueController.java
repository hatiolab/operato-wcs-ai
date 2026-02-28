package xyz.elidom.rabbitmq.rest;


import java.util.ArrayList;
import java.util.List;

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

import xyz.anythings.comm.rabbitmq.event.MwQueueManageEvent;
import xyz.anythings.comm.rabbitmq.event.model.IQueueNameModel;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.rabbitmq.client.SystemClient;
import xyz.elidom.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.rabbitmq.service.BrokerAdminService;
import xyz.elidom.rabbitmq.service.ServiceUtil;
import xyz.elidom.rabbitmq.service.model.Queue;
import xyz.elidom.rabbitmq.service.model.QueueItem;
import xyz.elidom.rabbitmq.service.model.QueueSearch;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 메시지 큐 관리 
 * @author yang
 *
 */
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/rabbitmq/queue")
@ServiceDesc(description = "Mq Message Queue Manager Service API")
public class MessageQueueController extends AbstractRestService {
	
	@Autowired
	BrokerAdminService adminServie ;
	
	@Autowired
	RabbitmqProperties properties;
	
	@Autowired
	RabbitmqProperties mqProperties;
	
	private Logger logger = LoggerFactory.getLogger(MessageQueueController.class);
	
	
	private String addSystemQueueUrl = "http://{addr}:{port}/rest/rabbitmq/queue/addSystemQueue?vhost={vhost}&queue={queue}";
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
		String vhost = "";
		String queueName = "";
		
		if (ValueUtil.isNotEmpty(query)) querys = FormatUtil.jsonToObject(query, Filter[].class);
		
		for(Filter filter : querys) {
			if(filter.getName().equalsIgnoreCase("site_code")) vhost = filter.getValue().toString();
			else if (filter.getName().equalsIgnoreCase("queue_name")) queueName = filter.getValue().toString();
		}
		
		// queue list 조회 
		QueueSearch queues = this.adminServie.getQueueList(vhost, page , limit, queueName);
		
		List<QueueItem> items = new ArrayList<QueueItem>();
		
		// filter 조건 적용 
		for(Queue queue : queues.getItems()) {
			QueueItem item = new QueueItem();
			String siteCode = queue.getVhost();
			
			String itemQueueName = queue.getName().replaceAll("mqtt-subscription-", "").replaceAll("qos1", "");
			
			boolean isSystemQueue = false;
			
			if(properties.isSystemQueue(itemQueueName)) isSystemQueue = true;
			else if (itemQueueName.startsWith("trace")) isSystemQueue = true;
			
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
	 * @param vhost
	 * @param queue
	 * @param is_system_queue
	 * @return
	 */
	@RequestMapping(value="/{vhost}/{queue}/{is_system_queue}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Queue Message purge")
	public Boolean purgeQueue(@PathVariable("vhost") String vhost,
			@PathVariable("queue") String queue,
			@PathVariable("is_system_queue") boolean is_system_queue) {
		
		queue = queue.replaceAll("\\.", "/");
		
		this.adminServie.purgeQueue(vhost, queue, is_system_queue);
		return true;
	}
	
	/**
	 * 큐 추가 삭제 
	 * @param list
	 * @return
	 */
	@RequestMapping(value="/update_multiple", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<QueueItem> list) {
		for(QueueItem item : list) {
			if(item.getCudFlag_().equalsIgnoreCase("d")) {
				this.adminServie.deleteQueue(item.getSiteCode(), item.getQueueName(), item.getIsSystemQueue());
			} else if(item.getCudFlag_().equalsIgnoreCase("c")) {
				this.adminServie.createQueue(item.getSiteCode(), item.getQueueName(), false);
			}
		}
		return true;
	}
	
	
	/**
	 * 사이트의 시스템 큐 추가 
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
	 * 사이트의 시스템 큐 제거 
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
	 * @param event
	 */
	@EventListener(condition = "#event.isExecuted() == false")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void getRabbitMqVhostQueueList(MwQueueManageEvent event) {
		
		List<IQueueNameModel> modelList = event.getModelList();
		
		for(IQueueNameModel model : modelList) {
			if(model.getCudFlag_().equalsIgnoreCase("c")) {
				this.requestAddSystemQueue(model.getDomainSite(), model.getQueueName());
			} else if( model.getCudFlag_().equalsIgnoreCase("u")) {
				this.requestRemoveSystemQueue(model.getDomainSite(), model.getBefQueueName());
				this.adminServie.deleteQueue(model.getDomainSite(), model.getBefQueueName(), true);
				this.requestAddSystemQueue(model.getDomainSite(), model.getQueueName());
			} else if (model.getCudFlag_().equalsIgnoreCase("d")) {
				this.requestRemoveSystemQueue(model.getDomainSite(), model.getQueueName());
				this.adminServie.deleteQueue(model.getDomainSite(), model.getQueueName(), true);
			}
		}
		
		event.setExecuted(true);
	}
	
	private void requestAddSystemQueue(String vHost, String queueName) {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(3000);
		// setReadTimeout deprecated in Spring 6.x - timeout handled by HttpClient configuration
		
		RestTemplate rest = new RestTemplate(factory);

		String managerAddr = SettingUtil.getValue("mq.manager.system.addresses", "localhost");
		String[] managerAddrs = managerAddr.split(",");
		
		for(String addr : managerAddrs){
			try {
				boolean restRes = rest.getForObject(this.addSystemQueueUrl, Boolean.class, 
						ValueUtil.newMap("addr,port,vhost,queue", addr,this.mqProperties.getManagerPort(), vHost, queueName));
				logger.info(String.format("add System Queue : %s , addr : %s , result %s", queueName, addr, restRes));
			}catch(Exception e) {
				logger.info(String.format("add System Queue : %s , addr : %s , result %s \n%s", queueName, addr, "err", e.getMessage()));
			}
		}
	}
	
	
	private void requestRemoveSystemQueue(String vHost, String queueName) {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(3000);
		// setReadTimeout deprecated in Spring 6.x - timeout handled by HttpClient configuration
		
		RestTemplate rest = new RestTemplate(factory);

		String managerAddr = SettingUtil.getValue("mq.manager.system.addresses", "localhost");
		String[] managerAddrs = managerAddr.split(",");

		for(String addr : managerAddrs){
			try {
				boolean restRes = rest.getForObject(this.removeSystemQueueUrl, Boolean.class, 
						ValueUtil.newMap("addr,port,vhost,queue", addr,this.mqProperties.getManagerPort(), vHost, queueName));
				logger.info(String.format("remove System Queue : %s , addr : %s , result %s", queueName, addr, restRes));
			}catch(Exception e) {
				logger.info(String.format("remove System Queue : %s , addr : %s , result %s \n%s", queueName, addr, "err", e.getMessage()));
			}
		}
	}
}