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
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.CentralPrintScheduler;


@Slf4j
@AllArgsConstructor
@RestController
@Profile("!prod") // Only active in non-prod environments
@RequestMapping("api/internal/v1/central-print")
@Tag(
    name="Central Print Scheduled Tasks",
    description = "Rest API to trigger scheduled tasks related to Central Print"
)
public class CentralPrintScheduledTasksController {
  CentralPrintScheduler centralPrintScheduler;

  @PostMapping("/run")
  @Operation(description = "Trigger the Central Print job scheduled task")
  @StandardApiResponse
  public ResponseEntity<String> triggerCentralPrintJob() {
    centralPrintScheduler.executeCentralPrintJob();
    return ResponseEntity.ok("Central print run task triggered.");
  }
}