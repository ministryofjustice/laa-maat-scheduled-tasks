package uk.gov.justice.laa.maat.scheduled.tasks.entity;

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
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "XHIBIT_TRIAL_DATA", schema = "HUB")
public class XhibitTrialDataEntity {

    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "xhibit_trial_data_gen_seq", sequenceName = "XHIBIT_TRIAL_DATA_SEQ", allocationSize = 1, schema = "HUB")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xhibit_trial_data_gen_seq")
    private Integer id;

    @Column(name = "FILENAME")
    private String filename;

    @Column(name = "XML_CLOB")
    @Lob
    private String data;

    public static XhibitTrialDataEntity fromDto(XhibitRecordSheetDTO dto) {
        return builder()
            .filename(dto.getFilename())
            .data(dto.getData())
            .build();
    }
}
