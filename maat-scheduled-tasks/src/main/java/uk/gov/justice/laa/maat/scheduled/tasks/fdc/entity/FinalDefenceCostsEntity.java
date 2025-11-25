package uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
    private Integer maatReference;

    @Column(name = "CASE_NO", length = 40)
    private String caseNo;

    @Column(name = "SUPP_ACCOUNT_CODE", length = 10)
    private String suppAccountCode;

    @Column(name = "COUR_COURT_CODE", length = 10)
    private String courtCode;

    @Column(name = "JUDICIAL_APPORTIONMENT")
    private int judicialApportionment;

    @Column(name = "TOTAL_CASE_COSTS")
    private BigDecimal finalDefenceCost;

    @Column(name = "ITEM_TYPE", length = 4)
    private String itemType;

    @Column(name = "PAID_AS_CLAIMED", length = 1)
    private String paidAsClaimed;
}
