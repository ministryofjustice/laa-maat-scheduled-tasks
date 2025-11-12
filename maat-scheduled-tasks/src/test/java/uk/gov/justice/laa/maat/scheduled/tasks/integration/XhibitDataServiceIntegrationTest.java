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
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.config.XhibitConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity.XhibitAppealDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetStatus;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.StoredProcedure;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.repository.XhibitAppealDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.repository.XhibitTrialDataRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service.AppealDataService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.StoredProcedureService;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service.TrialDataService;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.helper.ObjectKeyHelper;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service.XhibitItemService;

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

    @Autowired
    private XhibitItemService xhibitItemService;

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

        trialDataService.populateAndProcessData();

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

        appealDataService.populateAndProcessData();

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
        trialDataService.populateAndProcessData();
        List<XhibitTrialDataEntity> recordSheets = trialDataRepository.findAll();
        assertThat(recordSheets).isEmpty();
    }

    @Test
    void givenNoAppealRecordSheets_whenProcessAppealDataIsInvoked_thenNoDataIsProcessed() {
        appealDataService.populateAndProcessData();
        List<XhibitAppealDataEntity> recordSheets = appealDataRepository.findAll();
        assertThat(recordSheets).isEmpty();
    }

    @Test
    void givenSeveralTrialRecordSheetsInRandomChronologicalOrder_whenProcessTrialDataIsInvoked_thenTrialDataIsProcessedInCorrectOrder() {
        String content = "Sample Trial content";

        String[] filenames = {
                "XHIBIT_TRLRS_000013297453_20250521164303.xml",
                "XHIBIT_TRLRS_000077297453_20250420164303.xml",
                "XHIBIT_TRLRS_000013123453_20250403164303.xml",
                "XHIBIT_TRLRS_000013297477_20250521100503.xml",
                "XHIBIT_TRLRS_000013297488_20250522164303.xml",
                "XHIBIT_TRLRS_000013297424_20250521174302.xml"
        };

        for (String filename : filenames) {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(xhibitConfiguration.getS3DataBucketName())
                            .key(objectKeyHelper.buildKey(RecordSheetType.TRIAL, filename)).build(),
                    RequestBody.fromString(content));
        }

        when(storedProcedureService.callStoredProcedure(eq(
                StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE), any()))
                .thenReturn(new StoredProcedureResponse(Collections.emptyList()));

        trialDataService.populateAndProcessData();

        List<XhibitTrialDataEntity> recordSheets = trialDataRepository.findAll();
        assertThat(recordSheets)
                .hasSize(6);

        assertThat(recordSheets.get(0).getFilename()).isEqualTo("XHIBIT_TRLRS_000013123453_20250403164303.xml");
        assertThat(recordSheets.get(1).getFilename()).isEqualTo("XHIBIT_TRLRS_000077297453_20250420164303.xml");
        assertThat(recordSheets.get(2).getFilename()).isEqualTo("XHIBIT_TRLRS_000013297477_20250521100503.xml");
        assertThat(recordSheets.get(3).getFilename()).isEqualTo("XHIBIT_TRLRS_000013297453_20250521164303.xml");
        assertThat(recordSheets.get(4).getFilename()).isEqualTo("XHIBIT_TRLRS_000013297424_20250521174302.xml");
        assertThat(recordSheets.get(5).getFilename()).isEqualTo("XHIBIT_TRLRS_000013297488_20250522164303.xml");
    }

    @Test
    void givenSeveralAppealRecordSheetsInRandomChronologicalOrder_whenProcessTrialDataIsInvoked_thenTrialDataIsProcessedInCorrectOrder() {
        String content = "Sample Trial content";

        String[] filenames = {
                "XHIBIT_APLRS_007783297066_20250521150403.xml",
                "XHIBIT_APLRS_002013296692_20250521115927.xml",
                "XHIBIT_APLRS_003013296605_20250521110135.xml",
                "XHIBIT_APLRS_005513296328_20250520172136.xml",
                "XHIBIT_APLRS_005777296328_20250522172135.xml",
                "XHIBIT_APLRS_005222296328_20250520172135.xml"
        };

        for (String filename : filenames) {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(xhibitConfiguration.getS3DataBucketName())
                            .key(objectKeyHelper.buildKey(RecordSheetType.APPEAL, filename)).build(),
                    RequestBody.fromString(content));
        }

        when(storedProcedureService.callStoredProcedure(eq(
                StoredProcedure.APPEAL_DATA_TO_MAAT_PROCEDURE), any()))
                .thenReturn(new StoredProcedureResponse(Collections.emptyList()));

        appealDataService.populateAndProcessData();

        List<XhibitAppealDataEntity> recordSheets = appealDataRepository.findAll();
        assertThat(recordSheets)
                .hasSize(6);

        assertThat(recordSheets.get(0).getFilename()).isEqualTo("XHIBIT_APLRS_005222296328_20250520172135.xml");
        assertThat(recordSheets.get(1).getFilename()).isEqualTo("XHIBIT_APLRS_005513296328_20250520172136.xml");
        assertThat(recordSheets.get(2).getFilename()).isEqualTo("XHIBIT_APLRS_003013296605_20250521110135.xml");
        assertThat(recordSheets.get(3).getFilename()).isEqualTo("XHIBIT_APLRS_002013296692_20250521115927.xml");
        assertThat(recordSheets.get(4).getFilename()).isEqualTo("XHIBIT_APLRS_007783297066_20250521150403.xml");
        assertThat(recordSheets.get(5).getFilename()).isEqualTo("XHIBIT_APLRS_005777296328_20250522172135.xml");
    }

    @Test
    void givenSeveralInvalidlyNamedTrialRecordSheetsInRandomChronologicalOrder_whenProcessTrialDataIsInvoked_thenTrialDataIsProcessedInCorrectOrder() {
        String content = "Sample Trial content";

        String[] filenames = {
                "XHIBIT_TRLRS_000013297453_20250521164303.xml",
                "",
                "XHIBIT_TRLRS_000013123453_20250403164303.xml",
                "XHIB2110.xml",
                "blah",
                "XHIBIT_TRLRS_000013297424_20250521174302.xml"
        };

        for (String filename : filenames) {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(xhibitConfiguration.getS3DataBucketName())
                            .key(objectKeyHelper.buildKey(RecordSheetType.TRIAL, filename)).build(),
                    RequestBody.fromString(content));
        }

        when(storedProcedureService.callStoredProcedure(eq(
                StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE), any()))
                .thenReturn(new StoredProcedureResponse(Collections.emptyList()));

        trialDataService.populateAndProcessData();

        List<XhibitTrialDataEntity> recordSheets = trialDataRepository.findAll();
        assertThat(recordSheets)
                .hasSize(6);

        assertThat(recordSheets.get(0).getFilename()).isEqualTo("XHIBIT_TRLRS_000013123453_20250403164303.xml");
        assertThat(recordSheets.get(1).getFilename()).isEqualTo("XHIBIT_TRLRS_000013297453_20250521164303.xml");
        assertThat(recordSheets.get(2).getFilename()).isEqualTo("XHIBIT_TRLRS_000013297424_20250521174302.xml");
        assertThat(recordSheets.get(3).getFilename()).isEqualTo(null);
        assertThat(recordSheets.get(4).getFilename()).isEqualTo("XHIB2110.xml");
        assertThat(recordSheets.get(5).getFilename()).isEqualTo("blah");
    }

    @Test
    void givenNoTrialRecordSheets_whenProcessTrialDataIsInvoked_thenTrialDataIsProcessedCorrectly() {
        // no trial record sheets in S3

        trialDataService.populateAndProcessData();

        List<XhibitTrialDataEntity> recordSheets = trialDataRepository.findAll();
        assertThat(recordSheets)
                .hasSize(0);
    }

}
