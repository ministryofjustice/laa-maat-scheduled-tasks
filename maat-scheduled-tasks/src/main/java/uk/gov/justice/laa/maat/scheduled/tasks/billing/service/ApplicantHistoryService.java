package uk.gov.justice.laa.maat.scheduled.tasks.billing.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.repository.ApplicantHistoryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantHistoryService {

    private final ApplicantHistoryRepository applicantHistoryRepository;

    @Transactional(rollbackFor = MAATScheduledTasksException.class)
    public void resetApplicantHistory(String userModified, List<Integer> ids) {
        log.info("Resetting CCLF flag for extracted applicant histories...");
        int updatedRows = applicantHistoryRepository.resetApplicantHistory(userModified, ids);

        if (updatedRows != ids.size()) {
            String errorMsg = String.format(
                "Number of applicant histories reset: %d, does not equal those supplied in request: %d.",
                updatedRows, ids.size());
            log.error(errorMsg);
            throw new MAATScheduledTasksException(errorMsg);
        }
    }

}
