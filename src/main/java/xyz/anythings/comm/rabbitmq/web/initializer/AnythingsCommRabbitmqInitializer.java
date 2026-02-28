/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.anythings.comm.rabbitmq.web.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import xyz.anythings.comm.rabbitmq.config.ModuleProperties;
import xyz.elidom.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.rabbitmq.rest.VirtualHostController;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;
import xyz.elidom.util.BeanUtil;

/**
 * Anythings Communication Rabbitmq Startup시 Framework 초기화 클래스 
 * 
 * @author yang
 */
@Component
public class AnythingsCommRabbitmqInitializer { 

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(AnythingsCommRabbitmqInitializer.class);
	
	@Autowired
	@Qualifier("rest")
	private IServiceFinder restFinder;
	
	@Autowired
	private IEntityFieldCache entityFieldCache;
	
	@Autowired
	private ModuleProperties module;
	
	@Autowired
	private RabbitmqProperties properties;
	
	@Autowired
	private ModuleConfigSet configSet;
	
	@EventListener({ ContextRefreshedEvent.class })
	public void ready(ContextRefreshedEvent event) {
		this.logger.info("Anythings Communication Rabbitmq module initializing ready...");
		this.configSet.addConfig(this.module.getName(), this.module);
		this.scanServices();
	}
	
	@EventListener({ApplicationReadyEvent.class})
	@Order(Ordered.LOWEST_PRECEDENCE)
	void contextRefreshedEvent(ApplicationReadyEvent event) {
		if(this.properties.isUseMqModule()) {
			this.logger.info("Anythings Communication Rabbitmq module initializing started...");
			
			this.logger.info("RabbitMq Queue Listen Ready start ...");
				
			for(String vHostName : this.properties.getAppInitVHosts()) {
				BeanUtil.get(VirtualHostController.class).addVhostListener(vHostName);
			}
			
			this.logger.info("RabbitMq Queue Listen Ready Finished ...");
		}
	}
	
	/**
	 * 모듈 서비스 스캔 
	 */
	private void scanServices() {
		this.entityFieldCache.scanEntityFieldsByBasePackage(this.module.getBasePackage());
		this.restFinder.scanServicesByPackage(this.module.getName(), this.module.getBasePackage());
	}
}