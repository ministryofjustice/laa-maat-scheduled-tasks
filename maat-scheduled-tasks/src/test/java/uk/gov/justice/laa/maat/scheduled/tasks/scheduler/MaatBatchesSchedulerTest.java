package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.service.LocalManagementReportsService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.StoredProcedureService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MaatBatchesSchedulerTest {

    private static final String MAAT_BATCH_PROCESS_CORRESPONDENCE = "maat_batch.process_correspondence";
    private static final String MAAT_BATCH_INACTIVE_USERS = "maat_batch.process_inactive_users";
    private static final String MAAT_BATCH_FA_FIX = "maat_batch..FA_fix";

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
                .callStoredProcedure(MAAT_BATCH_PROCESS_CORRESPONDENCE);
    }

    @Test
    void shouldCallProcessDeactivateInactiveUsers() {
        // When
        scheduler.deactivateInactiveUsers();

        // Then
        verify(storedProcedureService, times(1))
                .callStoredProcedure(MAAT_BATCH_INACTIVE_USERS);
    }

    @Test
    void shouldCallExecuteFinancialAssessmentFix() {
        // When
        scheduler.executeFinancialAssessmentFix();

        // Then
        verify(storedProcedureService, times(1))
                .callStoredProcedure(MAAT_BATCH_FA_FIX);
    }
}