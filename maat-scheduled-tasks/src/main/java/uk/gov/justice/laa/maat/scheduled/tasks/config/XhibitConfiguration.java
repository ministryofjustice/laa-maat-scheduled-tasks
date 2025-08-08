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
    private String objectKeyAppealProcessedPrefix;

    @NotNull
    private String objectKeyAppealErroredPrefix;

    @NotNull
    private String objectKeyTrialPrefix;

    @NotNull
    private String objectKeyTrialProcessedPrefix;

    @NotNull
    private String objectKeyTrialErroredPrefix;

    @NotNull
    private String s3DataBucketName;

    @NotNull
    private String s3DataBucketAccessKey;

    @NotNull
    private String s3DataBucketAccessSecret;

}
