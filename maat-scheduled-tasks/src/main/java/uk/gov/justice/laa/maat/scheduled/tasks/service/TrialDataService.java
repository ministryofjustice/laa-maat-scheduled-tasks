package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitAppealDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitAppealDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitTrialDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialDataService {

    static final String TRIAL_DATA_TO_MAAT_PROCEDURE = "hub.xhibit_file_load.process_trial_record";
    static final String APPEAL_DATA_TO_MAAT_PROCEDURE = "hub.xhibit_file_load.process_appeal_record";

    private final XhibitDataService xhibitDataService;

    private final XhibitAppealDataRepository appealDataRepository;
    private final XhibitTrialDataRepository trialDataRepository;

    private final StoredProcedureService storedProcedureService;

    @Transactional
    public void populateAndProcessTrialDataInToMaat() {
        RecordSheetType recordSheetType = RecordSheetType.TRIAL;

        log.info("Starting to populate Trial Data in to Hub.");
        populateRecordSheets(recordSheetType);

        List<XhibitTrialDataEntity> toProcess = trialDataRepository.findAll();
        if (toProcess.isEmpty()) {
            return;
        }
        log.info("Starting to process Trial Data in to MAAT.");
        for (XhibitTrialDataEntity record : toProcess) {
            storedProcedureService.callStoredProcedure(TRIAL_DATA_TO_MAAT_PROCEDURE, Map.of("id", record.getId()));
        }

        log.info("Starting to mark records sheets as processed.");
        List<String> filenames = toProcess.stream().map(XhibitTrialDataEntity::getFilename).toList();
        xhibitDataService.markRecordsSheetsAsProcessed(filenames, recordSheetType);
    }

    @Transactional
    public void populateAndProcessAppealDataInToMaat() {
        RecordSheetType recordSheetType = RecordSheetType.APPEAL;

        log.info("Starting to populate Appeal Data in to Hub.");
        populateRecordSheets(recordSheetType);

        List<XhibitAppealDataEntity> toProcess = appealDataRepository.findAll();
        if (toProcess.isEmpty()) {
            return;
        }
        log.info("Starting to process Appeal Data in to MAAT.");
        for (XhibitAppealDataEntity record : toProcess) {
            storedProcedureService.callStoredProcedure(APPEAL_DATA_TO_MAAT_PROCEDURE, Map.of("id", record.getId()));
        }

        log.info("Starting to mark records sheets as processed.");
        List<String> filenames = toProcess.stream().map(XhibitAppealDataEntity::getFilename).toList();
        xhibitDataService.markRecordsSheetsAsProcessed(filenames, recordSheetType);
    }

    private void populateRecordSheets(RecordSheetType recordSheetType) {
        GetRecordSheetsResponse recordSheetsResponse;
        String continuationToken = null;

        do {
            recordSheetsResponse = xhibitDataService.getRecordSheets(
                recordSheetType, continuationToken);

            if (!recordSheetsResponse.getRetrievedRecordSheets().isEmpty()) {
                saveRecordSheets(recordSheetType, recordSheetsResponse);
            }

            if (!recordSheetsResponse.getErroredRecordSheets().isEmpty()) {
                List<String> erroredFilenames = recordSheetsResponse.getErroredRecordSheets().stream()
                    .map(XhibitRecordSheetDTO::getFilename)
                    .toList();
                xhibitDataService.markRecordSheetsAsErrored(erroredFilenames, recordSheetType);
            }

            continuationToken = recordSheetsResponse.getContinuationToken();

        } while (!recordSheetsResponse.allRecordSheetsRetrieved());
    }

    private void saveRecordSheets(RecordSheetType recordSheetType, GetRecordSheetsResponse recordSheetsResponse) {
        if (RecordSheetType.TRIAL.equals(recordSheetType)) {
            List<XhibitTrialDataEntity> entities = recordSheetsResponse.getRetrievedRecordSheets()
                .stream().map(XhibitTrialDataEntity::fromDto).toList();

            trialDataRepository.saveAll(entities);
        } else {
            List<XhibitAppealDataEntity> entities = recordSheetsResponse.getRetrievedRecordSheets()
                .stream().map(XhibitAppealDataEntity::fromDto).toList();
            
            appealDataRepository.saveAll(entities);
        }
    }

}
