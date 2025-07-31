package uk.gov.justice.laa.maat.scheduled.tasks.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaatReferenceId implements Serializable {

    private Integer maatId;
    private Integer applicantId;
    private Integer applicantHistoryId;
}
