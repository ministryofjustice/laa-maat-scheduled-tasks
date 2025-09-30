package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;

import static uk.gov.justice.laa.maat.scheduled.tasks.util.ListUtils.batchList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantHistoryBillingService {

    private final ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    private final BillingConfiguration billingConfiguration;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    private final ApplicantHistoryBillingMapper applicantHistoryBillingMapper;

    @Transactional
    public void sendApplicantHistoryToBilling(String userModified) {
        List<ApplicantHistoryBillingDTO> applicantHistories = extractApplicantHistory();

        if (applicantHistories.isEmpty()) {
            return;
        }

        resetApplicantHistory(applicantHistories, userModified);

        billingDataFeedLogService.saveBillingDataFeed(
            BillingDataFeedRecordType.APPLICANT_HISTORY,
            applicantHistories.toString());

        int requestBatchSize = Integer.parseInt(billingConfiguration.getRequestBatchSize());

        List<List<ApplicantHistoryBillingDTO>> batchedApplicantHistories = batchList(
            applicantHistories, requestBatchSize);

        for (List<ApplicantHistoryBillingDTO> currentBatch : batchedApplicantHistories) {
            UpdateApplicantHistoriesRequest applicantHistoriesRequest = UpdateApplicantHistoriesRequest.builder()
                .defendantHistories(currentBatch).build();

            crownCourtLitigatorFeesApiClient.updateApplicantsHistory(applicantHistoriesRequest);
        }

        log.info("Extracted applicant history data has been sent to the billing team.");
    }

    private List<ApplicantHistoryBillingDTO> extractApplicantHistory() {
        List<ApplicantHistoryBillingEntity> applicantHistoryEntities = applicantHistoryBillingRepository.extractApplicantHistoryForBilling();
        log.info("Application histories successfully extracted for billing data.");

        return applicantHistoryEntities
            .stream()
            .map(applicantHistoryBillingMapper::mapEntityToDTO)
            .toList();
    }

    private void resetApplicantHistory(List<ApplicantHistoryBillingDTO> applicantHistories,
        String userModified) {
        int resetBatchSize = Integer.parseInt(billingConfiguration.getResetBatchSize());

        // Batching IDs due to Oracle hard limit of 1000 on IN clause.
        List<List<Integer>> batchedIds = batchList(
            applicantHistories.stream().map(ApplicantHistoryBillingDTO::getId).toList(),
            resetBatchSize);

        for (List<Integer> batch : batchedIds) {
            int updatedRows = applicantHistoryBillingRepository.resetApplicantHistory(userModified,
                batch);
            log.info("CCLF Flag reset for batch of {} applicant histories", updatedRows);
        }
    }
}