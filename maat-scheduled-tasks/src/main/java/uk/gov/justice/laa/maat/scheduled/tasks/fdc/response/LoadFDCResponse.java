package uk.gov.justice.laa.maat.scheduled.tasks.fdc.response;

import java.util.List;

public record LoadFDCResponse<T>(boolean success, List<T> invalid, String message) {}
