package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitTrialDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialDataService {

    static final Map<String, Class<?>> OUTPUT_PARAMS = Map.of("p_error_code", String.class, "p_err_msg", String.class);
    static final String TRIAL_DATA_TO_MAAT_PROCEDURE = "hub.xhibit_file_load.process_trial_record";

    private final XhibitDataService xhibitDataService;
    private final XhibitTrialDataRepository trialDataRepository;
    private final StoredProcedureService storedProcedureService;

    @Transactional
    public void populateAndProcessTrialDataInToMaat() {
        RecordSheetType recordSheetType = RecordSheetType.TRIAL;

        GetRecordSheetsResponse recordSheetsResponse = xhibitDataService.getAllRecordSheets(recordSheetType);

        List<XhibitRecordSheetDTO> erroredRecordSheets = recordSheetsResponse.getErroredRecordSheets();
        if (!erroredRecordSheets.isEmpty()) {
            xhibitDataService.markRecordSheetsAsErrored(erroredRecordSheets, recordSheetType);
            log.info("Marked errored record sheets { records: {} }.", erroredRecordSheets.size());
        }

        List<XhibitRecordSheetDTO> recordSheets = recordSheetsResponse.getRetrievedRecordSheets();
        if (recordSheets.isEmpty()) {
            log.info("No trial data found to process, aborting");
            return;
        }

        saveRecordSheets(recordSheets);
        log.info("Populated trial data in to hub.");

        List<XhibitTrialDataEntity> toProcess = trialDataRepository.findAll();
        if (toProcess.isEmpty()) {
            log.info("No trial data found to process, aborting");
            return;
        }

        for (XhibitTrialDataEntity record : toProcess) {
            Map<String, Object> inputParams = Map.of("id", record.getId());
            storedProcedureService.callStoredProcedure(TRIAL_DATA_TO_MAAT_PROCEDURE, inputParams, OUTPUT_PARAMS);
        }
        log.info("Processed trial data in to MAAT. { records: {} }", toProcess.size());

        xhibitDataService.markRecordSheetsAsProcessed(recordSheets, recordSheetType);
        log.info("Marked trial record sheets as processed.");
    }

    private void saveRecordSheets(List<XhibitRecordSheetDTO> recordSheets) {
        List<XhibitTrialDataEntity> entities = recordSheets.stream().map(XhibitTrialDataEntity::fromDto).toList();
        trialDataRepository.saveAll(entities);
    }

}
