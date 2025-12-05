package uk.gov.justice.laa.maat.scheduled.tasks.fdc.util;

import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FdcReadyRequestDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FDCReadyEntity;
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

  public static FDCReadyEntity toFDCReadyEntity(FdcReadyRequestDTO dto) {

    FDCReadyEntity entity = new FDCReadyEntity();
    entity.setMaatId(dto.getMaatReference());
    entity.setFdcReady(dto.getFdcReady());
    entity.setItemType(parseFdcType(dto.getItemType()));

    return entity;
  }

  public static FDCType parseFdcType(String itemType) {
    if (itemType == null) {
      return null;
    }

    try {
      return FDCType.valueOf(itemType.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
