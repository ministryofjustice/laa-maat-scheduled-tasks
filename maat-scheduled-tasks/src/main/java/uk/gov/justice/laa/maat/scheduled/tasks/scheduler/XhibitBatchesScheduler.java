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

    @Scheduled(cron = "${xhibit_batch.trial_data_population.cron_expression}")
    public void executeTrialDataPopulationInToHub() {
        trialDataService.populateTrialDataInToHub();
    }

    @Scheduled(cron = "${xhibit_batch.trial_data_processing.cron_expression}")
    public void executeTrialDataProcessingInToMAAT() {
        trialDataService.processTrialDataInToMaat();
    }

}
