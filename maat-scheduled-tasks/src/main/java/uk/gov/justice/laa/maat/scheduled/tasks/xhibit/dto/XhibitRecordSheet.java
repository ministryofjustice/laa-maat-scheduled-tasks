package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XhibitRecordSheet {

    private String data;
    private String filename;
}
