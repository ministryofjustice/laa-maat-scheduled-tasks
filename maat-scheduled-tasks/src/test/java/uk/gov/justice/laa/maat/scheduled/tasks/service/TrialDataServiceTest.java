package uk.gov.justice.laa.maat.scheduled.tasks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitTrialDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.service.TrialDataService.TRIAL_DATA_TO_MAAT_PROCEDURE;

@ExtendWith(MockitoExtension.class)
class TrialDataServiceTest {

    @Mock
    private GetRecordSheetsResponse getRecordSheetsResponse;

    @Mock
    private XhibitDataService xhibitDataService;

    @Mock
    private XhibitTrialDataRepository trialDataRepository;

    @Mock
    private StoredProcedureService storedProcedureService;

    @InjectMocks
    private TrialDataService trialDataService;

    private final XhibitRecordSheetDTO xhibitRecordSheet1 = XhibitRecordSheetDTO.builder()
        .filename("file1.xml")
        .data("<NS1:TrialRecordSheet xmlns:NS1=\"http://www.courtservice.gov.uk/schemas/courtservice\"><NS1:DocumentID><NS1:DocumentName>TR Alice Bloggs</NS1:DocumentName></NS1:DocumentID></NS1:TrialRecordSheet>")
        .build();

    private final XhibitRecordSheetDTO xhibitRecordSheet2 = XhibitRecordSheetDTO.builder()
        .filename("file2.xml")
        .data("<NS1:TrialRecordSheet xmlns:NS1=\"http://www.courtservice.gov.uk/schemas/courtservice\"><NS1:DocumentID><NS1:DocumentName>TR Joe Bloggs</NS1:DocumentName></NS1:DocumentID></NS1:TrialRecordSheet>")
        .build();

    @Test
    void givenNoUnprocessedRecordSheets_whenPopulateAndProcessTrialDataIsInvoked_thenNoDataInToMaatIsPopulated() {
        when(getRecordSheetsResponse.allRecordSheetsRetrieved()).thenReturn(true);
        when(xhibitDataService.getRecordSheets(RecordSheetType.TRIAL, null)).thenReturn(getRecordSheetsResponse);

        trialDataService.populateAndProcessTrialDataInToMaat();

        verify(trialDataRepository, never()).saveAll(any());
        verify(xhibitDataService, times(1)).getRecordSheets(RecordSheetType.TRIAL, null);
        verify(xhibitDataService, never()).markRecordsSheetsAsProcessed(any(), any());
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
        verify(storedProcedureService, never()).callStoredProcedure(any(), any());
    }

    @Test
    void givenUnprocessRecordSheetsThatAreSuccessfullyRetrieved_whenPopulateAndProcessTrialDataIsInvoked_thenDataInToMaatIsPopulated() {
        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(List.of(xhibitRecordSheet1, xhibitRecordSheet2));
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(Collections.emptyList());
        when(getRecordSheetsResponse.allRecordSheetsRetrieved()).thenReturn(true);

        when(xhibitDataService.getRecordSheets(RecordSheetType.TRIAL, null)).thenReturn(getRecordSheetsResponse);
        when(trialDataRepository.findAll()).thenReturn(List.of(
            XhibitTrialDataEntity.builder().id(1).filename(xhibitRecordSheet1.getFilename()).data(xhibitRecordSheet1.getData()).build(),
            XhibitTrialDataEntity.builder().id(2).filename(xhibitRecordSheet2.getFilename()).data(xhibitRecordSheet2.getData()).build()
        ));

        trialDataService.populateAndProcessTrialDataInToMaat();

        verify(trialDataRepository, times(1)).saveAll(any());
        verify(xhibitDataService, times(1)).getRecordSheets(RecordSheetType.TRIAL, null);
        verify(storedProcedureService, times(1)).callStoredProcedure(TRIAL_DATA_TO_MAAT_PROCEDURE, Map.of("id", 1));
        verify(storedProcedureService, times(1)).callStoredProcedure(TRIAL_DATA_TO_MAAT_PROCEDURE, Map.of("id", 2));
        verify(xhibitDataService, times(1)).markRecordsSheetsAsProcessed(List.of(xhibitRecordSheet1.getFilename(), xhibitRecordSheet2.getFilename()), RecordSheetType.TRIAL);
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenUnprocessedRecordSheetsThatCannotBeRetrieved_whenPopulateAndProcessTrialDataIsInvoked_thenNoDataInToMaatIsPopulated() {
        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(Collections.emptyList());
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(List.of(xhibitRecordSheet1, xhibitRecordSheet2));
        when(getRecordSheetsResponse.allRecordSheetsRetrieved()).thenReturn(true);

        when(xhibitDataService.getRecordSheets(RecordSheetType.TRIAL, null)).thenReturn(getRecordSheetsResponse);

        trialDataService.populateAndProcessTrialDataInToMaat();

        verify(trialDataRepository, never()).saveAll(any());
        verify(xhibitDataService, times(1)).getRecordSheets(RecordSheetType.TRIAL, null);
        verify(xhibitDataService, never()).markRecordsSheetsAsProcessed(any(), any());
        verify(xhibitDataService, times(1)).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenMultiplePagesOfUnprocessedRecordSheets_whenPopulateAndProcessTrialDataIsInvoked_thenDataInToMaatIsPopulated() {
        GetRecordSheetsResponse firstResponse = GetRecordSheetsResponse.builder()
            .retrievedRecordSheets(List.of(xhibitRecordSheet1))
            .erroredRecordSheets(Collections.emptyList())
            .continuationToken("test-continuation-token")
            .allRecordSheetsRetrieved(false)
            .build();

        GetRecordSheetsResponse secondResponse = GetRecordSheetsResponse.builder()
            .retrievedRecordSheets(List.of(xhibitRecordSheet2))
            .erroredRecordSheets(Collections.emptyList())
            .continuationToken(null)
            .allRecordSheetsRetrieved(true)
            .build();

        when(xhibitDataService.getRecordSheets(RecordSheetType.TRIAL, null)).thenReturn(firstResponse);
        when(xhibitDataService.getRecordSheets(RecordSheetType.TRIAL, "test-continuation-token")).thenReturn(secondResponse);
        when(trialDataRepository.findAll()).thenReturn(List.of(
            XhibitTrialDataEntity.builder().id(1).filename(xhibitRecordSheet1.getFilename()).data(xhibitRecordSheet1.getData()).build(),
            XhibitTrialDataEntity.builder().id(2).filename(xhibitRecordSheet2.getFilename()).data(xhibitRecordSheet2.getData()).build()
        ));

        trialDataService.populateAndProcessTrialDataInToMaat();

        verify(trialDataRepository, times(2)).saveAll(any());
        verify(xhibitDataService, times(1)).getRecordSheets(RecordSheetType.TRIAL, null);
        verify(xhibitDataService, times(1)).getRecordSheets(RecordSheetType.TRIAL, "test-continuation-token");
        verify(storedProcedureService, times(1)).callStoredProcedure(TRIAL_DATA_TO_MAAT_PROCEDURE, Map.of("id", 1));
        verify(storedProcedureService, times(1)).callStoredProcedure(TRIAL_DATA_TO_MAAT_PROCEDURE, Map.of("id", 2));
        verify(xhibitDataService, times(1)).markRecordsSheetsAsProcessed(List.of(xhibitRecordSheet1.getFilename(), xhibitRecordSheet2.getFilename()), RecordSheetType.TRIAL);
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }
}
