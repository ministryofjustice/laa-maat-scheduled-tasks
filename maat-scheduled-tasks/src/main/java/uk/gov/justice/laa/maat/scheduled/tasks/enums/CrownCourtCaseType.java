package uk.gov.justice.laa.maat.scheduled.tasks.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CrownCourtCaseType {

    INDICTABLE("INDICTABLE"),
    SUMMARY_ONLY("SUMMARY ONLY"),
    CC_ALREADY("CC ALREADY"),
    EITHER_WAY("EITHER WAY"),
    APPEAL_CC("APPEAL CC");

    private final String value;
}
