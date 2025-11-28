package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.FdcReadyRequestDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.FDCReadyEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.config.FinalDefenceCostConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository.FinalDefenceCostsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalDefenceCostServiceImpl implements FinalDefenceCostService {

  private final FinalDefenceCostsRepository finalDefenceCostsRepository;
  private final EntityManager entityManager;
  private final FinalDefenceCostConfiguration fdcConfiguration;

  @Transactional
  public int processFinalDefenceCosts(List<FinalDefenceCostDto> dtos, int batchSize) {
    log.info("Loading Final Defence Costs data into HUB");

    List<FinalDefenceCostEntity> finalDefenceCosts = mapDtosToEntrities(dtos);

    int count = 0;
    int startIndex = 0;
    int endIndex = batchSize;

    if (endIndex > finalDefenceCosts.size()) {
      endIndex = finalDefenceCosts.size();
    }

    List<FinalDefenceCostEntity> listToSave = finalDefenceCosts.subList(startIndex, endIndex);

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

  @Transactional
  public int processFdcReadyItems(List<FdcReadyRequestDTO> requests) {
    log.info("Saving {} FDC Ready items", requests.size());
    int count = 0;
    int inBatch = 0;
    int batchSize = fdcConfiguration.getFetchSize();
    for (FdcReadyRequestDTO request : requests) {
      FDCReadyEntity entity = new FDCReadyEntity();
      entity.setMaatId(request.getMaatReference());
      entity.setFdcReady(request.getFdcReady());

      FDCType itemType = parseFdcType(request.getItemType());
      if (itemType == null) {
        log.warn("Invalid item_type: {} for maat_reference: {}",
                request.getItemType(), request.getMaatReference());
        continue;
      }
      entity.setItemType(itemType);

      entityManager.persist(entity);
      inBatch++;
      count++;

      if (inBatch >= batchSize) {
        entityManager.flush();
        entityManager.clear();
        inBatch = 0;
      }
    }

    if (inBatch > 0) {
      entityManager.flush();
      entityManager.clear();
    }

    log.info("Successfully saved {} FDC Ready items", count);
    return count;
  }

  private FDCType parseFdcType(String itemType) {
    if (itemType == null) return null;
    try {
      return FDCType.valueOf(itemType.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private List<FinalDefenceCostEntity> mapDtosToEntrities(List<FinalDefenceCostDto> dtos) {

    List<FinalDefenceCostEntity>  entities = new ArrayList<>();

    for (FinalDefenceCostDto dto : dtos) {
      FinalDefenceCostEntity entity = FinalDefenceCostEntity.builder()
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
