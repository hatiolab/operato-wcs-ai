/* Copyright © HatioLab Inc. All rights reserved. */
package operato.logis.pdas.web.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import operato.logis.pdas.config.ModuleProperties;
import operato.logis.pdas.query.store.PdasBatchQueryStore;
import operato.logis.pdas.query.store.PdasPickQueryStore;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;

/**
 * Operato Logis P-DAS Startup시 Framework 초기화 클래스 
 * 
 * @author shortstop
 */
@Component
public class OperatoLogisPdasInitializer {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(OperatoLogisPdasInitializer.class);
	/**
	 * RESTful Finder
	 */
	@Autowired
	@Qualifier("rest")
	private IServiceFinder restFinder;
	/**
	 * 엔티티 캐쉬
	 */
	@Autowired
	private IEntityFieldCache entityFieldCache;
	/**
	 * P-DAS 모듈
	 */
	@Autowired
	private ModuleProperties module;
	/**
	 * 모듈 컨피그 셋
	 */
	@Autowired
	private ModuleConfigSet configSet;
	/**
	 * 쿼리 매니저
	 */
	@Autowired
	private IQueryManager queryManager;
	/**
	 * P-DAS 배치용 쿼리 스토어
	 */	
	@Autowired
	private PdasBatchQueryStore pdasBatchQueryStore;
	/**
	 * P-DAS 피킹용 쿼리 스토어
	 */
	@Autowired
	private PdasPickQueryStore pdasPickQueryStore;
	
	@EventListener({ ContextRefreshedEvent.class })
	public void refresh(ContextRefreshedEvent event) {
		this.logger.info("Operato Logistics P-DAS module refreshing...");
		
		this.configSet.addConfig(this.module.getName(), this.module);
		this.scanServices();
		
		this.logger.info("Operato Logistics P-DAS module refreshed!");
	}
	
	@EventListener({ApplicationReadyEvent.class})
	void ready(ApplicationReadyEvent event) {
		this.logger.info("Operato Logistics P-DAS module initializing...");
		
		this.initQueryStores();
		
		this.logger.info("Operato Logistics P-DAS module initialized!");
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
		this.pdasBatchQueryStore.initQueryStore(dbType);
		this.pdasPickQueryStore.initQueryStore(dbType);
	}

}