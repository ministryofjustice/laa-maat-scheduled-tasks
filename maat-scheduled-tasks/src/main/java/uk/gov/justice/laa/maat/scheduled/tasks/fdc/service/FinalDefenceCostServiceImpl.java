package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FdcReadyRequestDTO;
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

  public List<FinalDefenceCostDTO> processFinalDefenceCosts(List<FinalDefenceCostDTO> dtos) {
    log.info("Loading Final Defence Costs data into HUB");

    List<FinalDefenceCostDTO> payloadDtos = new ArrayList<>(dtos);
    List<FinalDefenceCostDTO> invalidDtos = new ArrayList<>(payloadDtos.stream()
        .filter(not(fdcItemValidator::validate))
        .toList());

    payloadDtos.removeAll(invalidDtos);

    Map<Boolean, List<FinalDefenceCostDTO>> result =
        payloadDtos.stream()
            .collect(Collectors.partitioningBy(dto -> {
              try {
                finalDefenceCostEntitySaver.saveEntity(
                    FinalDefenceCostsHelper.toFinalDefenceCostEntity(dto)
                );
                return true;   // saved
              } catch (Exception e) {
                log.warn("Failed to save Final Defence Cost Entity", e);
                return false;  // failed
              }
            }));

    int saved = result.get(true).size();
    invalidDtos.addAll(result.get(false));

    log.info("{} FDC records processed successfully.", saved);

    return invalidDtos;
  }

  public List<FdcReadyRequestDTO> saveFdcReadyItems(List<FdcReadyRequestDTO> requestDTOs) {
    log.info("Saving {} FDC Ready items", requestDTOs.size());

    List<FdcReadyRequestDTO> validRequestDTOs = new ArrayList<>(requestDTOs);
    List<FdcReadyRequestDTO> invalidRequestDTOs = new ArrayList<>(requestDTOs.stream()
        .filter(not(fdcItemValidator::validate))
        .toList());

    validRequestDTOs.removeAll(invalidRequestDTOs);

    Map<Boolean, List<FdcReadyRequestDTO>> result =
        validRequestDTOs.stream()
            .collect(Collectors.partitioningBy(dto -> {

              try {
                fdcReadyEntitySaver.saveEntity(
                    FinalDefenceCostsHelper.toFDCReadyEntity(dto)
                );
                return true;   // saved
              } catch (Exception e) {
                log.warn("Failed to save FDC Ready Entity", e);
                return false;  // failed
              }
            }));

    int saved = result.get(true).size();
    invalidRequestDTOs.addAll(result.get(false));

    log.info("{} FDC Ready items successfully saved.", saved);
    return invalidRequestDTOs;
  }
}
