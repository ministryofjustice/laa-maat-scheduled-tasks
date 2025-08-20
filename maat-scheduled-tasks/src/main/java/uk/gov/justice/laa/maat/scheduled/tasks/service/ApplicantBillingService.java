package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Int;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;

import java.util.List;

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

        if (!applicants.isEmpty()) {
            List<Integer> ids = applicants.stream().map(ApplicantBillingDTO::getId).toList();

            int rowsReset = resetApplicantBilling(
                ResetApplicantBillingDTO.builder().userModified(userModified).ids(ids).build());

            if (rowsReset != ids.size()) {
                throw new MAATScheduledTasksException(String.format(
                    "Number of applicant rows reset - %s does not equal the number of rows retrieved - %s.",
                    rowsReset, ids));
            }

            // TODO: Don't think we can get the request body as declaritive web client, would this be good enough???
            billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT,
                applicants.toString());

            // TODO: Transactional should rollback as the declaritive web client throws a WebClientResponseException get reviewed!!!
            crownCourtLitigatorFeesApiClient.updateApplicants(applicants);
            log.info("Extracted applicant data has been sent to the billing team.");
        }
    }

    private List<ApplicantBillingDTO> findAllApplicantsForBilling() {
        List<ApplicantBillingEntity> applicants = applicantBillingRepository.findAllApplicantsForBilling();
        log.info("Extracted data for {} applicants", applicants.size());

        return applicants.stream().map(applicantMapper::mapEntityToDTO).toList();
    }

    private int resetApplicantBilling(ResetApplicantBillingDTO resetApplicantBillingDTO) {
        int updatedRows = applicantBillingRepository.resetApplicantBilling(
            resetApplicantBillingDTO.getIds(), resetApplicantBillingDTO.getUserModified());
        log.info("Reset SEND_TO_CCLF for {} applicants", updatedRows);

        return updatedRows;
    }

}
