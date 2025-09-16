package uk.gov.justice.laa.maat.scheduled.tasks.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.MaatBatchesScheduler;


@Slf4j
@AllArgsConstructor
@RestController
@Profile("!prod") // Only active in non-prod environments
@RequestMapping("api/internal/v1/maat-batches")
public class MaatBatchesScheduledTasksController {
  MaatBatchesScheduler maatBatchesScheduler;

  @PostMapping("/lmr-reports")
  public ResponseEntity<String> triggerLocalManagementReports() {
    maatBatchesScheduler.executeLocalManagementReports();
    return ResponseEntity.ok("MAAT Batches Local Management Report task triggered.");
  }

  @PostMapping("/evidence-reminder")
  public ResponseEntity<String> triggerEvidenceReminderLetter() {
    maatBatchesScheduler.generateEvidenceReminderLetter();
    return ResponseEntity.ok("MAAT Batches Evidence Reminder Letter task triggered.");
  }

  @PostMapping("/deactivate-inactive-users")
  public ResponseEntity<String> triggerDeactivateInactiveUsers() {
    maatBatchesScheduler.deactivateInactiveUsers();
    return ResponseEntity.ok("MAAT Batches Deactivate Inactive Users task triggered.");
  }

  @PostMapping("/financial-assessment-fix")
  public ResponseEntity<String> triggerFinancialAssessmentFix() {
    maatBatchesScheduler.executeFinancialAssessmentFix();
    return ResponseEntity.ok("MAAT Batches Financial Assessment Fix task triggered.");
  }
}