package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static uk.gov.justice.laa.maat.scheduled.tasks.util.ListUtils.batchList;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.function.TriConsumer;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchProcessingService {
    private final ApplicantBillingService applicantBillingService;
    private final ApplicantHistoryBillingService applicantHistoryBillingService;
    private final RepOrderBillingService repOrderBillingService;
    protected final BillingConfiguration billingConfiguration;

    public void processApplicantBatch() {
        processBatch(
            applicantBillingService::getBillingDTOList,
            applicantBillingService::processBatch
        );
    }


    public void processApplicantHistoryBatch() {
        processBatch(
            applicantHistoryBillingService::getBillingDTOList,
            applicantHistoryBillingService::processBatch
        );
    }

    public void processRepOrderBatch() {
        processBatch(
            repOrderBillingService::getBillingDTOList,
            repOrderBillingService::processBatch
        );
    }

    private <T> void processBatch(
        Supplier<List<T>> listSupplier,
        TriConsumer<List<T>, Integer, String> batchProcessor) {

        List<T> dtoList = listSupplier.get();
        if (dtoList.isEmpty()) {
            return;
        }

        List<List<T>> batches = batchList(dtoList, billingConfiguration.getBatchSize());
        IntStream.range(0, batches.size())
            .forEach(i -> batchProcessor.accept(batches.get(i), i + 1,
                billingConfiguration.getUserModified()));
    }
}
