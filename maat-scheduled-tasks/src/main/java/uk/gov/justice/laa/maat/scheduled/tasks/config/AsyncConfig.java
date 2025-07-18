package uk.gov.justice.laa.maat.scheduled.tasks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);       // Initial thread pool size
        executor.setMaxPoolSize(25);        // Max number of concurrent async threads
        executor.setQueueCapacity(100);     // Tasks waiting in queue before new threads are created
        executor.setThreadNamePrefix("async-sp-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300); // Wait for up to 5 mins on shutdown
        executor.initialize();
        return executor;
    }
}