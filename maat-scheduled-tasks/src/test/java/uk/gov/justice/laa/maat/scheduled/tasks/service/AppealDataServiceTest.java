package uk.gov.justice.laa.maat.scheduled.tasks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitAppealDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitAppealDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.service.AppealDataService.APPEAL_DATA_TO_MAAT_PROCEDURE;
import static uk.gov.justice.laa.maat.scheduled.tasks.service.AppealDataService.OUTPUT_PARAMS;

@ExtendWith(MockitoExtension.class)
class AppealDataServiceTest {

    @Mock
    private GetRecordSheetsResponse getRecordSheetsResponse;

    @Mock
    private XhibitDataService xhibitDataService;

    @Mock
    private XhibitAppealDataRepository appealDataRepository;

    @Mock
    private StoredProcedureService storedProcedureService;

    @InjectMocks
    private AppealDataService appealDataService;

    private final XhibitRecordSheetDTO xhibitRecordSheet1 = XhibitRecordSheetDTO.builder()
        .filename("file1.xml")
        .data("<NS1:TrialRecordSheet xmlns:NS1=\"http://www.courtservice.gov.uk/schemas/courtservice\"><NS1:DocumentID><NS1:DocumentName>TR Alice Bloggs</NS1:DocumentName></NS1:DocumentID></NS1:TrialRecordSheet>")
        .build();

    private final XhibitRecordSheetDTO xhibitRecordSheet2 = XhibitRecordSheetDTO.builder()
        .filename("file2.xml")
        .data("<NS1:TrialRecordSheet xmlns:NS1=\"http://www.courtservice.gov.uk/schemas/courtservice\"><NS1:DocumentID><NS1:DocumentName>TR Joe Bloggs</NS1:DocumentName></NS1:DocumentID></NS1:TrialRecordSheet>")
        .build();

    @Test
    void givenNoUnprocessedRecordSheets_whenPopulateAndProcessAppealDataIsInvoked_thenNoDataInToMaatIsPopulated() {
        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(getRecordSheetsResponse);

        appealDataService.populateAndProcessAppealDataInToMaat();

        verify(appealDataRepository, never()).saveAll(any());
        verify(xhibitDataService, times(1)).getAllRecordSheets(RecordSheetType.APPEAL);
        verify(xhibitDataService, never()).markRecordSheetsAsProcessed(any(), any());
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
        verify(storedProcedureService, never()).callStoredProcedure(any(), anyMap(), anyMap());
    }

    @Test
    void givenUnprocessRecordSheetsThatAreSuccessfullyRetrieved_whenPopulateAndProcessAppealDataIsInvoked_thenDataInToMaatIsPopulated() {
        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(List.of(xhibitRecordSheet1, xhibitRecordSheet2));
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(Collections.emptyList());

        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(getRecordSheetsResponse);
        when(appealDataRepository.findAll()).thenReturn(List.of(
            XhibitAppealDataEntity.builder().id(1).filename(xhibitRecordSheet1.getFilename()).data(xhibitRecordSheet1.getData()).build(),
            XhibitAppealDataEntity.builder().id(2).filename(xhibitRecordSheet2.getFilename()).data(xhibitRecordSheet2.getData()).build()
        ));

        appealDataService.populateAndProcessAppealDataInToMaat();

        verify(appealDataRepository, times(1)).saveAll(any());
        verify(xhibitDataService, times(1)).getAllRecordSheets(RecordSheetType.APPEAL);
        verify(storedProcedureService, times(1)).callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, Map.of("id", 1), OUTPUT_PARAMS);
        verify(storedProcedureService, times(1)).callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, Map.of("id", 2), OUTPUT_PARAMS);
        verify(xhibitDataService, times(1)).markRecordSheetsAsProcessed(List.of(xhibitRecordSheet1, xhibitRecordSheet2), RecordSheetType.APPEAL);
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenUnprocessedRecordSheetsThatCannotBeRetrieved_whenPopulateAndProcessAppealDataIsInvoked_thenNoDataInToMaatIsPopulated() {
        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(Collections.emptyList());
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(List.of(xhibitRecordSheet1, xhibitRecordSheet2));

        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(getRecordSheetsResponse);

        appealDataService.populateAndProcessAppealDataInToMaat();

        verify(appealDataRepository, never()).saveAll(any());
        verify(xhibitDataService, times(1)).getAllRecordSheets(RecordSheetType.APPEAL);
        verify(xhibitDataService, never()).markRecordSheetsAsProcessed(any(), any());
        verify(xhibitDataService, times(1)).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenMultiplePagesOfUnprocessedRecordSheets_whenPopulateAndProcessAppealDataIsInvoked_thenDataInToMaatIsPopulated() {
        GetRecordSheetsResponse response = GetRecordSheetsResponse.builder()
            .retrievedRecordSheets(List.of(xhibitRecordSheet1, xhibitRecordSheet2))
            .erroredRecordSheets(Collections.emptyList())
            .continuationToken(null)
            .allRecordSheetsRetrieved(true)
            .build();

        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(response);
        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(response);
        when(appealDataRepository.findAll()).thenReturn(List.of(
            XhibitAppealDataEntity.builder().id(1).filename(xhibitRecordSheet1.getFilename()).data(xhibitRecordSheet1.getData()).build(),
            XhibitAppealDataEntity.builder().id(2).filename(xhibitRecordSheet2.getFilename()).data(xhibitRecordSheet2.getData()).build()
        ));

        appealDataService.populateAndProcessAppealDataInToMaat();

        verify(appealDataRepository, times(1)).saveAll(any());
        verify(xhibitDataService, times(1)).getAllRecordSheets(RecordSheetType.APPEAL);
        verify(storedProcedureService, times(1)).callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, Map.of("id", 1), OUTPUT_PARAMS);
        verify(storedProcedureService, times(1)).callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, Map.of("id", 2), OUTPUT_PARAMS);
        verify(xhibitDataService, times(1)).markRecordSheetsAsProcessed(List.of(xhibitRecordSheet1, xhibitRecordSheet2), RecordSheetType.APPEAL);
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }
}
