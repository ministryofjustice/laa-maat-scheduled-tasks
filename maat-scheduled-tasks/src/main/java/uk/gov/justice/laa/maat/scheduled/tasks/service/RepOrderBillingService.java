package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
public class RepOrderBillingService extends BillingService<RepOrderBillingDTO> {

    private final RepOrderBillingRepository repOrderBillingRepository;
    private static final String REQUEST_LABEL = "rep order";

    public RepOrderBillingService(BillingDataFeedLogService billingDataFeedLogService,
        CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient,
        RepOrderBillingRepository repOrderBillingRepository) {
        super(billingDataFeedLogService, crownCourtLitigatorFeesApiClient);
      this.repOrderBillingRepository = repOrderBillingRepository;
    }

    @Override
    protected List<RepOrderBillingDTO> getBillingDTOList() {
        List<RepOrderBillingEntity> extractedRepOrders = repOrderBillingRepository.getRepOrdersForBilling();

        return extractedRepOrders.stream()
            .map(RepOrderBillingMapper::mapEntityToDTO)
            .toList();
    }

    @Override
    protected void resetBillingCCLFFlag(String userModified, List<Integer> ids) {
        repOrderBillingRepository.resetBillingFlagForRepOrderIds(userModified, ids);
        log.info("Resetting CCLF flag for Rep Orders.");
    }

    @Override
    protected BillingDataFeedRecordType getBillingDataFeedRecordType() {
        return BillingDataFeedRecordType.REP_ORDER;
    }

    @Override
    protected ResponseEntity<String> updateBillingRecords(List<RepOrderBillingDTO> repOrders) {
        UpdateRepOrdersRequest repOrdersRequest = UpdateRepOrdersRequest.builder()
            .repOrders(repOrders).build();
        return crownCourtLitigatorFeesApiClient.updateRepOrders(repOrdersRequest);
    }

    @Override
    protected String getRequestLabel() {
        return REQUEST_LABEL;
    }

    @Override
    protected void updateBillingRecordFailures(List<Integer> failedIds, String userModified) {
        List<RepOrderBillingEntity> failedRepOrders = repOrderBillingRepository.findAllById(failedIds);
        for (RepOrderBillingEntity failedRepOrder : failedRepOrders) {
            failedRepOrder.setSendToCclf(SENT_TO_CCLF_FAILURE_FLAG);
            failedRepOrder.setUserModified(userModified);
        }

        repOrderBillingRepository.saveAll(failedRepOrders);
    }
}
