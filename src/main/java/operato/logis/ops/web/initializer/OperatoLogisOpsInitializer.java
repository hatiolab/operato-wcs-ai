/* Copyright © HatioLab Inc. All rights reserved. */
package operato.logis.ops.web.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import operato.logis.ops.config.ModuleProperties;
import operato.logis.ops.query.store.OpsQueryStore;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;

/**
 * Operato Logis 오더 피킹 모듈 Startup시 Framework 초기화 클래스 
 * 
 * @author shortstop
 */
@Component
public class OperatoLogisOpsInitializer {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(OperatoLogisOpsInitializer.class);
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
	 * 오더 피킹 분류용 쿼리 스토어
	 */	
	@Autowired
	private OpsQueryStore opsQueryStore;
	
	@EventListener({ ContextRefreshedEvent.class })
	public void refresh(ContextRefreshedEvent event) {
		this.logger.info("Operato Logistics OPS module refreshing...");
		
		this.configSet.addConfig(this.module.getName(), this.module);
		this.scanServices();
		
		this.logger.info("Operato Logistics OPS refreshed!");
	}
	
	@EventListener({ApplicationReadyEvent.class})
	void ready(ApplicationReadyEvent event) {
		this.logger.info("Operato Logistics OPS module initializing...");
		
		this.initQueryStores();
		
		this.logger.info("Operato Logistics OPS module initialized!");
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
		this.opsQueryStore.initQueryStore(dbType);
	}

}