package uk.gov.justice.laa.maat.scheduled.tasks.fdc.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class FdcItemValidator {

  private final Validator validator;

  public <T> boolean validate(T dto) {

    Set<ConstraintViolation<T>> violations = validator.validate(dto);

    if (violations.isEmpty()) {
      return true;
    }

    log.warn(
        violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining("\n"))
    );

    return false;
  }
}
