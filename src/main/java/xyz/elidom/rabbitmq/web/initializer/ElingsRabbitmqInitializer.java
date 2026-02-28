/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.rabbitmq.web.initializer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import xyz.elidom.rabbitmq.client.TraceDead;
import xyz.elidom.rabbitmq.client.TraceDeliver;
import xyz.elidom.rabbitmq.client.TracePublish;
import xyz.elidom.rabbitmq.config.RabbitmqModuleProperties;
import xyz.elidom.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.rabbitmq.logger.TraceDeleteManager;
import xyz.elidom.rabbitmq.rest.VirtualHostController;
import xyz.elidom.rabbitmq.service.ElasticRestHandler;
import xyz.elidom.rabbitmq.service.model.VirtualHost;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * Elings Rabbitmq Startup시 Framework 초기화 클래스 
 * 
 * @author shortstop
 */
@Component
public class ElingsRabbitmqInitializer {

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(ElingsRabbitmqInitializer.class);
	
	@Autowired
	@Qualifier("rest")
	private IServiceFinder restFinder;
	
	@Autowired
	private IEntityFieldCache entityFieldCache;
	
	@Autowired
	private RabbitmqModuleProperties module;
	
	@Autowired
	private RabbitmqProperties properties;
	
	@Autowired
	private ModuleConfigSet configSet;
	

	@EventListener({ ContextRefreshedEvent.class })
	public void ready(ContextRefreshedEvent event) {
		this.logger.info("elings-rabbitmq module initializing ready...");
		this.configSet.addConfig(this.module.getName(), this.module);
		this.scanServices();		
	}
	
	@EventListener({ApplicationReadyEvent.class})
    void contextRefreshedEvent(ApplicationReadyEvent event) {
		
		if(this.properties.isUseMqModule() == false ) {
			this.logger.info("Rabbitmq module not used...");
			return;
		} 
		
		this.logger.info("Rabbitmq module initializing started...");
		
		// 트레이스 사용유무에 따라서 트레이스 기능 초기화 유무 결정 
		if(this.properties.getTraceUse()) {
			// 1 엘라스틱 초기화 
			if(this.properties.getTraceType().equalsIgnoreCase("elastic")) BeanUtil.get(ElasticRestHandler.class).setElasticClient();
			
			// rabbitmq trace init
			this.logger.info("Rabbitmq module trace startup!");
			
			// 1.1 트레이스 init
			BeanUtil.get(TracePublish.class).initLogger();
			BeanUtil.get(TraceDeliver.class).initLogger();
			BeanUtil.get(TraceDead.class).initLogger();
			
			// 1.2 삭제 메인 설정 
			if(this.properties.isDeleteMain()) {
				this.logger.info("Rabbitmq module : this server mq trace history delete main process....");
				BeanUtil.get(TraceDeleteManager.class).processStart();
			}
		} else {
			this.logger.info("Rabbitmq module trace not use... !");
		}
		
		// get site list 
		List<VirtualHost> vHosts = BeanUtil.get(VirtualHostController.class).getVhostList();
		List<String> vHostNames = new ArrayList<String>();
		
		if(ValueUtil.isNotEmpty(vHosts)) {
			for(VirtualHost vHost : vHosts) vHostNames.add(vHost.getName());
		}
		
		this.properties.setAppInitVHosts(vHostNames);
		
		this.logger.info("Rabbitmq module initializing finished : ");
    }
	
	/**
	 * 모듈 서비스 스캔 
	 */
	private void scanServices() {
		this.entityFieldCache.scanEntityFieldsByBasePackage(this.module.getBasePackage());
		this.restFinder.scanServicesByPackage(this.module.getName(), this.module.getBasePackage());
	}
}