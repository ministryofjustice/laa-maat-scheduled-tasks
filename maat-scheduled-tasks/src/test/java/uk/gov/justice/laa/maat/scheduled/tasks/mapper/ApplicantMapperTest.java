package uk.gov.justice.laa.maat.scheduled.tasks.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedApplicantBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;

class ApplicantMapperTest {

    private static final int TEST_ID = 1234343;

    private final ApplicantMapper mapper = Mappers.getMapper(ApplicantMapper.class);

    @Test
    void givenEntity_whenMappedToDTO_fieldsArePopulatedCorrectly() {
        ApplicantBillingEntity entity = getPopulatedApplicantBillingEntity(TEST_ID);
        ApplicantBillingDTO expectedDto = getApplicantDTO(TEST_ID);

        ApplicantBillingDTO actualDto = mapper.mapEntityToDTO(entity);

        assertThat(actualDto).usingRecursiveComparison().isEqualTo(expectedDto);
    }

}
