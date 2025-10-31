package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.StopWatch;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.RecordSheet;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.RecordSheetsPage;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity.XhibitEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetType;

@Slf4j
@RequiredArgsConstructor
public abstract class XhibitDataServiceBase<T extends XhibitEntity> {

    private final XhibitS3Service xhibitS3Service;
    private final JpaRepository<T, Integer> repository;
    private final XhibitProcedureService<T> procedureService;
    private final XhibitItemService xhibitItemService;

    public void populateAndProcessData() {
        RecordSheetType type = getRecordSheetType();

        // Get Record Sheets from S3
        StopWatch recordSheetsS3StopWatch = new StopWatch();
        recordSheetsS3StopWatch.start();
        log.info("Attempting to retrieve Record Sheets for type '{}'", type);
        RecordSheetsPage recordSheets = xhibitS3Service.getRecordSheets(type);
        recordSheetsS3StopWatch.stop();
        log.info("Retrieved Record Sheets for type '{}' from S3 in {} s", type,
                String.format("%.2f", recordSheetsS3StopWatch.getTotalTimeSeconds()));

        // If nothing back from S3 exit immediately
        if (recordSheets.retrieved().isEmpty() && recordSheets.errored()
                .isEmpty()) {
            log.info("No {} data found to process, aborting", type);
            return;
        }

        // S3 fetch errors are marked immediately and excluded from the DB path
        if (!recordSheets.errored().isEmpty()) {
            var fetchErr = recordSheets.errored().stream().map(RecordSheet::filename).toList();
            xhibitS3Service.markErrored(fetchErr, type);
            log.info("Marked {} {} record sheets as errored (failed to fetch from S3)", fetchErr.size(), type);
        }

        // If no Record Sheets to Process after removing errors then immediately return
        var toProcess = recordSheets.retrieved();
        int total = toProcess.size();
        if (total == 0) {
            log.info("Nothing to process after filtering fetch errors for {}", type);
            return;
        }

        // Process the Record Sheets from S3 in to MAAT
        StopWatch sw = new StopWatch();
        sw.start();
        List<String> processed = new ArrayList<>();
        List<String> errored   = new ArrayList<>();

        log.info("Starting {} individual processing: count={}", type, total);

        int i = 0;
        for (RecordSheet rs : toProcess) {
            i++;
            try {
                boolean ok = xhibitItemService.process(fromDto(rs), repository, procedureService);
                if (ok) processed.add(rs.filename()); else errored.add(rs.filename());
            } catch (Exception ex) {
                errored.add(rs.filename());
                log.warn("({}/{}) '{}' failed: {}", i, total, rs.filename(), ex.getMessage(), ex);
            }

            if (i % 500 == 0) {
                log.info("Progress {}: {}/{} done (ok={}, err={})",
                        type, i, total, processed.size(), errored.size());
            }
        }

        // Mark the S3 Record Sheets as processed or errored for deletion
        try {
            if (!errored.isEmpty()) {
                xhibitS3Service.markErrored(errored, type);
                log.info("Marked {} {} record sheets as errored", errored.size(), type);
            }
        } catch (Exception e) {
            log.error("Failed marking errored {} for {}", errored.size(), type, e);
        }
        try {
            if (!processed.isEmpty()) {
                xhibitS3Service.markProcessed(processed, type);
                log.info("Marked {} {} record sheets as processed", processed.size(), type);
            }
        } catch (Exception e) {
            log.error("Failed marking processed {} for {}", processed.size(), type, e);
        }

        sw.stop();
        log.info("Completed {}: ok={}, err={}, elapsed={} s",
                type, processed.size(), errored.size(), String.format("%.2f", sw.getTotalTimeSeconds()));
    }

    protected abstract T fromDto(RecordSheet dto);

    protected abstract RecordSheetType getRecordSheetType();
}

