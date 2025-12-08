package uk.gov.justice.laa.maat.scheduled.tasks.fdc.validator;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.YesNo;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostReadyDTO;

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
        FinalDefenceCostReadyDTO dto = FinalDefenceCostReadyDTO.builder()
                .maatReference(123)
                .fdcReady(YesNo.Y)
                .itemType(FDCType.LGFS)
                .build();
        assertThat(validator.validate(dto)).isTrue();
    }

    @Test
    void validate_returnsFalse_forNullMaatReference() {
        FinalDefenceCostReadyDTO dto = FinalDefenceCostReadyDTO.builder()
                .maatReference(null)
                .fdcReady(YesNo.Y)
                .itemType(FDCType.LGFS)
                .build();
        assertThat(validator.validate(dto)).isFalse();
    }


    @Test
    void validate_returnsFalse_forInvalidFdcReady() {
        FinalDefenceCostReadyDTO dto = FinalDefenceCostReadyDTO.builder()
                .maatReference(123)
                .fdcReady(null)
                .itemType(FDCType.LGFS)
                .build();
        assertThat(validator.validate(dto)).isFalse();
    }
}
