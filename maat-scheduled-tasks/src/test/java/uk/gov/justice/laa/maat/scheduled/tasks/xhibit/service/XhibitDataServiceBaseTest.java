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
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.RecordSheetsPage;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.RecordSheet;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.ProcedureResult;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetType;

@ExtendWith(MockitoExtension.class)
class XhibitDataServiceBaseTest {

    @Mock
    private XhibitS3Service xhibitS3Service;
    @Mock
    private StoredProcedureService storedProcedureService;
    @Mock
    private JpaRepository<TestEntity, Integer> repository;
    @Mock
    private XhibitProcedureService<TestEntity> procedureService;
    @Mock
    private RecordSheetsPage recordSheetsPage;

    @InjectMocks
    private TestDataServiceBase testDataService;

    private final RecordSheet recordSheet1 = RecordSheet.builder()
            .filename("trial_record_1.xml")
            .data("<NS1:TrialRecordSheet><NS1:DocumentID><NS1:DocumentName>TR Alice Bloggs</NS1:DocumentName></NS1:DocumentID></NS1:TrialRecordSheet>")
            .build();

    private final RecordSheet recordSheet2 = RecordSheet.builder()
            .filename("trial_record_2.xml")
            .data("<NS1:TrialRecordSheet><NS1:DocumentID><NS1:DocumentName>TR Joe Bloggs</NS1:DocumentName></NS1:DocumentID></NS1:TrialRecordSheet>")
            .build();

    @Test
    void givenNoUnprocessedRecordSheets_whenPopulateAndProcessAppealDataIsInvoked_thenNoDataInToMaatIsPopulated() {
        when(xhibitS3Service.getRecordSheets(RecordSheetType.APPEAL))
                .thenReturn(recordSheetsPage);

        testDataService.populateAndProcessData();

        verify(repository, never()).saveAll(any());
        verify(xhibitS3Service, times(1)).getRecordSheets(RecordSheetType.APPEAL);
        verify(xhibitS3Service, never()).markProcessed(any(), any());
        verify(xhibitS3Service, never()).markErrored(any(), any());
        verify(storedProcedureService, never()).callStoredProcedure(any(), anyList());
    }

    @Test
    void givenUnprocessRecordSheetsThatAreSuccessfullyRetrieved_whenPopulateAndProcessAppealDataIsInvoked_thenDataInToMaatIsPopulated() {

        when(recordSheetsPage.retrieved()).thenReturn(
                List.of(recordSheet1, recordSheet2));

        when(recordSheetsPage.errored()).thenReturn(Collections.emptyList());

        when(xhibitS3Service.getRecordSheets(RecordSheetType.APPEAL)).thenReturn(
                recordSheetsPage);

        when(repository.findAllById(anyList())).thenReturn(List.of(
                TestEntity.builder().id(1).filename(recordSheet1.filename())
                        .data(recordSheet1.data()).build(),
                TestEntity.builder().id(2).filename(recordSheet2.filename())
                        .data(recordSheet2.data()).build()
        ));
        when(procedureService.call(any(TestEntity.class))).thenReturn(ProcedureResult.SUCCESS);

        testDataService.populateAndProcessData();

        verify(repository).saveAllAndFlush(any());
        verify(xhibitS3Service).getRecordSheets(RecordSheetType.APPEAL);
        verify(procedureService, times(2)).call(any(TestEntity.class));
        verify(xhibitS3Service).markProcessed(
                List.of(recordSheet1.filename(), recordSheet2.filename()),
                RecordSheetType.APPEAL);
        verify(xhibitS3Service, never()).markErrored(any(), any());
    }

    @Test
    void givenUnprocessedRecordSheetsThatCannotBeRetrieved_whenPopulateAndProcessAppealDataIsInvoked_thenNoDataInToMaatIsPopulated() {
        when(recordSheetsPage.retrieved()).thenReturn(
                Collections.emptyList());
        when(recordSheetsPage.errored()).thenReturn(
                List.of(recordSheet1, recordSheet2));

        when(xhibitS3Service.getRecordSheets(RecordSheetType.APPEAL)).thenReturn(
                recordSheetsPage);

        testDataService.populateAndProcessData();

        verify(repository, times(1)).saveAllAndFlush(Collections.emptyList());
        verify(xhibitS3Service, times(1)).getRecordSheets(RecordSheetType.APPEAL);
        verify(xhibitS3Service, never()).markProcessed(any(), any());
        verify(xhibitS3Service, times(1)).markErrored(any(), any());
    }

