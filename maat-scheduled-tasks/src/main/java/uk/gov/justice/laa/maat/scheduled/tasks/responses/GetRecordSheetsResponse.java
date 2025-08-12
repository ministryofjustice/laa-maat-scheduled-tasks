package uk.gov.justice.laa.maat.scheduled.tasks.responses;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;

@Data
@NoArgsConstructor
public class GetRecordSheetsResponse {

    private final List<XhibitRecordSheetDTO> retrievedRecordSheets = new ArrayList<>();
    private final List<XhibitRecordSheetDTO> erroredRecordSheets = new ArrayList<>();
}
