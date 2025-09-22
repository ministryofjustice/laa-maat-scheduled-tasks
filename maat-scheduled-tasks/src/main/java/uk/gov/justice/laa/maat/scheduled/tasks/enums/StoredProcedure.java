package uk.gov.justice.laa.maat.scheduled.tasks.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StoredProcedure {

    REPORTS_BATCH_1(Schema.TOGDATA, "maat_batch", "process_reports_batch_1"),
    REPORTS_BATCH_2(Schema.TOGDATA, "maat_batch", "process_reports_batch_2"),
    REPORTS_BATCH_3(Schema.TOGDATA, "maat_batch", "process_reports_batch_3"),
    REPORTS_BATCH_4(Schema.TOGDATA, "maat_batch", "process_reports_batch_4"),
    REPORTS_BATCH_5(Schema.TOGDATA, "maat_batch", "process_reports_batch_5"),
    REPORTS_BATCH_6(Schema.TOGDATA, "maat_batch", "process_reports_batch_6"),

    MAAT_BATCH_FA_FIX(Schema.TOGDATA, "maat_batch", "FA_fix"),
    BATCH_CENTRAL_PRINT_RUN(Schema.REP, "xxrep_batch", "central_print_run"),
    MAAT_BATCH_INACTIVE_USERS(Schema.TOGDATA, "maat_batch", "process_inactive_users"),
    MAAT_BATCH_PROCESS_CORRESPONDENCE(Schema.TOGDATA, "maat_batch", "process_correspondence"),

    TRIAL_DATA_TO_MAAT_PROCEDURE(Schema.HUB, "xhibit_file_load", "process_trial_record"),
    APPEAL_DATA_TO_MAAT_PROCEDURE(Schema.HUB, "xhibit_file_load", "process_appeal_record"),

    TEST_PROCEDURE(Schema.TOGDATA, "test", "process_test");

    private final String schema;
    private final String packageName;
    private final String procedureName;

    public String getQualifiedName() {
        return String.format("%s.%s.%s", schema, packageName, procedureName);
    }

    private static class Schema {

        public static final String HUB = "hub";
        public static final String REP = "rep";
        public static final String TOGDATA = "togdata";
    }
}

