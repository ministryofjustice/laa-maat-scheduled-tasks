package uk.gov.justice.laa.maat.scheduled.tasks.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "billing")
public class BillingConfiguration {

    @NotNull
    private final String userModified;

    @NotNull
    private final String requestBatchSize;

    @NotNull
    private final String resetBatchSize;
}
