package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitAppealDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.StoredProcedureException;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitAppealDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.inputParameter;
import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.outputParameter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppealDataService {

    static final Collection<StoredProcedureParameter<?>> OUTPUT_PARAMETERS = List.of(
        outputParameter("p_error_code", String.class),
        outputParameter("p_err_msg", String.class)
    );
    static final String APPEAL_DATA_TO_MAAT_PROCEDURE = "hub.xhibit_file_load.process_appeal_record";

    private final XhibitDataService xhibitDataService;
    private final XhibitAppealDataRepository appealDataRepository;
    private final StoredProcedureService storedProcedureService;

    @Transactional
    public void populateAndProcessAppealDataInToMaat() {
        RecordSheetType recordSheetType = RecordSheetType.APPEAL;

        GetRecordSheetsResponse recordSheetsResponse = xhibitDataService.getAllRecordSheets(recordSheetType);
        List<XhibitRecordSheetDTO> recordSheets = recordSheetsResponse.getRetrievedRecordSheets();
        List<XhibitRecordSheetDTO> erroredRecordSheets = recordSheetsResponse.getErroredRecordSheets();

        if (recordSheets.isEmpty() && erroredRecordSheets.isEmpty()) {
            log.info("No trial data found to process, aborting");
            return;
        }

        List<String> processedFilenames = new ArrayList<>();
        List<String> erroredFilenames = new ArrayList<>(erroredRecordSheets.stream().map(XhibitRecordSheetDTO::getFilename).toList());

        saveRecordSheets(recordSheets);
        log.info("Populated appeal data in to hub.");

        List<XhibitAppealDataEntity> toProcess = appealDataRepository.findAll();
        for (XhibitAppealDataEntity record : toProcess) {
            processAppealRecord(record, erroredFilenames, processedFilenames);
        }

        if (!erroredFilenames.isEmpty()) {
            xhibitDataService.markRecordSheetsAsErrored(erroredFilenames, recordSheetType);
            log.info("Marked appeal record sheets as errored { records: {} }.", erroredFilenames.size());
        }

        if (!processedFilenames.isEmpty()) {
            xhibitDataService.markRecordSheetsAsProcessed(processedFilenames, recordSheetType);
            log.info("Marked appeal record sheets as processed. {records: {} }.", processedFilenames.size());
        }
    }

    private void processAppealRecord(XhibitAppealDataEntity record, List<String> failedProcedureFilenames, List<String> successfulProcedureFilenames) {
        List<StoredProcedureParameter<?>> parameters = getProcedureParameters(record);
        try {
            StoredProcedureResponse storedProcedureResponse = storedProcedureService.callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, parameters);
            if (isErrored(storedProcedureResponse)) {
                log.error("Appeal data stored procedure returned an error: { procedure: {}, recordId: {}, errorCode: {}, errorMessage: {} }",
                    APPEAL_DATA_TO_MAAT_PROCEDURE,
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

    private static List<StoredProcedureParameter<?>> getProcedureParameters(XhibitAppealDataEntity record) {
        List<StoredProcedureParameter<?>> parameters = new ArrayList<>(OUTPUT_PARAMETERS);
        parameters.add(inputParameter("id", record.getId()));
        return parameters;
    }

    private void saveRecordSheets(List<XhibitRecordSheetDTO> recordSheets) {
        List<XhibitAppealDataEntity> entities = recordSheets.stream().map(XhibitAppealDataEntity::fromDto).toList();
        appealDataRepository.saveAll(entities);
    }
}
