package uk.gov.justice.laa.maat.scheduled.tasks.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;

@Slf4j
public class RepOrderBillingMapper {
    private RepOrderBillingMapper() { }

    public static RepOrderBillingDTO mapEntityToDTO(RepOrderBillingEntity repOrderEntity) {
        Integer magsCourtId = !StringUtils.hasLength(repOrderEntity.getMagsCourtId())
            ? null : Integer.parseInt(repOrderEntity.getMagsCourtId());

        return RepOrderBillingDTO.builder()
            .id(repOrderEntity.getId())
            .applicantId(repOrderEntity.getApplicantId())
            .arrestSummonsNo(repOrderEntity.getArrestSummonsNo())
            .evidenceFeeLevel(repOrderEntity.getEvidenceFeeLevel())
            .supplierAccountCode(repOrderEntity.getSupplierAccountCode())
            .magsCourtId(magsCourtId)
            .magsCourtOutcome(repOrderEntity.getMagsCourtOutcome())
            .dateReceived(repOrderEntity.getDateReceived())
            .crownCourtRepOrderDate(repOrderEntity.getCrownCourtRepOrderDate())
            .offenceType(repOrderEntity.getOffenceType())
            .crownCourtWithdrawalDate(repOrderEntity.getCrownCourtWithdrawalDate())
            .applicantHistoryId(repOrderEntity.getApplicantHistoryId())
            .caseId(repOrderEntity.getCaseId())
            .committalDate(repOrderEntity.getCommittalDate())
            .repOrderStatus(repOrderEntity.getRepOrderStatus())
            .appealTypeCode(repOrderEntity.getAppealTypeCode())
            .crownCourtOutcome(repOrderEntity.getCrownCourtOutcome())
            .dateCreated(repOrderEntity.getDateCreated())
            .userCreated(repOrderEntity.getUserCreated())
            .dateModified(repOrderEntity.getDateModified())
            .userModified(repOrderEntity.getUserModified())
            .caseType(repOrderEntity.getCaseType())
            .build();
    }
}
