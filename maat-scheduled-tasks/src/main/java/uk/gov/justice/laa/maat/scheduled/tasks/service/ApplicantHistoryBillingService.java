package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.ResponseUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantHistoryBillingService {

    private final ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    private final ApplicantHistoryBillingMapper applicantHistoryBillingMapper;

    private static final String SENT_TO_CCLF_FAILURE_FLAG = "Y";
    private static final String REQUEST_LABEL = "applicant history";

    @Transactional
    public void sendApplicantHistoryToBilling(String userModified) {
        List<ApplicantHistoryBillingDTO> applicantHistories = extractApplicantHistory();

        if (applicantHistories.isEmpty()) {
            return;
        }

        List<Integer> ids = applicantHistories.stream().map(ApplicantHistoryBillingDTO::getId)
            .toList();

        resetApplicantHistory(
            ResetBillingDTO.builder().userModified(userModified).ids(ids).build());

        billingDataFeedLogService.saveBillingDataFeed(
            BillingDataFeedRecordType.APPLICANT_HISTORY,
            applicantHistories.toString());

        UpdateApplicantHistoriesRequest applicantHistoriesRequest = UpdateApplicantHistoriesRequest.builder()
            .defendantHistories(applicantHistories).build();
        
        ResponseEntity<String> response = crownCourtLitigatorFeesApiClient.updateApplicantsHistory(applicantHistoriesRequest);
        
        if (response.getStatusCode().value() == 207) {
            log.warn("Some applicant history failed to update in the CCR/CCLF database. This applicant history will be updated to be re-sent next time.");
            
            List<Integer> failedIds = ResponseUtils.getErroredIdsFromResponseBody(response.getBody(), REQUEST_LABEL);
            
            if (!failedIds.isEmpty()) {
                applicantHistoryBillingRepository.setCclfFlag(failedIds, userModified, SENT_TO_CCLF_FAILURE_FLAG);
            }
        } else {
            log.info("Extracted applicant history data has been sent to the billing team.");
        }
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