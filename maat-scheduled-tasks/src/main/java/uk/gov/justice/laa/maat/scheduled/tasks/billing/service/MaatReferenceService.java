package uk.gov.justice.laa.maat.scheduled.tasks.billing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.repository.MaatReferenceRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaatReferenceService {

    private final MaatReferenceRepository maatReferenceRepository;

    @Transactional
    public void populateMaatReferences() {
        log.info("Populating maat references table with data to send to CCLF...");

        if (maatReferenceRepository.count() != 0) {
            throw new MAATScheduledTasksException(
                "The maat references table is already populated.");
        }

        maatReferenceRepository.populateMaatReferences();
    }

}
