package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.StoredProcedure;

@Service
@RequiredArgsConstructor
public class LocalManagementReportsService {

    private static final List<StoredProcedure> REPORT_PROCEDURES = List.of(
            StoredProcedure.REPORTS_BATCH_1,
            StoredProcedure.REPORTS_BATCH_2,
            StoredProcedure.REPORTS_BATCH_3,
            StoredProcedure.REPORTS_BATCH_4,
            StoredProcedure.REPORTS_BATCH_5,
            StoredProcedure.REPORTS_BATCH_6
    );

    private final StoredProcedureService storedProcedureService;

    public void processReportsBatches() {
        REPORT_PROCEDURES.forEach(storedProcedureService::callStoredProcedure);
    }
}
