package uk.gov.justice.laa.maat.scheduled.tasks.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.BillingScheduler;
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.CentralPrintScheduler;


@Slf4j
@RequiredArgsConstructor
@RestController
@Profile("!prod") // Only active in non-prod environments
@RequestMapping("api/internal/v1/trigger-central-print-scheduler")
public class CentralPrintScheduledTasksController {

  @Autowired
  CentralPrintScheduler centralPrintScheduler;

  @PostMapping("/central-print-run")
  public ResponseEntity<String> triggerCentralPrintJob() {
    centralPrintScheduler.executeCentralPrintJob();
    return ResponseEntity.ok("Central print run task triggered.");
  }
}