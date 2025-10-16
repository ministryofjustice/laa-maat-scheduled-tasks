package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.service.BatchProcessingService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.BillingDataFeedLogService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.MaatReferenceService;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class BillingScheduler {

    public static final Integer OLDER_THAN_DAYS = 30;
    
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final MaatReferenceService maatReferenceService;
    private final BatchProcessingService batchProcessingService;

    @Scheduled(cron = "${billing.cclf_extract.cron_expression}")
    public void extractCCLFBillingData() {
        try {
            log.info("Starting extract for CCLF billing data...");
            maatReferenceService.populateMaatReferences();

            batchProcessingService.processApplicantBatch();
            batchProcessingService.processApplicantHistoryBatch();
            batchProcessingService.processRepOrderBatch();
        } catch (Exception exception) {
            log.error("Error running extract for CCLF billing data: {}", exception.getMessage());
        } finally {
            maatReferenceService.deleteMaatReferences();
            log.info("End of extract for CCLF billing data.");
        }
    }

    @Scheduled(cron = "${billing.cleanup_data_feed_log.cron_expression}")
    public void cleanupBillingDataFeedLog() {
        log.info("Starting billing data feed log cleanup...");
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(OLDER_THAN_DAYS);

        Long logsDeleted = billingDataFeedLogService.deleteLogsBeforeDate(dateThreshold);
        log.info("Billing data feed log cleanup completed. {} entries deleted.", logsDeleted);
    }
}
