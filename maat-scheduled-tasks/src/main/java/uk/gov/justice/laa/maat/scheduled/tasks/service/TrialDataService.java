package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitAppealDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitAppealDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitTrialDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialDataService {

    private final XhibitDataService xhibitDataService;

    private final XhibitTrialDataRepository trialDataRepository;

    private final XhibitAppealDataRepository appealDataRepository;

    @Transactional
    public void processTrialDataInToMaat() {
        log.info("Starting to process Trial Data in to MAAT...");
        populateRecordSheets(RecordSheetType.TRIAL);
        // TODO: Populate the rep orders table with data from xml clob
        // TODO: Then update s3 prefixes for processed and errored files
    }

    @Transactional
    public void processAppealDataInToMaat() {
        log.info("Starting to process Appeal Data in to MAAT...");
        populateRecordSheets(RecordSheetType.APPEAL);
        // TODO: Call tweaked SPs to update the rep orders table with data from xml clob
        // TODO: Then update s3 prefixes for processed and errored files
    }

    private void populateRecordSheets(RecordSheetType recordSheetType) {
        log.info("Populating records sheets for {} data.", recordSheetType);
        GetRecordSheetsResponse recordSheetsResponse;
        String continuationToken = null;

        do {
            recordSheetsResponse = xhibitDataService.getRecordSheets(
                recordSheetType, continuationToken);

            if (!recordSheetsResponse.getRetrievedRecordSheets().isEmpty()) {
                if (RecordSheetType.TRIAL.equals(recordSheetType)) {
                    List<XhibitTrialDataEntity> entities = recordSheetsResponse.getRetrievedRecordSheets()
                        .stream().map(dto ->
                            XhibitTrialDataEntity.builder()
                                .filename(dto.getFilename())
                                .data(dto.getData())
                                .build()).toList();
                    trialDataRepository.saveAll(entities);
                } else {
                    List<XhibitAppealDataEntity> entities = recordSheetsResponse.getRetrievedRecordSheets()
                        .stream().map(dto ->
                            XhibitAppealDataEntity.builder()
                                .filename(dto.getFilename())
                                .data(dto.getData())
                                .build()).toList();
                    appealDataRepository.saveAll(entities);
                }

                // TODO: Look at moving this to after all the files have been populated and SPs called
                xhibitDataService.markRecordsSheetsAsProcessed(
                    recordSheetsResponse.getRetrievedRecordSheets(), recordSheetType);
            }

            // TODO: Look at moving this to after all the files have been populated and SPs called
            if (!recordSheetsResponse.getErroredRecordSheets().isEmpty()) {
                xhibitDataService.markRecordSheetsAsErrored(
                    recordSheetsResponse.getErroredRecordSheets(), recordSheetType);
            }

            continuationToken = recordSheetsResponse.getContinuationToken();

        } while (!recordSheetsResponse.allRecordSheetsRetrieved());
    }
}
