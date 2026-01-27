package uk.gov.justice.laa.maat.scheduled.tasks.fdc.response;

import java.util.List;

public record FDCLoadResponseWithInvalids<T>(boolean success, List<T> invalid, String message) {}
