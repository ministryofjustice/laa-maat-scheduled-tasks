package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalManagementReportsService {

    private static final String STORED_PROCEDURE_PREFIX = "maat_batch.process_reports_batch_";
    private static final List<Integer> BATCH_NUMBERS = List.of(1, 2, 3, 4, 5, 6);

    private final StoredProcedureService storedProcedureService;

    /**
     * Processes report batches asynchronously by executing a series of stored procedures.
     * Each stored procedure handles a specific batch of reports.
     */
     public void processReportsBatches() {
        BATCH_NUMBERS.forEach(batchNumber ->
                storedProcedureService.callStoredProcedure(STORED_PROCEDURE_PREFIX + batchNumber)
        );
    }
}