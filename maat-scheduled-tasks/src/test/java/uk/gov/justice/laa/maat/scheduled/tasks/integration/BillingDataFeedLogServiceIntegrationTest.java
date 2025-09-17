package uk.gov.justice.laa.maat.scheduled.tasks.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.BillingDataFeedLogRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.service.BillingDataFeedLogService;

@SpringBootTest
@AutoConfigureMockMvc
public class BillingDataFeedLogServiceIntegrationTest {

    public static final LocalDateTime THRESHOLD_DATE = LocalDateTime.of(2025, 7, 1, 23, 15);
    public static final LocalDateTime BILLING_LOG_BEFORE_DATE = LocalDateTime.of(2025, 7, 1, 23, 0);
    public static final LocalDateTime BILLING_LOG_AFTER_DATE = LocalDateTime.of(2025, 7, 1, 23, 30);

    @Autowired
    private BillingDataFeedLogRepository billingDataFeedLogRepository;

    @Autowired
    private BillingDataFeedLogService billingDataFeedLogService;

    private List<BillingDataFeedLogEntity> billingLogsToDelete() {
        return List.of(
                TestEntityDataBuilder.getPopulatedBillingLogEntity(null, BILLING_LOG_BEFORE_DATE),
                TestEntityDataBuilder.getPopulatedBillingLogEntity(null, BILLING_LOG_BEFORE_DATE)
        );
    }

    private List<BillingDataFeedLogEntity> billingLogsToKeep() {
        return List.of(
                TestEntityDataBuilder.getPopulatedBillingLogEntity(null, BILLING_LOG_AFTER_DATE),
                TestEntityDataBuilder.getPopulatedBillingLogEntity(null, BILLING_LOG_AFTER_DATE)
        );
    }

    @Test
    void givenBillingLogDataExistsBeforeThreshold_whenDeleteLogsBeforeDateInvoked_thenBillingLogDataIsDeleted() {
        List<BillingDataFeedLogEntity> billingLogsToDelete = billingLogsToDelete();

        billingDataFeedLogRepository.saveAll(billingLogsToDelete);
        assertThat(billingDataFeedLogRepository.count()).isEqualTo(billingLogsToDelete.size());

        billingDataFeedLogService.deleteLogsBeforeDate(THRESHOLD_DATE);

        assertThat(billingDataFeedLogRepository.count()).isZero();
    }

    @Test
    void givenBillingLogDataExistsBeforeAndAfterThreshold_whenDeleteLogsBeforeDateInvoked_thenCorrectBillingLogDataIsDeleted() {
        List<BillingDataFeedLogEntity> billingLogsToKeep = billingLogsToKeep();
        List<BillingDataFeedLogEntity> billingLogsToDelete = billingLogsToDelete();

        billingDataFeedLogRepository.saveAll(billingLogsToDelete);
        billingDataFeedLogRepository.saveAll(billingLogsToKeep);

        billingDataFeedLogService.deleteLogsBeforeDate(THRESHOLD_DATE);

        List<BillingDataFeedLogEntity> remainingBillingLogs = billingDataFeedLogRepository.findAll();

        assertThat(billingDataFeedLogRepository.count()).isEqualTo(billingLogsToDelete.size());

        assertThat(remainingBillingLogs)
                .as("Some entities are remaining with an unexpected date")
                .isNotEmpty()
                .allMatch(log -> log.getDateCreated().equals(BILLING_LOG_AFTER_DATE));

    }
}
