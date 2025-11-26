package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostsEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository.FinalDefenceCostsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalDefenceCostServiceImpl implements FinalDefenceCostService {

  private final FinalDefenceCostsRepository finalDefenceCostsRepository;

  public int processFinalDefenceCosts(List<FinalDefenceCostDto> dtos, int batchSize) {
    log.info("Loading Final Defence Costs data into HUB");

    List<FinalDefenceCostsEntity> finalDefenceCosts = mapDtosToEntrities(dtos);

    int count = 0;
    int startIndex = 0;
    int endIndex = batchSize;

    if (endIndex > finalDefenceCosts.size()) {
      endIndex = finalDefenceCosts.size();
    }

    List<FinalDefenceCostsEntity> listToSave = finalDefenceCosts.subList(startIndex, endIndex);

    while (!listToSave.isEmpty()) {

      finalDefenceCostsRepository.saveAll(listToSave);
      count += listToSave.size();

      startIndex = endIndex;
      endIndex = startIndex + batchSize;
      if (endIndex > finalDefenceCosts.size()) {
        endIndex = finalDefenceCosts.size();
      }
      listToSave = finalDefenceCosts.subList(startIndex, endIndex);
    }

    log.info("{} FDC records processed successfully.", count);

    return count;
  }

  private List<FinalDefenceCostsEntity> mapDtosToEntrities(List<FinalDefenceCostDto> dtos) {

    List<FinalDefenceCostsEntity>  entities = new ArrayList<>();

    for (FinalDefenceCostDto dto : dtos) {
      FinalDefenceCostsEntity entity = FinalDefenceCostsEntity.builder()
          .maatReference(dto.getMaatReference())
          .caseNo(dto.getCaseNo())
          .suppAccountCode(dto.getSuppAccountCode())
          .courtCode(dto.getCourtCode())
          .judicialApportionment(dto.getJudicialApportionment())
          .finalDefenceCost(dto.getFinalDefenceCost())
          .itemType(dto.getItemType())
          .paidAsClaimed(dto.getPaidAsClaimed())
          .build();
      entities.add(entity);
    }

    return entities;
  }
}
