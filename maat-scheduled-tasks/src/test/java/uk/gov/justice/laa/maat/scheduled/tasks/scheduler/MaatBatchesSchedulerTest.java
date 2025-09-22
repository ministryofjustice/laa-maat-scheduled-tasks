package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.StoredProcedure;
import uk.gov.justice.laa.maat.scheduled.tasks.service.LocalManagementReportsService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.StoredProcedureService;

@ExtendWith(MockitoExtension.class)
class MaatBatchesSchedulerTest {

    @InjectMocks
    private MaatBatchesScheduler scheduler;

    @Mock
    private StoredProcedureService storedProcedureService;
    @Mock
    private LocalManagementReportsService localManagementReportsService;


    @Test
    void shouldCallProcessLocalManagementReportsBatches() {
        // When
        scheduler.executeLocalManagementReports();

        // Then
        verify(localManagementReportsService, times(1))
                .processReportsBatches();
    }

    @Test
    void shouldCallProcessEvidenceReminderLetter() {
        // When
        scheduler.generateEvidenceReminderLetter();

        // Then
        verify(storedProcedureService, times(1))
                .callStoredProcedure(StoredProcedure.MAAT_BATCH_PROCESS_CORRESPONDENCE);
    }

    @Test
    void shouldCallProcessDeactivateInactiveUsers() {
        // When
        scheduler.deactivateInactiveUsers();

        // Then
        verify(storedProcedureService, times(1))
                .callStoredProcedure(StoredProcedure.MAAT_BATCH_INACTIVE_USERS);
    }

    @Test
    void shouldCallExecuteFinancialAssessmentFix() {
        // When
        scheduler.executeFinancialAssessmentFix();

        // Then
        verify(storedProcedureService, times(1))
                .callStoredProcedure(StoredProcedure.MAAT_BATCH_FA_FIX);
    }
}