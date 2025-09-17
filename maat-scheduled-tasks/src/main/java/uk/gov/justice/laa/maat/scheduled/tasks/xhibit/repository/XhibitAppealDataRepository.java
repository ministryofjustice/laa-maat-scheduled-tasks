package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity.XhibitAppealDataEntity;

public interface XhibitAppealDataRepository extends JpaRepository<XhibitAppealDataEntity, Integer> {
}
