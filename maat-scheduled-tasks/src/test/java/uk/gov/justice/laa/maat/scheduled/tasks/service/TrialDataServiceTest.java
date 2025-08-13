package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.factory.PrototypeBeanFactory;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitTrialDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

@ExtendWith(MockitoExtension.class)
class TrialDataServiceTest {

    @Mock
    private GetRecordSheetsResponse getRecordSheetsResponse;

    @Mock
    private XhibitDataService xhibitDataService;

    @Mock
    private XhibitTrialDataRepository trialDataRepository;

    @Mock
    private PrototypeBeanFactory prototypeBeanFactory;

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

    @BeforeEach
    void setUp() {
        when(prototypeBeanFactory.getXhibitDataService()).thenReturn(xhibitDataService);
    }

    @Test
    void givenNoUnprocessedRecordSheets_whenPopulateTrialDataIsInvoked_thenNoDataIsPopulated() {
        when(xhibitDataService.getRecordSheets(RecordSheetType.TRIAL)).thenReturn(new GetRecordSheetsResponse());
        when(xhibitDataService.allRecordSheetsRetrieved()).thenReturn(true);

        trialDataService.populateTrialData();

        verify(trialDataRepository, never()).saveAll(any());
        verify(xhibitDataService, never()).markRecordsSheetsAsProcessed(any(), any());
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenUnprocessRecordSheetsThatAreSuccessfullyRetrieved_whenPopulateTrialDataIsInvoked_thenDataIsPopulated() {
        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(List.of(xhibitRecordSheet1, xhibitRecordSheet2));
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(Collections.emptyList());

        when(xhibitDataService.getRecordSheets(RecordSheetType.TRIAL)).thenReturn(getRecordSheetsResponse);
        when(xhibitDataService.allRecordSheetsRetrieved()).thenReturn(true);

        trialDataService.populateTrialData();

        verify(trialDataRepository, times(1)).saveAll(any());
        verify(xhibitDataService, times(1)).markRecordsSheetsAsProcessed(any(), any());
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenUnprocessedRecordSheetsThatCannotBeRetrieved_whenPopulateTrialDataIsInvoked_thenNoDataIsPopulated() {
        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(Collections.emptyList());
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(List.of(xhibitRecordSheet1, xhibitRecordSheet2));

        when(xhibitDataService.getRecordSheets(RecordSheetType.TRIAL)).thenReturn(getRecordSheetsResponse);
        when(xhibitDataService.allRecordSheetsRetrieved()).thenReturn(true);

        trialDataService.populateTrialData();

        verify(trialDataRepository, never()).saveAll(any());
        verify(xhibitDataService, never()).markRecordsSheetsAsProcessed(any(), any());
        verify(xhibitDataService, times(1)).markRecordSheetsAsErrored(any(), any());
    }
}
