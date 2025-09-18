package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "xhibit-batch")
public class XhibitConfiguration {

    @NotNull
    private final String objectKeyAppealPrefix;

    @NotNull
    private final String objectKeyProcessedPrefix;

    @NotNull
    private final String objectKeyErroredPrefix;

    @NotNull
    private final String objectKeyTrialPrefix;

    @NotNull
    private final String s3DataBucketName;

    @NotNull
    private final String fetchSize;
}
