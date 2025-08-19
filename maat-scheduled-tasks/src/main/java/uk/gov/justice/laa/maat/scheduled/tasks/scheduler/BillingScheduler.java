package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.service.BillingDataFeedLogService;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class BillingScheduler {

    public static final Integer OLDER_THAN_DAYS = 30;
    
    private final BillingDataFeedLogService billingDataFeedLogService;
    
    @Scheduled(cron = "${billing.cleanup_data_feed_log.cron_expression}")
    public void cleanupBillingDataFeedLog() {
        log.info("Starting billing data feed log cleanup...");
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(OLDER_THAN_DAYS);
        
        Long logsDeleted = billingDataFeedLogService.deleteLogsBeforeDate(dateThreshold);
        log.info("Billing data feed log cleanup completed. {} entries deleted.", logsDeleted);
    }
}
