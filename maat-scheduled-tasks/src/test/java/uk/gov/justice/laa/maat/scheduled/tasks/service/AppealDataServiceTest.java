package uk.gov.justice.laa.maat.scheduled.tasks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitAppealDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.StoredProcedureException;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitAppealDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.outputParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.safePopulate;
import static uk.gov.justice.laa.maat.scheduled.tasks.service.AppealDataService.APPEAL_DATA_TO_MAAT_PROCEDURE;
import static uk.gov.justice.laa.maat.scheduled.tasks.service.AppealDataService.OUTPUT_PARAMETERS;

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
        verify(storedProcedureService, never()).callStoredProcedure(any(), anyList());
    }

    @Test
    void givenUnprocessRecordSheetsThatAreSuccessfullyRetrieved_whenPopulateAndProcessAppealDataIsInvoked_thenDataInToMaatIsPopulated() {
        List<StoredProcedureParameter<?>> procedureParameters1 = new ArrayList<>(OUTPUT_PARAMETERS);
        procedureParameters1.add(StoredProcedureParameter.inputParameter("id", 1));
        List<StoredProcedureParameter<?>> procedureParameters2 = new ArrayList<>(OUTPUT_PARAMETERS);
        procedureParameters2.add(StoredProcedureParameter.inputParameter("id", 2));

        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(List.of(xhibitRecordSheet1, xhibitRecordSheet2));
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(Collections.emptyList());

        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(getRecordSheetsResponse);
        when(appealDataRepository.findAll()).thenReturn(List.of(
            XhibitAppealDataEntity.builder().id(1).filename(xhibitRecordSheet1.getFilename()).data(xhibitRecordSheet1.getData()).build(),
            XhibitAppealDataEntity.builder().id(2).filename(xhibitRecordSheet2.getFilename()).data(xhibitRecordSheet2.getData()).build()
        ));
        when(storedProcedureService.callStoredProcedure(any(), anyList())).thenReturn(new StoredProcedureResponse(List.of()));

        appealDataService.populateAndProcessAppealDataInToMaat();

        verify(appealDataRepository, times(1)).saveAll(any());
        verify(xhibitDataService, times(1)).getAllRecordSheets(RecordSheetType.APPEAL);
        verify(storedProcedureService, times(1)).callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, procedureParameters1);
        verify(storedProcedureService, times(1)).callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, procedureParameters2);
        verify(xhibitDataService, times(1)).markRecordSheetsAsProcessed(List.of(xhibitRecordSheet1.getFilename(), xhibitRecordSheet2.getFilename()), RecordSheetType.APPEAL);
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenUnprocessedRecordSheetsThatCannotBeRetrieved_whenPopulateAndProcessAppealDataIsInvoked_thenNoDataInToMaatIsPopulated() {
        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(Collections.emptyList());
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(List.of(xhibitRecordSheet1, xhibitRecordSheet2));

        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(getRecordSheetsResponse);

        appealDataService.populateAndProcessAppealDataInToMaat();

        verify(appealDataRepository, times(1)).saveAll(Collections.emptyList());
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

        List<StoredProcedureParameter<?>> procedureParameters1 = new ArrayList<>(OUTPUT_PARAMETERS);
        procedureParameters1.add(StoredProcedureParameter.inputParameter("id", 1));
        List<StoredProcedureParameter<?>> procedureParameters2 = new ArrayList<>(OUTPUT_PARAMETERS);
        procedureParameters2.add(StoredProcedureParameter.inputParameter("id", 2));

        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(response);
        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(response);
        when(appealDataRepository.findAll()).thenReturn(List.of(
            XhibitAppealDataEntity.builder().id(1).filename(xhibitRecordSheet1.getFilename()).data(xhibitRecordSheet1.getData()).build(),
            XhibitAppealDataEntity.builder().id(2).filename(xhibitRecordSheet2.getFilename()).data(xhibitRecordSheet2.getData()).build()
        ));
        when(storedProcedureService.callStoredProcedure(any(), anyList())).thenReturn(new StoredProcedureResponse(List.of()));

        appealDataService.populateAndProcessAppealDataInToMaat();

        verify(appealDataRepository, times(1)).saveAll(any());
        verify(xhibitDataService, times(1)).getAllRecordSheets(RecordSheetType.APPEAL);
        verify(storedProcedureService, times(1)).callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, procedureParameters1);
        verify(storedProcedureService, times(1)).callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, procedureParameters2);
        verify(xhibitDataService, times(1)).markRecordSheetsAsProcessed(List.of(xhibitRecordSheet1.getFilename(), xhibitRecordSheet2.getFilename()), RecordSheetType.APPEAL);
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenStoredProcedureFailures_whenHandlingExceptions_FailedRecordsAreMarked() {
        List<StoredProcedureParameter<?>> procedureParameters1 = new ArrayList<>(TrialDataService.OUTPUT_PARAMETERS);
        procedureParameters1.add(StoredProcedureParameter.inputParameter("id", 1));
        List<StoredProcedureParameter<?>> procedureParameters2 = new ArrayList<>(TrialDataService.OUTPUT_PARAMETERS);
        procedureParameters2.add(StoredProcedureParameter.inputParameter("id", 2));

        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(List.of(xhibitRecordSheet1, xhibitRecordSheet2));
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(Collections.emptyList());

        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(getRecordSheetsResponse);
        when(appealDataRepository.findAll()).thenReturn(List.of(
            XhibitAppealDataEntity.builder().id(1).filename(xhibitRecordSheet1.getFilename()).data(xhibitRecordSheet1.getData()).build(),
            XhibitAppealDataEntity.builder().id(2).filename(xhibitRecordSheet2.getFilename()).data(xhibitRecordSheet2.getData()).build()
        ));

        when(storedProcedureService.callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, procedureParameters1)).thenReturn(new StoredProcedureResponse(List.of()));
        doThrow(StoredProcedureException.class).when(storedProcedureService).callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, procedureParameters2);

        appealDataService.populateAndProcessAppealDataInToMaat();

        verify(storedProcedureService, times(1)).callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, procedureParameters1);
        verify(storedProcedureService, times(1)).callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, procedureParameters2);
        verify(xhibitDataService, times(1)).markRecordSheetsAsProcessed(List.of(xhibitRecordSheet1.getFilename()), RecordSheetType.APPEAL);
        verify(xhibitDataService, times(1)).markRecordSheetsAsErrored(List.of(xhibitRecordSheet2.getFilename()), RecordSheetType.APPEAL);
    }

    @Test
    void givenStoredProcedureResponse_whenContainsErrorCodeOutput_thenMarkFailed() {
        List<StoredProcedureParameter<?>> procedureParameters = new ArrayList<>(AppealDataService.OUTPUT_PARAMETERS);
        procedureParameters.add(StoredProcedureParameter.inputParameter("id", 1));
        StoredProcedureResponse errorResponse = new StoredProcedureResponse(List.of(
            safePopulate(outputParameter("p_error_code", String.class), "23"),
            safePopulate(outputParameter("p_err_msg", String.class), "error message")
        ));

        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(List.of(xhibitRecordSheet1));
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(Collections.emptyList());
        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(getRecordSheetsResponse);
        when(appealDataRepository.findAll()).thenReturn(List.of(
            XhibitAppealDataEntity.builder().id(1).filename(xhibitRecordSheet1.getFilename()).data(xhibitRecordSheet1.getData()).build()
        ));
        when(storedProcedureService.callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, procedureParameters)).thenReturn(errorResponse);

        appealDataService.populateAndProcessAppealDataInToMaat();
        verify(xhibitDataService, times(1)).markRecordSheetsAsErrored(List.of(xhibitRecordSheet1.getFilename()), RecordSheetType.APPEAL);
    }
}
