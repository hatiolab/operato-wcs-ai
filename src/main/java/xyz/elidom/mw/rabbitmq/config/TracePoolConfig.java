package xyz.elidom.mw.rabbitmq.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 메시지 추적을 위한 스레드 풀 설정
 *  
 * @author yang
 */
@Configuration
public class TracePoolConfig {

    @Bean(name = "mwTracePool")
    public Executor mwTracePool() {
    	ThreadPoolTaskScheduler poolScheduler = new ThreadPoolTaskScheduler();
		poolScheduler.setBeanName("mwTracePool");
		poolScheduler.setPoolSize(10);
		poolScheduler.setThreadNamePrefix("mq-");
		poolScheduler.setWaitForTasksToCompleteOnShutdown(false);
		poolScheduler.initialize();
        return poolScheduler;
    }
}
