package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.SdkRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import uk.gov.justice.laa.maat.scheduled.tasks.config.XhibitConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitTrialDataRepository;

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
            while (!xhibitDataService.isAllFilesRetrievedFromS3()) {
                List<XhibitRecordSheetDTO> recordSheetDTOS = xhibitDataService.getRecordSheets(RecordSheetType.TRIAL);

                List<XhibitTrialDataEntity> entities = recordSheetDTOS.stream().map(dto ->
                    XhibitTrialDataEntity.builder()
                        .filename(dto.getFilename())
                        .data(dto.getData())
                        .build()).toList();

                trialDataRepository.saveAll(entities);

                xhibitDataService.markFilesAsCompleted();
            }
        }
        catch (Exception e) {
            xhibitDataService.markFilesAsErrored();
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
