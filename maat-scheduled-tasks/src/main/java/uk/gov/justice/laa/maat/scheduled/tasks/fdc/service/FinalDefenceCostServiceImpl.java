package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.config.FinalDefenceCostConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostsEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.exception.FinalDefenceCostServiceException;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository.FinalDefenceCostsRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.ObjectsValidator;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalDefenceCostServiceImpl implements  FinalDefenceCostService {

  private final FinalDefenceCostsRepository finalDefenceCostsRepository;

  private final FinalDefenceCostConfiguration finalDefenceCostConfiguration;

  private final ObjectsValidator<FinalDefenceCostsEntity> postValidator;

  @Transactional
  public int processFinalDefenceCosts(List<FinalDefenceCostDto> dtos) {
    log.info("Loading FDC Final Defence Costs");

    int batchSize = Integer.parseInt(finalDefenceCostConfiguration.getBatchSize());

    List<FinalDefenceCostsEntity> finalDefenceCosts = mapDtosToEntrities(dtos);

    int startIndex = 0;
    int endIndex = batchSize;

    List<FinalDefenceCostsEntity> listToSave = finalDefenceCosts.subList(startIndex, endIndex);

    while (!listToSave.isEmpty()) {

      finalDefenceCostsRepository.saveAll(listToSave);

      startIndex = endIndex;
      endIndex = startIndex + batchSize;
      if (endIndex > finalDefenceCosts.size()) {
        endIndex = finalDefenceCosts.size();
      }
      listToSave = finalDefenceCosts.subList(startIndex, endIndex);
    }

    int count = finalDefenceCosts.size();
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
