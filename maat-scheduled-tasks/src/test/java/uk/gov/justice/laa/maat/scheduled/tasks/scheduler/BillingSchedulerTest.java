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
    void givenNoExtractData_whenExtractBillingDataIsInvoked_thenNoActionsPerformed() {
        when(applicantBillingService.findAllApplicantsForBilling()).thenReturn(Collections.emptyList());
        when(applicantHistoryBillingService.extractApplicantHistory()).thenReturn(Collections.emptyList());
        when(repOrderBillingService.getRepOrdersForBilling()).thenReturn(Collections.emptyList());

        scheduler.extractBillingData();

        verify(maatReferenceService).populateMaatReferences();
        verify(applicantBillingService, never()).sendApplicantsToBilling(anyList(), anyString());
        verify(applicantHistoryBillingService, never()).sendApplicantHistoryToBilling(anyList(), anyString());
        verify(repOrderBillingService, never()).sendRepOrdersToBilling(anyList(), anyString());
        verify(maatReferenceService).deleteMaatReferences();
    }

    @Test
    void givenNoExceptions_whenExtractBillingDataIsInvoked_thenExtractIsPerformed() {
        scheduler.extractBillingData();

        verify(maatReferenceService).populateMaatReferences();
        verify(applicantBillingService, never()).sendApplicantsToBilling(anyList(), anyString());
        verify(applicantHistoryBillingService, never()).sendApplicantHistoryToBilling(anyList(), anyString());
        verify(repOrderBillingService, never()).sendRepOrdersToBilling(anyList(), anyString());
        verify(maatReferenceService).deleteMaatReferences();
    }

    @Test
    void givenExceptionThrown_whenExtractBillingDataIsInvoked_thenMaatReferencesDeleted() {
        doThrow(new MAATScheduledTasksException("The maat references table is already populated."))
            .when(maatReferenceService).populateMaatReferences();

        scheduler.extractBillingData();

        verifyNoInteractions(applicantBillingService);
        verifyNoInteractions(applicantHistoryBillingService);
        verifyNoInteractions(repOrderBillingService);
        verify(maatReferenceService).deleteMaatReferences();
    }

    @Test
    void givenDataUnderBatchSize_whenExtractCCLFBillingDataIsInvoked_thenExtractIsPerformedInSingleBatch() {
        ApplicantBillingDTO applicant = getApplicantDTO(TEST_ID);
        ApplicantHistoryBillingDTO applicantHistory = getApplicantHistoryBillingDTO(TEST_ID);
        RepOrderBillingDTO repOrder = getRepOrderBillingDTO(TEST_ID);

        when(applicantBillingService.findAllApplicantsForBilling()).thenReturn(
            List.of(applicant, applicant));
        when(applicantHistoryBillingService.extractApplicantHistory()).thenReturn(List.of(applicantHistory, applicantHistory));
        when(repOrderBillingService.getRepOrdersForBilling()).thenReturn(List.of(repOrder, repOrder));
        when(billingConfiguration.getUserModified()).thenReturn(USER_MODIFIED);
        when(billingConfiguration.getBatchSize()).thenReturn(5);

        scheduler.extractBillingData();

        verify(maatReferenceService).populateMaatReferences();
        verify(applicantBillingService, times(1)).sendApplicantsToBilling(List.of(applicant, applicant), USER_MODIFIED);
        verify(applicantHistoryBillingService, times(1)).sendApplicantHistoryToBilling(List.of(applicantHistory, applicantHistory), USER_MODIFIED);
        verify(repOrderBillingService, times(1)).sendRepOrdersToBilling(List.of(repOrder, repOrder), USER_MODIFIED);
        verify(maatReferenceService).deleteMaatReferences();
    }

    @Test
    void givenDataOverBatchSize_whenExtractCCLFBillingDataIsInvoked_thenExtractIsPerformedInMultipleBatches() {
        ApplicantBillingDTO applicant = getApplicantDTO(TEST_ID);
        ApplicantHistoryBillingDTO applicantHistory = getApplicantHistoryBillingDTO(TEST_ID);
        RepOrderBillingDTO repOrder = getRepOrderBillingDTO(TEST_ID);

        when(applicantBillingService.findAllApplicantsForBilling()).thenReturn(List.of(applicant, applicant));
        when(applicantHistoryBillingService.extractApplicantHistory()).thenReturn(List.of(applicantHistory, applicantHistory));
        when(repOrderBillingService.getRepOrdersForBilling()).thenReturn(List.of(repOrder, repOrder));
        when(billingConfiguration.getUserModified()).thenReturn(USER_MODIFIED);
        when(billingConfiguration.getBatchSize()).thenReturn(1);

        scheduler.extractBillingData();

        verify(maatReferenceService).populateMaatReferences();
        verify(applicantBillingService, times(2)).sendApplicantsToBilling(List.of(applicant), USER_MODIFIED);
        verify(applicantHistoryBillingService, times(2)).sendApplicantHistoryToBilling(List.of(applicantHistory), USER_MODIFIED);
        verify(repOrderBillingService, times(2)).sendRepOrdersToBilling(List.of(repOrder), USER_MODIFIED);
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
