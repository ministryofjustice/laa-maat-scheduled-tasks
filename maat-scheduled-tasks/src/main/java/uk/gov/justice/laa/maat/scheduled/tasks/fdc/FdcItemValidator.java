package uk.gov.justice.laa.maat.scheduled.tasks.fdc;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class FdcItemValidator {

  private final Validator validator;

  public <T> Set<ConstraintViolation<T>> validate(T dto) {
    return validator.validate(dto);
  }
}
