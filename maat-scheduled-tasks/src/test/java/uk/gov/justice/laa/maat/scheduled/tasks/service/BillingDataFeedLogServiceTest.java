package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.BillingDataFeedLogRepository;

@ExtendWith(MockitoExtension.class)
public class BillingDataFeedLogServiceTest {

    private static final int TEST_ID = 1;
    public static final LocalDateTime THRESHOLD_DATE = LocalDateTime.of(2025, 8, 1, 10, 0);

    @Mock
    private BillingDataFeedLogRepository billingDataFeedLogRepository;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private BillingDataFeedLogService billingDataFeedLogService;

    @Test
    void givenValidData_whenSaveBillingDataFeedIsInvoked_thenDataIsSavedToRepository() throws JsonProcessingException {
        BillingDataFeedRecordType recordType = BillingDataFeedRecordType.APPLICANT;
        List<ApplicantBillingDTO> billingDTO = List.of(getApplicantDTO(TEST_ID));

        when(objectMapper.writeValueAsString(billingDTO)).thenReturn(
            "[ApplicantBillingDTO(id=1, firstName='test-first-name', lastName='test-last-name', " +
                "otherNames='test-other-names', dob='2025-07-31', gender='male', niNumber='AB123456C', " +
                "foreignId='foreign-ID', dateCreated='2024-08-29T11:38:12', userCreated='TEST', " +
                "dateModified='2024-04-01T10:45:09', userModified='TEST')]"
        );

        billingDataFeedLogService.saveBillingDataFeed(recordType, billingDTO);

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
        assertThatThrownBy(() -> {
            billingDataFeedLogService.deleteLogsBeforeDate(null);
        }).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("A date must be provided for the logs to be deleted.");
    }
}
