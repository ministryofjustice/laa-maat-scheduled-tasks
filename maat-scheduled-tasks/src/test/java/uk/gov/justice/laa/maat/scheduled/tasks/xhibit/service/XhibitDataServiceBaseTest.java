package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.service.StoredProcedureService;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.XhibitRecordSheet;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.ProcedureResult;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.GetRecordSheetsResponse;

@ExtendWith(MockitoExtension.class)
class XhibitDataServiceBaseTest {

    @Mock
    private XhibitDataService xhibitDataService;
    @Mock
    private StoredProcedureService storedProcedureService;
    @Mock
    private JpaRepository<TestEntity, Integer> repository;
    @Mock
    private XhibitProcedureService<TestEntity> procedureService;
    @Mock
    private GetRecordSheetsResponse getRecordSheetsResponse;

    @InjectMocks
    private TestDataServiceDataBase testDataService;

    private final XhibitRecordSheet recordSheet1 = XhibitRecordSheet.builder()
            .filename("trial_record_1.xml")
            .data("<NS1:TrialRecordSheet><NS1:DocumentID><NS1:DocumentName>TR Alice Bloggs</NS1:DocumentName></NS1:DocumentID></NS1:TrialRecordSheet>")
            .build();

    private final XhibitRecordSheet recordSheet2 = XhibitRecordSheet.builder()
            .filename("trial_record_2.xml")
            .data("<NS1:TrialRecordSheet><NS1:DocumentID><NS1:DocumentName>TR Joe Bloggs</NS1:DocumentName></NS1:DocumentID></NS1:TrialRecordSheet>")
            .build();

