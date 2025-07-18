package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.service.StoredProcedureService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CentralPrintSchedulerTest {

    private static final String BATCH_CENTRAL_PRINT_RUN = "rep.xxrep_batch.central_print_run";

    @InjectMocks
    private CentralPrintScheduler centralPrintScheduler;

    @Mock
    private StoredProcedureService storedProcedureService;

    /**
     * Test for executeCentralPrintJob method. It verifies
     * whether the stored procedure is called correctly.
     */
    @Test
    void whenExecuteCentralPrintJob_thenStoredProcedureIsCalled() {
        // Act
        centralPrintScheduler.executeCentralPrintJob();

        // Assert
        verify(storedProcedureService, times(1))
                .callStoredProcedure(BATCH_CENTRAL_PRINT_RUN);
    }
}