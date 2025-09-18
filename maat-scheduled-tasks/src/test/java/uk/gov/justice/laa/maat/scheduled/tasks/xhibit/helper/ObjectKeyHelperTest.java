package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.config.XhibitConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetStatus;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetType;

@ExtendWith(MockitoExtension.class)
class ObjectKeyHelperTest {

    @Mock
    private XhibitConfiguration config;

    @InjectMocks
    private ObjectKeyHelper helper;

    @BeforeEach
    void setUp() {
        lenient().when(config.getObjectKeyTrialPrefix()).thenReturn("trial/");
        lenient().when(config.getObjectKeyAppealPrefix()).thenReturn("appeal/");
        lenient().when(config.getObjectKeyProcessedPrefix()).thenReturn("processed");
        lenient().when(config.getObjectKeyErroredPrefix()).thenReturn("errored");
    }

    @Test
    void buildKey_returnsTrialPrefixPlusFilename() {
        String result = helper.buildKey(RecordSheetType.TRIAL, "file1.txt");
        assertThat(result).isEqualTo("trial/file1.txt");
    }

    @Test
    void buildKey_returnsAppealPrefixPlusFilename() {
        String result = helper.buildKey(RecordSheetType.APPEAL, "appeal1.txt");
        assertThat(result).isEqualTo("appeal/appeal1.txt");
    }

    @Test
    void buildKey_withStatus_returnsProcessedTrialKey() {
        String result = helper.buildKey(RecordSheetType.TRIAL, RecordSheetStatus.PROCESSED,
                "file2.txt");
        assertThat(result).isEqualTo("processed/trial/file2.txt");
    }

    @Test
    void buildKey_withStatus_returnsErroredAppealKey() {
        String result = helper.buildKey(RecordSheetType.APPEAL, RecordSheetStatus.ERRORED,
                "file3.txt");
        assertThat(result).isEqualTo("errored/appeal/file3.txt");
    }

    @Test
    void buildPrefix_returnsTrialPrefix() {
        String result = helper.buildPrefix(RecordSheetType.TRIAL);
        assertThat(result).isEqualTo("trial/");
    }

    @Test
    void buildPrefix_returnsAppealPrefix() {
        String result = helper.buildPrefix(RecordSheetType.APPEAL);
        assertThat(result).isEqualTo("appeal/");
    }

    @Test
    void buildPrefix_withStatus_returnsProcessedAppealPrefix() {
        String result = helper.buildPrefix(RecordSheetType.APPEAL, RecordSheetStatus.PROCESSED);
        assertThat(result).isEqualTo("processed/appeal/");
    }

    @Test
    void buildPrefix_withStatus_returnsErroredTrialPrefix() {
        String result = helper.buildPrefix(RecordSheetType.TRIAL, RecordSheetStatus.ERRORED);
        assertThat(result).isEqualTo("errored/trial/");
    }
}

