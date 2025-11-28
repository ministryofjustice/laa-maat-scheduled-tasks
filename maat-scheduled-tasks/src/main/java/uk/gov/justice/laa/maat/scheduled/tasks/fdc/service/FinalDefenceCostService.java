package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import java.util.List;

import uk.gov.justice.laa.maat.scheduled.tasks.dto.FdcReadyRequestDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;

public interface FinalDefenceCostService {

  int processFinalDefenceCosts(List<FinalDefenceCostDto> payload);

  int processFdcReadyItems(List<FdcReadyRequestDTO> requests);

}
