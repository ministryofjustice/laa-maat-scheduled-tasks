package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.Collection;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.BillingDataFeedLogMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.RepOrderBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateRepOrdersRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepOrderBillingService {

    private final RepOrderBillingRepository repOrderBillingRepository;
    private final BillingDataFeedLogMapper billingDataFeedLogMapper;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;

    @Transactional
    public void sendRepOrdersToBilling(String userModified) {
        List<RepOrderBillingDTO> repOrders = getRepOrdersForBilling();

        if (repOrders.isEmpty()) {
            return;
        }

        resetRepOrdersSentForBilling(repOrders, userModified);
        sendRepOrdersToBilling(repOrders);
    }

    public void resendRepOrdersToBilling() {
        List<BillingDataFeedLogEntity> billingLogEntities = billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.REP_ORDER);

        List<RepOrderBillingDTO> repOrders = billingLogEntities.stream()
            .map(billingDataFeedLogMapper::mapEntityToRepOrderBillingDtos)
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .toList();

        if (repOrders.isEmpty()) {
            return;
        }

        sendRepOrdersToBilling(repOrders);
    }

    private void sendRepOrdersToBilling(List<RepOrderBillingDTO> repOrders) {
        billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER, repOrders);

        UpdateRepOrdersRequest repOrdersRequest = UpdateRepOrdersRequest.builder()
            .repOrders(repOrders).build();

        crownCourtLitigatorFeesApiClient.updateRepOrders(repOrdersRequest);
        log.info("Extracted rep order data has been sent to the billing team.");
    }

    private List<RepOrderBillingDTO> getRepOrdersForBilling() {
        List<RepOrderBillingEntity> extractedRepOrders = repOrderBillingRepository.getRepOrdersForBilling();

        return extractedRepOrders.stream()
            .map(RepOrderBillingMapper::mapEntityToDTO)
            .toList();
    }

    private void resetRepOrdersSentForBilling(List<RepOrderBillingDTO> repOrders, String userModified) {
        List<Integer> repOrderIds = repOrders.stream().map(RepOrderBillingDTO::getId).toList();

        repOrderBillingRepository.resetBillingFlagForRepOrderIds(repOrderIds, userModified);
    }

}
