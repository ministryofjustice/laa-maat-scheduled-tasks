package uk.gov.justice.laa.maat.scheduled.tasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XhibitRecordSheetDTO {

    private String filename;
    private String data;
}
