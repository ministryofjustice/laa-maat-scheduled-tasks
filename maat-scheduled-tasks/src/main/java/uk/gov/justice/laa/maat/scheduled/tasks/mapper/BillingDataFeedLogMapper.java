package uk.gov.justice.laa.maat.scheduled.tasks.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.BillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;

@Component
@RequiredArgsConstructor
public class BillingDataFeedLogMapper {
    private final ObjectMapper objectMapper;

    public BillingDTO mapEntityToDTO(BillingDataFeedLogEntity entity) {
        BillingDataFeedRecordType recordType = Enum.valueOf(BillingDataFeedRecordType.class, entity.getRecordType());

        try {
            return switch (recordType) {
                case APPLICANT -> objectMapper.readValue(entity.getPayload(), ApplicantBillingDTO.class);
                case APPLICANT_HISTORY -> objectMapper.readValue(entity.getPayload(), ApplicantHistoryBillingDTO.class);
                case REP_ORDER -> objectMapper.readValue(entity.getPayload(), RepOrderBillingDTO.class);
            };
        }
        catch (JsonProcessingException e) {
            return null;
        }

    }
}