    @Test
    void givenMultiplePagesOfUnprocessedRecordSheets_whenPopulateAndProcessAppealDataIsInvoked_thenDataInToMaatIsPopulated() {
        RecordSheetsPage page = RecordSheetsPage.complete(List.of(recordSheet1, recordSheet2),
                Collections.emptyList());

        when(xhibitS3Service.getRecordSheets(RecordSheetType.APPEAL)).thenReturn(page)
                .thenReturn(page);

        when(repository.findAllById(anyList())).thenReturn(List.of(
                TestEntity.builder().id(1).filename(recordSheet1.filename())
                        .data(recordSheet1.data()).build(),
                TestEntity.builder().id(2).filename(recordSheet2.filename())
                        .data(recordSheet2.data()).build()
        ));
        when(procedureService.call(any(TestEntity.class))).thenReturn(ProcedureResult.SUCCESS);

        testDataService.populateAndProcessData();

        verify(repository).saveAllAndFlush(any());
        verify(xhibitS3Service).getRecordSheets(RecordSheetType.APPEAL);
        verify(procedureService, times(2)).call(any(TestEntity.class));
        verify(xhibitS3Service).markProcessed(
                List.of(recordSheet1.filename(), recordSheet2.filename()),
                RecordSheetType.APPEAL);
        verify(xhibitS3Service, never()).markErrored(any(), any());
    }

    @Test
    void givenStoredProcedureFailures_whenHandlingExceptions_FailedRecordsAreMarked() {

        when(recordSheetsPage.retrieved()).thenReturn(
                List.of(recordSheet1, recordSheet2));
        when(recordSheetsPage.errored()).thenReturn(Collections.emptyList());

        when(xhibitS3Service.getRecordSheets(RecordSheetType.APPEAL)).thenReturn(
                recordSheetsPage);
        when(repository.findAllById(anyList())).thenReturn(List.of(
                TestEntity.builder().id(1).filename(recordSheet1.filename())
                        .data(recordSheet1.data()).build(),
                TestEntity.builder().id(2).filename(recordSheet2.filename())
                        .data(recordSheet2.data()).build()
        ));

        when(procedureService.call(any(TestEntity.class))).thenReturn(ProcedureResult.SUCCESS)
                .thenReturn(ProcedureResult.FAILURE);

        testDataService.populateAndProcessData();

        verify(procedureService, times(2)).call(any(TestEntity.class));
        verify(xhibitS3Service, times(1)).markProcessed(
                List.of(recordSheet1.filename()), RecordSheetType.APPEAL);
        verify(xhibitS3Service, times(1)).markErrored(
                List.of(recordSheet2.filename()), RecordSheetType.APPEAL);
    }

    @Test
    void givenStoredProcedureResponse_whenContainsErrorCodeOutput_thenMarkFailed() {

        when(recordSheetsPage.retrieved()).thenReturn(
                List.of(recordSheet1));
        when(recordSheetsPage.errored()).thenReturn(Collections.emptyList());
        when(xhibitS3Service.getRecordSheets(RecordSheetType.APPEAL)).thenReturn(
                recordSheetsPage);
        when(repository.findAllById(anyList())).thenReturn(List.of(
                TestEntity.builder().id(1).filename(recordSheet1.filename())
                        .data(recordSheet1.data()).build()
        ));
        when(procedureService.call(any(TestEntity.class))).thenReturn(ProcedureResult.FAILURE);

        testDataService.populateAndProcessData();

        verify(xhibitS3Service, times(1)).markErrored(
                List.of(recordSheet1.filename()), RecordSheetType.APPEAL);
    }


    static class TestDataServiceBase extends XhibitDataServiceBase<TestEntity> {

        public TestDataServiceBase(XhibitS3Service xhibitS3Service,
                JpaRepository<TestEntity, Integer> repository,
                XhibitProcedureService<TestEntity> procedureService) {
            super(xhibitS3Service, repository, procedureService);
        }

        @Override
        protected RecordSheetType getRecordSheetType() {
            return RecordSheetType.APPEAL; // doesn’t matter for test
        }

        @Override
        protected TestEntity fromDto(RecordSheet dto) {
            return new TestEntity(1, dto.filename(), "");
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
    private record TestEntity(Integer id, String filename, String data) {}


}
