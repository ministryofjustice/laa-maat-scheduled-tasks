package uk.gov.justice.laa.maat.scheduled.tasks.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class TestOverrides {
    
    @Bean("crownCourtLitigatorFeesApiWebClient")
    public WebClient crownCourtLitigatorFeesApiWebClient() {
        return WebClient.builder().build();
    }
}
