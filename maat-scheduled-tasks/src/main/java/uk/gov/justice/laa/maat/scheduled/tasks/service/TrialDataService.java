package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.StoredProcedureException;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitTrialDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.inputParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.outputParameter;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialDataService {

    static final Collection<StoredProcedureParameter<?>> OUTPUT_PARAMETERS = List.of(
        outputParameter("p_error_code", String.class),
        outputParameter("p_err_msg", String.class)
    );
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
            List<String> filenames = erroredRecordSheets.stream().map(XhibitRecordSheetDTO::getFilename).toList();
            xhibitDataService.markRecordSheetsAsErrored(filenames, recordSheetType);
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

        List<String> successfulProcedureFilenames = new ArrayList<>();
        List<String> failedProcedureFilenames = new ArrayList<>();
        for (XhibitTrialDataEntity record : toProcess) {
            processTrialRecord(record, failedProcedureFilenames, successfulProcedureFilenames);
        }

        if (!failedProcedureFilenames.isEmpty()) {
            xhibitDataService.markRecordSheetsAsErrored(failedProcedureFilenames, recordSheetType);
            log.info("Marked errored record sheets from failed stored procedure { records: {} }.", failedProcedureFilenames.size());
        }

        if (!successfulProcedureFilenames.isEmpty()) {
            log.info("Processed trial data in to MAAT. { records: {} }", successfulProcedureFilenames.size());
        }

        xhibitDataService.markRecordSheetsAsProcessed(successfulProcedureFilenames, recordSheetType);
        log.info("Marked trial record sheets as processed.");
    }

    private void processTrialRecord(XhibitTrialDataEntity record, List<String> failedProcedureFilenames, List<String> successfulProcedureFilenames) {
        List<StoredProcedureParameter<?>> parameters = getProcedureParameters(record);
        try {
            StoredProcedureResponse storedProcedureResponse = storedProcedureService.callStoredProcedure(TRIAL_DATA_TO_MAAT_PROCEDURE, parameters);
            if (isErrored(storedProcedureResponse)) {
                log.error("Trial data stored procedure returned an error: { procedure: {}, recordId: {}, errorCode: {}, errorMessage: {} }",
                    TRIAL_DATA_TO_MAAT_PROCEDURE,
                    record.getId(),
                    storedProcedureResponse.getValue("p_error_code"),
                    storedProcedureResponse.getValue("p_err_msg")
                );
                failedProcedureFilenames.add(record.getFilename());
            } else {
                successfulProcedureFilenames.add(record.getFilename());
            }
        } catch (StoredProcedureException e) {
            failedProcedureFilenames.add(record.getFilename());
        }
    }

    private static boolean isErrored(StoredProcedureResponse storedProcedureResponse) {
        return storedProcedureResponse.hasValue("p_err_msg") && storedProcedureResponse.hasValue("p_error_code");
    }

    private static List<StoredProcedureParameter<?>> getProcedureParameters(XhibitTrialDataEntity record) {
        List<StoredProcedureParameter<?>> parameters = new ArrayList<>(OUTPUT_PARAMETERS);
        parameters.add(inputParameter("id", record.getId()));
        return parameters;
    }

    private void saveRecordSheets(List<XhibitRecordSheetDTO> recordSheets) {
        List<XhibitTrialDataEntity> entities = recordSheets.stream().map(XhibitTrialDataEntity::fromDto).toList();
        trialDataRepository.saveAll(entities);
    }
}
