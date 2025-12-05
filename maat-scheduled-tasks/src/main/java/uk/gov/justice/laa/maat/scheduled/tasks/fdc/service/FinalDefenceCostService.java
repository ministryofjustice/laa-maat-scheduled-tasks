package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import java.util.List;

import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FdcReadyRequestDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;

public interface FinalDefenceCostService {

  List<FinalDefenceCostDTO> processFinalDefenceCosts(List<FinalDefenceCostDTO> payload);

  List<FdcReadyRequestDTO> saveFdcReadyItems(List<FdcReadyRequestDTO> requests);

}
