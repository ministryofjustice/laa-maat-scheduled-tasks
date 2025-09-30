package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.Collection;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.BillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.BillingDataFeedLogMapper;
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
    private final BillingDataFeedLogMapper billingDataFeedLogMapper;

    @Transactional
    public void sendApplicantsToBilling(String userModified) {
        List<ApplicantBillingDTO> applicants = findAllApplicantsForBilling();

        if (applicants.isEmpty()) {
            return;
        }

        List<Integer> ids = applicants.stream().map(BillingDTO::getId).toList();

        resetApplicantBillingFlag(
            ResetApplicantBillingDTO.builder().userModified(userModified).ids(ids).build());

        sendApplicantsToBilling(applicants);
    }

    public void resendApplicantsToBilling(String userModified) {
        List<BillingDataFeedLogEntity> billingLogEntities = billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.APPLICANT);

        List<ApplicantBillingDTO> applicants = billingLogEntities.stream()
            .map(billingDataFeedLogMapper::mapEntityToApplicantBillingDtos)
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .toList();

        if (applicants.isEmpty()) {
            return;
        }

        sendApplicantsToBilling(applicants);
    }

    private void sendApplicantsToBilling(List<ApplicantBillingDTO> applicants) {
        billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT, applicants);

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

    private void resetApplicantBillingFlag(ResetApplicantBillingDTO resetApplicantBillingDTO) {
        int updatedRows = applicantBillingRepository.resetApplicantBilling(
            resetApplicantBillingDTO.getIds(), resetApplicantBillingDTO.getUserModified());

        log.info("Reset SEND_TO_CCLF for {} applicants", updatedRows);
    }

}
