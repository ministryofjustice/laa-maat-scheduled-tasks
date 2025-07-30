package uk.gov.justice.laa.maat.scheduled.tasks.exception;

public class RecordsAlreadyExistException extends RuntimeException {

    public RecordsAlreadyExistException(String message) {
        super(message);
    }
}
