package uk.gov.justice.laa.maat.scheduled.tasks.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;

@Repository
public interface BillingDataFeedLogRepository extends JpaRepository<BillingDataFeedLogEntity, Integer> {
    List<BillingDataFeedLogEntity> getBillingDataFeedLogEntitiesByRecordType(String recordType);

    Long deleteByDateCreatedBefore(LocalDateTime date);
}
