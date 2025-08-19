package uk.gov.justice.laa.maat.scheduled.tasks.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BillingDataFeedRecordType {
    APPLICANT("APPLICANT"),
    APPLICANT_HISTORY("APPLICANT_HISTORY"),
    REP_ORDER("REP_ORDER");
    
    private final String value;
}
