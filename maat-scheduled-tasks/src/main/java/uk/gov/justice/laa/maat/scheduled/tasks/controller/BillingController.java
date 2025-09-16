package uk.gov.justice.laa.maat.scheduled.tasks.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.BillingScheduler;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/v1/billing")
public class BillingController {

    private final BillingScheduler billingScheduler;

    @PostMapping
    @RequestMapping("/resend")
    public ResponseEntity<Void> resendBillingData() {
        billingScheduler.resendBillingData();

        return ResponseEntity.ok().build();
    }
}
