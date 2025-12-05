package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository.FinalDefenceCostsRepository;

@Service
@AllArgsConstructor
public class FinalDefenceCostEntitySaver {

  private final FinalDefenceCostsRepository finalDefenceCostsRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void saveEntity(FinalDefenceCostEntity entity) {
    finalDefenceCostsRepository.save(entity);
  }
}
