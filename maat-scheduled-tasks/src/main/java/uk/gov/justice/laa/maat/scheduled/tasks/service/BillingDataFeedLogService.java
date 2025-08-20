package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.BillingDataFeedLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingDataFeedLogService {

    private static final String INVALID_DATE_MESSAGE = "A date must be provided for the logs to be deleted.";
    
    private final BillingDataFeedLogRepository billingDataFeedLogRepository;

    public void saveBillingDataFeed(BillingDataFeedRecordType recordType, String payload) {
        BillingDataFeedLogEntity entity = new BillingDataFeedLogEntity();
        entity.setRecordType(recordType.getValue());
        entity.setDateCreated(LocalDateTime.now());
        entity.setPayload(payload);
        billingDataFeedLogRepository.save(entity);
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
