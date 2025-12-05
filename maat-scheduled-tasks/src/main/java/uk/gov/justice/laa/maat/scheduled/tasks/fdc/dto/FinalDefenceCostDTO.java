package uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.YesNoFlag;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalDefenceCostDTO {

  @NotNull(message = "maat_reference is mandatory.")
  @Min(value = 1, message = "maat_reference cannot be less than 1.")
  @JsonProperty("maat_reference")
  private int maatReference;

  @NotEmpty(message = "case_no is mandatory.")
  @NotNull(message = "case_no is mandatory.")
  @JsonProperty("case_no")
  private String caseNo;

  @NotEmpty(message = "supp_account_code is mandatory.")
  @NotNull(message = "supp_account_code is mandatory.")
  @JsonProperty("supp_account_code")
  private String suppAccountCode;

  @NotEmpty(message = "court_code is mandatory")
  @NotNull(message = "court_code is mandatory.")
  @JsonProperty("court_code")
  private String courtCode;

  @NotNull(message = "judicial_apportionment is mandatory.")
  @Min(value = 1, message = "Judicial apportionment cannot be less than 1.")
  @JsonProperty("judicial_apportionment")
  private int judicialApportionment;

  @NotNull(message = "final_defence_cost is mandatory.")
  @Min(value = 1, message = "Final defence cost cannot be less than 1.")
  @JsonProperty("final_defence_cost")
  private BigDecimal finalDefenceCost;

  @NotNull(message = "item_type is mandatory.")
  @JsonProperty("item_type")
  private FDCType itemType;

  @NotNull(message = "paid_as_claimed is mandatory.")
  @JsonProperty("paid_as_claimed")
  private YesNoFlag paidAsClaimed;
}
