package uk.gov.justice.laa.maat.scheduled.tasks.fdc.util;

import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostReadyDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostReadyEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostEntity;

public class FinalDefenceCostsHelper {

  public static FinalDefenceCostEntity toFinalDefenceCostEntity(FinalDefenceCostDTO dto) {

    FinalDefenceCostEntity entity = new FinalDefenceCostEntity();
    entity.setMaatReference(dto.getMaatReference());
    entity.setCaseNo(dto.getCaseNo());
    entity.setSuppAccountCode(dto.getSuppAccountCode());
    entity.setCourtCode(dto.getCourtCode());
    entity.setJudicialApportionment(dto.getJudicialApportionment());
    entity.setFinalDefenceCost(dto.getFinalDefenceCost());
    entity.setItemType(dto.getItemType());
    entity.setPaidAsClaimed(dto.getPaidAsClaimed());

    return entity;
  }

  public static FinalDefenceCostReadyEntity toFDCReadyEntity(FinalDefenceCostReadyDTO dto) {

    FinalDefenceCostReadyEntity entity = new FinalDefenceCostReadyEntity();
    entity.setMaatId(dto.getMaatReference());
    entity.setFdcReady(dto.getFdcReady());
    entity.setItemType(dto.getItemType());
    return entity;
  }

}
