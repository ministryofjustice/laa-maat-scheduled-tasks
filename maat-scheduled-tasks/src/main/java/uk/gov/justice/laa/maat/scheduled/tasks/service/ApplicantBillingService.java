package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.Collection;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantBillingService {

    private final ApplicantBillingRepository applicantBillingRepository;
    private final ApplicantMapper applicantMapper;
    private final BillingDataFeedLogMapper billingDataFeedLogMapper;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    private final CrownCourtRemunerationApiClient crownCourtRemunerationApiClient;

    public List<ApplicantBillingDTO> findAllApplicantsForBilling() {
        List<ApplicantBillingEntity> applicants = applicantBillingRepository.findAllApplicantsForBilling();
        log.debug("Extracted data for {} applicants.", applicants.size());

        return applicants.stream().map(applicantMapper::mapEntityToDTO).toList();
    }

    @Transactional
    public void sendApplicantsToBilling(List<ApplicantBillingDTO> applicants, String userModified) {
        if (applicants.isEmpty()) {
            return;
        }

        resetApplicantBilling(applicants, userModified);

        billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT,
            applicants);

        UpdateApplicantsRequest applicantsRequest = UpdateApplicantsRequest.builder()
            .defendants(applicants).build();

        crownCourtLitigatorFeesApiClient.updateApplicants(applicantsRequest);
        crownCourtRemunerationApiClient.updateApplicants(applicantsRequest);
    }

    public void resendApplicantsToBilling() {
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
        billingDataFeedLogService.saveBillingDataFeed(
            BillingDataFeedRecordType.APPLICANT, applicants);

        UpdateApplicantsRequest applicantsRequest = UpdateApplicantsRequest.builder()
            .defendants(applicants).build();

        crownCourtLitigatorFeesApiClient.updateApplicants(applicantsRequest);
        crownCourtRemunerationApiClient.updateApplicants(applicantsRequest);
    }

    private void resetApplicantBilling(List<ApplicantBillingDTO> applicants, String userModified) {
        List<Integer> ids = applicants.stream().map(ApplicantBillingDTO::getId).toList();

        int rowsUpdated = applicantBillingRepository.resetApplicantBilling(ids, userModified);
        log.debug("CCLF Flag reset for {} applicants.", rowsUpdated);
    }
}
