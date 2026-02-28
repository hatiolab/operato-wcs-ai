package xyz.elidom.mw.rabbitmq.rest;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import xyz.anythings.sys.event.EventPublisher;
import xyz.anythings.sys.event.model.MwQueueListEvent;
import xyz.anythings.sys.event.model.MwQueueManageEvent;
import xyz.anythings.sys.model.BaseResponse;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.mw.rabbitmq.client.BrokerSiteAdmin;
import xyz.elidom.mw.rabbitmq.client.SystemClient;
import xyz.elidom.mw.rabbitmq.client.TraceDead;
import xyz.elidom.mw.rabbitmq.client.TraceDeliver;
import xyz.elidom.mw.rabbitmq.client.TracePublish;
import xyz.elidom.mw.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.mw.rabbitmq.entity.Site;
import xyz.elidom.mw.rabbitmq.event.model.IQueueNameModel;
import xyz.elidom.mw.rabbitmq.event.model.MwQueueNameModel;
import xyz.elidom.mw.rabbitmq.model.SystemQueueNameModel;
import xyz.elidom.mw.rabbitmq.service.BrokerAdminService;
import xyz.elidom.mw.rabbitmq.service.ServiceUtil;
import xyz.elidom.mw.rabbitmq.service.model.VirtualHost;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 사이트 (Virtual Host) 관리 컨트롤러
 *  
 * @author yang
 */
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/rabbitmq/vhost")
@ServiceDesc(description = "Mq Site Manager Service API")
public class VirtualHostController extends AbstractRestService {
	
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(VirtualHostController.class);
	/**
	 * BrokerAdmin 서비스
	 */
	@Autowired
	private BrokerAdminService adminServie;
	/**
	 * RabbitMQ 프로퍼티
	 */
	@Autowired
	private RabbitmqProperties mqProperties;
	/**
	 * 이벤트 퍼블리셔
	 */
	@Autowired
	private EventPublisher eventPublisher;
	/**
	 * Virtual Host Listener 추가 URL
	 */
	private String addListenUrl = "http://{addr}:{port}/rest/rabbitmq/vhost/addListener?vhost={vhost}";
	/**
	 * Virtual Host Listener 삭제 URL
	 */
	private String removeListenUrl = "http://{addr}:{port}/rest/rabbitmq/vhost/removeListener?vhost={vhost}";
	
