package operato.logis.bms.web.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import operato.logis.bms.config.ModuleProperties;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;

/**
 * Operato Logis BMS Startup시 Framework 초기화 클래스 
 * 
 * @author yang
 */
@Component
public class OperatoLogisBmsInitializer {

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(OperatoLogisBmsInitializer.class);
	
	@Autowired
	@Qualifier("rest")
	private IServiceFinder restFinder;
	
	@Autowired
	private IEntityFieldCache entityFieldCache;
	
	@Autowired
	private ModuleProperties module;
	
	@Autowired
	private ModuleConfigSet configSet;
	
	@EventListener({ ContextRefreshedEvent.class })
	public void refresh(ContextRefreshedEvent event) {
		this.logger.info("Operato Logistics BMS module refreshing...");
		
		this.configSet.addConfig(this.module.getName(), this.module);
		this.scanServices();

		this.logger.info("Operato Logistics BMS module refreshed!");
	}
	
	@EventListener({ApplicationReadyEvent.class})
	void ready(ApplicationReadyEvent event) {
		this.logger.info("Operato Logistics BMS module initializing...");
		
		this.logger.info("Operato Logistics BMS module initialized!");
	}
	
	/**
	 * 모듈 서비스 스캔 
	 */
	private void scanServices() {
		this.entityFieldCache.scanEntityFieldsByBasePackage(this.module.getBasePackage());
		this.restFinder.scanServicesByPackage(this.module.getName(), this.module.getBasePackage());
	}

}
