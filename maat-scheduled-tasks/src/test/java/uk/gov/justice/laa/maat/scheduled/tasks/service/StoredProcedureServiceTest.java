package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.inOutParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.inputParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.outputParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.safePopulate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.StoredProcedure;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.StoredProcedureException;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureResponse;

@ExtendWith(MockitoExtension.class)
class StoredProcedureServiceTest {

    @InjectMocks
    private StoredProcedureService storedProcedureService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private StoredProcedureQuery storedProcedureQuery;

    @Test
    void testCallStoredProcedure_validProcedure_executesSuccessfully() {
        // Arrange
        when(entityManager.createStoredProcedureQuery(
                StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE.getQualifiedName()))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.execute()).thenReturn(true);

        // Act
        storedProcedureService.callStoredProcedure(StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE);

        // Assert
        verify(storedProcedureQuery).execute();
        verify(entityManager).createStoredProcedureQuery(
                StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE.getQualifiedName());
    }

    @Test
    void testCallStoredProcedure_nullName_throwsIllegalArgumentException() {
        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
                storedProcedureService.callStoredProcedure(null)
        );

        assertEquals("Stored procedure must not be null", exception.getMessage());
    }

    @Test
    void testCallStoredProcedure_executionFails_throwsStoredProcedureException() {
        // Arrange
        String procedureName = "faulty_procedure";
        when(entityManager.createStoredProcedureQuery(
                StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE.getQualifiedName()))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.execute()).thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        StoredProcedureException exception = assertThrows(StoredProcedureException.class, () ->
                storedProcedureService.callStoredProcedure(
                        StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE)
        );
        assertTrue(exception.getMessage().contains("Failed to execute stored procedure"));
    }

    @Test
    void testCalledStoredProcedure_withParameters_registeredAndExecutedSuccessfully() {
        List<StoredProcedureParameter<?>> parameters = List.of(
                inputParameter("inParam1", "value1"),
                inputParameter("inParam2", 3),
                outputParameter("outParam1", String.class),
                outputParameter("outParam2", Integer.class),
                inOutParameter("inOutParam1", "inOutValue"),
                inOutParameter("inOutParam2", 1234L)
        );

        when(entityManager.createStoredProcedureQuery(
                StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE.getQualifiedName()))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getOutputParameterValue("outParam1")).thenReturn("returnValue");
        when(storedProcedureQuery.getOutputParameterValue("outParam2")).thenReturn(5);
        when(storedProcedureQuery.getOutputParameterValue("inOutParam1")).thenReturn("inOutValue");
        when(storedProcedureQuery.getOutputParameterValue("inOutParam2")).thenReturn(1234L);

        storedProcedureService.callStoredProcedure(StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE,
                parameters);

        verify(storedProcedureQuery).registerStoredProcedureParameter("inParam1", String.class,
                ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter("inParam2", Integer.class,
                ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter("outParam1", String.class,
                ParameterMode.OUT);
        verify(storedProcedureQuery).registerStoredProcedureParameter("outParam2", Integer.class,
                ParameterMode.OUT);
        verify(storedProcedureQuery).registerStoredProcedureParameter("inOutParam1", String.class,
                ParameterMode.INOUT);
        verify(storedProcedureQuery).registerStoredProcedureParameter("inOutParam2", Long.class,
                ParameterMode.INOUT);

        verify(storedProcedureQuery).setParameter("inParam1", "value1");
        verify(storedProcedureQuery).setParameter("inParam2", 3);
        verify(storedProcedureQuery).setParameter("inOutParam1", "inOutValue");
        verify(storedProcedureQuery).setParameter("inOutParam2", 1234L);

        verify(storedProcedureQuery).execute();
    }

    @Test
    void executedStoredProcedure_outputParams_populatedAndReturnedInResponse() {
        List<StoredProcedureParameter<?>> parameters = List.of(
                outputParameter("outParam1", Integer.class),
                outputParameter("outParam2", String.class),
                outputParameter("outParam3", Object.class)
        );

        when(entityManager.createStoredProcedureQuery(
                StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE.getQualifiedName()))
                .thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getOutputParameterValue("outParam1")).thenReturn(3);
        when(storedProcedureQuery.getOutputParameterValue("outParam2")).thenReturn("outValue");
        when(storedProcedureQuery.getOutputParameterValue("outParam3")).thenReturn(null);

        StoredProcedureResponse expectedResponse = new StoredProcedureResponse(
                List.of(
                        safePopulate(outputParameter("outParam1", Integer.class), 3),
                        safePopulate(outputParameter("outParam2", String.class), "outValue"),
                        safePopulate(outputParameter("outParam3", Object.class), null)
                )
        );
        assertEquals(expectedResponse, storedProcedureService.callStoredProcedure(
                StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE, parameters));
    }
}
