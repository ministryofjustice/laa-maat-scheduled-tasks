package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.RepOrderBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;
import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateRepOrdersRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepOrderBillingService {

    private final RepOrderBillingRepository repOrderBillingRepository;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;

    public List<RepOrderBillingDTO> getRepOrdersForBilling() {
        List<RepOrderBillingEntity> extractedRepOrders = repOrderBillingRepository.getRepOrdersForBilling();
        log.debug("Extracted data for {} rep orders.", extractedRepOrders.size());

        return extractedRepOrders.stream()
            .map(RepOrderBillingMapper::mapEntityToDTO)
            .toList();
    }

    @Transactional
    public void sendRepOrdersToBilling(List<RepOrderBillingDTO> repOrders, String userModified) {
        resetRepOrderBilling(repOrders, userModified);

        billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER,
            repOrders);

        UpdateRepOrdersRequest repOrdersRequest = UpdateRepOrdersRequest.builder()
            .repOrders(repOrders).build();

        crownCourtLitigatorFeesApiClient.updateRepOrders(repOrdersRequest);
    }

    private void resetRepOrderBilling(List<RepOrderBillingDTO> repOrders, String userModified) {
        List<Integer> ids = repOrders.stream().map(RepOrderBillingDTO::getId).toList();

        int rowsUpdated = repOrderBillingRepository.resetBillingFlagForRepOrderIds(userModified,
            ids);
        log.debug("CCLF Flag reset for {} rep orders.", rowsUpdated);
    }
}