    @Test
    void givenNoUnprocessedRecordSheets_whenPopulateAndProcessAppealDataIsInvoked_thenNoDataInToMaatIsPopulated() {
        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL))
                .thenReturn(getRecordSheetsResponse);

        testDataService.populateAndProcessData();

        verify(repository, never()).saveAll(any());
        verify(xhibitDataService, times(1)).getAllRecordSheets(RecordSheetType.APPEAL);
        verify(xhibitDataService, never()).markRecordSheetsAsProcessed(any(), any());
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
        verify(storedProcedureService, never()).callStoredProcedure(any(), anyList());
    }

    @Test
    void givenUnprocessRecordSheetsThatAreSuccessfullyRetrieved_whenPopulateAndProcessAppealDataIsInvoked_thenDataInToMaatIsPopulated() {

        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(
                List.of(recordSheet1, recordSheet2));

        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(Collections.emptyList());

        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(
                getRecordSheetsResponse);

        when(repository.findAllById(anyList())).thenReturn(List.of(
                TestEntity.builder().id(1).filename(recordSheet1.getFilename())
                        .data(recordSheet1.getData()).build(),
                TestEntity.builder().id(2).filename(recordSheet2.getFilename())
                        .data(recordSheet2.getData()).build()
        ));
        when(procedureService.call(any(TestEntity.class))).thenReturn(ProcedureResult.SUCCESS);

        testDataService.populateAndProcessData();

        verify(repository).saveAllAndFlush(any());
        verify(xhibitDataService).getAllRecordSheets(RecordSheetType.APPEAL);
        verify(procedureService, times(2)).call(any(TestEntity.class));
        verify(xhibitDataService).markRecordSheetsAsProcessed(
                List.of(recordSheet1.getFilename(), recordSheet2.getFilename()),
                RecordSheetType.APPEAL);
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenUnprocessedRecordSheetsThatCannotBeRetrieved_whenPopulateAndProcessAppealDataIsInvoked_thenNoDataInToMaatIsPopulated() {
        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(
                Collections.emptyList());
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(
                List.of(recordSheet1, recordSheet2));

        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(
                getRecordSheetsResponse);

        testDataService.populateAndProcessData();

        verify(repository, times(1)).saveAllAndFlush(Collections.emptyList());
        verify(xhibitDataService, times(1)).getAllRecordSheets(RecordSheetType.APPEAL);
        verify(xhibitDataService, never()).markRecordSheetsAsProcessed(any(), any());
        verify(xhibitDataService, times(1)).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenMultiplePagesOfUnprocessedRecordSheets_whenPopulateAndProcessAppealDataIsInvoked_thenDataInToMaatIsPopulated() {
        GetRecordSheetsResponse response = GetRecordSheetsResponse.builder()
                .retrievedRecordSheets(List.of(recordSheet1, recordSheet2))
                .erroredRecordSheets(Collections.emptyList())
                .continuationToken(null)
                .allRecordSheetsRetrieved(true)
                .build();

        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(response);
        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(response);
        when(repository.findAllById(anyList())).thenReturn(List.of(
                TestEntity.builder().id(1).filename(recordSheet1.getFilename())
                        .data(recordSheet1.getData()).build(),
                TestEntity.builder().id(2).filename(recordSheet2.getFilename())
                        .data(recordSheet2.getData()).build()
        ));
        when(procedureService.call(any(TestEntity.class))).thenReturn(ProcedureResult.SUCCESS);

        testDataService.populateAndProcessData();

        verify(repository).saveAllAndFlush(any());
        verify(xhibitDataService).getAllRecordSheets(RecordSheetType.APPEAL);
        verify(procedureService, times(2)).call(any(TestEntity.class));
        verify(xhibitDataService).markRecordSheetsAsProcessed(
                List.of(recordSheet1.getFilename(), recordSheet2.getFilename()),
                RecordSheetType.APPEAL);
        verify(xhibitDataService, never()).markRecordSheetsAsErrored(any(), any());
    }

    @Test
    void givenStoredProcedureFailures_whenHandlingExceptions_FailedRecordsAreMarked() {

        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(
                List.of(recordSheet1, recordSheet2));
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(Collections.emptyList());

        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(
                getRecordSheetsResponse);
        when(repository.findAllById(anyList())).thenReturn(List.of(
                TestEntity.builder().id(1).filename(recordSheet1.getFilename())
                        .data(recordSheet1.getData()).build(),
                TestEntity.builder().id(2).filename(recordSheet2.getFilename())
                        .data(recordSheet2.getData()).build()
        ));

        when(procedureService.call(any(TestEntity.class))).thenReturn(ProcedureResult.SUCCESS)
                .thenReturn(ProcedureResult.FAILURE);

        testDataService.populateAndProcessData();

        verify(procedureService, times(2)).call(any(TestEntity.class));
        verify(xhibitDataService, times(1)).markRecordSheetsAsProcessed(
                List.of(recordSheet1.getFilename()), RecordSheetType.APPEAL);
        verify(xhibitDataService, times(1)).markRecordSheetsAsErrored(
                List.of(recordSheet2.getFilename()), RecordSheetType.APPEAL);
    }

    @Test
    void givenStoredProcedureResponse_whenContainsErrorCodeOutput_thenMarkFailed() {

        when(getRecordSheetsResponse.getRetrievedRecordSheets()).thenReturn(
                List.of(recordSheet1));
        when(getRecordSheetsResponse.getErroredRecordSheets()).thenReturn(Collections.emptyList());
        when(xhibitDataService.getAllRecordSheets(RecordSheetType.APPEAL)).thenReturn(
                getRecordSheetsResponse);
        when(repository.findAllById(anyList())).thenReturn(List.of(
                TestEntity.builder().id(1).filename(recordSheet1.getFilename())
                        .data(recordSheet1.getData()).build()
        ));
        when(procedureService.call(any(TestEntity.class))).thenReturn(ProcedureResult.FAILURE);

        testDataService.populateAndProcessData();

        verify(xhibitDataService, times(1)).markRecordSheetsAsErrored(
                List.of(recordSheet1.getFilename()), RecordSheetType.APPEAL);
    }


    static class TestDataServiceDataBase extends XhibitDataServiceBase<TestEntity> {

        public TestDataServiceDataBase(XhibitDataService xhibitDataService,
                JpaRepository<TestEntity, Integer> repository,
                XhibitProcedureService<TestEntity> procedureService) {
            super(xhibitDataService, repository, procedureService);
        }

        @Override
        protected RecordSheetType getRecordSheetType() {
            return RecordSheetType.APPEAL; // doesn’t matter for test
        }

        @Override
        protected TestEntity fromDto(XhibitRecordSheet dto) {
            return new TestEntity(1, dto.getFilename(), "");
        }

        @Override
        protected Integer getEntityId(TestEntity entity) {
            return entity.id();
        }

        @Override
        protected String getFilename(TestEntity entity) {
            return entity.filename();
        }
    }

    @Builder
    record TestEntity(Integer id, String filename, String data) {

    }


}
