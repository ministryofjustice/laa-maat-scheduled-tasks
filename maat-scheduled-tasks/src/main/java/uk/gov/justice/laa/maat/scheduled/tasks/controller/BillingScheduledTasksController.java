package uk.gov.justice.laa.maat.scheduled.tasks.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.annotation.StandardApiResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.BillingScheduler;


@Slf4j
@AllArgsConstructor
@RestController
@Profile("!prod") // Only active in non-prod environments
@RequestMapping("api/internal/v1/billing")
@Tag(
    name="Billing Scheduled Tasks",
    description = "Rest API to trigger scheduled tasks related to Billing"
)
public class BillingScheduledTasksController {
  BillingScheduler billingScheduler;

  @Operation(description = "Trigger the data feed log cleanup scheduled task")
  @StandardApiResponse
  @PostMapping("/data-feed-log-cleanup")
  public ResponseEntity<String> triggerDataFeedLogCleanup() {
    billingScheduler.cleanupBillingDataFeedLog();
    return ResponseEntity.ok("Billing data feed log cleanup task triggered.");
  }
}