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

import static uk.gov.justice.laa.maat.scheduled.tasks.util.ListUtils.batchList;

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

        resetApplicantBilling(applicants, userModified);

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
        // Batching IDs due to Oracle hard limit of 1000 on IN clause.
        List<List<Integer>> batchedIds = batchList(
            applicants.stream().map(ApplicantBillingDTO::getId).toList(), 1000);

        for (List<Integer> batch : batchedIds) {
            int updatedRows = applicantBillingRepository.resetApplicantBilling(batch, userModified);
            log.info("CCLF Flag reset for batch of {} applicants", updatedRows);
        }
    }
}
