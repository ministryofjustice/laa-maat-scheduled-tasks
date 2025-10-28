package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import java.io.UncheckedIOException;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.InvalidObjectStateException;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.config.XhibitConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.RecordSheet;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.RecordSheetsPage;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetStatus;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.exception.XhibitDataServiceException;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.helper.ObjectKeyHelper;

@Slf4j
@Service
@RequiredArgsConstructor
public class XhibitS3Service {

    private final S3Client s3Client;
    private final ObjectKeyHelper objectKeyHelper;
    private final XhibitConfiguration xhibitConfiguration;

    public RecordSheetsPage getRecordSheets(RecordSheetType recordSheetType) {
        RecordSheetsPage page = RecordSheetsPage.empty();
        String prefix = objectKeyHelper.buildPrefix(recordSheetType);
        log.debug("Prefix = '{}'", prefix);

        do {
            page = fetchPage(page.continuationToken(), prefix, page);
        } while (!page.complete());

        return page;
    }

    private RecordSheetsPage fetchPage(String continuationToken, String prefix,
            RecordSheetsPage accumulator) {
        log.debug("Got here 1");
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(xhibitConfiguration.getS3DataBucketName())
                .maxKeys(Integer.parseInt(xhibitConfiguration.getFetchSize()))
                .prefix(prefix);
      log.debug("Got here 2");
        if (continuationToken != null) {
            requestBuilder.continuationToken(continuationToken);
        }

        try {
            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
          log.debug("Got here 3");
            if (response.contents().isEmpty()) {
                return accumulator.next(null, true);
            }

            List<RecordSheet> errored = new ArrayList<>();
            List<RecordSheet> retrieved = new ArrayList<>();
          log.debug("Got here 4");

            response.contents().stream().map(S3Object::key).forEach(key -> {
                log.debug("Got here 5, Key = '{}'", key);
                String filename = key.substring(prefix.length());
                log.debug("Got here 6, Filename = '{}'", filename);
                RecordSheet.RecordSheetBuilder builder = RecordSheet.builder()
                        .filename(filename);

                try {
                    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                            .bucket(xhibitConfiguration.getS3DataBucketName())
                            .key(key)
                            .build();
                    log.debug("Got here 7, S3 Bucket name = '{}'", xhibitConfiguration.getS3DataBucketName());
                  log.debug("Got here 8, key = '{}'", getObjectRequest.key());
                  ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(
                      getObjectRequest);
                  log.debug("Got here 9");
                  String data = objectAsBytes.asUtf8String();
                  log.debug("Got here 9A, data = '{}'", data);
                    builder.data(data);
                    retrieved.add(builder.build());
                } catch (NoSuchKeyException | InvalidObjectStateException ex) {
                    log.error("Failed to retrieve object from S3. key={}", key, ex);
                    errored.add(builder.build());
                } catch (UncheckedIOException ue) {
                  log.error("Failed to process object. key={}", key, ue);
                  if (ue.getCause().getClass().equals(CharacterCodingException.class)) {
                    errored.add(builder.build());
                  }
                }
            });
          log.debug("Got here 10, retrieved = {}, errored = {}", retrieved.size(), errored.size());
            return accumulator
                    .withErrored(errored)
                    .withRetrieved(retrieved)
                    .next(response.nextContinuationToken(), !response.isTruncated());
        } catch (SdkClientException | AwsServiceException ex) {
            log.error("AWS S3 error: {}", ex.getMessage());
            throw new XhibitDataServiceException(ex.getMessage());
        }
    }

    public void markProcessed(List<String> filenames, RecordSheetType recordSheetType) {
        move(filenames, recordSheetType, RecordSheetStatus.PROCESSED);
    }

    public void markErrored(List<String> filenames, RecordSheetType recordSheetType) {
        move(filenames, recordSheetType, RecordSheetStatus.ERRORED);
    }

    private void move(List<String> filenames,
            RecordSheetType recordSheetType, RecordSheetStatus recordSheetStatus) {
        String sourceKeyPrefix = objectKeyHelper.buildPrefix(recordSheetType);
        String destinationKeyPrefix =
                objectKeyHelper.buildPrefix(recordSheetType, recordSheetStatus);

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
                        || !StringUtils.hasLength(copyObjectResponse.copyObjectResult().eTag())) {
                    log.warn("Failed to copy record sheet {} with source key {}, skipping delete",
                            filename, copyObjectRequest.sourceKey());
                    return;
                }

                DeleteObjectResponse deleteObjectResponse = s3Client.deleteObject(
                        deleteObjectRequest);
                if (!s3DeleteWasSuccessful(deleteObjectResponse)) {
                    log.warn("Failed to delete record sheet {} with source key {}", filename,
                            deleteObjectRequest.key());
                }
            });
        } catch (SdkClientException | AwsServiceException ex) {
            throw new XhibitDataServiceException(ex.getMessage());
        }
    }

    public boolean s3DeleteWasSuccessful(DeleteObjectResponse response) {
        return response.sdkHttpResponse() == null || response.sdkHttpResponse().isSuccessful();
    }
}
