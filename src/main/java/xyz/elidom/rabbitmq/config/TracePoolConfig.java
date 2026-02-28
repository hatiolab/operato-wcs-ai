package xyz.elidom.rabbitmq.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * trace pool 생성 
 * @author yang
 *
 */
@Configuration
public class TracePoolConfig {

    @Bean(name = "tracePool")
    public Executor tracePool() {
    		ThreadPoolTaskScheduler poolScheduler = new ThreadPoolTaskScheduler();
		poolScheduler.setBeanName("tracePool");
		poolScheduler.setPoolSize(10);
		poolScheduler.setThreadNamePrefix("mq-");
		poolScheduler.setWaitForTasksToCompleteOnShutdown(false);
		poolScheduler.initialize();
		
        return poolScheduler;
    }
}
