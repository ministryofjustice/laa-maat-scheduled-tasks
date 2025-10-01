package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.BillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;

@Slf4j
@Service
public class ApplicantHistoryBillingService extends BillingService {

    private final ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    private final ApplicantHistoryBillingMapper applicantHistoryBillingMapper;
    private static final String REQUEST_LABEL = "applicant history";

    public ApplicantHistoryBillingService(BillingDataFeedLogService billingDataFeedLogService,
        CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient,
        ApplicantHistoryBillingRepository applicantHistoryBillingRepository,
        ApplicantHistoryBillingMapper applicantHistoryBillingMapper) {
            super(billingDataFeedLogService, crownCourtLitigatorFeesApiClient);
            this.applicantHistoryBillingRepository = applicantHistoryBillingRepository;
            this.applicantHistoryBillingMapper = applicantHistoryBillingMapper;
    }

    @Override
    protected List<? extends BillingDTO> getBillingDTOList() {
        List<ApplicantHistoryBillingEntity> applicantHistoryEntities = applicantHistoryBillingRepository.extractApplicantHistoryForBilling();
        log.info("Application histories successfully extracted for billing data.");

        return applicantHistoryEntities
            .stream()
            .map(applicantHistoryBillingMapper::mapEntityToDTO)
            .toList();
    }

    @Override
    protected void resetBillingCCLFFlag(String userModified, List ids) {
        applicantHistoryBillingRepository.resetApplicantHistory(userModified, ids);
        log.info("Resetting CCLF flag for extracted applicant histories.");
    }

    @Override
    protected BillingDataFeedRecordType getBillingDataFeedRecordType() {
        return BillingDataFeedRecordType.APPLICANT_HISTORY;
    }

    @Override
    protected ResponseEntity<String> updateBillingRecords(List applicantHistories) {
        UpdateApplicantHistoriesRequest applicantHistoriesRequest = UpdateApplicantHistoriesRequest.builder()
            .defendantHistories(applicantHistories).build();

        return crownCourtLitigatorFeesApiClient.updateApplicantsHistory(applicantHistoriesRequest);
    }

    @Override
    protected String getRequestLabel() {
        return REQUEST_LABEL;
    }

    @Override
    protected void updateBillingRecordFailures(List failedIds, String userModified) {
        List<ApplicantHistoryBillingEntity> failedApplicantHistory = applicantHistoryBillingRepository.findAllById(failedIds);
        for (ApplicantHistoryBillingEntity failedHistory : failedApplicantHistory) {
            failedHistory.setSendToCclf(SENT_TO_CCLF_FAILURE_FLAG);
            failedHistory.setUserModified(userModified);
        }

        applicantHistoryBillingRepository.saveAll(failedApplicantHistory);
    }
}