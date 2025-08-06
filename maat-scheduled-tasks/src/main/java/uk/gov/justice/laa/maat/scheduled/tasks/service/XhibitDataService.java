package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import uk.gov.justice.laa.maat.scheduled.tasks.config.XhibitConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;

@Slf4j
@Service
@RequiredArgsConstructor
public class XhibitDataService {

    private final S3Client s3Client;

    private final XhibitConfiguration xhibitConfiguration;

    @Getter
    private boolean allFilesRetrievedFromS3;

    private String continuationToken;

    public List<XhibitRecordSheetDTO> getRecordSheets(RecordSheetType recordSheetType) {
        String objectKeyPrefix = recordSheetType == RecordSheetType.TRIAL
            ? xhibitConfiguration.getObjectKeyTrialPrefix() : xhibitConfiguration.getObjectKeyAppealPrefix();

        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
            .bucket(xhibitConfiguration.getS3DataBucketName())
            .prefix(objectKeyPrefix)
            .continuationToken(continuationToken)
            .build();

        ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

        if (listObjectsResponse.contents().isEmpty()) {
            return Collections.emptyList();
        }

        List<XhibitRecordSheetDTO> results = new ArrayList<>();

        listObjectsResponse.contents().forEach(s3Object -> {
            ResponseBytes<GetObjectResponse> objectResponse = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                    .bucket(xhibitConfiguration.getS3DataBucketName())
                    .key(s3Object.key())
                    .build());

            String trialDataXml = objectResponse.asUtf8String();

            results.add(XhibitRecordSheetDTO.builder()
                .filename(s3Object.key().split(objectKeyPrefix)[1])
                .data(trialDataXml)
                .build());
        });

        if (!listObjectsResponse.isTruncated()) {
            allFilesRetrievedFromS3 = true;
        }

        continuationToken = listObjectsResponse.continuationToken();

        return results;
    }

    public void markFilesAsCompleted() {

    }

    public void markFilesAsErrored() {

    }

}
