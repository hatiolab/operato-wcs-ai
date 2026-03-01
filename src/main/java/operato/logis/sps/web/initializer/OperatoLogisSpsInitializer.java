/* Copyright © HatioLab Inc. All rights reserved. */
package operato.logis.sps.web.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import operato.logis.sps.config.ModuleProperties;
import operato.logis.sps.query.store.SpsQueryStore;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;

/**
 * Operato Logis 단포 모듈 Startup시 Framework 초기화 클래스 
 * 
 * @author shortstop
 */
@Component
public class OperatoLogisSpsInitializer {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(OperatoLogisSpsInitializer.class);
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
	 * 단포 모듈
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
	 * 단포 분류용 쿼리 스토어
	 */	
	@Autowired
	private SpsQueryStore spsQueryStore;
	
	@EventListener({ ContextRefreshedEvent.class })
	public void refresh(ContextRefreshedEvent event) {
		this.logger.info("Operato Logistics SPS module refreshing...");
		
		this.logger.info("Operato Logistics SPS refreshed!");
	}
	
	@EventListener({ApplicationReadyEvent.class})
	void ready(ApplicationReadyEvent event) {
		this.logger.info("Operato Logistics SPS module initializing...");
		
		this.configSet.addConfig(this.module.getName(), this.module);
		this.scanServices();
		this.initQueryStores();
		this.logger.info("Operato Logistics SPS module initialized!");
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
		this.spsQueryStore.initQueryStore(dbType);
	}

}