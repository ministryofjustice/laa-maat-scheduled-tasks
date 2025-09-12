package uk.gov.justice.laa.maat.scheduled.tasks.responses;

public record LoadFDCResponse(boolean success, int recordsInserted, String message) {}
