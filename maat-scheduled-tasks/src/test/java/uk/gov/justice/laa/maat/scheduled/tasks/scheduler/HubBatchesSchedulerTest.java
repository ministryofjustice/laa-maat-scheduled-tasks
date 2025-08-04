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
class HubBatchesSchedulerTest {

    @InjectMocks
    private HubBatchesScheduler scheduler;

    @Mock
    private TrialDataService trialDataService;

    @Test
    void shouldCallPopulateTrialDataInToHub() {
        // When
        scheduler.executeTrialDataPopulationInToHub();

        // Then
        verify(trialDataService, times(1))
                .populateTrialDataInToHub();
    }

}