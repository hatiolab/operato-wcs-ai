package xyz.elidom.rabbitmq.rest;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import xyz.anythings.comm.rabbitmq.event.MwLogisQueueListEvent;
import xyz.anythings.comm.rabbitmq.event.model.IQueueNameModel;
import xyz.anythings.comm.rabbitmq.model.SystemQueueNameModel;
import xyz.anythings.sys.event.EventPublisher;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.rabbitmq.client.BrokerSiteAdmin;
import xyz.elidom.rabbitmq.client.SystemClient;
import xyz.elidom.rabbitmq.client.TraceDead;
import xyz.elidom.rabbitmq.client.TraceDeliver;
import xyz.elidom.rabbitmq.client.TracePublish;
import xyz.elidom.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.rabbitmq.entity.Site;
import xyz.elidom.rabbitmq.service.BrokerAdminService;
import xyz.elidom.rabbitmq.service.ServiceUtil;
import xyz.elidom.rabbitmq.service.model.VirtualHost;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 사이트 관리 레스트 서비스 
 * @author yang
 *
 */
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/rabbitmq/vhost")
@ServiceDesc(description = "Mq Site Manager Service API")
public class VirtualHostController extends AbstractRestService {
	
	@Autowired
	BrokerAdminService adminServie ;
	
	@Autowired
	RabbitmqProperties mqProperties;
	
	@Autowired
	private EventPublisher eventPublisher;

		
	private Logger logger = LoggerFactory.getLogger(VirtualHostController.class);
	
	
	private String addListenUrl = "http://{addr}:{port}/rest/rabbitmq/vhost/addListener?vhost={vhost}";
	private String removeListenUrl = "http://{addr}:{port}/rest/rabbitmq/vhost/removeListener?vhost={vhost}";
	
	@Override
	protected Class<?> entityClass() {
		return Site.class;
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
		Page<?> searchRes = this.search(this.entityClass(), page, limit, select, sort, query);
		
		List<VirtualHost> vHostList = this.getVhostList();
		
		for(Object obj : searchRes.getList()) {
			Site site = (Site)obj;
			
			boolean existsBroker = false;
			for(VirtualHost host : vHostList) {
				if(ValueUtil.isEqualIgnoreCase(host.getName(), site.getSiteCode())) {
					existsBroker = true;
					
					site.setNetworkFromClient(ServiceUtil.valueToByteStringPerSec(host.getRecvOctDetails()));
					site.setNetworkToClient(ServiceUtil.valueToByteStringPerSec(host.getSendOctDetails()));
					
					break;
				}
			}
			site.setExistsBroker(existsBroker);
		}
		
		return searchRes;
	}
	
	/**
	 * 사이트 코드 , 명 to map 
	 * @return
	 */
	public HashMap<String,String> getVirtualHostMap(){
		Query query = new Query();
		query.addSelect("site_code", "site_name");
		
		List<?> res = this.queryManager.selectList(this.entityClass(), query);
		
		HashMap<String, String> resultMap = new HashMap<String,String>();

		for(Object result : res) {
			Site site = (Site)result;
			resultMap.put(site.getSiteCode(), site.getSiteName());
		}
		
		return resultMap;
	}
	
	/**
	 * 브로커 사이트 리스트 조회 
	 * @return
	 */
	public List<VirtualHost> getVhostList(){
		List<VirtualHost> list = this.adminServie.getVhostsList();
		
		Predicate<VirtualHost> hostPredicate = p-> p.getName().equals("/");
        list.removeIf(hostPredicate);
        
		return list;
	}
	
