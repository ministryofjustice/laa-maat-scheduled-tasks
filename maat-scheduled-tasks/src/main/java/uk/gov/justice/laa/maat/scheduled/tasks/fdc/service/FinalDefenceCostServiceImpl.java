package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FdcReadyRequestDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FDCReadyEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.validator.FdcItemValidator;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.config.FinalDefenceCostConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository.FinalDefenceCostsReadyRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository.FinalDefenceCostsRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.util.ListUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalDefenceCostServiceImpl implements FinalDefenceCostService {

  private final FinalDefenceCostsRepository finalDefenceCostsRepository;
  private final FinalDefenceCostsReadyRepository finalDefenceCostsReadyRepository;
  private final FinalDefenceCostConfiguration fdcConfiguration;
  private final FdcItemValidator fdcItemValidator;

  @Transactional
  public int processFinalDefenceCosts(List<FinalDefenceCostDTO> dtos) {
    log.info("Loading Final Defence Costs data into HUB");

    List<FinalDefenceCostDTO> payloadDtos = new ArrayList<>(dtos);
        List<FinalDefenceCostDTO> invalidDtos = payloadDtos.stream()
        .filter(dto -> !fdcItemValidator.validate(dto))
        .toList();

    log.warn("Invalid FDC records: {}: ", invalidDtos);

    payloadDtos.removeAll(invalidDtos);

    int batchSize = fdcConfiguration.getFetchSize();
    List<FinalDefenceCostEntity> fdcEntities = payloadDtos.stream()
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
      finalDefenceCostsRepository.flush();
    }

    log.info("{} FDC records processed successfully.", count);

    return count;
  }

  @Transactional
  public int saveFdcReadyItems(List<FdcReadyRequestDTO> requestDTOs) {
    log.info("Saving {} FDC Ready items", requestDTOs.size());

    List<FdcReadyRequestDTO> validRequestDTOs = new ArrayList<>(requestDTOs);
    List<FdcReadyRequestDTO> invalidRequestDTOs = requestDTOs.stream()
            .filter(dto -> !fdcItemValidator.validate(dto))
            .toList();

    log.warn("Invalid Fdc Ready records: {}: ", invalidRequestDTOs);

    validRequestDTOs.removeAll(invalidRequestDTOs);

    int batchSize = fdcConfiguration.getFetchSize();

    List<FDCReadyEntity> validEntities = validRequestDTOs.stream()
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

    int savedCount = 0;
    List<List<FDCReadyEntity>> batchesEntities = ListUtils.batchList(validEntities, batchSize);
    for (List<FDCReadyEntity> batch : batchesEntities) {
      List<FDCReadyEntity> saved = finalDefenceCostsReadyRepository.saveAll(batch);
      finalDefenceCostsReadyRepository.flush();
      savedCount += saved.size();
    }


    log.info("Successfully saved {} FDC Ready items", savedCount);
    return savedCount;
  }

  private FDCType parseFdcType(String itemType) {
    if (itemType == null) return null;
    try {
      return FDCType.valueOf(itemType.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
