package uk.gov.justice.laa.maat.scheduled.tasks.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitAppealDataEntity;

public interface XhibitAppealDataRepository extends JpaRepository<XhibitAppealDataEntity, Integer> {

    @Query("SELECT xad.id FROM XhibitAppealDataEntity xad WHERE xad.status = 'UNPROCESSED'")
    List<Integer> findAllUnprocessedIds();
    
}
