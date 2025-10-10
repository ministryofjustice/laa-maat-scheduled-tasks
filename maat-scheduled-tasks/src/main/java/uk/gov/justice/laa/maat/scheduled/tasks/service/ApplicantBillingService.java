package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;
import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.ResponseUtils;

@Slf4j
@Service
public class ApplicantBillingService extends BillingService<ApplicantBillingDTO> {

    private final ApplicantBillingRepository applicantBillingRepository;
    private final ApplicantMapper applicantMapper;
    private static final String REQUEST_LABEL = "applicant";

    public ApplicantBillingService(BillingDataFeedLogService billingDataFeedLogService,
        CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient,
        ApplicantBillingRepository applicantBillingRepository, ApplicantMapper applicantMapper, 
        BillingConfiguration billingConfiguration, ResponseUtils responseUtils) {
        super(billingDataFeedLogService, crownCourtLitigatorFeesApiClient, billingConfiguration, responseUtils);
      this.applicantBillingRepository = applicantBillingRepository;
      this.applicantMapper = applicantMapper;
    }

    @Override
    protected List<ApplicantBillingDTO> getBillingDTOList() {
        List<ApplicantBillingEntity> applicants = applicantBillingRepository.findAllApplicantsForBilling();
        log.info("Extracted data for {} applicants", applicants.size());

        return applicants.stream().map(applicantMapper::mapEntityToDTO).toList();
    }

    @Override
    protected void resetBillingCCLFFlag(String userModified, List<Integer> ids) {
        int updatedRows = applicantBillingRepository.resetApplicantBilling(
            ids, userModified);
        log.info("Reset SEND_TO_CCLF for {} applicants", updatedRows);
    }

    @Override
    protected BillingDataFeedRecordType getBillingDataFeedRecordType() {
        return BillingDataFeedRecordType.APPLICANT;
    }

    @Override
    protected ResponseEntity<String> updateBillingRecords(List<ApplicantBillingDTO> applicants) {
        UpdateApplicantsRequest applicantsRequest = UpdateApplicantsRequest.builder()
            .defendants(applicants).build();

        return crownCourtLitigatorFeesApiClient.updateApplicants(applicantsRequest);
    }

    @Override
    protected String getRequestLabel() {
        return REQUEST_LABEL;
    }

    @Override
    protected void updateBillingRecordFailures(List<Integer> failedIds, String userModified) {
            List<ApplicantBillingEntity> failedApplicants = applicantBillingRepository.findAllById(failedIds);
            for (ApplicantBillingEntity failedApplicant : failedApplicants) {
                failedApplicant.setSendToCclf(SEND_TO_CCLF_FAILURE_FLAG);
                failedApplicant.setUserModified(userModified);
            }

            applicantBillingRepository.saveAll(failedApplicants);
    }
}
