package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.service.ApplicantBillingService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.ApplicantHistoryBillingService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.BillingDataFeedLogService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.MaatReferenceService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.RepOrderBillingService;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class BillingScheduler {

    public static final Integer OLDER_THAN_DAYS = 30;

    private final BillingConfiguration billingConfiguration;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final MaatReferenceService maatReferenceService;
    private final RepOrderBillingService repOrderBillingService;
    private final ApplicantBillingService applicantBillingService;
    private final ApplicantHistoryBillingService applicantHistoryBillingService;

    @Scheduled(cron = "${billing.cclf_extract.cron_expression}")
    public void extractCCLFBillingData() {
        try {
            log.info("Starting extract for CCLF billing data...");
            maatReferenceService.populateMaatReferences();

            applicantBillingService.sendApplicantsToBilling(billingConfiguration.getUserModified());
            applicantHistoryBillingService.sendApplicantHistoryToBilling(
                billingConfiguration.getUserModified());
            repOrderBillingService.sendRepOrdersToBilling(billingConfiguration.getUserModified());
        } catch (Exception exception) {
            log.error("Error running extract for CCLF billing data: {}", exception.getMessage());
        } finally {
            maatReferenceService.deleteMaatReferences();
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
