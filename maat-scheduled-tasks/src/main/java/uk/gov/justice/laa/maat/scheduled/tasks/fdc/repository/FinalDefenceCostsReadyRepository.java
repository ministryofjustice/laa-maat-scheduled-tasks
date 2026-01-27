package uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostReadyEntity;

@Repository
public interface FinalDefenceCostsReadyRepository extends JpaRepository<FinalDefenceCostReadyEntity, Integer> {

}
