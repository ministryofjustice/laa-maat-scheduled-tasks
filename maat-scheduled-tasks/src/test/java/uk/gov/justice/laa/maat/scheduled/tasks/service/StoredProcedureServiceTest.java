package uk.gov.justice.laa.maat.scheduled.tasks.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.StoredProcedureQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void testCallStoredProcedure_executionFails_throwsRuntimeException() {
        // Arrange
        String procedureName = "faulty_procedure";
        when(entityManager.createStoredProcedureQuery(procedureName)).thenReturn(storedProcedureQuery);
        when(storedProcedureQuery.execute()).thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                storedProcedureService.callStoredProcedure(procedureName)
        );
        assertTrue(exception.getMessage().contains("Failed to execute stored procedure"));
    }
}