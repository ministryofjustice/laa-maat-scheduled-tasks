package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialDataService {

    public void populateTrialDataInToHub() {
        log.info("Starting to populate Trial Data in to Hub.");
    }

}
