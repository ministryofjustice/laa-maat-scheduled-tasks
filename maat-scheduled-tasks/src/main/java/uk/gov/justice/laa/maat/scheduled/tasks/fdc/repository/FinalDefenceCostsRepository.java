package uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostsEntity;

@Repository
public interface FinalDefenceCostsRepository extends JpaRepository<FinalDefenceCostsEntity, Integer> {

}
