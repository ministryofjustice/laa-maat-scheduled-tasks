package uk.gov.justice.laa.maat.scheduled.tasks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RepOrderBillingDTO extends BillingDTO {

    @JsonProperty("def_id")
    private Integer applicantId;

    @JsonProperty("arrest_summons_no")
    private String arrestSummonsNo;

    @JsonProperty("efel_fee_level")
    private String evidenceFeeLevel;

    @JsonProperty("suppl_account_code")
    private String supplierAccountCode;

    @JsonProperty("maco_court")
    private Integer magsCourtId;

    @JsonProperty("mcoo_outcome")
    private String magsCourtOutcome;

    @JsonProperty("date_received")
    private LocalDate dateReceived;

    @JsonProperty("cc_reporder_date")
    private LocalDate crownCourtRepOrderDate;

    @JsonProperty("ofty_offence_type")
    private String offenceType;

    @JsonProperty("cc_withdrawal_date")
    private LocalDate crownCourtWithdrawalDate;

    @JsonProperty("def_hist_id")
    private Integer applicantHistoryId;

    @JsonProperty("case_number")
    private String caseId;

    @JsonProperty("committal_date")
    private LocalDate committalDate;

    @JsonProperty("rors_status")
    private String repOrderStatus;

    @JsonProperty("apty_code")
    private String appealTypeCode;

    @JsonProperty("ccoo_outcome")
    private String crownCourtOutcome;

    @JsonProperty("caty_case_type")
    private String caseType;
}
