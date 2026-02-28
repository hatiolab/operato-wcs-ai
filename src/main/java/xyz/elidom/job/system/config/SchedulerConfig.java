/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.job.system.config;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import xyz.elidom.core.CoreConfigConstants;
import xyz.elidom.util.ValueUtil;

@Configuration
public class SchedulerConfig {
	
	@Resource
	private Environment env;

    @Autowired
    private DataSource dataSource;

    @Bean
	public SchedulerFactoryBean quartz(ApplicationContext applicationContext) {
		SchedulerFactoryBean bean = new SchedulerFactoryBean();
		bean.setApplicationContext(applicationContext);
		boolean isEnable = ValueUtil.toBoolean(this.env.getProperty(CoreConfigConstants.QUARTZ_SCHEDULER_ENALBE, "true"));
		bean.setAutoStartup(isEnable);
		bean.setDataSource(dataSource);
		bean.setConfigLocation(new ClassPathResource("properties/quartz.properties"));
		bean.setWaitForJobsToCompleteOnShutdown(true);
        return bean;	
	}
}