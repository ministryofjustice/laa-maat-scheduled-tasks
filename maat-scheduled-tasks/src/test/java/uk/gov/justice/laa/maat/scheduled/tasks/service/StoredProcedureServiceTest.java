package uk.gov.justice.laa.maat.scheduled.tasks.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.StoredProcedureException;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        String procedureName = "my_procedure";
        when(entityManager.createStoredProcedureQuery(procedureName)).thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.execute()).thenReturn(true);

        // Act
        storedProcedureService.callStoredProcedure(procedureName);

        // Assert
        verify(entityManager).createStoredProcedureQuery(procedureName);
        verify(storedProcedureQuery).execute();
    }

    @Test
    void testCallStoredProcedure_nullName_throwsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                storedProcedureService.callStoredProcedure(null)
        );
        assertEquals("Stored procedure name cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCallStoredProcedure_emptyName_throwsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                storedProcedureService.callStoredProcedure(" ")
        );
        assertEquals("Stored procedure name cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCallStoredProcedure_executionFails_throwsStoredProcedureException() {
        // Arrange
        String procedureName = "faulty_procedure";
        when(entityManager.createStoredProcedureQuery(procedureName)).thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.execute()).thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        StoredProcedureException exception = assertThrows(StoredProcedureException.class, () ->
                storedProcedureService.callStoredProcedure(procedureName)
        );
        assertTrue(exception.getMessage().contains("Failed to execute stored procedure"));
    }

    @Test
    void testCallStoredProcedure_withParameters_registeredAndSetSuccessfully() {
        String procedureName = "my_procedure";
        Map<String, Object> inputParams = Map.of("param1", "value1", "param2", 3);

        when(entityManager.createStoredProcedureQuery(procedureName)).thenReturn(storedProcedureQuery);

        storedProcedureService.callStoredProcedure(procedureName, inputParams, Collections.emptyMap());

        verify(storedProcedureQuery).registerStoredProcedureParameter("param1", String.class, ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter("param2", Integer.class, ParameterMode.IN);
        verify(storedProcedureQuery).setParameter("param1", "value1");
        verify(storedProcedureQuery).setParameter("param2", 3);
        verify(storedProcedureQuery).execute();
    }

    @Test
    void testCallStoredProcedure_WithInAndOutParams_registeredAndSetSuccessfully() {
        String procedureName = "my_procedure";
        Map<String, Object> inputParams = Map.of("inParam1", "value1", "inParam2", 3);
        Map<String, Class<?>> outParams = Map.of("outParam1", String.class, "outParam2", Integer.class);

        when(entityManager.createStoredProcedureQuery(procedureName)).thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.getOutputParameterValue("outParam1")).thenReturn("returnValue");
        when(storedProcedureQuery.getOutputParameterValue("outParam2")).thenReturn(5);

        storedProcedureService.callStoredProcedure(procedureName, inputParams, outParams);

        verify(storedProcedureQuery).registerStoredProcedureParameter("inParam1", String.class, ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter("inParam2", Integer.class, ParameterMode.IN);
        verify(storedProcedureQuery).registerStoredProcedureParameter("outParam1", String.class, ParameterMode.OUT);
        verify(storedProcedureQuery).registerStoredProcedureParameter("outParam2", Integer.class, ParameterMode.OUT);
        verify(storedProcedureQuery).setParameter("inParam1", "value1");
        verify(storedProcedureQuery).setParameter("inParam2", 3);
        verify(storedProcedureQuery).execute();
    }

}
