package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.RecordSheet;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "XHIBIT_APPEAL_DATA", schema = "HUB")
public class XhibitAppealDataEntity implements XhibitEntity{
    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "xhibit_appeal_data_gen_seq", sequenceName = "XHIBIT_APPEAL_DATA_SEQ", allocationSize = 1, schema = "HUB")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xhibit_appeal_data_gen_seq")
    private Integer id;

    @Column(name = "FILENAME")
    private String filename;

    @Lob
    @Column(name = "XML_CLOB")
    private String data;

    public static XhibitAppealDataEntity fromDto(RecordSheet dto) {
        return builder()
                .data(dto.data())
                .filename(dto.filename())
                .build();
    }
}
