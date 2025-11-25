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
  public int processFinalDefenceCosts(String payload) {
    log.info("Loading FDC Final Defence Costs");

    int batchSize = Integer.parseInt(finalDefenceCostConfiguration.getBatchSize());

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    objectMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
    List<FinalDefenceCostsEntity> finalDefenceCosts = null;

    try {
      finalDefenceCosts = objectMapper.readValue(payload, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new FinalDefenceCostServiceException(String.format("FDC data load failed with exception: %s", e.getMessage()));
    }

    List<FinalDefenceCostsEntity> listToSave =  new ArrayList<>();
    List<FinalDefenceCostsEntity> invalidFdcEntities =  new ArrayList<>();
    int valid = 0;
    int inValid = 0;

    for (FinalDefenceCostsEntity entity : finalDefenceCosts) {
      if (isValidEntity(entity)) {
        listToSave.add(entity);
        valid++;
      } else  {
        invalidFdcEntities.add(entity);
        inValid++;
      }

      if (listToSave.size() == batchSize) {
        finalDefenceCostsRepository.saveAllAndFlush(listToSave);
        listToSave = new ArrayList<>();
      }
    }

    if (!listToSave.isEmpty()) {
      finalDefenceCostsRepository.saveAllAndFlush(listToSave);
    }

    log.info("{} FDC records processed successfully.", valid);
    log.info("The following {} FDC records failed validation: {}", inValid, invalidFdcEntities);

    return finalDefenceCosts.size();
  }

  private boolean isValidEntity(FinalDefenceCostsEntity entity) {
    var violations = postValidator.validate(entity);

    if (!violations.isEmpty()) {

      String errors = String.join("\n", violations);
      log.error(errors);
      return false;
    }

    return true;
  }

  private static Integer getInt(String[] cols, Map<String, Integer> idx, String key) {
        String v = getStr(cols, idx, key);
        return (v == null) ? null : Integer.valueOf(v);
    }

    private static String getStr(String[] cols, Map<String, Integer> idx, String key) {
        Integer i = idx.get(key.toUpperCase());
        if (i == null || i >= cols.length) return null;
        String v = cols[i].trim();
        return v.isEmpty() ? null : v;
    }


    private static BigDecimal getDecimal(String[] cols, Map<String, Integer> idx) {
        String v = getStr(cols, idx, "final_defence_cost");
        return (v == null) ? null : new BigDecimal(v);
    }
}
