package uk.gov.justice.laa.maat.scheduled.tasks.helper;

import java.util.List;

public record StoredProcedureResponse(List<StoredProcedureParameter<?>> outputs) {

    public boolean hasValue(String parameterName) {
        return outputs.stream()
            .filter(p -> p.getName().equals(parameterName))
            .anyMatch(StoredProcedureParameter::hasValue);
    }

    public Object getValue(String parameterName) {
        return outputs.stream()
            .filter(o -> o.getName().equals(parameterName))
            .findFirst()
            .map(StoredProcedureParameter::getValue)
            .orElse(null);
    }
}
