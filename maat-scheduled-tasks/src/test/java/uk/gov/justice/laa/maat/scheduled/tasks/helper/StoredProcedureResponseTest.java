package uk.gov.justice.laa.maat.scheduled.tasks.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.outputParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.parameterWithValue;

import java.util.List;
import org.junit.jupiter.api.Test;

class StoredProcedureResponseTest {

    @Test
    void hasValue_parameterWithValue_returnsTrue() {
        var param = parameterWithValue(outputParameter("param1", String.class), "value1");
        var response = new StoredProcedureResponse(List.of(param));
        assertThat(response.hasValue("param1")).isTrue();
    }

    @Test
    void getValue_parameterWithValue_returnsValue() {
        var param = parameterWithValue(outputParameter("param1", Integer.class), 42);
        var response = new StoredProcedureResponse(List.of(param));
        assertThat(response.getValue("param1")).isEqualTo(42);
    }

    @Test
    void getValue_parameterNotPresent_returnsNull() {
        var param = parameterWithValue(outputParameter("param1", String.class), "value1");
        var response = new StoredProcedureResponse(List.of(param));
        assertThat(response.getValue("param2")).isNull();
    }

    @Test
    void getValue_parameterNull_returnsNull() {
        var param = parameterWithValue(outputParameter("param1", String.class), null);
        var response = new StoredProcedureResponse(List.of(param));
        assertThat(response.getValue("param1")).isNull();
    }
}

