package uk.gov.justice.laa.maat.scheduled.tasks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.MaatReferenceId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@IdClass(MaatReferenceId.class)
@Table(name = "MAAT_REFS_TO_EXTRACT", schema = "TOGDATA")
public class MaatReferenceEntity {

    @Id
    @Column(name = "MAAT_ID")
    private Integer maatId;

    @Id
    @Column(name = "APPL_ID")
    private Integer applicantId;

    @Id
    @Column(name = "APHI_ID")
    private Integer applicantHistoryId;
}
