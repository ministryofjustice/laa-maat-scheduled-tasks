package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.RecordSheet;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.RecordSheetsPage;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.ProcedureResult;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.repository.XhibitTrialDataRepository;

@ExtendWith(MockitoExtension.class)
class TrialDataServiceTest {

    @Mock
    private XhibitS3Service xhibitS3Service;

    @Mock
    private XhibitTrialDataRepository trialDataRepository;

    @Mock
    private TrialDataProcedureService trialDataProcedureService;

    @Mock
    private XhibitItemService xhibitItemService;

    @InjectMocks
    private TrialDataService trialDataService;

    @Test
    void populateAndProcessData_delegatesToCollaborators() {
        RecordSheet mockRecordSheet = RecordSheet.builder()
                .filename("file1.txt")
                .data("data1")
                .build();

        RecordSheetsPage response = RecordSheetsPage.complete(List.of(mockRecordSheet),
                Collections.emptyList());

        when(xhibitS3Service.getRecordSheets(RecordSheetType.TRIAL))
                .thenReturn(response);

        XhibitTrialDataEntity entity = XhibitTrialDataEntity.fromDto(mockRecordSheet);
        when(xhibitItemService.process(entity, trialDataRepository, trialDataProcedureService)).thenReturn(true);

        trialDataService.populateAndProcessData();

        verify(xhibitS3Service).getRecordSheets(RecordSheetType.TRIAL);
        verify(xhibitItemService).process(entity, trialDataRepository, trialDataProcedureService);
        verify(xhibitS3Service).markProcessed(List.of("file1.txt"), RecordSheetType.TRIAL);

        verify(xhibitS3Service, never()).markErrored(anyList(), any());
    }
}
