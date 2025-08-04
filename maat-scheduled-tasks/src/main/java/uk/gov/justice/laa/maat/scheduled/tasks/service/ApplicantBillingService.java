package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.text.MessageFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantBillingService {

    private final ApplicantBillingRepository applicantBillingRepository;
    private final ApplicantMapper applicantMapper;

    public List<ApplicantBillingDTO> findAllApplicantsForBilling() {
        List<ApplicantBillingEntity> applicants = applicantBillingRepository.findAllApplicantsForBilling();
        log.info("Extracted data for {} applicants", applicants.size());

        return applicants.stream().map(applicantMapper::mapEntityToDTO).toList();
    }

    @Transactional(rollbackFor = MAATScheduledTasksException.class)
    public void resetApplicantBilling(ResetApplicantBillingDTO resetApplicantBillingDTO) {
        int updatedRows = applicantBillingRepository.resetApplicantBilling(resetApplicantBillingDTO.getIds(), resetApplicantBillingDTO.getUserModified());
        log.info("Reset SEND_TO_CCLF for {} applicants", updatedRows);

        if (updatedRows != resetApplicantBillingDTO.getIds().size()) {
            String message = MessageFormat.format(
                "Unable to reset applicants sent for billing as only {0} applicant(s) could be processed (from a total of {1} applicant(s))", 
                updatedRows, resetApplicantBillingDTO.getIds().size());
            log.error(message);
            throw new MAATScheduledTasksException(message);
        }
    }

}
