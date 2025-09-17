package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service.AppealDataService;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service.TrialDataService;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class XhibitBatchesScheduler {

    private final AppealDataService appealDataService;
    private final TrialDataService trialDataService;

    @Scheduled(cron = "${xhibit-batch.appeal_data_processing.cron_expression}")
    public void executeAppealDataProcessing() {
        appealDataService.populateAndProcessData();
    }

    @Scheduled(cron = "${xhibit-batch.trial_data_processing.cron_expression}")
    public void executeTrialDataProcessing() {
        trialDataService.populateAndProcessData();
    }

}
