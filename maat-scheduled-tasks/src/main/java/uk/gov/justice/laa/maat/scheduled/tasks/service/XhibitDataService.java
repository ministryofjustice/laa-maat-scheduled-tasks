package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.InvalidObjectStateException;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;
import uk.gov.justice.laa.maat.scheduled.tasks.config.XhibitConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO.XhibitRecordSheetDTOBuilder;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetStatus;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.XhibitDataServiceException;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

import java.text.MessageFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class XhibitDataService {

    private final S3Client s3Client;

    private final XhibitConfiguration xhibitConfiguration;

    public GetRecordSheetsResponse getAllRecordSheets(RecordSheetType recordSheetType) {
        GetRecordSheetsResponse recordSheetsResponse = new GetRecordSheetsResponse();
        String objectKeyPrefix = getPrefixString(recordSheetType);

        do {
            recordSheetsResponse = buildRecordSheetsResponse(recordSheetsResponse, objectKeyPrefix);
        } while (!recordSheetsResponse.allRecordSheetsRetrieved());

        return recordSheetsResponse;
    }

    private GetRecordSheetsResponse buildRecordSheetsResponse(GetRecordSheetsResponse recordSheetsResponse, String objectKeyPrefix) {
        String continuationToken = recordSheetsResponse.getContinuationToken();
        ListObjectsV2Request.Builder listObjectsRequestBuilder = ListObjectsV2Request.builder()
            .bucket(xhibitConfiguration.getS3DataBucketName())
            .maxKeys(xhibitConfiguration.getS3PageSize())
            .prefix(objectKeyPrefix);

        if (continuationToken != null) {
            listObjectsRequestBuilder.continuationToken(continuationToken);
        }

        ListObjectsV2Request listObjectsV2Request = listObjectsRequestBuilder.build();

        try {
            ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsV2Request);
            List<S3Object> contents = listObjectsResponse.contents();

            if (contents.isEmpty()) {
                recordSheetsResponse.allRecordSheetsRetrieved(true);
                return recordSheetsResponse;
            }

            contents.stream().map(S3Object::key).forEach(key -> {
                String filename = key.substring(objectKeyPrefix.length());
                XhibitRecordSheetDTOBuilder recordSheetDTOBuilder = XhibitRecordSheetDTO.builder()
                    .filename(filename);

                try {
                    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(xhibitConfiguration.getS3DataBucketName())
                        .key(key)
                        .build();

                    String data = s3Client.getObjectAsBytes(getObjectRequest).asUtf8String();
                    recordSheetDTOBuilder.data(data);

                    recordSheetsResponse.getRetrievedRecordSheets().add(recordSheetDTOBuilder.build());
                } catch (NoSuchKeyException | InvalidObjectStateException ex) {
                    recordSheetsResponse.getErroredRecordSheets().add(recordSheetDTOBuilder.build());
                }
            });

            recordSheetsResponse.allRecordSheetsRetrieved(!listObjectsResponse.isTruncated());
            recordSheetsResponse.setContinuationToken(listObjectsResponse.nextContinuationToken());

            return recordSheetsResponse;
        } catch (SdkClientException | AwsServiceException ex) {
            log.error("AWS S3 error: {}", ex.getMessage());

            throw new XhibitDataServiceException(ex.getMessage());
        }
    }

    public void markRecordSheetsAsProcessed(List<XhibitRecordSheetDTO> recordSheets, RecordSheetType recordSheetType) {
        List<String> filenames = recordSheets.stream().map(XhibitRecordSheetDTO::getFilename).toList();
        renameRecordSheets(filenames, recordSheetType, RecordSheetStatus.PROCESSED);
    }

    public void markRecordSheetsAsErrored(List<XhibitRecordSheetDTO> recordSheets, RecordSheetType recordSheetType) {
        List<String> filenames = recordSheets.stream().map(XhibitRecordSheetDTO::getFilename).toList();
        renameRecordSheets(filenames, recordSheetType, RecordSheetStatus.ERRORED);
    }

    private void renameRecordSheets(List<String> filenames,
        RecordSheetType recordSheetType, RecordSheetStatus recordSheetStatus) {
        String sourceKeyPrefix = getPrefixString(recordSheetType);
        String destinationKeyPrefix = getPrefixString(recordSheetType, recordSheetStatus);

        try {
            filenames.forEach(filename -> {
                CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .sourceBucket(xhibitConfiguration.getS3DataBucketName())
                    .sourceKey(sourceKeyPrefix + filename)
                    .destinationBucket(xhibitConfiguration.getS3DataBucketName())
                    .destinationKey(destinationKeyPrefix + filename)
                    .build();

                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(xhibitConfiguration.getS3DataBucketName())
                    .key(sourceKeyPrefix + filename)
                    .build();

                CopyObjectResponse copyObjectResponse = s3Client.copyObject(copyObjectRequest);
                if (copyObjectResponse == null || copyObjectResponse.copyObjectResult() == null
                    || !StringUtils.hasLength( copyObjectResponse.copyObjectResult().eTag())) {
                    log.warn("Failed to copy record sheet {} with source key {}, skipping delete", filename, copyObjectRequest.sourceKey());

                    return;
                }

                DeleteObjectResponse deleteObjectResponse = s3Client.deleteObject(deleteObjectRequest);

                if (!deleteObjectResponse.sdkHttpResponse().isSuccessful()) {
                    log.warn("Failed to delete record sheet {} with source key {}", filename, deleteObjectRequest.key());
                }
            });
        } catch (SdkClientException | AwsServiceException ex) {
            throw new XhibitDataServiceException(ex.getMessage());
        }
    }

    private String getPrefixString(RecordSheetType type) {
        return type == RecordSheetType.TRIAL ? xhibitConfiguration.getObjectKeyTrialPrefix()
            : xhibitConfiguration.getObjectKeyAppealPrefix();
    }

    private String getPrefixString(RecordSheetType type, RecordSheetStatus status) {
        String processedStatus = status == RecordSheetStatus.PROCESSED
            ? xhibitConfiguration.getObjectKeyProcessedPrefix()
            : xhibitConfiguration.getObjectKeyErroredPrefix();

        return MessageFormat.format("{0}/{1}/", processedStatus, type.toString().toLowerCase());
    }
}
