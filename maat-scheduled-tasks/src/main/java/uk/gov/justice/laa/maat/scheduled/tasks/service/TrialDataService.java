package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.factory.PrototypeBeanFactory;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitTrialDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialDataService {

    private final PrototypeBeanFactory prototypeBeanFactory;

    private final XhibitTrialDataRepository trialDataRepository;

    public void populateTrialData() {
        XhibitDataService xhibitDataService = prototypeBeanFactory.getXhibitDataService();

        log.info("Starting to populate Trial Data in to Hub.");

        do {
            GetRecordSheetsResponse recordSheetsResponse = xhibitDataService.getRecordSheets(
                RecordSheetType.TRIAL);

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

        } while (!xhibitDataService.allRecordSheetsRetrieved());
    }

    public void processTrialDataInToMaat() {
        log.info("Starting to process Trial Data in to MAAT.");
        // TODO
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
