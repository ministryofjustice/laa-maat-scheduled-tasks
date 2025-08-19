package uk.gov.justice.laa.maat.scheduled.tasks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitTrialDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
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
    void givenNoUnprocessedRecordSheets_whenPopulateTrialDataIsInvoked_thenNoDataIsPopulated() {
        when(getRecordSheetsResponse.allRecordSheetsRetrieved()).thenReturn(true);
        when(xhibitDataService.getRecordSheets(RecordSheetType.TRIAL, null)).thenReturn(getRecordSheetsResponse);

        trialDataService.populateTrialData();

        verify(trialDataRepository, never()).saveAll(any());
        verify(xhibitDataService, times(1)).getRecordSheets(RecordSheetType.TRIAL, null);
        verify(xhibitDataService, never()).markRecordsSheetsAsProcessed(any(), any());
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenUnprocessRecordSheetsThatAreSuccessfullyRetrieved_whenPopulateTrialDataIsInvoked_thenDataIsPopulated() {
        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(List.of(xhibitRecordSheet1, xhibitRecordSheet2));
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(Collections.emptyList());
        when(getRecordSheetsResponse.allRecordSheetsRetrieved()).thenReturn(true);

        when(xhibitDataService.getRecordSheets(RecordSheetType.TRIAL, null)).thenReturn(getRecordSheetsResponse);

        trialDataService.populateTrialData();

        verify(trialDataRepository, times(1)).saveAll(any());
        verify(xhibitDataService, times(1)).getRecordSheets(RecordSheetType.TRIAL, null);
        verify(xhibitDataService, times(1)).markRecordsSheetsAsProcessed(any(), any());
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenUnprocessedRecordSheetsThatCannotBeRetrieved_whenPopulateTrialDataIsInvoked_thenNoDataIsPopulated() {
        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(Collections.emptyList());
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(List.of(xhibitRecordSheet1, xhibitRecordSheet2));
        when(getRecordSheetsResponse.allRecordSheetsRetrieved()).thenReturn(true);

        when(xhibitDataService.getRecordSheets(RecordSheetType.TRIAL, null)).thenReturn(getRecordSheetsResponse);

        trialDataService.populateTrialData();

        verify(trialDataRepository, never()).saveAll(any());
        verify(xhibitDataService, times(1)).getRecordSheets(RecordSheetType.TRIAL, null);
        verify(xhibitDataService, never()).markRecordsSheetsAsProcessed(any(), any());
        verify(xhibitDataService, times(1)).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenMultiplePagesOfUnprocessedRecordSheets_whenPopulateTrialDataIsInvoked_thenDataIsPopulated() {
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

        trialDataService.populateTrialData();

        verify(trialDataRepository, times(2)).saveAll(any());
        verify(xhibitDataService, times(1)).getRecordSheets(RecordSheetType.TRIAL, null);
        verify(xhibitDataService, times(1)).getRecordSheets(RecordSheetType.TRIAL, "test-continuation-token");
        verify(xhibitDataService, times(2)).markRecordsSheetsAsProcessed(any(), any());
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void processTrialDataInToMaat() {
        when(trialDataRepository.findAllUnprocessedIds()).thenReturn(List.of(1, 2, 3));

        trialDataService.processTrialDataInToMaat();

        verify(trialDataRepository, times(1)).findAllUnprocessedIds();
        verify(storedProcedureService, times(3)).callStoredProcedure(eq(TRIAL_DATA_TO_MAAT_PROCEDURE), anyMap());
    }
}
