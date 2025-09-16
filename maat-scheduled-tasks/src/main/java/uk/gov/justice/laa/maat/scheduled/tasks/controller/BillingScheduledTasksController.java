package uk.gov.justice.laa.maat.scheduled.tasks.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.BillingScheduler;


@Slf4j
@AllArgsConstructor
@RestController
@Profile("!prod") // Only active in non-prod environments
@RequestMapping("api/internal/v1/billing")
public class BillingScheduledTasksController {
  BillingScheduler billingScheduler;

  @PostMapping("/data-feed-log-cleanup")
  public ResponseEntity<String> triggerDataFeedLogCleanup() {
    billingScheduler.cleanupBillingDataFeedLog();
    return ResponseEntity.ok("Billing data feed log cleanup task triggered.");
  }
}