package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.RepOrderBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;
import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateRepOrdersRequest;

import static uk.gov.justice.laa.maat.scheduled.tasks.util.ListUtils.batchList;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepOrderBillingService {

    private final RepOrderBillingRepository repOrderBillingRepository;
    private final BillingConfiguration billingConfiguration;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;

    @Transactional
    public void sendRepOrdersToBilling(String userModified) {
        List<RepOrderBillingDTO> repOrders = getRepOrdersForBilling();

        if (repOrders.isEmpty()) {
            return;
        }

        int updatedRows = repOrderBillingRepository.resetBillingFlagForRepOrderIds(userModified);
        log.info("CCLF Flag reset for {} rep orders", updatedRows);

        billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER,
            repOrders.toString());

        List<List<RepOrderBillingDTO>> batchedRepOrders = batchList(repOrders,
            billingConfiguration.getRequestBatchSize());

        for (List<RepOrderBillingDTO> currentBatch : batchedRepOrders) {
            UpdateRepOrdersRequest repOrdersRequest = UpdateRepOrdersRequest.builder()
                .repOrders(currentBatch).build();

            crownCourtLitigatorFeesApiClient.updateRepOrders(repOrdersRequest);
        }

        log.info("Extracted rep order data has been sent to the billing team.");
    }

    private List<RepOrderBillingDTO> getRepOrdersForBilling() {
        List<RepOrderBillingEntity> extractedRepOrders = repOrderBillingRepository.getRepOrdersForBilling();

        return extractedRepOrders.stream()
            .map(RepOrderBillingMapper::mapEntityToDTO)
            .toList();
    }
}
