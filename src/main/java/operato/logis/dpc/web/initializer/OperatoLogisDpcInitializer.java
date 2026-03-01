/* Copyright © HatioLab Inc. All rights reserved. */
package operato.logis.dpc.web.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import operato.logis.dpc.config.ModuleProperties;
import operato.logis.dpc.query.store.DpcBatchQueryStore;
import operato.logis.dpc.query.store.DpcBoxQueryStore;
import operato.logis.dpc.query.store.DpcPickQueryStore;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;

/**
 * Operato Logis DPC Startup시 Framework 초기화 클래스 
 * 
 * @author shortstop
 */
@Component
public class OperatoLogisDpcInitializer {

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(OperatoLogisDpcInitializer.class);
	
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
	
	/**
	 * DPC 배치용 쿼리 스토어
	 */	
	@Autowired
	private DpcBatchQueryStore dpcBatchQueryStore;
	/**
	 * DPC 피킹용 쿼리 스토어
	 */
	@Autowired
	private DpcPickQueryStore dpcPickQueryStore;
	/**
	 * DPC 박스 처리용 쿼리 스토어
	 */	
	@Autowired
	private DpcBoxQueryStore dpcBoxQueryStore;
	
	@EventListener({ ContextRefreshedEvent.class })
	public void refresh(ContextRefreshedEvent event) {
		this.logger.info("Operato Logistics DPC module refreshing...");
		
		this.logger.info("Operato Logistics DPC module refreshed!");
	}
	
	@EventListener({ApplicationReadyEvent.class})
	void ready(ApplicationReadyEvent event) {
		this.logger.info("Operato Logistics DPC module initializing...");
		
		this.configSet.addConfig(this.module.getName(), this.module);
		this.scanServices();
		this.initQueryStores();
		this.logger.info("Operato Logistics DPC module initialized!");
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
		this.dpcBatchQueryStore.initQueryStore(dbType);
		this.dpcPickQueryStore.initQueryStore(dbType);
		this.dpcBoxQueryStore.initQueryStore(dbType);
	}

}