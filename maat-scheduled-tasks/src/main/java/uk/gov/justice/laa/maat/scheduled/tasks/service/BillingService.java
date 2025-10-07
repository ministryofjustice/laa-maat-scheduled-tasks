package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static uk.gov.justice.laa.maat.scheduled.tasks.util.ListUtils.batchList;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.BillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.ResponseUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class BillingService <T extends BillingDTO>{
    private final BillingDataFeedLogService billingDataFeedLogService;
    protected final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    protected final BillingConfiguration billingConfiguration;
    protected static final Boolean SEND_TO_CCLF_FAILURE_FLAG = true;

    protected abstract List<T> getBillingDTOList();
    protected abstract void resetBillingCCLFFlag(String userModified, List<Integer> ids);
    protected abstract BillingDataFeedRecordType getBillingDataFeedRecordType();
    protected abstract ResponseEntity<String> updateBillingRecords(
        List<T> billingDTOList);
    protected abstract String getRequestLabel();
    protected abstract void updateBillingRecordFailures(List<Integer> failedIds, String userModified);


    @Transactional
    public void sendToBilling(String userModified) {
        List<T> billingDTOList = getBillingDTOList();

        if (billingDTOList.isEmpty()) {
            return;
        }

        List<List<T>> billingBatches = batchList(billingDTOList,
            billingConfiguration.getBatchSize());

        for (List<T> currentBatch : billingBatches) {
            processBatch(currentBatch, userModified);
        }
    }

    @Transactional
    protected void processBatch(List<T> currentBatch, String userModified) {
        log.debug("Processing batch of {} applicants...", currentBatch.size());

        List<Integer> ids = currentBatch.stream().map(BillingDTO::getId).toList();
        resetBillingCCLFFlag(userModified, ids);

        billingDataFeedLogService.saveBillingDataFeed(getBillingDataFeedRecordType(), currentBatch);

        ResponseEntity<String> response = updateBillingRecords(currentBatch);

        if (response.getStatusCode().value() == HttpStatus.MULTI_STATUS.value()) {
            log.warn("Some {} records failed to update in the CCR/CCLF database. These records will be updated to be re-sent next time.",
                getRequestLabel());

            List<Integer> failedIds = ResponseUtils.getErroredIdsFromResponseBody(response.getBody(), getRequestLabel());

            if (!failedIds.isEmpty()) {
                updateBillingRecordFailures(failedIds, userModified);
            }
        } else {
            log.info("Extracted {} data has been sent to the billing team.", getRequestLabel());
        }
    }
}
