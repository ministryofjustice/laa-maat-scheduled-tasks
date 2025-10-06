package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantBillingService {

    private final ApplicantBillingRepository applicantBillingRepository;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    private final ApplicantMapper applicantMapper;

    @Transactional
    public void sendApplicantsToBilling(String userModified) {
        List<ApplicantBillingDTO> applicants = findAllApplicantsForBilling();

        if (applicants.isEmpty()) {
            return;
        }

        List<Integer> ids = applicants.stream().map(ApplicantBillingDTO::getId).toList();


        billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT,
            applicants.toString());

        UpdateApplicantsRequest applicantsRequest = UpdateApplicantsRequest.builder()
            .defendants(applicants).build();

        crownCourtLitigatorFeesApiClient.updateApplicants(applicantsRequest);
        log.info("Extracted applicant data has been sent to the billing team.");
    }

    private List<ApplicantBillingDTO> findAllApplicantsForBilling() {
        List<ApplicantBillingEntity> applicants = applicantBillingRepository.findAllApplicantsForBilling();
        log.info("Extracted data for {} applicants", applicants.size());

        return applicants.stream().map(applicantMapper::mapEntityToDTO).toList();
    }
    private void resetApplicantBilling(List<ApplicantBillingDTO> applicants, String userModified) {
        List<Integer> ids = applicants.stream().map(ApplicantBillingDTO::getId).toList();

        int rowsUpdated = applicantBillingRepository.resetApplicantBilling(ids, userModified);
        log.debug("CCLF Flag reset for {} applicants.", rowsUpdated);
    }

}
