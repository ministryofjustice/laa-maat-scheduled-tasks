package uk.gov.justice.laa.maat.scheduled.tasks.helper;

import jakarta.persistence.ParameterMode;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StoredProcedureParameter<T> {
    private final String name;
    private final Class<T> type;
    private final T value;
    private final ParameterMode mode;

    public static <T> StoredProcedureParameter<T> inputParameter(String name, T value) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.getClass();
        return new StoredProcedureParameter<>(name, type, value, ParameterMode.IN);
    }

    public static <T> StoredProcedureParameter<T> outputParameter(String name, Class<T> type) {
        return new StoredProcedureParameter<>(name, type, null, ParameterMode.OUT);
    }

    public static <T> StoredProcedureParameter<T> inOutParameter(String name, T value) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) value.getClass();
        return new StoredProcedureParameter<>(name, type, value, ParameterMode.INOUT);
    }

    @SuppressWarnings("unchecked")
    public static <T> StoredProcedureParameter<T> safePopulate(StoredProcedureParameter<?> parameter, Object value) {
        if (value == null) {
            return (StoredProcedureParameter<T>) parameter;
        }
        if (!parameter.getType().isInstance(value)) {
            String typeMismatchError = "Type mismatch for parameter: %s, expected: %s, but got: %s"
                .formatted(
                    parameter.getName(),
                    parameter.getType().getSimpleName(),
                    value.getClass().getSimpleName()
                );
            throw new IllegalArgumentException(typeMismatchError);
        }

        Class<T> type = (Class<T>) parameter.getType();
        T castedValue = type.cast(value);
        return parameterWithValue((StoredProcedureParameter<T>) parameter, castedValue);
    }

    static <T> StoredProcedureParameter<T> parameterWithValue(StoredProcedureParameter<T> parameter, T value) {
        return new StoredProcedureParameter<>(parameter.name, parameter.type, value, parameter.mode);
    }

    public boolean isOutputParameter() {
        return mode == ParameterMode.OUT || mode == ParameterMode.INOUT;
    }

    public boolean isInputParameter() {
        return mode == ParameterMode.IN || mode == ParameterMode.INOUT;
    }

    public boolean hasValue() {
        return value != null;
    }
}

