package uk.gov.justice.laa.maat.scheduled.tasks.service;

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
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitTrialDataRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialDataService {

    // TODO: Potentially move the S3Client out into a separate XhibitDataService
    private final S3Client s3Client;

    private final XhibitTrialDataRepository trialDataRepository;

    public void populateTrialData() {
        log.info("Starting to populate Trial Data in to Hub.");
    }

    public void processTrialDataInToMaat() {
        log.info("Starting to process Trial Data in to MAAT.");
        // TODO
    }

    public void populateAppealData() {
        log.info("Starting to populate Appeal Data in to Hub.");

        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
            .bucket("bucket-name")
            .prefix("incoming/trial/")
            .build();

        ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

        listObjectsResponse.contents().forEach(s3Object -> {
            ResponseBytes<GetObjectResponse> objectResponse = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket("bucket-name")
                .key(s3Object.key())
                .build());

            String trialDataXml = objectResponse.asUtf8String();

            XhibitTrialDataEntity entity = XhibitTrialDataEntity.builder()
                .status("UNPROCESSED")
                .filename(s3Object.key().split("incoming/trial")[1])
                .data(trialDataXml)
                .build();

            trialDataRepository.save(entity);
        });

    }

    public void processAppealDataInToMaat() {
        log.info("Starting to process Appeal Data in to MAAT.");
        // TODO
    }
}
