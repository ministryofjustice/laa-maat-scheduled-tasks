package uk.gov.justice.laa.maat.scheduled.tasks.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.BillingScheduler;
import uk.gov.justice.laa.maat.scheduled.tasks.service.BatchProcessingService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/v1/billing")
public class BillingController {

    private final BatchProcessingService batchProcessingService;
    private final BillingScheduler billingScheduler;

    @GetMapping
    public ResponseEntity<Void> getBillingData() {
        billingScheduler.extractBillingData();

        return ResponseEntity.ok().build();
    }

    @PostMapping
    @RequestMapping("/resend")
    public ResponseEntity<Void> resendBillingData() {
        try {
            batchProcessingService.resendBillingRecords();
        } catch (Exception exception) {
            log.error("Error running manual extract for billing data: {}", exception.getMessage());
            throw exception;
        }

        return ResponseEntity.accepted().build();
    }
}
