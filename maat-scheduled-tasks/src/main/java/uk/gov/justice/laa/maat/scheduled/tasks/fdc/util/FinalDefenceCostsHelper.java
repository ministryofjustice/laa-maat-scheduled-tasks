package uk.gov.justice.laa.maat.scheduled.tasks.fdc.util;

import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostEntity;

public class FinalDefenceCostsHelper {

  public static FinalDefenceCostEntity toFinalDefenceCostEntity(FinalDefenceCostDTO dto) {
    return FinalDefenceCostEntity.builder()
        .maatReference(dto.getMaatReference())
        .caseNo(dto.getCaseNo())
        .suppAccountCode(dto.getSuppAccountCode())
        .courtCode(dto.getCourtCode())
        .judicialApportionment(dto.getJudicialApportionment())
        .finalDefenceCost(dto.getFinalDefenceCost())
        .itemType(dto.getItemType())
        .paidAsClaimed(dto.getPaidAsClaimed())
        .build();
  }
}
