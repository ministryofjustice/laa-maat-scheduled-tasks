package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.StoredProcedure;
import uk.gov.justice.laa.maat.scheduled.tasks.service.StoredProcedureService;

@ExtendWith(MockitoExtension.class)
class CentralPrintSchedulerTest {

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
                .callStoredProcedure(StoredProcedure.BATCH_CENTRAL_PRINT_RUN);
    }
}