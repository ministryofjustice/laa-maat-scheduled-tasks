package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostReadyEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository.FinalDefenceCostsReadyRepository;

@Service
@AllArgsConstructor
public class FDCReadyEntitySaver {

  private final FinalDefenceCostsReadyRepository finalDefenceCostsReadyRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public FinalDefenceCostReadyEntity saveEntity(FinalDefenceCostReadyEntity entity) {
    return finalDefenceCostsReadyRepository.save(entity);
  }
}
