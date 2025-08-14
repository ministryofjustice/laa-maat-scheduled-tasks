package uk.gov.justice.laa.maat.scheduled.tasks.responses;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetRecordSheetsResponse {

    private List<XhibitRecordSheetDTO> retrievedRecordSheets = new ArrayList<>();
    private List<XhibitRecordSheetDTO> erroredRecordSheets = new ArrayList<>();

    private String continuationToken;

    @Getter
    @Accessors(fluent = true)
    private boolean allRecordSheetsRetrieved;
}
