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
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.MaatBatchesScheduler;


@Slf4j
@AllArgsConstructor
@RestController
@Profile("!prod") // Only active in non-prod environments
@RequestMapping("api/internal/v1/maat-batches")
@Tag(
    name="MAAT Batches Scheduled Tasks",
    description = "Rest API to trigger scheduled tasks related to MAAT Batches"
)
public class MaatBatchesScheduledTasksController {
  MaatBatchesScheduler maatBatchesScheduler;

  @PostMapping("/lmr-reports")
  @Operation(description = "Trigger the MAAT Batches Local Management Report scheduled task")
  @StandardApiResponse
  public ResponseEntity<String> triggerLocalManagementReports() {
    maatBatchesScheduler.executeLocalManagementReports();
    return ResponseEntity.ok("MAAT Batches Local Management Report task triggered.");
  }

  @PostMapping("/evidence-reminder")
  @Operation(description = "Trigger the MAAT Batches Evidence Reminder Letter scheduled task")
  @StandardApiResponse
  public ResponseEntity<String> triggerEvidenceReminderLetter() {
    maatBatchesScheduler.generateEvidenceReminderLetter();
    return ResponseEntity.ok("MAAT Batches Evidence Reminder Letter task triggered.");
  }

  @PostMapping("/deactivate-inactive-users")
  @Operation(description = "Trigger the MAAT Batches Deactivate Inactive Users scheduled task")
  @StandardApiResponse
  public ResponseEntity<String> triggerDeactivateInactiveUsers() {
    maatBatchesScheduler.deactivateInactiveUsers();
    return ResponseEntity.ok("MAAT Batches Deactivate Inactive Users task triggered.");
  }

  @PostMapping("/financial-assessment-fix")
  @Operation(description = "Trigger the MAAT Batches Financial Assessment Fix scheduled task")
  @StandardApiResponse
  public ResponseEntity<String> triggerFinancialAssessmentFix() {
    maatBatchesScheduler.executeFinancialAssessmentFix();
    return ResponseEntity.ok("MAAT Batches Financial Assessment Fix task triggered.");
  }
}