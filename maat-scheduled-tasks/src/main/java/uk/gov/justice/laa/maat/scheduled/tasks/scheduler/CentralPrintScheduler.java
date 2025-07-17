package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.service.StoredProcedureService;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class CentralPrintScheduler {
    private static final String BATCH_CENTRAL_PRINT_RUN = "xxrep_batch.central_print_run";
    private final StoredProcedureService storedProcedureService;

    @Scheduled(cron = "${maat_batch.central_print.cron_expression}")
    public void executeCentralPrintJob() {
        storedProcedureService.callStoredProcedure(BATCH_CENTRAL_PRINT_RUN);
    }
}
