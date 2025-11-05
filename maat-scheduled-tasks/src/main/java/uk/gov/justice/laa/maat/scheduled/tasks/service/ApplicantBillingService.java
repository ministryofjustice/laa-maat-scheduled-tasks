package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtRemunerationApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.BillingDataFeedLogMapper;
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

    public ApplicantBillingService(
        BillingDataFeedLogService billingDataFeedLogService,
        BillingDataFeedLogMapper billingDataFeedLogMapper,
        CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient, 
        CrownCourtRemunerationApiClient crownCourtRemunerationApiClient,
        ApplicantBillingRepository applicantBillingRepository, ApplicantMapper applicantMapper, 
        BillingConfiguration billingConfiguration, ResponseUtils responseUtils) {
        super(billingDataFeedLogService, billingDataFeedLogMapper, crownCourtLitigatorFeesApiClient,
            crownCourtRemunerationApiClient, billingConfiguration, responseUtils);
      this.applicantBillingRepository = applicantBillingRepository;
      this.applicantMapper = applicantMapper;
    }

    @Override
    protected List<ApplicantBillingDTO> getNewBillingRecords() {
        List<ApplicantBillingEntity> applicants = applicantBillingRepository.findAllApplicantsForBilling();
        log.info("Extracted data for {} applicants", applicants.size());

        return applicants.stream().map(applicantMapper::mapEntityToDTO).toList();
    }

    @Override
    protected List<ApplicantBillingDTO> getPreviouslySentBillingRecords() {
        List<BillingDataFeedLogEntity> billingLogEntities = billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.APPLICANT);

        return billingLogEntities.stream()
            .map(billingDataFeedLogMapper::mapEntityToApplicantBillingDtos)
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    protected void resetBillingFlag(List<Integer> ids) {
        int rowsUpdated = applicantBillingRepository.resetApplicantBilling(
            ids, billingConfiguration.getUserModified());
        log.debug("Billing Flag reset for {} applicants.", rowsUpdated);
    }

    @Override
    protected BillingDataFeedRecordType getBillingDataFeedRecordType() {
        return BillingDataFeedRecordType.APPLICANT;
    }

    @Override
    protected List<ResponseEntity<String>> updateBillingRecords(List<ApplicantBillingDTO> applicants) {
        UpdateApplicantsRequest applicantsRequest = UpdateApplicantsRequest.builder()
            .defendants(applicants).build();

        List<ResponseEntity<String>> responses = new ArrayList<>();

        responses.add(crownCourtLitigatorFeesApiClient.updateApplicants(applicantsRequest));
        responses.add(crownCourtRemunerationApiClient.updateApplicants(applicantsRequest));
        
        return responses;
    }

    @Override
    protected String getRequestLabel() {
        return REQUEST_LABEL;
    }

    @Override
    protected void updateBillingRecordFailures(List<Integer> failedIds) {
            List<ApplicantBillingEntity> failedApplicants = applicantBillingRepository.findAllById(failedIds);
            for (ApplicantBillingEntity failedApplicant : failedApplicants) {
                failedApplicant.setSendToCclf(true);
                failedApplicant.setUserModified(billingConfiguration.getUserModified());
            }

            applicantBillingRepository.saveAll(failedApplicants);
    }
}
