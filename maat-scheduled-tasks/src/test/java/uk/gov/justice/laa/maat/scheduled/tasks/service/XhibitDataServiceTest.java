package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.CopyObjectResult;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.InvalidObjectStateException;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import uk.gov.justice.laa.maat.scheduled.tasks.config.XhibitConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.XhibitDataServiceException;
import uk.gov.justice.laa.maat.scheduled.tasks.matchers.CopyObjectRequestArgumentMatcher;
import uk.gov.justice.laa.maat.scheduled.tasks.matchers.DeleteObjectRequestArgumentMatcher;
import uk.gov.justice.laa.maat.scheduled.tasks.matchers.GetObjectRequestArgumentMatcher;
import uk.gov.justice.laa.maat.scheduled.tasks.matchers.ListObjectsV2RequestArgumentMatcher;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.util.ObjectKeyHelper;

@ExtendWith(MockitoExtension.class)
class XhibitDataServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    ListObjectsV2Response listObjectsV2Response;

    @Mock
    XhibitConfiguration xhibitConfiguration;

    private XhibitDataService xhibitDataService;

    private final XhibitRecordSheetDTO xhibitRecordSheet1 = XhibitRecordSheetDTO.builder()
        .filename("file1.xml")
        .data("<NS1:TrialRecordSheet xmlns:NS1=\"http://www.courtservice.gov.uk/schemas/courtservice\"><NS1:DocumentID><NS1:DocumentName>TR Alice Bloggs</NS1:DocumentName></NS1:DocumentID></NS1:TrialRecordSheet>")
        .build();

    private final XhibitRecordSheetDTO xhibitRecordSheet2 = XhibitRecordSheetDTO.builder()
        .filename("file2.xml")
        .data("<NS1:TrialRecordSheet xmlns:NS1=\"http://www.courtservice.gov.uk/schemas/courtservice\"><NS1:DocumentID><NS1:DocumentName>TR Joe Bloggs</NS1:DocumentName></NS1:DocumentID></NS1:TrialRecordSheet>")
        .build();

    @BeforeEach
    void beforeEach() {
        lenient().when(xhibitConfiguration.getObjectKeyTrialPrefix()).thenReturn("trial/");
        lenient().when(xhibitConfiguration.getObjectKeyProcessedPrefix()).thenReturn("processed");
        lenient().when(xhibitConfiguration.getObjectKeyErroredPrefix()).thenReturn("errored");
        lenient().when(xhibitConfiguration.getS3DataBucketName()).thenReturn("bucket");
        lenient().when(xhibitConfiguration.getFetchSize()).thenReturn("1");

        ObjectKeyHelper objectKeyHelper = new ObjectKeyHelper(xhibitConfiguration);
        xhibitDataService = new XhibitDataService(s3Client, objectKeyHelper, xhibitConfiguration);
    }

    @Test
    void givenNoRecordSheetsExist_whenGetRecordSheetsResponseIsInvoked_thenNoRecordSheetsReturned() {
        when(listObjectsV2Response.contents()).thenReturn(Collections.emptyList());
        when(s3Client.listObjectsV2(ArgumentMatchers.any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Response);

        GetRecordSheetsResponse recordSheetsResponse = xhibitDataService.getAllRecordSheets(RecordSheetType.TRIAL);

        assertTrue(recordSheetsResponse.getRetrievedRecordSheets().isEmpty());
        assertTrue(recordSheetsResponse.getErroredRecordSheets().isEmpty());
        assertTrue(recordSheetsResponse.allRecordSheetsRetrieved());
    }

    @Test
    void givenRecordSheetsExist_whenGetRecordSheetsResponseIsInvoked_thenRecordSheetsAreReturned() {
        when(listObjectsV2Response.contents()).thenReturn(List.of(
            S3Object.builder().key("trial/file1.xml").build(),
            S3Object.builder().key("trial/file2.xml").build()
        ));
        when(listObjectsV2Response.isTruncated()).thenReturn(false);
        when(listObjectsV2Response.nextContinuationToken()).thenReturn(null);
        when(s3Client.listObjectsV2(ArgumentMatchers.any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Response);

        List<XhibitRecordSheetDTO> expectedRecordSheets = List.of(xhibitRecordSheet1, xhibitRecordSheet2);

        setupFileResponse(xhibitRecordSheet1, "trial/file1.xml");
        setupFileResponse(xhibitRecordSheet2, "trial/file2.xml");

        GetRecordSheetsResponse recordSheetsResponse = xhibitDataService.getAllRecordSheets(RecordSheetType.TRIAL);

        assertEquals(expectedRecordSheets, recordSheetsResponse.getRetrievedRecordSheets());
    }

    @Test
    void givenRecordSheetsExistThatCannotBeRetrieved_whenGetRecordSheetsResponseIsInvoked_thenPartialRecordSheetsReturned() {
        when(listObjectsV2Response.contents()).thenReturn(List.of(
            S3Object.builder().key("trial/file1.xml").build(),
            S3Object.builder().key("trial/file2.xml").build(),
            S3Object.builder().key("trial/file3.xml").build()
        ));
        when(listObjectsV2Response.isTruncated()).thenReturn(false);
        when(listObjectsV2Response.nextContinuationToken()).thenReturn(null);
        when(s3Client.listObjectsV2(ArgumentMatchers.any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Response);

        List<XhibitRecordSheetDTO> expectedSuccessfulRecordSheets = List.of(xhibitRecordSheet2);
        List<String> expectedErroredRecordSheets = List.of("file1.xml", "file3.xml");

        setupErrorFileResponse("trial/file1.xml");
        setupFileResponse(xhibitRecordSheet2, "trial/file2.xml");
        setupErrorFileResponse("trial/file3.xml");

        GetRecordSheetsResponse recordSheetsResponse = xhibitDataService.getAllRecordSheets(RecordSheetType.TRIAL);

        assertEquals(expectedSuccessfulRecordSheets, recordSheetsResponse.getRetrievedRecordSheets());
        assertEquals(expectedErroredRecordSheets, recordSheetsResponse.getErroredRecordSheets().stream().map(
            XhibitRecordSheetDTO::getFilename).toList());
    }

    @Test
    void givenUnhandledAwsExceptionThrown_whenGetRecordSheetsResponseIsInvoked_thenXhibitDataServiceExceptionIsThrown() {
        doThrow(SdkClientException.class).when(s3Client).listObjectsV2(ArgumentMatchers.any(ListObjectsV2Request.class));

        assertThrows(XhibitDataServiceException.class, () -> xhibitDataService.getAllRecordSheets(RecordSheetType.TRIAL));
    }

    @Test
    void givenMultiplePagesOfRecordSheets_whenGetRecordSheetsResponseIsInvoked_thenAllRecordSheetsReturned() {
        ListObjectsV2Response firstResponse = ListObjectsV2Response.builder()
            .contents(List.of(S3Object.builder().key("trial/file1.xml").build()))
            .isTruncated(true)
            .nextContinuationToken("test-continuation-token")
            .build();

        ListObjectsV2Response secondResponse = ListObjectsV2Response.builder()
            .contents(List.of(S3Object.builder().key("trial/file2.xml").build()))
            .isTruncated(false)
            .nextContinuationToken(null)
            .build();

        doReturn(firstResponse).when(s3Client).listObjectsV2(argThat(new ListObjectsV2RequestArgumentMatcher(null)));
        doReturn(secondResponse).when(s3Client).listObjectsV2(argThat(new ListObjectsV2RequestArgumentMatcher("test-continuation-token")));

        List<XhibitRecordSheetDTO> expectedSuccessfulRecordSheets = List.of(xhibitRecordSheet2);
        List<String> expectedErroredRecordSheets = List.of("file1.xml");

        setupErrorFileResponse("trial/file1.xml");
        setupFileResponse(xhibitRecordSheet2, "trial/file2.xml");

        GetRecordSheetsResponse recordSheetsResponse = xhibitDataService.getAllRecordSheets(RecordSheetType.TRIAL);

        assertEquals(expectedSuccessfulRecordSheets, recordSheetsResponse.getRetrievedRecordSheets());
        assertEquals(expectedErroredRecordSheets, recordSheetsResponse.getErroredRecordSheets().stream().map(
            XhibitRecordSheetDTO::getFilename).toList());

        verify(s3Client, times(2)).listObjectsV2(ArgumentMatchers.any(ListObjectsV2Request.class));
    }

    @Test
    void givenNoRecordSheets_whenMarkRecordSheetsAsProcessedIsInvoked_thenNoRecordSheetsAreUpdated() {
        xhibitDataService.markRecordSheetsAsProcessed(Collections.emptyList(), RecordSheetType.TRIAL);

        verify(s3Client, never()).copyObject(ArgumentMatchers.any(CopyObjectRequest.class));
        verify(s3Client, never()).deleteObject(ArgumentMatchers.any(DeleteObjectRequest.class));
    }

    @Test
    void givenRecordSheets_whenMarkRecordSheetsAsProcessedIsInvoked_thenRecordSheetsAreUpdated() {
        setupCopyResponse("trial/file1.xml", "processed/trial/file1.xml", true);
        setupCopyResponse("trial/file2.xml", "processed/trial/file2.xml", true);
        setupDeleteResponse();

        xhibitDataService.markRecordSheetsAsProcessed(List.of(xhibitRecordSheet1.getFilename(), xhibitRecordSheet2.getFilename()), RecordSheetType.TRIAL);

        verify(s3Client, times(1)).copyObject(argThat(
            new CopyObjectRequestArgumentMatcher("trial/file1.xml", "processed/trial/file1.xml")));
        verify(s3Client, times(1)).copyObject(argThat(
            new CopyObjectRequestArgumentMatcher("trial/file2.xml", "processed/trial/file2.xml")));
        verify(s3Client, times(1)).deleteObject(argThat(
            new DeleteObjectRequestArgumentMatcher("trial/file1.xml")));
        verify(s3Client, times(1)).deleteObject(argThat(
            new DeleteObjectRequestArgumentMatcher("trial/file2.xml")));
    }

    @Test
    void givenErrorCopyingOneFile_whenMarkRecordSheetsAsProcessedIsInvoked_thenPartialRecordSheetsAreUpdated() {
        setupCopyResponse("trial/file1.xml", "processed/trial/file1.xml", false);
        setupCopyResponse("trial/file2.xml", "processed/trial/file2.xml", true);
        setupDeleteResponse();

        xhibitDataService.markRecordSheetsAsProcessed(List.of(xhibitRecordSheet1.getFilename(), xhibitRecordSheet2.getFilename()), RecordSheetType.TRIAL);

        verify(s3Client, times(2)).copyObject(ArgumentMatchers.any(CopyObjectRequest.class));
        verify(s3Client, never()).deleteObject(argThat(
            new DeleteObjectRequestArgumentMatcher("trial/file1.xml")));
        verify(s3Client, times(1)).deleteObject(argThat(
            new DeleteObjectRequestArgumentMatcher("trial/file2.xml")));
    }

    @Test
    void markRecordSheetsAsProcessed_throwsException_whenUnhandledAwsExceptionOccurs() {
        setupCopyResponse("trial/file1.xml", "processed/trial/file1.xml", true);
        doThrow(SdkClientException.class).when(s3Client).deleteObject(ArgumentMatchers.any(DeleteObjectRequest.class));

        assertThrows(XhibitDataServiceException.class, () -> xhibitDataService.markRecordSheetsAsProcessed(List.of(xhibitRecordSheet1.getFilename()), RecordSheetType.TRIAL));
    }

    private void setupCopyResponse(String sourceKey, String destinationKey, boolean successful) {
        String eTag = successful ? "success" : null;

        CopyObjectResponse copyObjectResponse = CopyObjectResponse.builder()
            .copyObjectResult(CopyObjectResult.builder().eTag(eTag).build())
            .build();

        doReturn(copyObjectResponse).when(s3Client).copyObject(argThat(new CopyObjectRequestArgumentMatcher(sourceKey, destinationKey)));
    }

    private void setupDeleteResponse() {
        doReturn(DeleteObjectResponse.builder()
            .sdkHttpResponse(SdkHttpResponse.builder().statusCode(204).build())
            .build()).when(s3Client).deleteObject(ArgumentMatchers.any(DeleteObjectRequest.class));
    }

    private void setupFileResponse(XhibitRecordSheetDTO xhibitRecordSheetDTO, String objectKey) {
        ResponseBytes<GetObjectResponse> fileObjectResponse = ResponseBytes.fromByteArray(
            mock(GetObjectResponse.class),
            xhibitRecordSheetDTO.getData().getBytes(StandardCharsets.UTF_8)
        );

        doReturn(fileObjectResponse).when(s3Client).getObjectAsBytes(argThat(new GetObjectRequestArgumentMatcher(objectKey)));
    }

    private void setupErrorFileResponse(String objectKey) {
        doThrow(InvalidObjectStateException.class).when(s3Client).getObjectAsBytes(argThat(new GetObjectRequestArgumentMatcher(objectKey)));
    }

}
