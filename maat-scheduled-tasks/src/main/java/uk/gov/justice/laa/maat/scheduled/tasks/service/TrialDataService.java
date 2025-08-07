package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitTrialDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialDataService {

    // TODO: Potentially move the S3Client out into a separate XhibitDataService
    private final S3Client s3Client;

    private final XhibitDataService xhibitDataService;

    private final XhibitTrialDataRepository trialDataRepository;

    public void populateTrialData() {
        log.info("Starting to populate Trial Data in to Hub.");

        try {
            do {
                GetRecordSheetsResponse recordSheetsResponse = xhibitDataService.getRecordSheets(RecordSheetType.TRIAL);

                if (!recordSheetsResponse.getRetrievedRecordSheets().isEmpty()) {
                    List<XhibitTrialDataEntity> entities = recordSheetsResponse.getRetrievedRecordSheets().stream().map(dto ->
                        XhibitTrialDataEntity.builder()
                            .filename(dto.getFilename())
                            .data(dto.getData())
                            .build()).toList();

                    trialDataRepository.saveAll(entities);

                    xhibitDataService.renameRecordSheets(recordSheetsResponse.getRetrievedRecordSheets(), ???);
                }

                if (!recordSheetsResponse.getErroredRecordSheets().isEmpty()) {
                    xhibitDataService.renameRecordSheets(recordSheetsResponse.getErroredRecordSheets(), ???);
                }

            } while (!xhibitDataService.allRecordSheetsRetrieved());
        }
        catch (Exception e) {
            // Do something
        }

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
