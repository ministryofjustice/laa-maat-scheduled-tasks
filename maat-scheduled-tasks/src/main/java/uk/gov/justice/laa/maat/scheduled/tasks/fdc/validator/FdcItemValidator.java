package uk.gov.justice.laa.maat.scheduled.tasks.fdc.validator;

import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class FdcItemValidator {

  private final Validator validator;

  public <T> boolean validate(T dto) {
    return validator.validate(dto).isEmpty();
  }
}