	@Override
	protected Class<?> entityClass() {
		return Site.class;
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page,
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
	 * 도메인 정보로 부터 사이트 코드, 사이트 명 매핑 정보 리턴
	 * 
	 * @return
	 */
	public Map<String, String> getVirtualHostMap() {
		Query query = new Query();
		query.addSelect("site_code", "site_name");
		List<?> res = this.queryManager.selectList(this.entityClass(), query);
		
		Map<String, String> resultMap = new HashMap<String, String>();
		for(Object result : res) {
			Site site = (Site)result;
			resultMap.put(site.getSiteCode(), site.getSiteName());
		}
		
		return resultMap;
	}
	
	/**
	 * RabbitMQ Virtual Host 리스트 조회
	 * 
	 * @return
	 */
	public List<VirtualHost> getVhostList() {
		List<VirtualHost> list = this.adminServie.getVhostsList();
		Predicate<VirtualHost> hostPredicate = p -> p.getName().equals(SysConstants.DASH);
        list.removeIf(hostPredicate);
		return list;
	}
	
	/**
	 * Virtual Host 리스너 추가
	 * 
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
			
			if(this.mqProperties.getTraceUse()) {
				BeanUtil.get(TracePublish.class).addVirtualHost(vhost);
				BeanUtil.get(TraceDeliver.class).addVirtualHost(vhost);
				BeanUtil.get(TraceDead.class).addVirtualHost(vhost);
			}
			
			BeanUtil.get(SystemClient.class).addVirtualHost(vhost, queueList);
		}
		
		return true;
	}
	
	/**
	 * Virtual Host 리스너 삭제
	 * 
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
	 * Virtual Host 멀티플 업데이트
	 * 
	 * @param list
	 * @return
	 */
	@RequestMapping(value="/update_multiple", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Site> list) {
		// 1. CUD 처리 
		boolean result = this.cudMultipleData(this.entityClass(), list);
		
		// 2. RabbitMQ에 Virtual Host 추가, 삭제 처리
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(3000);
		factory.setConnectionRequestTimeout(3000);
		
		RestTemplate rest = new RestTemplate(factory);
		String managerAddr = SettingUtil.getValue("mq.manager.system.addresses", "localhost");
		String[] managerAddrs = managerAddr.split(SysConstants.COMMA);
		
		for(Site site : list) {
			String cudFlag = site.getCudFlag_();
			
			if(cudFlag.equalsIgnoreCase(SysConstants.CUD_FLAG_DELETE)) {
				for(String addr : managerAddrs) {
					try {
						Map<String, Object> removeParams = ValueUtil.newMap("addr,port,vhost", addr, this.mqProperties.getManagerPort(), site.getSiteCode());
						boolean restRes = rest.getForObject(this.removeListenUrl, Boolean.class, removeParams);
						this.logger.info(String.format("remove listen site : %s , addr : %s , result %s", site.getSiteCode(), addr, restRes));
						
					} catch(Exception e) {
						this.logger.info(String.format("remove listen site : %s , addr : %s , result %s \n%s", site.getSiteCode(), addr, "err", e.getMessage()));
					}
				}
				
				if(site.getExistsBroker()) {
					this.adminServie.removeVhost(site.getSiteCode());
				}
			
			} else if (cudFlag.equalsIgnoreCase(SysConstants.CUD_FLAG_CREATE)) {
				this.adminServie.addVhost(site.getSiteCode(), ValueUtil.isEmpty(site.getUseTrace()) ? false : true);
				
				for(String addr : managerAddrs) {
					try {
						boolean restRes = rest.getForObject(this.addListenUrl, Boolean.class, ValueUtil.newMap("addr,port,vhost", addr,this.mqProperties.getManagerPort(), site.getSiteCode()));
						logger.info(String.format("add listen site : %s , addr : %s , result %s", site.getSiteCode(), addr, restRes));
					} catch(Exception e) {
						logger.info(String.format("add listen site : %s , addr : %s , result %s \n%s", site.getSiteCode(), addr, "err", e.getMessage()));
					}
				}				
			} else if (cudFlag.equalsIgnoreCase(SysConstants.CUD_FLAG_UPDATE)) {
				this.adminServie.setVirtualHostTrace(ValueUtil.isEmpty(site.getUseTrace()) ? false : true, site.getSiteCode());
			}
		}
		
		return result;
	}
	
	/**
	 * 도메인 M/W 사이트 기본 큐 생성
	 * 
	 * @param site
	 * @return
	 */
	@RequestMapping(value="/{site_id}/add/default_queue", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Add default queue")
	public BaseResponse addDefaultQueue() {
		Domain domain = Domain.currentDomain();
		
		if(ValueUtil.isNotEmpty(domain.getMwSiteCd())) {
			String defaultQueueName = domain.getId() + SysConstants.DASH + domain.getMwSiteCd();
			return new BaseResponse(this.handleDefaultQueue(domain, SysConstants.CUD_FLAG_CREATE, defaultQueueName));
		} else {
			throw new ElidomRuntimeException("도메인에 미들웨어 사이트 코드가 설정되지 않았습니다.");
		}
	}
	
	/**
	 * 도메인 M/W 사이트 기본 큐 삭제
	 * 
	 * @param site
	 * @return
	 */
	@RequestMapping(value="/{site_id}/remove/default_queue", method=RequestMethod.DELETE, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Remove default queue")
	public BaseResponse removeDefaultQueue() {
		Domain domain = Domain.currentDomain();
		
		if(ValueUtil.isNotEmpty(domain.getMwSiteCd())) {
			String defaultQueueName = domain.getId() + SysConstants.DASH + domain.getMwSiteCd();
			return new BaseResponse(this.handleDefaultQueue(domain, SysConstants.CUD_FLAG_DELETE, defaultQueueName));
		} else {
			throw new ElidomRuntimeException("도메인에 미들웨어 사이트 코드가 설정되지 않았습니다.");
		}
	}
		
	/**
	 * Virtual Host 초기 생성 큐 리스트 조회
	 * 
	 * @param domain
	 * @return
	 */
	private List<SystemQueueNameModel> setVhostQueueList(Domain domain) {
		List<SystemQueueNameModel> systemQueueList = new ArrayList<SystemQueueNameModel>();
		Long domainId = domain.getId();
		String mwWiteCd = domain.getMwSiteCd();
		
		// 기본 도메인 큐 생성 
		systemQueueList.add(new SystemQueueNameModel(mwWiteCd, domainId + SysConstants.DASH + mwWiteCd));
		
		// 초기 생성 큐 리스트 요청 이벤트
		MwQueueListEvent initEvent = new MwQueueListEvent(domainId);
		initEvent.setExecuted(false);
		initEvent = (MwQueueListEvent)this.eventPublisher.publishEvent(initEvent);
		
		if(initEvent.isExecuted()) {
			for(IQueueNameModel model : initEvent.getQueueNames()) {
				systemQueueList.add(new SystemQueueNameModel(mwWiteCd, model.getQueueName()));
			}
		}
		
		return systemQueueList;
	}
	
	/**
	 * 기본 큐 생성 or 삭제
	 * 
	 * @param domain
	 * @param cudFlag
	 * @param queueType
	 * @return
	 */
	private boolean handleDefaultQueue(Domain domain, String cudFlag, String queueName) {
		Long domainId = domain.getId();
		String mwSiteCd = domain.getMwSiteCd();		
		List<IQueueNameModel> queueModels = ValueUtil.toList(new MwQueueNameModel(domainId, mwSiteCd, null, queueName, cudFlag));		
		MwQueueManageEvent event = new MwQueueManageEvent(domainId, queueModels);
		this.eventPublisher.publishEvent(event);
		return true;
	}
}