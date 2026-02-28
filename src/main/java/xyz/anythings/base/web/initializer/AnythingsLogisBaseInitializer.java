/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.anythings.base.web.initializer;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import xyz.anythings.base.config.ModuleProperties;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.query.store.BatchQueryStore;
import xyz.anythings.base.query.store.BoxQueryStore;
import xyz.anythings.base.query.store.ConfigQueryStore;
import xyz.anythings.base.query.store.EtcQueryStore;
import xyz.anythings.base.query.store.IndicatorQueryStore;
import xyz.anythings.base.query.store.InstructionQueryStore;
import xyz.anythings.base.query.store.PreprocessQueryStore;
import xyz.anythings.base.query.store.StockQueryStore;
import xyz.anythings.base.service.impl.JobConfigProfileService;
import xyz.anythings.gw.entity.IndConfigSet;
import xyz.anythings.gw.service.IndConfigProfileService;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.config.ModuleConfigSet;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.api.IEntityFieldCache;
import xyz.elidom.sys.system.service.api.IServiceFinder;
import xyz.elidom.util.ValueUtil;

/**
 * Anythings Logis Base Startup시 Framework 초기화 클래스 
 * 
 * @author yang
 */
@Component
public class AnythingsLogisBaseInitializer {

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(AnythingsLogisBaseInitializer.class);
	
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
	private BatchQueryStore batchQueryStore;
	
	@Autowired
	private ConfigQueryStore configQueryStore;
	
	@Autowired
	private IndicatorQueryStore indicatorQueryStore;
	
	@Autowired
	private InstructionQueryStore instructionQueryStore;
	
	@Autowired
	private PreprocessQueryStore preprocessQueryStore;
	
	@Autowired
	private BoxQueryStore boxQueryStore;
	
	@Autowired
	private StockQueryStore stockQueryStore;
	
	@Autowired
	private EtcQueryStore etcQueryStore;
	
	@Autowired
	private JobConfigProfileService jobConfigProfileSvc;
	
	@Autowired
	private IndConfigProfileService indConfigProfileSvc;

	@EventListener({ ContextRefreshedEvent.class })
	public void refresh(ContextRefreshedEvent event) {
		this.logger.info("Anythings Logistics Base module refreshing...");
		this.configSet.addConfig(this.module.getName(), this.module);
		this.scanServices();		
		this.logger.info("Anythings Logistics Base module refreshed!");
	}
	
	@EventListener({ApplicationReadyEvent.class})
    void ready(ApplicationReadyEvent event) {
		this.logger.info("Anythings Logistics Base module initializing...");
		
		this.initQueryStores();
		this.initStageConfigProfiles();
		this.initBatchIndConfigProfiles();
		
		this.logger.info("Anythings Logistics Base module initialized!");
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
		this.batchQueryStore.initQueryStore(dbType);
		this.configQueryStore.initQueryStore(dbType);
		this.indicatorQueryStore.initQueryStore(dbType);
		this.instructionQueryStore.initQueryStore(dbType);
		this.preprocessQueryStore.initQueryStore(dbType);
		this.etcQueryStore.initQueryStore(dbType);
		this.boxQueryStore.initQueryStore(dbType);
		this.stockQueryStore.initQueryStore(dbType);
	}
	
	/**
	 * 스테이지 설정 프로파일 초기화
	 */
	private void initStageConfigProfiles() {
		String sql = "select id from domains";
		List<Domain> domainList = this.queryManager.selectListBySql(sql, new HashMap<String, Object>(1), Domain.class, 0, 0);
		
		for(Domain domain : domainList) {
			this.jobConfigProfileSvc.buildStageConfigSet(domain.getId());
		}
	}

	/**
	 * 작업 배치별 표시기 설정 프로파일 초기화 
	 */
	private void initBatchIndConfigProfiles() {
		String sql = "select id, ind_config_set_id, job_config_set_id from job_batches where status = :status order by id";
		List<JobBatch> batches = this.queryManager.selectListBySql(sql, ValueUtil.newMap("status", JobBatch.STATUS_RUNNING), JobBatch.class, 0, 0);
		
		if(ValueUtil.isNotEmpty(batches)) {
			for(JobBatch batch : batches) {
				this.jobConfigProfileSvc.addConfigSet(batch);
				
				if(ValueUtil.isNotEmpty(batch.getIndConfigSetId())) {
					IndConfigSet indConfigSet = this.queryManager.select(IndConfigSet.class, batch.getIndConfigSetId());
					if(indConfigSet != null) {
						this.indConfigProfileSvc.addConfigSet(batch.getId(), indConfigSet);
					}
				}
			}
		}
	}

}