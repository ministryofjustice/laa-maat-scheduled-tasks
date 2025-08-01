package uk.gov.justice.laa.maat.scheduled.tasks.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum CrownCourtTrialOutcome {

    CONVICTED("CONVICTED"),
    PART_CONVICTED("PART CONVICTED"),
    AQUITTED("AQUITTED");

    private final String value;

}
