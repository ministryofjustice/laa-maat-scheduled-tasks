package uk.gov.justice.laa.maat.scheduled.tasks.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.BillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;

@ExtendWith(MockitoExtension.class)
class BillingDataFeedLogMapperTest {

    private ObjectMapper objectMapper;

    private BillingDataFeedLogMapper mapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

        mapper = new BillingDataFeedLogMapper(objectMapper);
    }

    @Test
    void givenValidBillingEntityForApplicantDto_whenMapEntityToDtoIsInvoked_thenPopulatedDtoIsReturned()
        throws JsonProcessingException {
        ApplicantBillingDTO validDto = getApplicantDTO(123);
        String serialisedDto = objectMapper.writeValueAsString(validDto);

        BillingDataFeedLogEntity entity = BillingDataFeedLogEntity.builder()
            .id(1)
            .recordType(BillingDataFeedRecordType.APPLICANT.getValue())
            .payload(serialisedDto)
            .build();

        ApplicantBillingDTO deserialisedDto = (ApplicantBillingDTO) mapper.mapEntityToDTO(entity);

        assertEquals(validDto, deserialisedDto);
    }

    @Test
    void givenBillingEntityWithInvalidPayload_whenMapEntityToDtoIsInvoked_thenReturnsNull() {
        String serialisedDto = "{\"id\":123,\"first_name:}";

        BillingDataFeedLogEntity entity = BillingDataFeedLogEntity.builder()
            .id(1)
            .recordType(BillingDataFeedRecordType.APPLICANT.getValue())
            .payload(serialisedDto)
            .build();

        BillingDTO deserialisedDto = mapper.mapEntityToDTO(entity);

        assertNull(deserialisedDto);
    }
}
