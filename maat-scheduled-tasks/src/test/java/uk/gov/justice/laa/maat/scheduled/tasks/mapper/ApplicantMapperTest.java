package uk.gov.justice.laa.maat.scheduled.tasks.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedApplicantBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;

class ApplicantMapperTest {

    private static final int TEST_ID = 1234343;

    private final ApplicantMapper mapper = Mappers.getMapper(ApplicantMapper.class);

    @Test
    void givenEntity_whenMappedToDTO_fieldsArePopulatedCorrectly() {
        ApplicantBillingEntity entity = getPopulatedApplicantBillingEntity(TEST_ID);
        ApplicantBillingDTO expectedDto = getApplicantDTO(TEST_ID);

        ApplicantBillingDTO actualDto = mapper.mapEntityToDTO(entity);
        assertEquals(expectedDto.getId(), actualDto.getId());
        assertEquals(expectedDto.getFirstName(), actualDto.getFirstName());
        assertEquals(expectedDto.getLastName(), actualDto.getLastName());
        assertEquals(expectedDto.getOtherNames(), actualDto.getOtherNames());
        assertEquals(expectedDto.getDob(), actualDto.getDob());
        assertEquals(expectedDto.getGender(), actualDto.getGender());
        assertEquals(expectedDto.getNiNumber(), actualDto.getNiNumber());
        assertEquals(expectedDto.getForeignId(), actualDto.getForeignId());
        assertEquals(expectedDto.getDateCreated(), actualDto.getDateCreated());
        assertEquals(expectedDto.getUserCreated(), actualDto.getUserCreated());
        assertEquals(expectedDto.getDateModified(), actualDto.getDateModified());
        assertEquals(expectedDto.getUserModified(), actualDto.getUserModified());
    }

}
