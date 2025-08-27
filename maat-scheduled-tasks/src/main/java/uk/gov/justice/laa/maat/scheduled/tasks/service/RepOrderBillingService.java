package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.text.MessageFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetRepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.RepOrderBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepOrderBillingService {

    private final RepOrderBillingRepository repOrderBillingRepository;
    private final BillingDataFeedLogService billingDataFeedLogService;
    private final CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;

    @Transactional
    public void sendRepOrdersToBilling(String userModified) {
        List<RepOrderBillingDTO> repOrders = getRepOrdersForBilling();

        if (!repOrders.isEmpty()) {
            List<Integer> ids = repOrders.stream().map(RepOrderBillingDTO::getId).toList();

            resetRepOrdersSentForBilling(
                ResetRepOrderBillingDTO.builder().userModified(userModified).ids(ids).build());

            billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER,
                repOrders.toString());
            
            crownCourtLitigatorFeesApiClient.updateRepOrders(repOrders);
            log.info("Extracted rep order data has been sent to the billing team.");
        }
    }

    private List<RepOrderBillingDTO> getRepOrdersForBilling() {
        List<RepOrderBillingEntity> extractedRepOrders = repOrderBillingRepository.getRepOrdersForBilling();

        return extractedRepOrders.stream()
            .map(RepOrderBillingMapper::mapEntityToDTO)
            .toList();
    }

    private void resetRepOrdersSentForBilling(ResetRepOrderBillingDTO resetRepOrderBillingDTO) {
        repOrderBillingRepository.resetBillingFlagForRepOrderIds(
            resetRepOrderBillingDTO.getUserModified(), resetRepOrderBillingDTO.getIds());
    }

}
