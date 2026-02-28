package xyz.elidom.print.web.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import xyz.elidom.mw.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.print.config.PrintModuleProperties;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;

/**
 * Print Startup시 Framework 초기화 클래스 
 * 
 * @author shortstop
 */
@Component
public class ElingsPrintInitializer {
    /**
     * Logger
     */
    private Logger logger = LoggerFactory.getLogger(ElingsPrintInitializer.class);
    
    @Autowired
    @Qualifier("rest")
    private IServiceFinder restFinder;
    
    @Autowired
    private IEntityFieldCache entityFieldCache;
    
    @Autowired
    private PrintModuleProperties module;
    
	@Autowired
	private RabbitmqProperties properties;
    
    @Autowired
    private ModuleConfigSet configSet;
            
    @EventListener({ ContextRefreshedEvent.class })
    public void refresh(ContextRefreshedEvent event) {
        this.logger.info("Operato Print module refreshing...");
        
        this.configSet.addConfig(this.module.getName(), this.module);
        this.scanServices();        
        
        this.logger.info("Operato Print module refreshed!");
    }
    
    @EventListener({ApplicationReadyEvent.class})
    void ready(ApplicationReadyEvent event) {
		this.logger.info("Operato Print module initializing started...");
		
    	// 1. MQ 모듈 활성화 여부 체크
		if(!this.properties.isUseMqModule()) {
			return;
		}
		
        this.logger.info("Operato Print module initialized!");
    }
    
    /**
     * 모듈 서비스 스캔 
     */
    private void scanServices() {
        this.entityFieldCache.scanEntityFieldsByBasePackage(this.module.getBasePackage());
        this.restFinder.scanServicesByPackage(this.module.getName(), this.module.getBasePackage());
    }
}
