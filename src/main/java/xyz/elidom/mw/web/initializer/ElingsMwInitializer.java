package xyz.elidom.mw.web.initializer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import xyz.elidom.mw.config.MwModuleProperties;
import xyz.elidom.mw.rabbitmq.client.TraceDead;
import xyz.elidom.mw.rabbitmq.client.TraceDeliver;
import xyz.elidom.mw.rabbitmq.client.TracePublish;
import xyz.elidom.mw.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.mw.rabbitmq.logger.TraceDeleteManager;
import xyz.elidom.mw.rabbitmq.rest.VirtualHostController;
import xyz.elidom.mw.rabbitmq.service.ElasticRestHandler;
import xyz.elidom.mw.rabbitmq.service.model.VirtualHost;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * MW Startup시 Framework 초기화 클래스 
 * 
 * @author shortstop
 */
@Component
public class ElingsMwInitializer {
    /**
     * Logger
     */
    private Logger logger = LoggerFactory.getLogger(ElingsMwInitializer.class);
    
    @Autowired
    @Qualifier("rest")
    private IServiceFinder restFinder;
    
    @Autowired
    private IEntityFieldCache entityFieldCache;
    
    @Autowired
    private MwModuleProperties module;
    
	@Autowired
	private RabbitmqProperties properties;
    
    @Autowired
    private ModuleConfigSet configSet;
            
    @EventListener({ ContextRefreshedEvent.class })
    public void refresh(ContextRefreshedEvent event) {
        this.logger.info("Operato MW module refreshing...");
        
        this.configSet.addConfig(this.module.getName(), this.module);
        this.scanServices();        
        
        this.logger.info("Operato MW module refreshed!");
    }
    
    @EventListener({ApplicationReadyEvent.class})
    void ready(ApplicationReadyEvent event) {
    	// 1. MQ 모듈 활성화 여부 체크
		if(!this.properties.isUseMqModule()) {
			this.logger.info("Operato MW module is deactivated!");
			return;
		}
		this.logger.info("Operato MW module initializing started...");
		
		// 2. 트레이스 사용 유무에 따라서 트레이스 기능 초기화 유무 결정 
		if(this.properties.getTraceUse()) {
			// 2.1 엘라스틱 사용시 초기화 
			if(this.properties.getTraceType().equalsIgnoreCase("elastic")) {
				BeanUtil.get(ElasticRestHandler.class).setElasticClient();
			}
			
			this.logger.info("Middleware Message Trace Function is activated!");
			
			// 2.2 트레이스 설정 초기화
			BeanUtil.get(TracePublish.class).initLogger();
			BeanUtil.get(TraceDeliver.class).initLogger();
			BeanUtil.get(TraceDead.class).initLogger();
			
			// 2.3 삭제 메인 설정
			if(this.properties.isDeleteMain()) {
				BeanUtil.get(TraceDeleteManager.class).processStart();
				this.logger.info("Everyday this server will delete middleware trace message histories!");
			}
		} else {
			this.logger.info("Middleware Message Trace Function is deactivated!");
		}
		
		// 3. Virtual Host 리스트 조회
		VirtualHostController vCtrl = BeanUtil.get(VirtualHostController.class);
		List<VirtualHost> vHosts = vCtrl.getVhostList();
		
		if(ValueUtil.isNotEmpty(vHosts)) {
			List<String> vHostNames = new ArrayList<String>();
			
			// 4. 미들웨어로 부터 VirtualHost 리스트를 조회한 후 이 VirtualHost 이름이 도메인 정보에 존재하는지 체크하는 것만 VirtualHost 등록 
			for(VirtualHost vHost : vHosts) {
				Domain domain = Domain.findByMwSiteCd(vHost.getName());
				if(domain != null) {
					vHostNames.add(vHost.getName());
				}
			}
			
			// 5. Virtual Host 리스트 등록
			this.properties.setAppInitVHosts(vHostNames);
			for(String vHostName : vHostNames) {
				vCtrl.addVhostListener(vHostName);
			}
		}
		
        this.logger.info("Operato MW module initialized!");
    }
    
    /**
     * 모듈 서비스 스캔 
     */
    private void scanServices() {
        this.entityFieldCache.scanEntityFieldsByBasePackage(this.module.getBasePackage());
        this.restFinder.scanServicesByPackage(this.module.getName(), this.module.getBasePackage());
    }
    
	/**
	 * 기본 큐 생성 or 삭제
	 * 
	 * @param domain
	 * @param cudFlag
	 * @param queueType
	 * @return
	 */
	/*private boolean handleDefaultQueue(Domain domain, String cudFlag, String queueName) {
		Long domainId = domain.getId();
		String mwSiteCd = domain.getMwSiteCd();		
		List<Map<String, Object>> queueModels = new ArrayList<Map<String, Object>>();
		queueModels.add(ValueUtil.newMap("siteCd,queueName,cudFlag", mwSiteCd, queueName, cudFlag));						
		MwQueueManageEvent event = new MwQueueManageEvent(domainId, queueModels);
		this.eventPublisher.publishEvent(event);
		return true;
	}*/
}
