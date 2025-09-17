package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.XhibitRecordSheet;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.ProcedureResult;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.GetRecordSheetsResponse;

@Slf4j
@RequiredArgsConstructor
public abstract class XhibitDataServiceBase<T> {

    private final XhibitDataService xhibitDataService;
    private final JpaRepository<T, Integer> repository;
    private final XhibitProcedureService<T> procedureService;

    @Transactional
    public void populateAndProcessData() {
        RecordSheetType type = getRecordSheetType();

        GetRecordSheetsResponse response = xhibitDataService.getAllRecordSheets(type);
        if (response.getRetrievedRecordSheets().isEmpty() && response.getErroredRecordSheets()
                .isEmpty()) {
            log.info("No {} data found to process, aborting", type);
            return;
        }

        // Save
        List<T> entities = response.getRetrievedRecordSheets().stream()
                .map(this::fromDto)
                .toList();

        repository.saveAllAndFlush(entities);
        log.info("Populated {} data into hub", type);

        // Process only the entities we just saved
        List<T> toProcess = repository.findAllById(
                entities.stream().map(this::getEntityId).toList()
        );

        // Partition results
        Map<Boolean, List<String>> results = toProcess.stream()
                .collect(Collectors.partitioningBy(
                        e -> procedureService.call(e) == ProcedureResult.SUCCESS,
                        Collectors.mapping(this::getFilename, Collectors.toList())
                ));

        List<String> processed = results.get(true);
        List<String> errored = Stream.concat(
                results.get(false).stream(),
                response.getErroredRecordSheets().stream().map(XhibitRecordSheet::getFilename)
        ).toList();

        if (!errored.isEmpty()) {
            xhibitDataService.markRecordSheetsAsErrored(errored, type);
            log.info("Marked {} {} record sheets as errored", errored.size(), type);
        }

        if (!processed.isEmpty()) {
            xhibitDataService.markRecordSheetsAsProcessed(processed, type);
            log.info("Marked {} {} record sheets as processed", processed.size(), type);
        }
    }

    protected abstract RecordSheetType getRecordSheetType();

    protected abstract T fromDto(XhibitRecordSheet dto);

    protected abstract Integer getEntityId(T entity);

    protected abstract String getFilename(T entity);
}

