package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.service.BillingDataFeedLogService;

@ExtendWith(MockitoExtension.class)
public class BillingSchedulerTest {

    @InjectMocks
    private BillingScheduler scheduler;

    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    
    @Test
    void testCleanupBillingDataFeedLog() {
        // When
        scheduler.cleanupBillingDataFeedLog();

        // Then
        verify(billingDataFeedLogService, times(1))
            .deleteLogsBeforeDate(any());
    }

}
