package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetRepOrderBillingDTO;
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
    private static final String USER_MODIFIED = "scheduled"; // TODO: Whats the userModified going to be (limit of 10 on rep orders)???

    private final BillingDataFeedLogService billingDataFeedLogService;
    private final MaatReferenceService maatReferenceService;
    private final RepOrderBillingService repOrderBillingService;
    private final ApplicantBillingService applicantBillingService;
    private final ApplicantHistoryBillingService applicantHistoryBillingService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;

    @Scheduled(cron = "${billing.cclf_extract.cron_expression}")
    public void extractCCLFBillingData() {
        log.info("Starting extract for cclf billing data...");
        try {
            maatReferenceService.populateMaatReferences();

            List<RepOrderBillingDTO> repOrders = repOrderBillingService.getRepOrdersForBilling();
            List<ApplicantBillingDTO> applicants = applicantBillingService.findAllApplicantsForBilling();
            List<ApplicantHistoryBillingDTO> applicantHistories = applicantHistoryBillingService.extractApplicantHistory();

            // TODO: Need to check for empty list and send only if list is not empty do this individually, or if rep orders empty then abort or sends???
            crownCourtLitigatorFeesApiClient.updateRepOrders(repOrders);
            crownCourtLitigatorFeesApiClient.updateApplicants(applicants);
            crownCourtLitigatorFeesApiClient.updateApplicantsHistory(applicantHistories);

            repOrderBillingService.resetRepOrdersSentForBilling(
                ResetRepOrderBillingDTO.builder().userModified(USER_MODIFIED)
                    .ids(repOrders.stream().map(RepOrderBillingDTO::getId).toList()).build());
            applicantBillingService.resetApplicantBilling(
                ResetApplicantBillingDTO.builder().userModified(USER_MODIFIED)
                    .ids(applicants.stream().map(ApplicantBillingDTO::getId).toList()).build());
            applicantHistoryBillingService.resetApplicantHistory(
                ResetBillingDTO.builder().userModified(USER_MODIFIED).ids(
                        applicantHistories.stream().map(ApplicantHistoryBillingDTO::getId).toList())
                    .build());

            maatReferenceService.deleteMaatReferences();
        } catch () {

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
