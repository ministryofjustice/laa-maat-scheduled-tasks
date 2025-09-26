package uk.gov.justice.laa.maat.scheduled.tasks.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantHistoryBillingDTO;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getRepOrderBillingDTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;

@ExtendWith(MockitoExtension.class)
class BillingDataFeedLogMapperTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .build();

    private final BillingDataFeedLogMapper mapper = new BillingDataFeedLogMapper(objectMapper);

    @Test
    void givenEmptyPayloadProvided_whenMapDtoToEntityIsInvoked_thenExceptionIsThrown() {
        List<ApplicantBillingDTO> dtos = Collections.emptyList();

        assertThrows(IllegalArgumentException.class,
            () -> mapper.mapDtoToEntity(BillingDataFeedRecordType.APPLICANT, dtos));
    }

    @Test
    void givenValidPayload_whenMapDtoToEntityIsInvoked_thenEntityWithSerialisedPayloadIsReturned()
        throws JsonProcessingException {
        List<ApplicantBillingDTO> dtos = List.of(getApplicantDTO(123));
        String expectedPayload = objectMapper.writeValueAsString(dtos);

        BillingDataFeedLogEntity entity = mapper.mapDtoToEntity(BillingDataFeedRecordType.APPLICANT, dtos);

        assertThat(expectedPayload).isEqualTo(entity.getPayload());
    }

    @Test
    void givenValidBillingEntityForApplicantDto_whenMapEntityToApplicantBillingDtosIsInvoked_thenPopulatedDtoIsReturned()
        throws JsonProcessingException {
        List<ApplicantBillingDTO> expectedDtos = List.of(getApplicantDTO(123));
        String serialisedDto = objectMapper.writeValueAsString(expectedDtos);

        BillingDataFeedLogEntity entity = BillingDataFeedLogEntity.builder()
            .id(1)
            .recordType(BillingDataFeedRecordType.APPLICANT.getValue())
            .payload(serialisedDto)
            .build();

        List<ApplicantBillingDTO> actualDtos = mapper.mapEntityToApplicantBillingDtos(entity);

        assertThat(expectedDtos).isEqualTo(actualDtos);
    }

    @Test
    void givenPayloadIsUnableToBeDeserialised_whenMapEntityToApplicantBillingDtosIsInvoked_thenReturnsNull() {
         BillingDataFeedLogEntity entity = BillingDataFeedLogEntity.builder()
            .id(1)
            .recordType(BillingDataFeedRecordType.APPLICANT.getValue())
            .payload("{\"id\":123,\"first_name\":}")
            .build();

        List<ApplicantBillingDTO> deserialisedDtos = mapper.mapEntityToApplicantBillingDtos(entity);

        assertThat(deserialisedDtos).isNull();
    }

    @Test
    void givenValidBillingEntityForApplicantHistoryDto_whenMapEntityToApplicantHistoryBillingDtosIsInvoked_thenPopulatedDtoIsReturned()
        throws JsonProcessingException {
        List<ApplicantHistoryBillingDTO> expectedDtos = List.of(getApplicantHistoryBillingDTO(456));
        String serialisedDto = objectMapper.writeValueAsString(expectedDtos);

        BillingDataFeedLogEntity entity = BillingDataFeedLogEntity.builder()
            .id(1)
            .recordType(BillingDataFeedRecordType.APPLICANT_HISTORY.getValue())
            .payload(serialisedDto)
            .build();

        List<ApplicantHistoryBillingDTO> actualDtos = mapper.mapEntityToApplicationHistoryBillingDtos(entity);

        assertThat(expectedDtos).isEqualTo(actualDtos);
    }

    @Test
    void givenPayloadIsUnableToBeDeserialised_whenMapEntityToApplicantHistoryBillingDtosIsInvoked_thenReturnsNull() {
        BillingDataFeedLogEntity entity = BillingDataFeedLogEntity.builder()
            .id(1)
            .recordType(BillingDataFeedRecordType.APPLICANT_HISTORY.getValue())
            .payload("{\"id\":456,\"first_name\":}")
            .build();

        List<ApplicantHistoryBillingDTO> deserialisedDtos = mapper.mapEntityToApplicationHistoryBillingDtos(entity);

        assertThat(deserialisedDtos).isNull();
    }

    @Test
    void givenValidBillingEntityForRepOrderDto_whenMapEntityToRepOrderBillingDtosIsInvoked_thenPopulatedDtoIsReturned()
        throws JsonProcessingException {
        List<RepOrderBillingDTO> expectedDtos = List.of(getRepOrderBillingDTO(78));
        String serialisedDto = objectMapper.writeValueAsString(expectedDtos);

        BillingDataFeedLogEntity entity = BillingDataFeedLogEntity.builder()
            .id(1)
            .recordType(BillingDataFeedRecordType.REP_ORDER.getValue())
            .payload(serialisedDto)
            .build();

        List<RepOrderBillingDTO> actualDtos = mapper.mapEntityToRepOrderBillingDtos(entity);

        assertThat(expectedDtos).isEqualTo(actualDtos);
    }

    @Test
    void givenPayloadIsUnableToBeDeserialised_whenMapEntityToRepOrderBillingDtosIsInvoked_thenReturnsNull() {
        BillingDataFeedLogEntity entity = BillingDataFeedLogEntity.builder()
            .id(1)
            .recordType(BillingDataFeedRecordType.REP_ORDER.getValue())
            .payload("{\"id\":78,\"applicant_id\":}")
            .build();

        List<RepOrderBillingDTO> deserialisedDtos = mapper.mapEntityToRepOrderBillingDtos(entity);

        assertThat(deserialisedDtos).isNull();
    }
}
