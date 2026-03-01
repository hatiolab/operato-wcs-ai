package operato.logis.sms.web.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import operato.logis.sms.config.ModuleProperties;
import operato.logis.sms.query.SmsQueryStore;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;

/**
 * Operato Logis SMS Startup시 Framework 초기화 클래스 
 * 
 * @author yang
 */
@Component
public class OperatoLogisSmsInitializer {

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(OperatoLogisSmsInitializer.class);
	
	@Autowired
	@Qualifier("rest")
	private IServiceFinder restFinder;
	
	@Autowired
	private IEntityFieldCache entityFieldCache;
	
	@Autowired
	private ModuleProperties module;
	
	@Autowired
	private ModuleConfigSet configSet;
	
	@Autowired
	private IQueryManager queryManager;
	
	@Autowired
	private SmsQueryStore smsQueryStore;
	
	@EventListener({ ContextRefreshedEvent.class })
	public void refresh(ContextRefreshedEvent event) {
		this.logger.info("Operato Logistics SMS module refreshing...");
		
		this.configSet.addConfig(this.module.getName(), this.module);
		this.scanServices();
		
		this.logger.info("Operato Logistics SMS module refreshed!");
	}
	
	@EventListener({ApplicationReadyEvent.class})
    void ready(ApplicationReadyEvent event) {
		this.logger.info("Operato Logistics SMS module initializing...");
		
		this.initQueryStores();
		
		this.logger.info("Operato Logistics SMS module initialized!");
    }
	
	/**
	 * 모듈 서비스 스캔 
	 */
	private void scanServices() {
		this.entityFieldCache.scanEntityFieldsByBasePackage(this.module.getBasePackage());
		this.restFinder.scanServicesByPackage(this.module.getName(), this.module.getBasePackage());
	}
	
	/**
	 * 쿼리 스토어 초기화
	 */
	private void initQueryStores() {
		String dbType = this.queryManager.getDbType();
		this.smsQueryStore.initQueryStore(dbType);
	}
	
}
