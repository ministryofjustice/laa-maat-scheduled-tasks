package uk.gov.justice.laa.maat.scheduled.tasks.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorDTO {
  int statusCode;
  String message;
}
