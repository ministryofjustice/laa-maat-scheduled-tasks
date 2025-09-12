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
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.XhibitBatchesScheduler;


@Slf4j
@RequiredArgsConstructor
@RestController
@Profile("!prod") // Only active in non-prod environments
@RequestMapping("api/internal/v1/trigger-xhibit-batches-scheduler")
public class XhibitBatchesScheduledTasksController {

  @Autowired
  XhibitBatchesScheduler xhibitBatchesScheduler;

  @PostMapping("/appeal-data-processing")
  public ResponseEntity<String> triggerAppealDataProcessing() {
    xhibitBatchesScheduler.executeAppealDataProcessing();
    return ResponseEntity.ok("Xhibit Batches Appeal Data Processing task triggered.");
  }

  @PostMapping("/trial-data-processing")
  public ResponseEntity<String> triggerTrialDataProcessing() {
    xhibitBatchesScheduler.executeTrialDataProcessing();
    return ResponseEntity.ok("Xhibit Batches Trial Data Processing task triggered.");
  }
}