package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtRemunerationApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
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
    private final CrownCourtRemunerationApiClient crownCourtRemunerationApiClient;
    private final ApplicantHistoryBillingMapper applicantHistoryBillingMapper;
    private final BillingDataFeedLogMapper billingDataFeedLogMapper;

    public List<ApplicantHistoryBillingDTO> extractApplicantHistory() {
        List<ApplicantHistoryBillingEntity> applicantHistoryEntities = applicantHistoryBillingRepository.extractApplicantHistoryForBilling();
        log.debug("Extracted data for {} applicant histories.", applicantHistoryEntities.size());

        return applicantHistoryEntities.stream().map(applicantHistoryBillingMapper::mapEntityToDTO)
            .toList();
    }

    @Transactional
    public void sendApplicantHistoryToBilling(List<ApplicantHistoryBillingDTO> applicantHistories,
        String userModified) {

        if (applicantHistories.isEmpty()) {
            return;
        }

        resetApplicantHistoryFlag(applicantHistories, userModified);
        sendApplicantHistoryToBilling(applicantHistories);
    }

    public void resendApplicantHistoryToBilling() {
        List<BillingDataFeedLogEntity> billingLogEntities = billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.APPLICANT_HISTORY);

        List<ApplicantHistoryBillingDTO> applicantHistories = billingLogEntities.stream()
            .map(billingDataFeedLogMapper::mapEntityToApplicationHistoryBillingDtos)
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .toList();

        if (applicantHistories.isEmpty()) {
            return;
        }

        sendApplicantHistoryToBilling(applicantHistories);
    }

    private void sendApplicantHistoryToBilling(List<ApplicantHistoryBillingDTO> applicantHistories) {
        billingDataFeedLogService.saveBillingDataFeed(
            BillingDataFeedRecordType.APPLICANT_HISTORY, applicantHistories);

        UpdateApplicantHistoriesRequest applicantHistoriesRequest = UpdateApplicantHistoriesRequest
            .builder()
            .defendantHistories(applicantHistories).build();

        crownCourtLitigatorFeesApiClient.updateApplicantsHistory(applicantHistoriesRequest);
        crownCourtRemunerationApiClient.updateApplicantsHistory(applicantHistoriesRequest);
    }

    private void resetApplicantHistoryFlag(
        List<ApplicantHistoryBillingDTO> applicantHistories, String userModified) {
        List<Integer> applicantHistoryIds = applicantHistories.stream()
            .map(ApplicantHistoryBillingDTO::getId)
            .toList();

        int rowsUpdated = applicantHistoryBillingRepository.resetApplicantHistory(
            applicantHistoryIds, userModified);

        log.debug("CCLF Flag reset for {} applicant histories.", rowsUpdated);
    }
}