package uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class FinalDefenceCostDto {

  @NotNull(message = "MAAT reference is mandatory.")
  @Min(value = 1, message = "MAAT reference cannot be less than 1.")
  private Integer maatReference;

  @NotEmpty(message = "Case number is mandatory.")
  @NotNull(message = "Case number MUST not be null.")
  private String caseNo;

  @NotEmpty(message = "Supplier account code is mandatory.")
  @NotNull(message = "Supplier account code is mandatory.")
  private String suppAccountCode;

  @NotEmpty(message = "Court code is mandatory")
  @NotNull(message = "Court code is mandatory.")
  private String courtCode;

  @NotNull(message = "Judicial apportionment is mandatory.")
  @Min(value = 1, message = "Judicial apportionment cannot be less than 1.")
  private int judicialApportionment;

  @NotNull(message = "Final defence cost is mandatory.")
  @Min(value = 1, message = "Final defence cost cannot be less than 1.")
  private BigDecimal finalDefenceCost;

  @NotEmpty(message = "Item type is mandatory.")
  @NotNull(message = "Item type MUST not be null.")
  @Pattern(regexp = "^(LGFS|AGFS)$", message = "Item type must be 'LGFS' or 'AGFS'")
  private String itemType;

  @NotEmpty(message = "Paid as claim indication is mandatory.")
  @NotNull(message = "Paid as claim indication must be supplied.")
  @Pattern(regexp = "^[YN]$", message = "Item type must be 'Y' or 'N'")
  private String paidAsClaimed;
}
