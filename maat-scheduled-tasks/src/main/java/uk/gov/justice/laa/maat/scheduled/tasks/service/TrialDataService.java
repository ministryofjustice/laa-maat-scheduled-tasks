package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    static final String TRIAL_DATA_TO_MAAT_PROCEDURE = "xhibit_file_load.process_trial_record";

    private final XhibitDataService xhibitDataService;

    private final XhibitTrialDataRepository trialDataRepository;

    private final StoredProcedureService storedProcedureService;

    public void populateTrialData() {
        log.info("Starting to populate Trial Data in to Hub.");

        GetRecordSheetsResponse recordSheetsResponse;
        String continuationToken = null;

        do {
            recordSheetsResponse = xhibitDataService.getRecordSheets(
                RecordSheetType.TRIAL, continuationToken);

            if (!recordSheetsResponse.getRetrievedRecordSheets().isEmpty()) {
                List<XhibitTrialDataEntity> entities = recordSheetsResponse.getRetrievedRecordSheets()
                    .stream().map(dto ->
                        XhibitTrialDataEntity.builder()
                            .filename(dto.getFilename())
                            .data(dto.getData())
                            .build()).toList();

                trialDataRepository.saveAll(entities);

                xhibitDataService.markRecordsSheetsAsProcessed(
                    recordSheetsResponse.getRetrievedRecordSheets(), RecordSheetType.TRIAL);
            }

            if (!recordSheetsResponse.getErroredRecordSheets().isEmpty()) {
                xhibitDataService.markRecordSheetsAsErrored(
                    recordSheetsResponse.getErroredRecordSheets(), RecordSheetType.TRIAL);
            }

            continuationToken = recordSheetsResponse.getContinuationToken();

        } while (!recordSheetsResponse.allRecordSheetsRetrieved());
    }

    public void processTrialDataInToMaat() {
        log.info("Starting to process Trial Data in to MAAT.");
        List<Integer> unprocessedIds = trialDataRepository.findUnprocessedIds();
        for (Integer id : unprocessedIds) {
            storedProcedureService.callStoredProcedure(TRIAL_DATA_TO_MAAT_PROCEDURE, Map.of("id", id));
        }
    }

    public void populateAppealData() {
        log.info("Starting to populate Appeal Data in to Hub.");
        // TODO
    }

    public void processAppealDataInToMaat() {
        log.info("Starting to process Appeal Data in to MAAT.");
        // TODO
    }

}
