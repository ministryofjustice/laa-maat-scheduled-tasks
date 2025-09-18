package uk.gov.justice.laa.maat.scheduled.tasks.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.inOutParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.inputParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.outputParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.safePopulate;

import jakarta.persistence.ParameterMode;
import org.junit.jupiter.api.Test;

class StoredProcedureParameterTest {

    @Test
    void testInputParameter() {
        StoredProcedureParameter<String> param = inputParameter("username", "name");

        assertThat(param.getName()).isEqualTo("username");
        assertThat(param.getType()).isEqualTo(String.class);
        assertThat(param.getValue()).isEqualTo("name");
        assertThat(param.getMode()).isEqualTo(ParameterMode.IN);
    }

    @Test
    void testOutputParameter() {
        StoredProcedureParameter<Integer> param = outputParameter("userId", Integer.class);

        assertThat(param.getValue()).isNull();
        assertThat(param.getName()).isEqualTo("userId");
        assertThat(param.getType()).isEqualTo(Integer.class);
        assertThat(param.getMode()).isEqualTo(ParameterMode.OUT);
    }

    @Test
    void testInOutParameter() {
        StoredProcedureParameter<Boolean> param = inOutParameter("active", true);

        assertThat(param.getValue()).isTrue();
        assertThat(param.getName()).isEqualTo("active");
        assertThat(param.getType()).isEqualTo(Boolean.class);
        assertThat(param.getMode()).isEqualTo(ParameterMode.INOUT);
    }

    @Test
    void badValueTypeThrowsException() {
        StoredProcedureParameter<Boolean> outParam = outputParameter("userId", Boolean.class);
        assertThatThrownBy(() -> safePopulate(outParam, "notBoolean"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Type mismatch for parameter: userId, expected: Boolean, but got: String");
    }

    @Test
    void nullParameterValue_ReturnsOriginalParameter() {
        StoredProcedureParameter<String> outParam = outputParameter("userId", String.class);
        assertThat(safePopulate(outParam, null)).isEqualTo(outParam);
    }
}
