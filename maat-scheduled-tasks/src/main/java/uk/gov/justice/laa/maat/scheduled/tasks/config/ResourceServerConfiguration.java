package uk.gov.justice.laa.maat.scheduled.tasks.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class ResourceServerConfiguration {

    @Value("${httpRequest.scope}")
    private String maatScheduledTasksScope;

    @Bean
    protected BearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint() {
        BearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();
        bearerTokenAuthenticationEntryPoint.setRealmName("Crime MAAT Scheduled Tasks API");
        return bearerTokenAuthenticationEntryPoint;
    }

    @Bean
    public AccessDeniedHandler bearerTokenAccessDeniedHandler() {
        return new BearerTokenAccessDeniedHandler();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(disableCsrfAsMadeRedundantByOath2AndJwt())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/api/**").hasAuthority("SCOPE_" + maatScheduledTasksScope + "/standard")
                .anyRequest().authenticated())
            .oauth2ResourceServer((oauth2) -> oauth2
                .accessDeniedHandler(bearerTokenAccessDeniedHandler())
                .authenticationEntryPoint(bearerTokenAuthenticationEntryPoint())
                .jwt(withDefaults()));
        return http.build();
    }

    private Customizer<CsrfConfigurer<HttpSecurity>> disableCsrfAsMadeRedundantByOath2AndJwt() {
        return AbstractHttpConfigurer::disable;
    }
}
