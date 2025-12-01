package uk.gov.justice.laa.maat.scheduled.tasks.fdc.validator;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FdcReadyRequestDTO;

import static org.assertj.core.api.Assertions.assertThat;

class FdcItemValidatorTest {

    private FdcItemValidator validator;

    @BeforeEach
    void setUp() {
        Validator jakartaValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new FdcItemValidator(jakartaValidator);
    }

    @Test
    void validate_returnsTrue_forValidDTO() {
        FdcReadyRequestDTO dto = FdcReadyRequestDTO.builder()
                .maatReference(123)
                .fdcReady("Y")
                .itemType("LGFS")
                .build();
        assertThat(validator.validate(dto)).isTrue();
    }

    @Test
    void validate_returnsFalse_forNullMaatReference() {
        FdcReadyRequestDTO dto = FdcReadyRequestDTO.builder()
                .maatReference(null)
                .fdcReady("Y")
                .itemType("LGFS")
                .build();
        assertThat(validator.validate(dto)).isFalse();
    }


    @Test
    void validate_returnsFalse_forInvalidFdcReady() {
        FdcReadyRequestDTO dto = FdcReadyRequestDTO.builder()
                .maatReference(123)
                .fdcReady("X")
                .itemType("LGFS")
                .build();
        assertThat(validator.validate(dto)).isFalse();
    }

    @Test
    void validate_returnsFalse_forLowerCaseItemType() {
        FdcReadyRequestDTO dto = FdcReadyRequestDTO.builder()
                .maatReference(123)
                .fdcReady("Y")
                .itemType("lgfs")
                .build();
        assertThat(validator.validate(dto)).isTrue();
    }

    @Test
    void validate_returnsFalse_forInvalidItemType() {
        FdcReadyRequestDTO dto = FdcReadyRequestDTO.builder()
                .maatReference(123)
                .fdcReady("Y")
                .itemType("INVALID")
                .build();
        assertThat(validator.validate(dto)).isFalse();
    }

    @Test
    void validate_returnsFalse_forBlankFields() {
        FdcReadyRequestDTO dto = FdcReadyRequestDTO.builder()
                .maatReference(123)
                .fdcReady("")
                .itemType("")
                .build();
        assertThat(validator.validate(dto)).isFalse();
    }
}
