package uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@Entity
@Table(name = "final_defence_costs", schema = "HUB")
public class FinalDefenceCostsEntity {
    @Id
    @SequenceGenerator(name = "fdc_gen_seq", sequenceName = "S_GENERAL_SEQUENCE", allocationSize = 1, schema = "TOGDATA")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fdc_gen_seq")
    @Column(name = "HDAT_ID")
    private Integer hdatId;

    @Column(name = "MAAT_REFERENCE")
    @NotNull(message = "MAAT reference MUST not be null.")
    private Integer maatReference;

    @Column(name = "CASE_NO", length = 40)
    @NotEmpty(message = "Case number MUST not be empty.")
    @NotNull(message = "Case number MUST not be null.")
    private String caseNo;

    @Column(name = "SUPP_ACCOUNT_CODE", length = 10)
    @NotEmpty(message = "Supplier account code MUST be supplied.")
    @NotNull(message = "Supplier account code MUST not be null.")
    private String suppAccountCode;

    @Column(name = "COUR_COURT_CODE", length = 10)
    @NotEmpty(message = "Court code MUST be supplied.")
    @NotNull(message = "Court code MUST not be null.")
    private String courtCode;

    @Column(name = "JUDICIAL_APPORTIONMENT")
    @NotNull(message = "Judicial apportionment MUST not be null.")
    private int judicialApportionment;

    @Column(name = "TOTAL_CASE_COSTS")
    @NotNull(message = "Final defence cost MUST not be null.")
    private BigDecimal finalDefenceCost;

    @Column(name = "ITEM_TYPE", length = 4)
    @NotEmpty(message = "Item type MUST be supplied.")
    @NotNull(message = "Item type MUST not be null.")
    @Pattern(regexp = "^(LGSF|AGFS)$", message = "Item type MUST either be 'LGFS' or 'AGFS'")
    private String itemType;

    @Column(name = "PAID_AS_CLAIMED", length = 1)
    @NotEmpty(message = "Paid as claim indication MUST be supplied.")
    @NotNull(message = "Paid as claim indication MUST be supplied.")
    @Pattern(regexp = "^(Y|N)$", message = "Item type MUST either be 'Y' or 'N'")
    private String paidAsClaimed;
}
