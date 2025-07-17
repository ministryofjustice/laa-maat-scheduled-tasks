package uk.gov.justice.laa.maat.scheduled.tasks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalManagementReportsServiceTest {

    @InjectMocks
    private LocalManagementReportsService localManagementReportsService;

    @Mock
    private StoredProcedureService storedProcedureService;

    /**
     * Test class for LocalManagementReportsService.
     * The processReportsBatches() method should call stored procedures for each batch number defined.
     */

    @Test
    void processReportsBatches_shouldCallAllStoredProcedures() {
        // Act
        localManagementReportsService.processReportsBatches();

        // Assert
        verify(storedProcedureService, times(1))
                .callStoredProcedure("maat_batch.process_reports_batch_1");
        verify(storedProcedureService, times(1))
                .callStoredProcedure("maat_batch.process_reports_batch_2");
        verify(storedProcedureService, times(1))
                .callStoredProcedure("maat_batch.process_reports_batch_3");
        verify(storedProcedureService, times(1))
                .callStoredProcedure("maat_batch.process_reports_batch_4");
        verify(storedProcedureService, times(1))
                .callStoredProcedure("maat_batch.process_reports_batch_5");
        verify(storedProcedureService, times(1))
                .callStoredProcedure("maat_batch.process_reports_batch_6");
    }
}