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
    private String objectKeyAppealPrefix;

    @NotNull
    private String objectKeyProcessedPrefix;

    @NotNull
    private String objectKeyErroredPrefix;

    @NotNull
    private String objectKeyTrialPrefix;

    @NotNull
    private String s3DataBucketName;
}
