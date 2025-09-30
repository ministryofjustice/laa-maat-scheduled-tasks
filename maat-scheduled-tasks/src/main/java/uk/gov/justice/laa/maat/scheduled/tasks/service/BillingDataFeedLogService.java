package uk.gov.justice.laa.maat.scheduled.tasks.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.BillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.BillingDataFeedLogMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.BillingDataFeedLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingDataFeedLogService {

    private static final String INVALID_DATE_MESSAGE = "A date must be provided for the logs to be deleted.";

    private final BillingDataFeedLogMapper billingDataFeedLogMapper;
    private final BillingDataFeedLogRepository billingDataFeedLogRepository;

    public List<BillingDataFeedLogEntity> getBillingDataFeedLogs(BillingDataFeedRecordType recordType) {
        return billingDataFeedLogRepository.getBillingDataFeedLogEntitiesByRecordType(recordType.getValue());
    }

    public <T extends BillingDTO> void saveBillingDataFeed(BillingDataFeedRecordType recordType, List<T> payload) {
        try {
            BillingDataFeedLogEntity entity = billingDataFeedLogMapper.mapDtoToEntity(recordType, payload);

            billingDataFeedLogRepository.save(entity);
        }
        catch (JsonProcessingException ex) {
            log.error("Unable to serialise payload of type {}", recordType, ex);
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
