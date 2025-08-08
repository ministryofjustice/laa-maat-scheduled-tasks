package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.InvalidObjectStateException;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectNotInActiveTierErrorException;
import uk.gov.justice.laa.maat.scheduled.tasks.config.XhibitConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetStatus;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.XhibitDataServiceException;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class XhibitDataService {

    private final S3Client s3Client;

    private final XhibitConfiguration xhibitConfiguration;

    @Accessors(fluent = true)
    @Getter
    private boolean allRecordSheetsRetrieved;

    private String continuationToken;

    public GetRecordSheetsResponse getRecordSheets(RecordSheetType recordSheetType) {
        try {
            GetRecordSheetsResponse recordSheetsResponse = new GetRecordSheetsResponse();

            String objectKeyPrefix = getPrefixString(recordSheetType);

            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(xhibitConfiguration.getS3DataBucketName())
                .prefix(objectKeyPrefix)
                .continuationToken(continuationToken)
                .build();

            ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

            if (listObjectsResponse.contents().isEmpty()) {
                return recordSheetsResponse;
            }

            listObjectsResponse.contents().forEach(s3Object -> {
                try {
                    ResponseBytes<GetObjectResponse> objectResponse = s3Client.getObjectAsBytes(
                        GetObjectRequest.builder()
                            .bucket(xhibitConfiguration.getS3DataBucketName())
                            .key(s3Object.key())
                            .build());

                    String trialDataXml = objectResponse.asUtf8String();

                    recordSheetsResponse.getRetrievedRecordSheets()
                        .add(XhibitRecordSheetDTO.builder()
                            .filename(s3Object.key().split(objectKeyPrefix)[1])
                            .data(trialDataXml)
                            .build());
                } catch (NoSuchKeyException | InvalidObjectStateException ex) {
                    recordSheetsResponse.getErroredRecordSheets().add(XhibitRecordSheetDTO.builder()
                        .filename(s3Object.key().split(objectKeyPrefix)[1])
                        .build());
                }

            });

            if (!listObjectsResponse.isTruncated()) {
                allRecordSheetsRetrieved = true;
            }

            continuationToken = listObjectsResponse.continuationToken();

            return recordSheetsResponse;
        } catch (SdkClientException | AwsServiceException ex) {
            throw new XhibitDataServiceException(ex.getMessage());
        }
    }

    public void markRecordsSheetsAsProcessed(List<XhibitRecordSheetDTO> recordSheets, RecordSheetType recordSheetType) {
        renameRecordSheets(recordSheets, recordSheetType, RecordSheetStatus.PROCESSED);
    }

    public void markRecordSheetsAsErrored(List<XhibitRecordSheetDTO> recordSheets, RecordSheetType recordSheetType) {
        renameRecordSheets(recordSheets, recordSheetType, RecordSheetStatus.ERRORED);
    }

    private void renameRecordSheets(List<XhibitRecordSheetDTO> recordSheets,
        RecordSheetType recordSheetType, RecordSheetStatus recordSheetStatus) {
        String sourceKeyPrefix = getPrefixString(recordSheetType);
        String destinationKeyPrefix = getPrefixString(recordSheetType, recordSheetStatus);

        log.info(String.format("Assigning {} record sheets the prefix: {}", recordSheetStatus,
            destinationKeyPrefix));

        try {
            recordSheets.forEach(recordSheet -> {
                String fileName = recordSheet.getFilename();

                CopyObjectRequest copyObjectrequest = CopyObjectRequest.builder()
                    .sourceBucket(xhibitConfiguration.getS3DataBucketName())
                    .sourceKey(sourceKeyPrefix + fileName)
                    .destinationBucket(xhibitConfiguration.getS3DataBucketName())
                    .destinationKey(destinationKeyPrefix + fileName)
                    .build();

                try {
                    s3Client.copyObject(copyObjectrequest);
                } catch (ObjectNotInActiveTierErrorException ex) {
                    log.warn("One of the record sheets is in Amazon S3 Glacier.");
                }

                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(xhibitConfiguration.getS3DataBucketName())
                    .key(sourceKeyPrefix + fileName)
                    .build();

                s3Client.deleteObject(deleteObjectRequest);
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

        return MessageFormat.format("{0}/{1}/", type.toString().toLowerCase(), processedStatus);
    }
}
