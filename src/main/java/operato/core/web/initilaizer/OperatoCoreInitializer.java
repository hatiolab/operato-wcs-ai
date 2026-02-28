package operato.core.web.initilaizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import operato.core.config.OperatoCoreModuleProperties;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.config.module.IModuleProperties;
import xyz.elidom.sys.system.service.api.IServiceFinder;

/**
 * Operato Core 모듈 Startup시 Framework 초기화 클래스 
 * 
 * @author shortstop
 */
@Component
public class OperatoCoreInitializer {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(OperatoCoreInitializer.class);
	/**
	 * Operato Core 모듈 프로퍼티
	 */
	@Autowired
	private OperatoCoreModuleProperties module;
	/**
	 * 모듈 셋
	 */
	@Autowired
	private ModuleConfigSet configSet;
	/**
	 * 서비스 파인더
	 */
	@Autowired
	@Qualifier("rest")
	private IServiceFinder restFinder;

	@EventListener({ ContextRefreshedEvent.class })
	public void ready(ContextRefreshedEvent event) {
		this.logger.info("Operato Core module initializing ready...");
		IModuleProperties mainModule = this.configSet.getApplicationModule();
		if(mainModule == null) {
			this.configSet.addConfig(this.module.getName(), this.module);
			this.configSet.setApplicationModule(this.module.getName());
		}
	}
	
	@EventListener({ApplicationReadyEvent.class})
    void contextRefreshedEvent(ApplicationReadyEvent event) {
		logger.info("Operato Core module initializing started...");
		
		// TODO
		
		logger.info("Operato Core module initializing finished");
    }
}
