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
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.XhibitBatchesScheduler;


@Slf4j
@AllArgsConstructor
@RestController
@Profile("!prod") // Only active in non-prod environments
@RequestMapping("api/internal/v1/xhibit-batches")
@Tag(
    name="Xhibit Batches Scheduled Tasks",
    description = "Rest API to trigger scheduled tasks related to Xhibit Batches"
)
public class XhibitBatchesScheduledTasksController {
  XhibitBatchesScheduler xhibitBatchesScheduler;

  @PostMapping("/appeal-data-processing")
  @Operation(description = "Trigger the Xhibit Batches Appeal Data Processing scheduled task")
  @StandardApiResponse
  public ResponseEntity<String> triggerAppealDataProcessing() {
    xhibitBatchesScheduler.executeAppealDataProcessing();
    return ResponseEntity.ok("Xhibit Batches Appeal Data Processing task triggered.");
  }

  @PostMapping("/trial-data-processing")
  @Operation(description = "Trigger the Xhibit Batches Trial Data Processing scheduled task")
  @StandardApiResponse
  public ResponseEntity<String> triggerTrialDataProcessing() {
    xhibitBatchesScheduler.executeTrialDataProcessing();
    return ResponseEntity.ok("Xhibit Batches Trial Data Processing task triggered.");
  }
}