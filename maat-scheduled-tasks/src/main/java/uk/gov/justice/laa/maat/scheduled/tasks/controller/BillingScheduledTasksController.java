package uk.gov.justice.laa.maat.scheduled.tasks.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.env.Environment;
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.BillingScheduler;


@Slf4j
@RequiredArgsConstructor
@RestController
@Profile("!prod") // Only active in non-prod environments
@RequestMapping("api/internal/v1/trigger-billing-scheduler")
public class BillingScheduledTasksController {

  @Autowired
  BillingScheduler billingScheduler;

  @Autowired
  private Environment environment;

  @PostMapping("/data-feed-log-cleanup")
  public ResponseEntity<String> triggerAction() {
    billingScheduler.cleanupBillingDataFeedLog();
    return ResponseEntity.ok("Billing data feed log cleanup task triggered. Environment = " + environment.getProperty("spring.profiles.active"));
  }
}