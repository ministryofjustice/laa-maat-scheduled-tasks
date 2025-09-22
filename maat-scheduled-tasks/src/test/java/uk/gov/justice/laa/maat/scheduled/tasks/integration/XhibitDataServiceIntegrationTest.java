package uk.gov.justice.laa.maat.scheduled.tasks.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.config.LocalstackS3TestConfig;
import uk.gov.justice.laa.maat.scheduled.tasks.config.XhibitConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitAppealDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetStatus;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.StoredProcedure;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitAppealDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitTrialDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.service.AppealDataService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.StoredProcedureService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.TrialDataService;
import uk.gov.justice.laa.maat.scheduled.tasks.util.ObjectKeyHelper;

@SpringBootTest
@Testcontainers
@ActiveProfiles("integration")
@TestInstance(Lifecycle.PER_CLASS)
@Import(LocalstackS3TestConfig.class)
@EnableConfigurationProperties(XhibitConfiguration.class)
class XhibitDataServiceIntegrationTest {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private XhibitConfiguration xhibitConfiguration;

    @Autowired
    private TrialDataService trialDataService;

    @Autowired
    private AppealDataService appealDataService;

    @Autowired
    private XhibitTrialDataRepository trialDataRepository;

    @Autowired
    private XhibitAppealDataRepository appealDataRepository;

    @Autowired
    private ObjectKeyHelper objectKeyHelper;

    @MockitoBean
    private StoredProcedureService storedProcedureService;

    @BeforeAll
    void setUp() {
        s3Client.createBucket(CreateBucketRequest.builder()
                .bucket(xhibitConfiguration.getS3DataBucketName()).build());
    }

    @BeforeEach
    void cleanup() {
        ListObjectsV2Response listObjects = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(xhibitConfiguration.getS3DataBucketName())
                        .build()
        );

        listObjects.contents().forEach(s3Object ->
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(xhibitConfiguration.getS3DataBucketName())
                        .key(s3Object.key())
                        .build())
        );

        trialDataRepository.deleteAll();
        appealDataRepository.deleteAll();
    }


    @Test
    void givenTrialRecordSheet_whenProcessTrialDataIsInvoked_thenTrialDataIsProcessed() {
        String content = "Sample Trial content";
        String filename = "XHIBIT_TRLRS_21523512.xml";

        s3Client.putObject(
                PutObjectRequest.builder().bucket(xhibitConfiguration.getS3DataBucketName())
                        .key(objectKeyHelper.buildKey(RecordSheetType.TRIAL, filename)).build(),
                RequestBody.fromString(content));

        when(storedProcedureService.callStoredProcedure(eq(
                StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE), any()))
                .thenReturn(new StoredProcedureResponse(Collections.emptyList()));

        trialDataService.populateAndProcessTrialDataInToMaat();

        assertThatNoException().isThrownBy(() -> s3Client.getObject(
                GetObjectRequest.builder().bucket(xhibitConfiguration.getS3DataBucketName())
                        .key(objectKeyHelper.buildKey(RecordSheetType.TRIAL,
                                RecordSheetStatus.PROCESSED, filename)).build())
        );

        List<XhibitTrialDataEntity> recordSheets = trialDataRepository.findAll();
        assertThat(recordSheets)
                .hasSize(1)
                .first()
                .satisfies(sheet -> {
                    assertThat(sheet.getFilename()).isEqualTo(filename);
                    assertThat(sheet.getData()).isEqualTo(content);
                });
    }

    @Test
    void givenAppealRecordSheet_whenProcessTrialDataIsInvoked_thenTrialDataIsProcessed() {
        String content = "Sample appeal content";
        String filename = "XHIBIT_TRLRS_21947812.xml";

        s3Client.putObject(
                PutObjectRequest.builder().bucket(xhibitConfiguration.getS3DataBucketName())
                        .key(objectKeyHelper.buildKey(RecordSheetType.APPEAL, filename)).build(),
                RequestBody.fromString(content));

        when(storedProcedureService.callStoredProcedure(eq(
                StoredProcedure.APPEAL_DATA_TO_MAAT_PROCEDURE), any()))
                .thenReturn(new StoredProcedureResponse(Collections.emptyList()));

        appealDataService.populateAndProcessAppealDataInToMaat();

        assertThatNoException().isThrownBy(() -> s3Client.getObject(
                GetObjectRequest.builder().bucket(xhibitConfiguration.getS3DataBucketName())
                        .key(objectKeyHelper.buildKey(RecordSheetType.APPEAL,
                                RecordSheetStatus.PROCESSED, filename)).build())
        );

        List<XhibitAppealDataEntity> recordSheets = appealDataRepository.findAll();
        assertThat(recordSheets)
                .hasSize(1)
                .first()
                .satisfies(sheet -> {
                    assertThat(sheet.getFilename()).isEqualTo(filename);
                    assertThat(sheet.getData()).isEqualTo(content);
                });
    }

    @Test
    void givenNoTrialRecordSheets_whenProcessTrialDataIsInvoked_thenNoDataIsProcessed() {
        trialDataService.populateAndProcessTrialDataInToMaat();
        List<XhibitTrialDataEntity> recordSheets = trialDataRepository.findAll();
        assertThat(recordSheets).isEmpty();
    }

    @Test
    void givenNoAppealRecordSheets_whenProcessAppealDataIsInvoked_thenNoDataIsProcessed() {
        appealDataService.populateAndProcessAppealDataInToMaat();
        List<XhibitAppealDataEntity> recordSheets = appealDataRepository.findAll();
        assertThat(recordSheets).isEmpty();
    }

}
