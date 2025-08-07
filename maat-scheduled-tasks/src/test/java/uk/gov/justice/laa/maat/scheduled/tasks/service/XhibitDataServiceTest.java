package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import uk.gov.justice.laa.maat.scheduled.tasks.config.XhibitConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.matchers.GetObjectRequestArgumentMatcher;
import uk.gov.justice.laa.maat.scheduled.tasks.matchers.ListObjectsV2RequestArgumentMatcher;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.GetRecordSheetsResponse;

@ExtendWith(MockitoExtension.class)
class XhibitDataServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    ListObjectsV2Request listObjectsV2Request;

    @Mock
    ListObjectsV2Response listObjectsV2Response;

    @Mock
    XhibitConfiguration xhibitConfiguration;

    @InjectMocks
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
        when(xhibitConfiguration.getObjectKeyTrialPrefix()).thenReturn("trial/");
        when(xhibitConfiguration.getS3DataBucketName()).thenReturn("bucket");
    }

    @Test
    void getRecordSheets_whenNoDataFound_thenReturnsEmptyList() {
        when(listObjectsV2Response.contents()).thenReturn(Collections.emptyList());
        when(s3Client.listObjectsV2(ArgumentMatchers.any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Response);

        GetRecordSheetsResponse recordSheetsResponse = xhibitDataService.getRecordSheets(RecordSheetType.TRIAL);

        assertTrue(recordSheetsResponse.getRetrievedRecordSheets().isEmpty());
    }

    @Test
    void getRecordSheets_whenDataFound_thenReturnsRecordSheets() {
        when(listObjectsV2Response.contents()).thenReturn(List.of(
            S3Object.builder().key("trial/file1.xml").build(),
            S3Object.builder().key("trial/file2.xml").build()
        ));
        when(listObjectsV2Response.isTruncated()).thenReturn(true);
        when(listObjectsV2Response.continuationToken()).thenReturn("test-continuation-token");
        when(s3Client.listObjectsV2(ArgumentMatchers.any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Response);

        List<XhibitRecordSheetDTO> expectedRecordSheets = List.of(xhibitRecordSheet1, xhibitRecordSheet2);

        setupFileResponse(xhibitRecordSheet1, "trial/file1.xml");
        setupFileResponse(xhibitRecordSheet2, "trial/file2.xml");

        GetRecordSheetsResponse recordSheetsResponse = xhibitDataService.getRecordSheets(RecordSheetType.TRIAL);

        assertEquals(expectedRecordSheets, recordSheetsResponse.getRetrievedRecordSheets());
    }

    @Test
    void getRecordSheets_whenContinuationTokenExists_thenReturnsNextPageOfRecordSheets() {
        when(listObjectsV2Response.contents()).thenReturn(List.of(
            S3Object.builder().key("trial/file1.xml").build()
        ));
        when(listObjectsV2Response.isTruncated()).thenReturn(true);
        when(listObjectsV2Response.continuationToken()).thenReturn("test-continuation-token");
        when(s3Client.listObjectsV2(ArgumentMatchers.any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Response);

        List<XhibitRecordSheetDTO> expectedRecordSheets = List.of(xhibitRecordSheet1);

        setupFileResponse(xhibitRecordSheet1, "trial/file1.xml");

        GetRecordSheetsResponse recordSheetsResponse = xhibitDataService.getRecordSheets(RecordSheetType.TRIAL);

        assertEquals(expectedRecordSheets, recordSheetsResponse.getRetrievedRecordSheets());
        assertThat(xhibitDataService.isAllFilesRetrieved()).isFalse();

        ListObjectsV2Response nextPageResponse = mock(ListObjectsV2Response.class);

        when(nextPageResponse.contents()).thenReturn(List.of(
            S3Object.builder().key("trial/file2.xml").build()
        ));
        when(nextPageResponse.isTruncated()).thenReturn(false);
        when(nextPageResponse.continuationToken()).thenReturn(null);
        when(s3Client.listObjectsV2(argThat(new ListObjectsV2RequestArgumentMatcher("test-continuation-token")))).thenReturn(nextPageResponse);

        expectedRecordSheets = List.of(xhibitRecordSheet2);

        setupFileResponse(xhibitRecordSheet2, "trial/file2.xml");

        recordSheetsResponse = xhibitDataService.getRecordSheets(RecordSheetType.TRIAL);

        assertEquals(expectedRecordSheets, recordSheetsResponse.getRetrievedRecordSheets());
        assertThat(xhibitDataService.isAllFilesRetrieved()).isTrue();
    }

    private void setupFileResponse(XhibitRecordSheetDTO xhibitRecordSheetDTO, String objectKey) {
        ResponseBytes<GetObjectResponse> fileObjectResponse = ResponseBytes.fromByteArray(
            mock(GetObjectResponse.class),
            xhibitRecordSheetDTO.getData().getBytes(StandardCharsets.UTF_8)
        );

        doReturn(fileObjectResponse).when(s3Client).getObjectAsBytes(argThat(new GetObjectRequestArgumentMatcher(objectKey)));
    }

}
