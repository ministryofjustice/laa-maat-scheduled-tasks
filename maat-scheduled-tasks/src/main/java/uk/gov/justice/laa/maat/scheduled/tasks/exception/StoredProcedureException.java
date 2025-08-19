package uk.gov.justice.laa.maat.scheduled.tasks.exception;

public class StoredProcedureException extends RuntimeException {
    public StoredProcedureException(String message, Exception cause) {
        super(message, cause);
    }
}
