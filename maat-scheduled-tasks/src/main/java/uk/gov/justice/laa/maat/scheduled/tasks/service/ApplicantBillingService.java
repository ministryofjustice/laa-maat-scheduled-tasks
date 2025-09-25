package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.ResponseUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantBillingService {

    private final ApplicantBillingRepository applicantBillingRepository;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    private final ApplicantMapper applicantMapper;
    private static final String SENT_TO_CCLF_FAILURE_FLAG = "Y";
    private static final String REQUEST_LABEL = "applicant";

    @Transactional
    public void sendApplicantsToBilling(String userModified) {
        List<ApplicantBillingDTO> applicants = findAllApplicantsForBilling();

        if (applicants.isEmpty()) {
            return;
        }

        List<Integer> ids = applicants.stream().map(ApplicantBillingDTO::getId).toList();

        resetApplicantBilling(
            ResetApplicantBillingDTO.builder().userModified(userModified).ids(ids).build());

        billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT,
            applicants.toString());

        UpdateApplicantsRequest applicantsRequest = UpdateApplicantsRequest.builder()
            .defendants(applicants).build();
        
        ResponseEntity<String> response = crownCourtLitigatorFeesApiClient.updateApplicants(applicantsRequest);

        if (response.getStatusCode().value() == HttpStatus.MULTI_STATUS.value()) {
            log.warn("Some applicants failed to update in the CCR/CCLF database. These applicants will be updated to be re-sent next time.");

            List<Integer> failedIds = ResponseUtils.getErroredIdsFromResponseBody(response.getBody(), REQUEST_LABEL);
            
            if (!failedIds.isEmpty()) {
                applicantBillingRepository.setCclfFlag(failedIds, userModified, SENT_TO_CCLF_FAILURE_FLAG);
            }
        } else {
            log.info("Extracted applicant data has been sent to the billing team.");
        }
    }

    private List<ApplicantBillingDTO> findAllApplicantsForBilling() {
        List<ApplicantBillingEntity> applicants = applicantBillingRepository.findAllApplicantsForBilling();
        log.info("Extracted data for {} applicants", applicants.size());

        return applicants.stream().map(applicantMapper::mapEntityToDTO).toList();
    }

    private void resetApplicantBilling(ResetApplicantBillingDTO resetApplicantBillingDTO) {
        int updatedRows = applicantBillingRepository.resetApplicantBilling(
            resetApplicantBillingDTO.getIds(), resetApplicantBillingDTO.getUserModified());
        log.info("Reset SEND_TO_CCLF for {} applicants", updatedRows);
    }
}
