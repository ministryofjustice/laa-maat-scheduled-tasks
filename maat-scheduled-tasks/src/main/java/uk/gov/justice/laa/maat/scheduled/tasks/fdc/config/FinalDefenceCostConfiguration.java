package uk.gov.justice.laa.maat.scheduled.tasks.fdc.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "fdc-batch")
public class FinalDefenceCostConfiguration {

    @NotNull
    private final int fetchSize;
}
