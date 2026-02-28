/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.job.web.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import xyz.elidom.job.config.JobModuleProperties;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.config.module.IModuleProperties;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;

/**
 * Elings Job Startup시 Framework 초기화 클래스 
 * 
 * @author shortstop
 */
@Component
public class ElingsJobInitializer {

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(ElingsJobInitializer.class);
	
	@Autowired
	@Qualifier("rest")
	private IServiceFinder restFinder;
	
	@Autowired
	private IEntityFieldCache entityFieldCache;
	
	@Autowired
	private JobModuleProperties module;
	
	@Autowired
	private ModuleConfigSet configSet;
		
	@EventListener({ ContextRefreshedEvent.class })
	public void ready(ContextRefreshedEvent event) {
		logger.info("Job module initializing ready...");
		this.configSet.addConfig(this.module.getName(), this.module);
		this.scanServices(this.module);		
	}
	
	@EventListener({ApplicationReadyEvent.class})
    void contextRefreshedEvent(ApplicationReadyEvent event) {
		logger.info("Job module initializing started...");
		logger.info("Job module initializing finished");
    }
	
	/**
	 * 모듈 서비스 스캔 
	 * 
	 * @param module
	 */
	private void scanServices(IModuleProperties module) {
		this.entityFieldCache.scanEntityFieldsByBasePackage(module.getBasePackage());
		this.restFinder.scanServicesByPackage(module.getName(), module.getBasePackage());
	}
}