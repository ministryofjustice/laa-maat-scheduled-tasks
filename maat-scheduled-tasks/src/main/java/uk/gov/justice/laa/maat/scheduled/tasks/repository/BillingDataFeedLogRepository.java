package uk.gov.justice.laa.maat.scheduled.tasks.repository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;

@Repository
public interface BillingDataFeedLogRepository extends JpaRepository<BillingDataFeedLogEntity, Integer> {
    Long deleteByDateCreatedBefore(LocalDateTime date);
}
