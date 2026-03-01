/* Copyright © HatioLab Inc. All rights reserved. */
package operato.logis.insp.web.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import operato.logis.insp.config.ModuleProperties;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;

/**
 * Operato Logis Inspection Startup시 Framework 초기화 클래스 
 * 
 * @author shortstop
 */
@Component
public class OperatoLogisInspInitializer {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(OperatoLogisInspInitializer.class);
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
	 * 출고 검수 모듈
	 */
	@Autowired
	private ModuleProperties module;
	/**
	 * 모듈 컨피그 셋
	 */
	@Autowired
	private ModuleConfigSet configSet;
	
	@EventListener({ ContextRefreshedEvent.class })
	public void refresh(ContextRefreshedEvent event) {
		this.logger.info("Operato Logistics Inspection module refreshing...");
		
		this.logger.info("Operato Logistics Inspection refreshed!");
	}
	
	@EventListener({ApplicationReadyEvent.class})
	void ready(ApplicationReadyEvent event) {
		this.logger.info("Operato Logistics Inspection module initializing...");
		
		this.configSet.addConfig(this.module.getName(), this.module);
		this.scanServices();
		this.logger.info("Operato Logistics Inspection module initialized!");
	}
	
	/**
	 * 모듈 서비스 스캔 
	 */
	private void scanServices() {
		this.entityFieldCache.scanEntityFieldsByBasePackage(this.module.getBasePackage());
		this.restFinder.scanServicesByPackage(this.module.getName(), this.module.getBasePackage());
	}

}