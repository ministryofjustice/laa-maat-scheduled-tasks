package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.service.TrialDataService;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class XhibitBatchesScheduler {

    private final TrialDataService trialDataService;

    @Scheduled(cron = "-")
    public void executeAppealDataProcessing() {
        trialDataService.populateAppealData();
        trialDataService.processAppealDataInToMaat();
    }

    @Scheduled(cron = "-")
    public void executeTrialDataProcessing() {
        trialDataService.populateTrialData();
        trialDataService.processTrialDataInToMaat();
    }

}
