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
}

