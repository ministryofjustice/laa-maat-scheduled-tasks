package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;
import uk.gov.justice.laa.maat.scheduled.tasks.service.ApplicantBillingService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.ApplicantHistoryBillingService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.BatchProcessingService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.BillingDataFeedLogService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.MaatReferenceService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.RepOrderBillingService;

@ExtendWith(MockitoExtension.class)
public class BillingSchedulerTest {

    @InjectMocks
    private BillingScheduler scheduler;
    @Mock
    private MaatReferenceService maatReferenceService;
    @Mock
    private BatchProcessingService batchProcessingService;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;

    @Test
    void givenNoExceptions_whenExtractBillingDataIsInvoked_thenExtractIsPerformed() {
        scheduler.extractCCLFBillingData();

        verify(maatReferenceService).populateMaatReferences();
        verify(batchProcessingService, times(1)).processApplicantBatch();
        verify(batchProcessingService, times(1)).processApplicantHistoryBatch();
        verify(batchProcessingService, times(1)).processRepOrderBatch();
        verify(maatReferenceService).deleteMaatReferences();
    }

    @Test
    void givenExceptionThrown_whenExtractBillingDataIsInvoked_thenMaatReferencesDeleted() {
        doThrow(new MAATScheduledTasksException("The maat references table is already populated."))
            .when(maatReferenceService).populateMaatReferences();

        scheduler.extractCCLFBillingData();

        verifyNoInteractions(batchProcessingService);
        verify(maatReferenceService).deleteMaatReferences();
    }

    @Test
    void testCleanupBillingDataFeedLog() {
        // When
        scheduler.cleanupBillingDataFeedLog();

        // Then
        verify(billingDataFeedLogService, times(1))
            .deleteLogsBeforeDate(any());
    }
}
