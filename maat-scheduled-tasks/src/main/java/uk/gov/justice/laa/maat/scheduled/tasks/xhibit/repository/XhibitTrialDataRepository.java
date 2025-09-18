package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity.XhibitTrialDataEntity;

public interface XhibitTrialDataRepository extends JpaRepository<XhibitTrialDataEntity, Integer> {
}
