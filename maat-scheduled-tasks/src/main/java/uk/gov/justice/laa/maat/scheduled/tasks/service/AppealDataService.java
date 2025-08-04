package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppealDataService {

    public void processAppealDataInToMAAT() {
        log.info("Starting to process Appeal Data in to MAAT.");
    }
}
