package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.RecordSheet;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.RecordSheetsPage;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.ProcedureResult;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetType;

@Slf4j
@RequiredArgsConstructor
public abstract class XhibitDataServiceBase<T> {

    private final XhibitS3Service xhibitS3Service;
    private final JpaRepository<T, Integer> repository;
    private final XhibitProcedureService<T> procedureService;

    @Transactional
    public void populateAndProcessData() {
        RecordSheetType type = getRecordSheetType();

        RecordSheetsPage recordSheets = xhibitS3Service.getRecordSheets(type);
        if (recordSheets.retrieved().isEmpty() && recordSheets.errored()
                .isEmpty()) {
            log.info("No {} data found to process, aborting", type);
            return;
        }

        // Save
        List<T> entities = recordSheets.retrieved().stream()
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
        List<String> errored = Stream.concat(results.get(false).stream(),
                recordSheets.errored().stream().map(RecordSheet::filename)
        ).toList();

        if (!errored.isEmpty()) {
            xhibitS3Service.markErrored(errored, type);
            log.info("Marked {} {} record sheets as errored", errored.size(), type);
        }

        if (!processed.isEmpty()) {
            xhibitS3Service.markProcessed(processed, type);
            log.info("Marked {} {} record sheets as processed", processed.size(), type);
        }
    }

    protected abstract T fromDto(RecordSheet dto);

    protected abstract Integer getEntityId(T entity);

    protected abstract String getFilename(T entity);

    protected abstract RecordSheetType getRecordSheetType();
}

