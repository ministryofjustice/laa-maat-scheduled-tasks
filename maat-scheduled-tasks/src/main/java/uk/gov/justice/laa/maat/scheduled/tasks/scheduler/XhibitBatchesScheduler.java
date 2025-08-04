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

    @Scheduled(cron = "${xhibit_batch.appeal_data_processing.cron_expression}")
    public void executeAppealDataProcessing() {
        trialDataService.populateAppealDataInToHub();
        trialDataService.processAppealDataInToMaat();
    }

    @Scheduled(cron = "${xhibit_batch.trial_data_processing.cron_expression}")
    public void executeTrialDataProcessing() {
        trialDataService.populateTrialDataInToHub();
        trialDataService.processTrialDataInToMaat();
    }

}
