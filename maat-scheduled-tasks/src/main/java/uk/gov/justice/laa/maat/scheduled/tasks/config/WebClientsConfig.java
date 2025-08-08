package uk.gov.justice.laa.maat.scheduled.tasks.config;

import io.github.resilience4j.retry.RetryRegistry;
import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.filter.Resilience4jRetryFilter;
import uk.gov.justice.laa.maat.scheduled.tasks.filter.WebClientFilters;

import java.time.Duration;
import java.util.List;

@Configuration
@AllArgsConstructor
public class WebClientsConfig {
    public static final int MAX_IN_MEMORY_SIZE = 10485760;
    private static final String CCLF_API_WEB_CLIENT_NAME = "crownCourtLitigatorFeesApiWebClient";

    @Bean
    WebClientCustomizer webClientCustomizer() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
                .maxConnections(500)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(120))
                .evictInBackground(Duration.ofSeconds(180))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .build();
        return webClientBuilder -> {
            webClientBuilder.clientConnector(
                    new ReactorClientHttpConnector(
                            HttpClient.create(connectionProvider)
                                    .resolver(DefaultAddressResolverGroup.INSTANCE)
                                    .compress(true)
                                    .responseTimeout(Duration.ofSeconds(30))
                    )
            );
            webClientBuilder.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            webClientBuilder.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            webClientBuilder.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE));
        };
    }

    @Bean(CCLF_API_WEB_CLIENT_NAME)
    WebClient crownCourtLitigatorFeesApiWebClient(WebClient.Builder clientBuilder,
                                                  ServicesConfiguration servicesConfiguration,
                                                  ClientRegistrationRepository clientRegistrations,
                                                  OAuth2AuthorizedClientRepository authorizedClients,
                                                  RetryRegistry retryRegistry) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients);
        oauth2Filter.setDefaultClientRegistrationId(servicesConfiguration.getCclfApi().getRegistrationId());

        Resilience4jRetryFilter retryFilter = new Resilience4jRetryFilter(retryRegistry, CCLF_API_WEB_CLIENT_NAME);

        return clientBuilder
                .baseUrl(servicesConfiguration.getCclfApi().getBaseUrl())
                .filters(filters -> configureFilters(filters, oauth2Filter, retryFilter))
                .build();
    }

    @Bean
    CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient(
            @Qualifier(CCLF_API_WEB_CLIENT_NAME) WebClient webClient) {
        HttpServiceProxyFactory httpServiceProxyFactory =
                HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(CrownCourtLitigatorFeesApiClient.class);
    }

    private void configureFilters(List<ExchangeFilterFunction> filters,
                                  ServletOAuth2AuthorizedClientExchangeFilterFunction oauthFilter,
                                  ExchangeFilterFunction retryFilter) {
        filters.add(WebClientFilters.logRequestHeaders());
        filters.add(retryFilter);
        filters.add(oauthFilter);
        filters.add(WebClientFilters.errorResponseHandler());
        filters.add(WebClientFilters.handleNotFoundResponse());
        filters.add(WebClientFilters.logResponse());
    }
}
