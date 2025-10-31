package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtRemunerationApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.BillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.ResponseUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class BillingService <T extends BillingDTO>{
    private final BillingDataFeedLogService billingDataFeedLogService;
    protected final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    protected final CrownCourtRemunerationApiClient crownCourtRemunerationApiClient;
    protected final BillingConfiguration billingConfiguration;
    protected final ResponseUtils responseUtils;

    protected abstract List<T> getBillingDTOList();
    protected abstract void resetBillingFlag(List<Integer> ids);
    protected abstract BillingDataFeedRecordType getBillingDataFeedRecordType();
    protected abstract List<ResponseEntity<String>> updateBillingRecords(
        List<T> billingDTOList);
    protected abstract String getRequestLabel();
    protected abstract void updateBillingRecordFailures(List<Integer> failedIds);
    
    @Transactional
    protected void processBatch(List<T> currentBatch, Integer batchNumber) {
        log.info("Processing {} batch {} containing {} records", getRequestLabel(), batchNumber, currentBatch.size());

        List<Integer> ids = currentBatch.stream().map(BillingDTO::getId).toList();
        resetBillingFlag(ids);

        billingDataFeedLogService.saveBillingDataFeed(getBillingDataFeedRecordType(), currentBatch);

        List<ResponseEntity<String>> responses = updateBillingRecords(currentBatch);

        // We don't have a separate flag to distinguish between CCR or CCLF, so we will reset the cclf flag regardless of which api has failures
        for (ResponseEntity<String> response : responses) {
            if (response.getStatusCode().value() == HttpStatus.MULTI_STATUS.value()) {
                log.warn("Some {} records failed to update in the CCR/CCLF database. These records will be updated to be re-sent next time.",
                    getRequestLabel());

                List<Integer> failedIds = responseUtils.getErroredIdsFromResponseBody(response.getBody(), getRequestLabel());

                if (!failedIds.isEmpty()) {
                    updateBillingRecordFailures(failedIds);
                }
            } else {
                log.info("Extracted {} data for batch {} has been sent to the billing team.", getRequestLabel(), batchNumber);
            }
        }
    }
}
