package uk.gov.justice.laa.maat.scheduled.tasks.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.config.XhibitConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetStatus;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;

@Component
@RequiredArgsConstructor
public class ObjectKeyHelper {

    private final XhibitConfiguration config;

    public String buildKey(RecordSheetType type, String filename) {
        return buildPrefix(type) + filename;
    }

    public String buildKey(RecordSheetType type, RecordSheetStatus status, String filename) {
        return buildPrefix(type, status) + filename;
    }

    public String buildPrefix(RecordSheetType type) {
        return switch (type) {
            case TRIAL -> config.getObjectKeyTrialPrefix();
            case APPEAL -> config.getObjectKeyAppealPrefix();
        };
    }

    public String buildPrefix(RecordSheetType type, RecordSheetStatus status) {
        String baseStatus = switch (status) {
            case PROCESSED -> config.getObjectKeyProcessedPrefix();
            case ERRORED -> config.getObjectKeyErroredPrefix();
        };

        return baseStatus + "/" + type.name().toLowerCase() + "/";
    }
}

