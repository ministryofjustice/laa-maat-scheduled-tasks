package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.service.TrialDataService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class XhibitBatchesSchedulerTest {

    @InjectMocks
    private XhibitBatchesScheduler scheduler;

    @Mock
    private TrialDataService trialDataService;

    @Test
    void testExecuteTrialDataProcessing() {
        // When
        scheduler.executeTrialDataProcessing();

        // Then
        verify(trialDataService, times(1))
                .populateTrialDataInToHub();
        verify(trialDataService, times(1))
                .processTrialDataInToMaat();
    }

    @Test
    void testExecuteAppealDataProcessing() {
        // When
        scheduler.executeAppealDataProcessing();

        // Then
        verify(trialDataService, times(1))
                .populateAppealDataInToHub();
        verify(trialDataService, times(1))
                .processAppealDataInToMaat();
    }

}