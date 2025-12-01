package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.FdcReadyRequestDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.FDCReadyEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.FdcItemValidator;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.config.FinalDefenceCostConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository.FinalDefenceCostsReadyRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository.FinalDefenceCostsRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalDefenceCostServiceImpl implements FinalDefenceCostService {

  private final FinalDefenceCostsRepository finalDefenceCostsRepository;
  private final FinalDefenceCostsReadyRepository finalDefenceCostsReadyRepository;
  private final FinalDefenceCostConfiguration fdcConfiguration;
  private final FdcItemValidator fdcItemValidator;

  @Transactional
  public int processFinalDefenceCosts(List<FinalDefenceCostDto> dtos) {
    log.info("Loading Final Defence Costs data into HUB");

    List<FinalDefenceCostDto> invalidDtos = dtos.stream()
        .filter(dto -> !fdcItemValidator.validate(dto))
        .toList();

    log.warn("Invalid FDC records: {}: ", invalidDtos);

    dtos.removeAll(invalidDtos);

    int batchSize = fdcConfiguration.getFetchSize();
    List<FinalDefenceCostEntity> fdcEntities = dtos.stream()
        .map(dto -> FinalDefenceCostEntity.builder()
        .maatReference(dto.getMaatReference())
        .caseNo(dto.getCaseNo())
        .suppAccountCode(dto.getSuppAccountCode())
        .courtCode(dto.getCourtCode())
        .judicialApportionment(dto.getJudicialApportionment())
        .finalDefenceCost(dto.getFinalDefenceCost())
        .itemType(dto.getItemType())
        .paidAsClaimed(dto.getPaidAsClaimed())
        .build()).toList();

    int count = fdcEntities.size();
    for (int i = 0; i < count; i += batchSize) {
      int end = Math.min(i + batchSize, count);
      finalDefenceCostsRepository.saveAll(fdcEntities.subList(i, end));
    }

    log.info("{} FDC records processed successfully.", count);

    return count;
  }

  @Transactional
  public int processFdcReadyItems(List<FdcReadyRequestDTO> requests) {
    log.info("Saving {} FDC Ready items", requests.size());
    int batchSize = fdcConfiguration.getFetchSize();

    List<FDCReadyEntity> validEntities = requests.stream()
            .map(request -> {
              FDCType itemType = parseFdcType(request.getItemType());
              if (itemType == null) {
                log.warn("Invalid item_type: {} for maat_reference: {}",
                        request.getItemType(), request.getMaatReference());
                return null;
              }
              FDCReadyEntity entity = new FDCReadyEntity();
              entity.setMaatId(request.getMaatReference());
              entity.setFdcReady(request.getFdcReady());
              entity.setItemType(itemType);
              return entity;
            })
            .filter(Objects::nonNull)
            .toList();

    int count = validEntities.size();
    for (int i = 0; i < count; i += batchSize) {
      int end = Math.min(i + batchSize, count);
      finalDefenceCostsReadyRepository.saveAll(validEntities.subList(i, end));
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

//  private List<FinalDefenceCostEntity> mapDtosToEntrities(List<FinalDefenceCostDto> dtos) {
//
//    List<FinalDefenceCostEntity>  entities = dtos.stream()
//        .map(dto -> {
//          FinalDefenceCostEntity entity = FinalDefenceCostEntity.builder()
//          .maatReference(dto.getMaatReference())
//          .caseNo(dto.getCaseNo())
//          .suppAccountCode(dto.getSuppAccountCode())
//          .courtCode(dto.getCourtCode())
//          .judicialApportionment(dto.getJudicialApportionment())
//          .finalDefenceCost(dto.getFinalDefenceCost())
//          .itemType(dto.getItemType())
//          .paidAsClaimed(dto.getPaidAsClaimed())
//          .build();
//      return entity;
//    }).toList();
//
//    return entities;
//  }
}
