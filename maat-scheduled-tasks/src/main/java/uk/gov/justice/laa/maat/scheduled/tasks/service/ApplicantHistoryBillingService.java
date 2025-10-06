package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
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

    public List<ApplicantHistoryBillingDTO> extractApplicantHistory() {
        List<ApplicantHistoryBillingEntity> applicantHistoryEntities = applicantHistoryBillingRepository.extractApplicantHistoryForBilling();
        log.debug("Extracted data for {} applicant histories.", applicantHistoryEntities.size());

        return applicantHistoryEntities.stream().map(applicantHistoryBillingMapper::mapEntityToDTO)
            .toList();
    }

    @Transactional
    public void sendApplicantHistoryToBilling(List<ApplicantHistoryBillingDTO> applicantHistories,
        String userModified) {
        resetApplicantHistory(applicantHistories, userModified);

        billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT_HISTORY,
            applicantHistories);

        UpdateApplicantHistoriesRequest applicantHistoriesRequest = UpdateApplicantHistoriesRequest.builder()
            .defendantHistories(applicantHistories).build();

        crownCourtLitigatorFeesApiClient.updateApplicantsHistory(applicantHistoriesRequest);
    }

    private void resetApplicantHistory(List<ApplicantHistoryBillingDTO> applicantHistories,
        String userModified) {
        List<Integer> ids = applicantHistories.stream().map(ApplicantHistoryBillingDTO::getId)
            .toList();

        int rowsUpdated = applicantHistoryBillingRepository.resetApplicantHistory(userModified,
            ids);
        log.debug("CCLF Flag reset for {} applicant histories.", rowsUpdated);
    }
}