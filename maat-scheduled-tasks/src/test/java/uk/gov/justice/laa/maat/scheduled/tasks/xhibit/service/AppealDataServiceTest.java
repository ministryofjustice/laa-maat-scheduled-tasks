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
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity.XhibitAppealDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.ProcedureResult;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.repository.XhibitAppealDataRepository;

@ExtendWith(MockitoExtension.class)
class AppealDataServiceTest {

    @Mock
    private XhibitS3Service xhibitS3Service;

    @Mock
    private XhibitAppealDataRepository appealDataRepository;

    @Mock
    private AppealDataProcedureService appealDataProcedureService;

    @InjectMocks
    private AppealDataService appealDataService;

    @Test
    void populateAndProcessTrialDataInToMaat_delegatesToCollaborators() {
        RecordSheet mockRecordSheet = RecordSheet.builder()
                .filename("file1.txt")
                .data("data1")
                .build();

        RecordSheetsPage response = RecordSheetsPage.complete(List.of(mockRecordSheet),
                Collections.emptyList());

        when(xhibitS3Service.getRecordSheets(RecordSheetType.APPEAL))
                .thenReturn(response);

        XhibitAppealDataEntity entity = XhibitAppealDataEntity.fromDto(mockRecordSheet);
        when(appealDataRepository.findAllById(anyList())).thenReturn(List.of(entity));
        when(appealDataProcedureService.call(entity)).thenReturn(ProcedureResult.SUCCESS);

        appealDataService.populateAndProcessData();

        verify(appealDataProcedureService).call(entity);
        verify(appealDataRepository).findAllById(anyList());
        verify(appealDataRepository).saveAllAndFlush(anyList());
        verify(xhibitS3Service).getRecordSheets(RecordSheetType.APPEAL);
        verify(xhibitS3Service).markProcessed(List.of("file1.txt"), RecordSheetType.APPEAL);

        verify(xhibitS3Service, never()).markErrored(anyList(), any());
    }
}
