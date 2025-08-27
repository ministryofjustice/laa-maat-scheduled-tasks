package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantHistoryBillingService {

    private final ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    private final ApplicantHistoryBillingMapper applicantHistoryBillingMapper;

    @Transactional
    public void sendApplicantHistoryToBilling(String userModified) {
        List<ApplicantHistoryBillingDTO> applicantHistories = extractApplicantHistory();

        if (!applicantHistories.isEmpty()) {
            List<Integer> ids = applicantHistories.stream().map(ApplicantHistoryBillingDTO::getId)
                .toList();

            resetApplicantHistory(
                ResetBillingDTO.builder().userModified(userModified).ids(ids).build());

            billingDataFeedLogService.saveBillingDataFeed(
                BillingDataFeedRecordType.APPLICANT_HISTORY,
                applicantHistories.toString());

            crownCourtLitigatorFeesApiClient.updateApplicantsHistory(applicantHistories);
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