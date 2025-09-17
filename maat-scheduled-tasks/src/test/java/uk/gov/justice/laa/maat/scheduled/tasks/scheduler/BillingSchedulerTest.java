package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;
import uk.gov.justice.laa.maat.scheduled.tasks.service.ApplicantBillingService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.ApplicantHistoryBillingService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.BillingDataFeedLogService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.MaatReferenceService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.RepOrderBillingService;

@ExtendWith(MockitoExtension.class)
public class BillingSchedulerTest {

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

    @BeforeEach
    void setUp() {
        lenient().when(billingConfiguration.getUserModified()).thenReturn("test");
    }

    @Test
    void givenNoExceptions_whenExtractBillingDataIsInvoked_thenExtractIsPerformed() {
        scheduler.extractBillingData();

        verify(maatReferenceService).populateMaatReferences();
        verify(applicantBillingService).sendApplicantsToBilling(anyString());
        verify(applicantHistoryBillingService).sendApplicantHistoryToBilling(anyString());
        verify(repOrderBillingService).sendRepOrdersToBilling(anyString());
        verify(maatReferenceService).deleteMaatReferences();
    }

    @Test
    void givenExceptionThrown_whenExtractBillingDataIsInvoked_thenMaatReferencesDeleted() {
        doThrow(new MAATScheduledTasksException("The maat references table is already populated."))
            .when(maatReferenceService).populateMaatReferences();

        scheduler.extractBillingData();

        verify(applicantBillingService, never()).sendApplicantsToBilling(anyString());
        verify(applicantHistoryBillingService, never()).sendApplicantHistoryToBilling(anyString());
        verify(repOrderBillingService, never()).sendRepOrdersToBilling(anyString());
        verify(maatReferenceService).deleteMaatReferences();
    }

    @Test
    void givenNoExceptions_whenResendBillingDataIsInvoked_thenResendIsPerformed() {
        scheduler.resendBillingData();

        verify(applicantBillingService).resendApplicantsToBilling(anyString());
        verify(applicantHistoryBillingService).resendApplicantHistoryToBilling(anyString());
        verify(repOrderBillingService).resendRepOrdersToBilling(anyString());
    }

    @Test
    void givenExceptionThrown_whenResendBillingDataIsInvoked_thenExceptionIsLoggedAndRethrown() {
        MAATScheduledTasksException expectedException = new MAATScheduledTasksException("Something went wrong.");

        doThrow(expectedException)
            .when(applicantBillingService).resendApplicantsToBilling(anyString());

        MAATScheduledTasksException actualException = assertThrows(MAATScheduledTasksException.class,
            () -> scheduler.resendBillingData());

        assertEquals(expectedException, actualException);

        verify(applicantBillingService, times(1)).resendApplicantsToBilling(anyString());
        verify(applicantHistoryBillingService, never()).resendApplicantHistoryToBilling(anyString());
        verify(repOrderBillingService, never()).resendRepOrdersToBilling(anyString());
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
