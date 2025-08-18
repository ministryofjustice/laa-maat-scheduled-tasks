package uk.gov.justice.laa.maat.scheduled.tasks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitTrialDataEntity;

import java.util.List;

public interface XhibitTrialDataRepository extends JpaRepository<XhibitTrialDataEntity, Integer> {

    @Query("SELECT xtd.id FROM XhibitTrialDataEntity xtd WHERE xtd.status = 'UNPROCESSED'")
    List<Integer> findAllUnprocessedIds();

}
