package uk.gov.justice.laa.maat.scheduled.tasks.helper;

import jakarta.persistence.ParameterMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.inOutParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.inputParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.outputParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.safePopulate;

class StoredProcedureParameterTest {

    @Test
    void testInputParameter() {
        StoredProcedureParameter<String> param = inputParameter("username", "name");

        assertEquals("username", param.getName());
        assertEquals(String.class, param.getType());
        assertEquals("name", param.getValue());
        assertEquals(ParameterMode.IN, param.getMode());
    }

    @Test
    void testOutputParameter() {
        StoredProcedureParameter<Integer> param = outputParameter("userId", Integer.class);

        assertEquals("userId", param.getName());
        assertEquals(Integer.class, param.getType());
        assertNull(param.getValue());
        assertEquals(ParameterMode.OUT, param.getMode());
    }

    @Test
    void testInOutParameter() {
        StoredProcedureParameter<Boolean> param = inOutParameter("active", true);

        assertEquals("active", param.getName());
        assertEquals(Boolean.class, param.getType());
        assertEquals(true, param.getValue());
        assertEquals(ParameterMode.INOUT, param.getMode());
    }

    @Test
    void badValueTypeThrowsException() {
        StoredProcedureParameter<Boolean> outParam = outputParameter("userId", Boolean.class);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> safePopulate(outParam, "notBoolean"));
        String expectedMessage = "Type mismatch for parameter: userId, expected: Boolean, but got: String";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void nullParameterValue_ReturnsOriginalParameter() {
        StoredProcedureParameter<String> outParam = outputParameter("userId", String.class);
        assertEquals(outParam, safePopulate(outParam, null));
    }
}
