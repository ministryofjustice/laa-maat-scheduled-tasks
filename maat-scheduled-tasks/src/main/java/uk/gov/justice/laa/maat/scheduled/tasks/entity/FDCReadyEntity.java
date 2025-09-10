package uk.gov.justice.laa.maat.scheduled.tasks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "fdc_ready", schema = "HUB")
public class FDCReadyEntity {
    @Id
    @SequenceGenerator(name = "fdc_ready_gen_seq", sequenceName = "S_GENERAL_SEQUENCE", allocationSize = 1, schema = "TOGDATA")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fdc_ready_gen_seq")
    @Column(name = "HDAT_ID")
    private Integer hdatId;

    @Column(name = "ID", nullable = false)
    private Integer maatId;

    @Column(name = "FDC_READY", length = 1)
    private String fdcReady; // Y/N

    @Enumerated(EnumType.STRING)
    @Column(name = "ITEM_TYPE", length = 4)
    private FDCType itemType; // LGFS or AGFS
}
