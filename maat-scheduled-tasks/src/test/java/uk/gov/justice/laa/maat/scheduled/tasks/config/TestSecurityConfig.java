package uk.gov.justice.laa.maat.scheduled.tasks.config;



import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {
//    @Bean("crownCourtLitigatorFeesApiWebClient")
//    public WebClient crownCourtLitigatorFeesApiWebClient() {
//        return WebClient.builder().build(); // simple dummy client
//    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf().disable()
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }
}
