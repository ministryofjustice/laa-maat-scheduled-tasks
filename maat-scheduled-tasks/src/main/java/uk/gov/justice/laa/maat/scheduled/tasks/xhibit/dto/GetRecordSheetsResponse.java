package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetRecordSheetsResponse {

    @Builder.Default
    private List<XhibitRecordSheet> retrievedRecordSheets = new ArrayList<>();
    @Builder.Default
    private List<XhibitRecordSheet> erroredRecordSheets = new ArrayList<>();

    private String continuationToken;

    @Getter
    @Accessors(fluent = true)
    private boolean allRecordSheetsRetrieved;
}
