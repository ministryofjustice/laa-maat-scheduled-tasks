package uk.gov.justice.laa.maat.scheduled.tasks.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;
import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.util.ListUtils;

import static uk.gov.justice.laa.maat.scheduled.tasks.util.ListUtils.batchList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantBillingService {

    private final ApplicantBillingRepository applicantBillingRepository;
    private final ApplicantMapper applicantMapper;
    private final BillingConfiguration billingConfiguration;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .build();

    @Transactional
    public void sendApplicantsToBilling(String userModified) {
        List<ApplicantBillingDTO> applicants = findAllApplicantsForBilling();

        if (applicants.isEmpty()) {
            return;
        }

        resetApplicantBilling(applicants, userModified);

        billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT,
            applicants.toString());

        List<List<ApplicantBillingDTO>> batchedApplicants = batchList(applicants,
            billingConfiguration.getRequestBatchSize());

        for (List<ApplicantBillingDTO> currentBatch : batchedApplicants) {
            UpdateApplicantsRequest applicantsRequest = UpdateApplicantsRequest.builder()
                .defendants(currentBatch).build();

            crownCourtLitigatorFeesApiClient.updateApplicants(applicantsRequest);
        }

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
            applicants.stream().map(ApplicantBillingDTO::getId).toList(),
            billingConfiguration.getResetBatchSize());

        for (List<Integer> batch : batchedIds) {
            int updatedRows = applicantBillingRepository.resetApplicantBilling(batch, userModified);
            log.info("CCLF Flag reset for batch of {} applicants", updatedRows);
        }
    }
}
