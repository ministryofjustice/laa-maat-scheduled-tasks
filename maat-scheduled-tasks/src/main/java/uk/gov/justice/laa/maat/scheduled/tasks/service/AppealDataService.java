package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitAppealDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitAppealDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppealDataService {

    static final Map<String, Class<?>> OUTPUT_PARAMS = Map.of("p_error_code", String.class, "p_err_msg", String.class);
    static final String APPEAL_DATA_TO_MAAT_PROCEDURE = "hub.xhibit_file_load.process_appeal_record";

    private final XhibitDataService xhibitDataService;
    private final XhibitAppealDataRepository appealDataRepository;
    private final StoredProcedureService storedProcedureService;

    @Transactional
    public void populateAndProcessAppealDataInToMaat() {
        RecordSheetType recordSheetType = RecordSheetType.APPEAL;

        GetRecordSheetsResponse recordSheetsResponse = xhibitDataService.getAllRecordSheets(recordSheetType);

        List<XhibitRecordSheetDTO> erroredRecordSheets = recordSheetsResponse.getErroredRecordSheets();
        if (!erroredRecordSheets.isEmpty()) {
            xhibitDataService.markRecordSheetsAsErrored(erroredRecordSheets, recordSheetType);
            log.info("Marked errored record sheets { records: {} }.", erroredRecordSheets.size());
        }

        List<XhibitRecordSheetDTO> recordSheets = recordSheetsResponse.getRetrievedRecordSheets();
        if (recordSheets.isEmpty()) {
            log.info("No appeal data found to process, aborting");
            return;
        }

        saveRecordSheets(recordSheets);
        log.info("Populated appeal data in to hub.");

        List<XhibitAppealDataEntity> toProcess = appealDataRepository.findAll();
        if (toProcess.isEmpty()) {
            log.info("No appeal data found to process, aborting");
            return;
        }

        for (XhibitAppealDataEntity record : toProcess) {
            Map<String, Object> inputParams = Map.of("id", record.getId());
            storedProcedureService.callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, inputParams, OUTPUT_PARAMS);
        }
        log.info("Processed appeal data in to MAAT. { records: {} }", toProcess.size());

        xhibitDataService.markRecordSheetsAsProcessed(recordSheets, recordSheetType);
        log.info("Marked appeal record sheets as processed.");
    }

    private void saveRecordSheets(List<XhibitRecordSheetDTO> recordSheets) {
        List<XhibitAppealDataEntity> entities = recordSheets.stream().map(XhibitAppealDataEntity::fromDto).toList();
        appealDataRepository.saveAll(entities);
    }
}
