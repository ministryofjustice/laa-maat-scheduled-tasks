package uk.gov.justice.laa.maat.scheduled.tasks.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.BillingScheduler;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingScheduler billingScheduler;

    @GetMapping
    public ResponseEntity getBillingData() {

        billingScheduler.extractCCLFBillingData();

        return ResponseEntity.ok().build();
    }

}
