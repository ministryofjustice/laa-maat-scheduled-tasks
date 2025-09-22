package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service.AppealDataService;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service.TrialDataService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class XhibitBatchesSchedulerTest {

    @Mock
    private AppealDataService appealDataService;
    @Mock
    private TrialDataService trialDataService;
    @InjectMocks
    private XhibitBatchesScheduler scheduler;

    @Test
    void testExecuteTrialDataProcessing() {
        // When
        scheduler.executeTrialDataProcessing();

        // Then
        verify(trialDataService, times(1))
            .populateAndProcessData();
    }

    @Test
    void testExecuteAppealDataProcessing() {
        // When
        scheduler.executeAppealDataProcessing();

        // Then
        verify(appealDataService, times(1))
            .populateAndProcessData();
    }

}