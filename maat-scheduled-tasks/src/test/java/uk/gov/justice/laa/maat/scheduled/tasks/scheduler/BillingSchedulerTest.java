package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantHistoryBillingDTO;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getRepOrderBillingDTO;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;
import uk.gov.justice.laa.maat.scheduled.tasks.service.ApplicantBillingService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.ApplicantHistoryBillingService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.BillingDataFeedLogService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.MaatReferenceService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.RepOrderBillingService;

@ExtendWith(MockitoExtension.class)
public class BillingSchedulerTest {

    private static final Integer TEST_ID = 1;
    private static final String USER_MODIFIED = "TEST";

    @InjectMocks
    private BillingScheduler scheduler;

    @Mock
    private BillingConfiguration billingConfiguration;
    @Mock
    private MaatReferenceService maatReferenceService;
    @Mock
    private ApplicantBillingService applicantBillingService;
    @Mock
    private ApplicantHistoryBillingService applicantHistoryBillingService;
    @Mock
    private RepOrderBillingService repOrderBillingService;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;


    @Test
    void givenNoExceptions_whenExtractCCLFBillingDataIsInvoked_thenExtractIsPerformed() {
        when(billingConfiguration.getUserModified()).thenReturn("test");

        scheduler.extractCCLFBillingData();

        verify(maatReferenceService).populateMaatReferences();
        verify(applicantBillingService).sendToBilling(anyString());
        verify(applicantHistoryBillingService).sendToBilling(anyString());
        verify(repOrderBillingService).sendToBilling(anyString());
        verify(maatReferenceService).deleteMaatReferences();
    }

    @Test
    void givenExceptionThrown_whenExtractCCLFBillingDataIsInvoked_thenMaatReferencesDeleted() {
        doThrow(new MAATScheduledTasksException(
            "The maat references table is already populated.")).when(maatReferenceService)
            .populateMaatReferences();

        scheduler.extractCCLFBillingData();

        verifyNoInteractions(applicantBillingService);
        verifyNoInteractions(applicantHistoryBillingService);
        verifyNoInteractions(repOrderBillingService);
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
