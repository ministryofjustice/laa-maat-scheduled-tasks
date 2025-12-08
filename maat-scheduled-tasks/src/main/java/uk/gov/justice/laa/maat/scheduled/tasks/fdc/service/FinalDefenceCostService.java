package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import java.util.List;

import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostReadyDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;

public interface FinalDefenceCostService {

  List<FinalDefenceCostDTO> saveFDCItems(List<FinalDefenceCostDTO> payload);

  List<FinalDefenceCostReadyDTO> saveFdcReadyItems(List<FinalDefenceCostReadyDTO> requests);

}