	/**
	 * 사이트 리스너 추가 
	 * @param vhost
	 * @return
	 */
	@RequestMapping(value = "/addListener", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Add Virtual host Trace, System Listener")
	public boolean addVhostListener(@RequestParam(name="vhost") String vhost) {
		
		Domain domain = Domain.findByMwSiteCd(vhost);
		
		// 사이트 코드로 도메인 검색에 성공 하면 큐 생성
		if(ValueUtil.isNotEmpty(domain)) {
			List<SystemQueueNameModel> queueList = this.setVhostQueueList(domain);
			
			BeanUtil.get(BrokerSiteAdmin.class).addVirtualHost(vhost);
			
			if(this.mqProperties.getTraceUse() == true) {
				BeanUtil.get(TracePublish.class).addVirtualHost(vhost);
				BeanUtil.get(TraceDeliver.class).addVirtualHost(vhost);
				BeanUtil.get(TraceDead.class).addVirtualHost(vhost);
			}
			
			BeanUtil.get(SystemClient.class).addVirtualHost(vhost,queueList);
		}
		
		return true;
	}
	
	/**
	 * 사이트 리스너 삭제 
	 * @param vhost
	 * @return
	 */
	@RequestMapping(value = "/removeListener", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Remove Virtual host Trace, System Listener")
	public boolean removeVhostListener(@RequestParam(name="vhost") String vhost) {
		BeanUtil.get(BrokerSiteAdmin.class).removeVirtualHost(vhost);
		
		BeanUtil.get(TracePublish.class).removeVirtualHost(vhost);
		BeanUtil.get(TraceDeliver.class).removeVirtualHost(vhost);
		BeanUtil.get(TraceDead.class).removeVirtualHost(vhost);
		
		BeanUtil.get(SystemClient.class).removeVirtualHost(vhost);
		
		return true;
	}
	
	/**
	 * 사이트 추가 삭제 
	 * @param list
	 * @return
	 */
	@RequestMapping(value="/update_multiple", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Site> list) {
		
		boolean result = this.cudMultipleData(this.entityClass(), list);
		
		/**
		 * TODO : 추가 / 삭제 시 여러 대의 operato-server에다가 추가/삭제 명령을 날려야 함.......
		 */
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(3000);
		// factory.setReadTimeout(3000); // deprecated in Spring 6.x
		
		RestTemplate rest = new RestTemplate(factory);
		
		String managerAddr = SettingUtil.getValue("mq.manager.system.addresses", "localhost");
		String[] managerAddrs = managerAddr.split(",");
		
		for(Site site : list) {
			if(site.getCudFlag_().equalsIgnoreCase("d")) {
				this.removeVhostListener(site.getSiteCode());
				
				for(String addr : managerAddrs){
					try {
						boolean restRes = rest.getForObject(this.removeListenUrl, Boolean.class, ValueUtil.newMap("addr,port,vhost", addr,this.mqProperties.getManagerPort(), site.getSiteCode()));
						logger.info(String.format("remove listen site : %s , addr : %s , result %s", site.getSiteCode(), addr, restRes));
					}catch(Exception e) {
						logger.info(String.format("remove listen site : %s , addr : %s , result %s \n%s", site.getSiteCode(), addr, "err", e.getMessage()));
					}
				}
				
				if(site.getExistsBroker()) this.adminServie.removeVhost(site.getSiteCode());
			} else if (site.getCudFlag_().equalsIgnoreCase("c")) {
				this.adminServie.addVhost(site.getSiteCode(), site.getUseTrace());

				for(String addr : managerAddrs){
					try {
						boolean restRes = rest.getForObject(this.addListenUrl, Boolean.class, ValueUtil.newMap("addr,port,vhost", addr,this.mqProperties.getManagerPort(), site.getSiteCode()));
						logger.info(String.format("add listen site : %s , addr : %s , result %s", site.getSiteCode(), addr, restRes));
					}catch(Exception e) {
						logger.info(String.format("add listen site : %s , addr : %s , result %s \n%s", site.getSiteCode(), addr, "err", e.getMessage()));
					}
				}
				
				this.addVhostListener(site.getSiteCode());
			} else if (site.getCudFlag_().equalsIgnoreCase("u")) {
				this.adminServie.setVirtualHostTrace(site.getUseTrace(), site.getSiteCode());
			}
		}
		
		return result;
	}
	
	private List<SystemQueueNameModel> setVhostQueueList(Domain domain) {
		List<SystemQueueNameModel> systemQueueList = new ArrayList<SystemQueueNameModel>();
		
		// 초기 생성 큐 리스트 요청 이벤트 
		MwLogisQueueListEvent initEvent = new MwLogisQueueListEvent(domain.getId());
		initEvent = (MwLogisQueueListEvent)eventPublisher.publishEvent(initEvent);
		
		if(initEvent.isExecuted()) {
			for(IQueueNameModel queueModel : initEvent.getInitQueueNames()) {
				queueModel.setDomainSite(domain.getMwSiteCd());
				systemQueueList.add(new SystemQueueNameModel(domain.getMwSiteCd(), queueModel.getQueueName()));
			}
		}
		
		return systemQueueList;
	}
}