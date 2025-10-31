package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static uk.gov.justice.laa.maat.scheduled.tasks.util.ListUtils.batchList;

import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.BillingDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchProcessingService {
    private final ApplicantBillingService applicantBillingService;
    private final ApplicantHistoryBillingService applicantHistoryBillingService;
    private final RepOrderBillingService repOrderBillingService;
    protected final BillingConfiguration billingConfiguration;

    public void processApplicantBatch() {
        processBatch(applicantBillingService);
    }

    public void processApplicantHistoryBatch() {
        processBatch(applicantHistoryBillingService);
    }

    public void processRepOrderBatch() {
        processBatch(repOrderBillingService);
    }

    private <T extends BillingDTO> void processBatch(BillingService<T> billingService) {
        List<T> billingsDtos = billingService.getBillingDTOList();

        if (billingsDtos.isEmpty()) {
            return;
        }

        List<List<T>> batches = batchList(billingsDtos, billingConfiguration.getBatchSize());
        IntStream.range(0, batches.size())
            .forEach(i -> billingService.processBatch(batches.get(i), i + 1));
    }
}
