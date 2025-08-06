package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import uk.gov.justice.laa.maat.scheduled.tasks.config.XhibitConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.XhibitRecordSheetDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;

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

    @BeforeEach
    void beforeEach() {
        when(xhibitConfiguration.getObjectKeyTrialPrefix()).thenReturn("trial");
        when(xhibitConfiguration.getS3DataBucketName()).thenReturn("bucket");
    }

    @Test
    void getRecordSheets_whenNoDataFound_thenReturnsEmptyList() {
        when(listObjectsV2Response.contents()).thenReturn(Collections.emptyList());
        when(s3Client.listObjectsV2(ArgumentMatchers.any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Response);

        List<XhibitRecordSheetDTO> recordSheetDtos = xhibitDataService.getRecordSheets(RecordSheetType.TRIAL);

        assertTrue(recordSheetDtos.isEmpty());
    }

    @Test
    void getRecordSheets_whenDataFound_thenReturnsRecordSheets() {
        when(listObjectsV2Response.contents()).thenReturn(List.of(
            S3Object.builder().key("trial/file1.xml").build(),
            S3Object.builder().key("trial/file2.xml").build()
        ));
        when(s3Client.listObjectsV2(ArgumentMatchers.any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Response);

        List<XhibitRecordSheetDTO> expectedRecordSheets = List.of(
            XhibitRecordSheetDTO.builder()
                .filename("file1.xml")
                .data("<NS1:TrialRecordSheet xmlns:NS1=\"http://www.courtservice.gov.uk/schemas/courtservice\"><NS1:DocumentID><NS1:DocumentName>TR Alice Bloggs</NS1:DocumentName></NS1:DocumentID></NS1:TrialRecordSheet>")
                .build(),
            XhibitRecordSheetDTO.builder()
                .filename("file2.xml")
                .data("<NS1:TrialRecordSheet xmlns:NS1=\"http://www.courtservice.gov.uk/schemas/courtservice\"><NS1:DocumentID><NS1:DocumentName>TR Joe Bloggs</NS1:DocumentName></NS1:DocumentID></NS1:TrialRecordSheet>")
                .build()
        );

        // TODO: Need to set-up the individual requests - may need a custom arg matcher.
        when(s3Client.getObjectAsBytes()).thenReturn(expectedRecordSheets.get(0).getData().getBytes(
                StandardCharsets.UTF_8);
        when(s3Client.getObjectAsBytes()).thenReturn(expectedRecordSheets.get(1).getData().getBytes(
            StandardCharsets.UTF_8);

        List<XhibitRecordSheetDTO> actualRecordSheets = xhibitDataService.getRecordSheets(RecordSheetType.TRIAL);

        assertEquals(expectedRecordSheets, actualRecordSheets);
    }

}
