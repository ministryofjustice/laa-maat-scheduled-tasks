package uk.gov.justice.laa.maat.scheduled.tasks.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder().region(Region.of(awsRegion)).build();
    }

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