package uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity;

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
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fdc_ready", schema = "HUB")
public class FDCReadyEntity {

    @Id
    @SequenceGenerator(name = "fdc_ready_gen_seq", sequenceName = "FDC_READY_SEQUENCE", allocationSize = 1, schema = "HUB")
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
