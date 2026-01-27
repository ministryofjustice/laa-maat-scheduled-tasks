package uk.gov.justice.laa.maat.scheduled.tasks.fdc.response;

public record LoadFDCResponse(boolean success, int recordsInserted, String message) {}
