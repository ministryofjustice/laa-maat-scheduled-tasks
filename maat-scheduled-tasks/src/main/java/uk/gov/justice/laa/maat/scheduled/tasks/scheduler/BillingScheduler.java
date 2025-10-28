package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import static uk.gov.justice.laa.maat.scheduled.tasks.util.ListUtils.batchList;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
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
    public void extractBillingData() {
        try {
            log.info("Starting extract for CCLF billing data...");
            maatReferenceService.populateMaatReferences();

            extractApplicantBillingData();
            extractApplicantHistoryBillingData();
            extractRepOrderBillingData();
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

    private void extractApplicantBillingData() {
        List<ApplicantBillingDTO> applicants = applicantBillingService.findAllApplicantsForBilling();

        if (applicants.isEmpty()) {
            return;
        }

        List<List<ApplicantBillingDTO>> applicantBatches = batchList(applicants,
            billingConfiguration.getBatchSize());

        for (List<ApplicantBillingDTO> currentBatch : applicantBatches) {
            log.debug("Processing batch of {} applicants...", currentBatch.size());
            applicantBillingService.sendApplicantsToBilling(currentBatch,
                billingConfiguration.getUserModified());
        }

        log.info("Applicant data has been extracted and sent to the billing team.");
    }

    private void extractApplicantHistoryBillingData() {
        List<ApplicantHistoryBillingDTO> applicantHistories = applicantHistoryBillingService.extractApplicantHistory();

        if (applicantHistories.isEmpty()) {
            return;
        }

        List<List<ApplicantHistoryBillingDTO>> applicantHistoryBatches = batchList(
            applicantHistories, billingConfiguration.getBatchSize());

        for (List<ApplicantHistoryBillingDTO> currentBatch : applicantHistoryBatches) {
            log.debug("Processing batch of {} applicant histories...", currentBatch.size());
            applicantHistoryBillingService.sendApplicantHistoryToBilling(currentBatch,
                billingConfiguration.getUserModified());
        }

        log.info("Applicant history data has been extracted and sent to the billing team.");
    }

    private void extractRepOrderBillingData() {
        List<RepOrderBillingDTO> repOrders = repOrderBillingService.getRepOrdersForBilling();

        if (repOrders.isEmpty()) {
            return;
        }

        List<List<RepOrderBillingDTO>> repOrderBatches = batchList(repOrders,
            billingConfiguration.getBatchSize());

        for (List<RepOrderBillingDTO> currentBatch : repOrderBatches) {
            log.debug("Processing batch of {} rep orders...", currentBatch.size());
            repOrderBillingService.sendRepOrdersToBilling(currentBatch,
                billingConfiguration.getUserModified());
        }

        log.info("Rep order data has been extracted and sent to the billing team.");
    }
}
