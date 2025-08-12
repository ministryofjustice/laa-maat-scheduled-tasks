package uk.gov.justice.laa.maat.scheduled.tasks.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@RequiredArgsConstructor
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
}
