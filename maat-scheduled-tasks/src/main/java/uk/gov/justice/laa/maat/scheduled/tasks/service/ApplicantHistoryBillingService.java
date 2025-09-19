package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.BillingDataFeedLogMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantHistoryBillingService {

    private final ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    private final ApplicantHistoryBillingMapper applicantHistoryBillingMapper;
    private final BillingDataFeedLogMapper billingDataFeedLogMapper;

    @Transactional
    public void sendApplicantHistoryToBilling(String userModified) {
        List<ApplicantHistoryBillingDTO> applicantHistories = extractApplicantHistory();

        if (applicantHistories.isEmpty()) {
            return;
        }

        sendApplicantHistoryToBilling(applicantHistories, userModified);
    }

    public void resendApplicantHistoryToBilling(String userModified) {
        List<BillingDataFeedLogEntity> billingLogEntities = billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.APPLICANT_HISTORY);

        List<ApplicantHistoryBillingDTO> applicantHistories = billingLogEntities.stream()
            .map(billingDataFeedLogMapper::mapEntityToApplicationHistoryBillingDtos)
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .toList();

        if (applicantHistories.isEmpty()) {
            return;
        }

        sendApplicantHistoryToBilling(applicantHistories, userModified);
    }

    public void sendApplicantHistoryToBilling(List<ApplicantHistoryBillingDTO> applicantHistories, String userModified) {
        List<Integer> ids = applicantHistories.stream().map(ApplicantHistoryBillingDTO::getId)
            .toList();

        resetApplicantHistory(
            ResetBillingDTO.builder().userModified(userModified).ids(ids).build());

        billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT_HISTORY, applicantHistories);

        UpdateApplicantHistoriesRequest applicantHistoriesRequest = UpdateApplicantHistoriesRequest.builder()
            .defendantHistories(applicantHistories).build();

        crownCourtLitigatorFeesApiClient.updateApplicantsHistory(applicantHistoriesRequest);
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

    private void resetApplicantHistory(ResetBillingDTO resetBillingDTO) {
        log.info("Resetting CCLF flag for extracted applicant histories.");
        applicantHistoryBillingRepository.resetApplicantHistory(resetBillingDTO.getUserModified(),
            resetBillingDTO.getIds());
    }
}