package uk.gov.justice.laa.maat.scheduled.tasks.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.BillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.BillingDataFeedLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingDataFeedLogService {

    private static final String INVALID_DATE_MESSAGE = "A date must be provided for the logs to be deleted.";

    private final BillingDataFeedLogRepository billingDataFeedLogRepository;
    private final ObjectMapper objectMapper;

    public void saveBillingDataFeed(BillingDataFeedRecordType recordType,
        List<? extends BillingDTO> billingDTO) {
        try {
            String payload = objectMapper.writeValueAsString(billingDTO);

            BillingDataFeedLogEntity entity = BillingDataFeedLogEntity.builder()
                .recordType(recordType.getValue())
                .dateCreated(LocalDateTime.now())
                .payload(payload)
                .build();

            billingDataFeedLogRepository.save(entity);
            log.debug("Data feed saved for applicants.");
        } catch (JsonProcessingException exception) {
            String errorMsg = String.format(
                "Error serializing payload to store in data feed table: %s",
                exception.getMessage());
            throw new MAATScheduledTasksException(errorMsg);
        }
    }

    @Transactional
    public Long deleteLogsBeforeDate(LocalDateTime dateTime) {

        if (dateTime == null) {
            log.error("No date passed to deleteLogsBeforeDate()");
            throw new IllegalArgumentException(INVALID_DATE_MESSAGE);
        }

        return billingDataFeedLogRepository.deleteByDateCreatedBefore(dateTime);
    }
}
