package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.BillingDataFeedLogRepository;

@ExtendWith(MockitoExtension.class)
public class BillingDataFeedLogServiceTest {

    public static final LocalDateTime THRESHOLD_DATE = LocalDateTime.of(2025, 8, 1, 10, 0);

    @Mock
    private BillingDataFeedLogRepository billingDataFeedLogRepository;
    @InjectMocks
    private BillingDataFeedLogService billingDataFeedLogService;

    @Test
    void givenNoDataExists_whenGetBillingDataFeedLogsIsInvoked_thenReturnEmptyList() {
        List<BillingDataFeedLogEntity> records = billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.APPLICANT);

        assertThat(records).isEmpty();
    }

    @Test
    void givenNoMatchingDataExists_whenGetBillingDataFeedLogsIsInvoked_thenReturnEmptyList() {
        BillingDataFeedLogEntity repOrderBillingEntity = BillingDataFeedLogEntity.builder()
            .id(1)
            .recordType(BillingDataFeedRecordType.REP_ORDER.getValue())
            .payload("")
            .build();

        lenient().when(billingDataFeedLogRepository.getBillingDataFeedLogEntitiesByRecordType(BillingDataFeedRecordType.REP_ORDER.getValue()))
            .thenReturn(List.of(repOrderBillingEntity));

        List<BillingDataFeedLogEntity> records = billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.APPLICANT);

        assertThat(records).isEmpty();
    }

    @Test
    void givenMatchingDataExists_whenGetBillingDataFeedLogsIsInvoked_thenReturnsRecords() {
        BillingDataFeedLogEntity applicantBillingEntity = BillingDataFeedLogEntity.builder()
            .id(123)
            .recordType(BillingDataFeedRecordType.APPLICANT.getValue())
            .payload("")
            .build();

        when(billingDataFeedLogRepository.getBillingDataFeedLogEntitiesByRecordType(BillingDataFeedRecordType.APPLICANT.getValue()))
            .thenReturn(List.of(applicantBillingEntity));

        List<BillingDataFeedLogEntity> records = billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.APPLICANT);

        assertThat(applicantBillingEntity).isEqualTo(records.getFirst());
    }

    @Test
    void givenValidData_whenSaveBillingDataFeedIsInvoked_thenDataIsSavedToRepository() {
        BillingDataFeedRecordType recordType = BillingDataFeedRecordType.APPLICANT;
        String payload = "[ApplicantBillingDTO(id=1, firstName='John', lastName='Doe', dob='1983-02-03'," +
            " gender='Male', niNumber='SR096795A', dateCreated='2025-01-01', userCreated='test-u')]";

        billingDataFeedLogService.saveBillingDataFeed(recordType, payload);

        verify(billingDataFeedLogRepository).save(any(BillingDataFeedLogEntity.class));
    }

    @Test
    void givenADate_whenDeleteLogsBeforeDateInvoked_thenDeleteByDateCreatedBeforeIsInvoked() {
        when(billingDataFeedLogRepository.deleteByDateCreatedBefore(THRESHOLD_DATE)).thenReturn(2L);

        Long logsDeleted = billingDataFeedLogService.deleteLogsBeforeDate(THRESHOLD_DATE);

        verify(billingDataFeedLogRepository).deleteByDateCreatedBefore(THRESHOLD_DATE);
        assertThat(logsDeleted).isEqualTo(2L);
    }

    @Test
    void givenNoDate_whenDeleteLogsBeforeDateInvoked_thenAnIllegalArgumentExceptionIsThrown() {
        assertThatThrownBy(() -> billingDataFeedLogService.deleteLogsBeforeDate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A date must be provided for the logs to be deleted.");
    }
}
