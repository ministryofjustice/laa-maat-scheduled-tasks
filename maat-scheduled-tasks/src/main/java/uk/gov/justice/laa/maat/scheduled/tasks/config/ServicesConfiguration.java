package uk.gov.justice.laa.maat.scheduled.tasks.config;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "services")
public class ServicesConfiguration {
    @NotNull
    private CCLFApi cclfApi;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CCLFApi {
        @NotNull
        private String baseUrl;

        @NotNull
        private String registrationId;
    }
}
