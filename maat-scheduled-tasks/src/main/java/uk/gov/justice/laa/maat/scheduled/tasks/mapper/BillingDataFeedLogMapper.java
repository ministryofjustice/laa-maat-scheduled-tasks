package uk.gov.justice.laa.maat.scheduled.tasks.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.BillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingDataFeedLogMapper {
    private final ObjectMapper objectMapper;

    public <T extends BillingDTO> BillingDataFeedLogEntity mapDtoToEntity(BillingDataFeedRecordType recordType, List<T> dtos)
        throws JsonProcessingException {
        if (dtos == null || dtos.isEmpty()) {
            throw new IllegalArgumentException("Data to be serialised must be provided");
        }

        return BillingDataFeedLogEntity.builder()
            .recordType(recordType.getValue())
            .dateCreated(LocalDateTime.now())
            .payload(objectMapper.writeValueAsString(dtos))
            .build();
    }

    public List<ApplicantBillingDTO> mapEntityToApplicantBillingDtos(BillingDataFeedLogEntity entity) {
        try {
            return objectMapper.readValue(entity.getPayload(), new TypeReference<>() {});
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            logDeserialisationException(entity, ex);
            return null;
        }
    }

    public List<ApplicantHistoryBillingDTO> mapEntityToApplicationHistoryBillingDtos(BillingDataFeedLogEntity entity) {
        try {
            return objectMapper.readValue(entity.getPayload(), new TypeReference<>() {});
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            logDeserialisationException(entity, ex);
            return null;
        }
    }

    public List<RepOrderBillingDTO> mapEntityToRepOrderBillingDtos(BillingDataFeedLogEntity entity) {
        try {
            return objectMapper.readValue(entity.getPayload(), new TypeReference<>() {});
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            logDeserialisationException(entity, ex);
            return null;
        }
    }

    private void logDeserialisationException(BillingDataFeedLogEntity entity, Exception ex) {
        log.error("Unable to deserialise entity payload of type {}", entity.getRecordType(), ex);
    }
}
