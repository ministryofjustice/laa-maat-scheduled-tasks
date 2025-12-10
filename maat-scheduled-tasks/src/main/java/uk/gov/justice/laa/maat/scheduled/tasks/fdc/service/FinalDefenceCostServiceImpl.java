package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import static java.lang.String.format;
import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostReadyDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.FinalDefenceCostsHelper;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.validator.FdcItemValidator;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalDefenceCostServiceImpl implements FinalDefenceCostService {

  private final FinalDefenceCostEntitySaver finalDefenceCostEntitySaver;
  private final FDCReadyEntitySaver fdcReadyEntitySaver;
  private final FdcItemValidator fdcItemValidator;

  public List<FinalDefenceCostDTO> saveFDCItems(List<FinalDefenceCostDTO> dtos) {
    log.info("Loading Final Defence Costs data into HUB");

    List<FinalDefenceCostDTO> invalidDtos = new ArrayList<>();
    int saved = 0;

    for (FinalDefenceCostDTO dto : dtos) {

      if (!fdcItemValidator.validate(dto)) {
        log.error("FDC record is not valid (MAAT reference: {})", dto.getMaatReference());
        invalidDtos.add(dto);
        continue;
      }

      try {
        finalDefenceCostEntitySaver.saveEntity(
            FinalDefenceCostsHelper.toFinalDefenceCostEntity(dto)
        );
        saved++;
      } catch (Exception e) {
        log.error("Failed to save FDC record with MAAT Reference: {}", dto.getMaatReference(), e);
        invalidDtos.add(dto);
      }
    }

    log.info("{} FDC records processed successfully.", saved);

    return invalidDtos;
  }

  public List<FinalDefenceCostReadyDTO> saveFdcReadyItems(List<FinalDefenceCostReadyDTO> requestDTOs) {
    log.info("Saving {} FDC Ready items", requestDTOs.size());

    List<FinalDefenceCostReadyDTO> invalidRequestDTOs = new ArrayList<>();
    int saved = 0;

    for (FinalDefenceCostReadyDTO dto : requestDTOs) {

      if (!fdcItemValidator.validate(dto)) {
        log.error("FDC Ready record is not valid (MAAT reference: {})", dto.getMaatReference());
        invalidRequestDTOs.add(dto);
        continue;
      }

      try {
        fdcReadyEntitySaver.saveEntity(
            FinalDefenceCostsHelper.toFDCReadyEntity(dto)
        );
        saved++;
      } catch (Exception e) {
        log.error("Failed to save FDC Ready record with MAAT Reference: {}", dto.getMaatReference(), e);
        invalidRequestDTOs.add(dto);
      }
    }

    log.info("{} FDC Ready items successfully saved.", saved);

    return invalidRequestDTOs;
  }
}
