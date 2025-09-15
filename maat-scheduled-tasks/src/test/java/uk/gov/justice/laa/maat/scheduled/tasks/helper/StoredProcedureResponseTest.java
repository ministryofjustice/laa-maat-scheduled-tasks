package uk.gov.justice.laa.maat.scheduled.tasks.helper;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.outputParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.parameterWithValue;

class StoredProcedureResponseTest {

    @Test
    void hasValue_parameterWithValue_returnsTrue() {
        var param = parameterWithValue(outputParameter("param1", String.class), "value1");
        var response = new StoredProcedureResponse(List.of(param));

        assertTrue(response.hasValue("param1"));
    }

    @Test
    void getValue_parameterWithValue_returnsValue() {
        var param = parameterWithValue(outputParameter("param1", Integer.class), 42);
        var response = new StoredProcedureResponse(List.of(param));

        assertEquals(42, response.getValue("param1"));
    }

    @Test
    void getValue_parameterNotPresent_returnsNull() {
        var param = parameterWithValue(outputParameter("param1", String.class), "value1");
        var response = new StoredProcedureResponse(List.of(param));

        assertNull(response.getValue("param2"));
    }

    @Test
    void getValue_parameterNull_returnsNull() {
        var param = parameterWithValue(outputParameter("param1", String.class), null);
        var response = new StoredProcedureResponse(List.of(param));

        assertNull(response.getValue("param1"));
    }
}

