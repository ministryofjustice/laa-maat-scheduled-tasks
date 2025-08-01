package uk.gov.justice.laa.maat.scheduled.tasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepOrderBillingDTO {
    private Integer id;
    private Integer applicantId;
    private String arrestSummonsNo;
    private String evidenceFeeLevel;
    private String supplierAccountCode;
    private Integer magsCourtId;
    private String magsCourtOutcome;
    private LocalDate dateReceived;
    private LocalDate crownCourtRepOrderDate;
    private String offenceType;
    private LocalDate crownCourtWithdrawalDate;
    private Integer applicantHistoryId;
    private String caseId;
    private LocalDate committalDate;
    private String repOrderStatus;
    private String appealTypeCode;
    private String crownCourtOutcome;
    private LocalDate dateCreated;
    private String userCreated;
    private LocalDateTime dateModified;
    private String userModified;
    private String caseType;

}
