package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtRemunerationApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.RepOrderBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;
import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateRepOrdersRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.ResponseUtils;

@Slf4j
@Service
public class RepOrderBillingService extends BillingService<RepOrderBillingDTO> {

    private final RepOrderBillingRepository repOrderBillingRepository;
    private static final String REQUEST_LABEL = "rep order";
    
    public RepOrderBillingService(BillingDataFeedLogService billingDataFeedLogService,
        CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient,
        CrownCourtRemunerationApiClient crownCourtRemunerationApiClient,
        RepOrderBillingRepository repOrderBillingRepository, BillingConfiguration billingConfiguration, 
        ResponseUtils responseUtils) {
        super(billingDataFeedLogService, crownCourtLitigatorFeesApiClient, 
            crownCourtRemunerationApiClient, billingConfiguration, responseUtils);
      this.repOrderBillingRepository = repOrderBillingRepository;
    }

    @Override
    protected List<RepOrderBillingDTO> getBillingDTOList() {
        List<RepOrderBillingEntity> extractedRepOrders = repOrderBillingRepository.getRepOrdersForBilling();
        log.debug("Extracted data for {} rep orders.", extractedRepOrders.size());

        return extractedRepOrders.stream()
            .map(RepOrderBillingMapper::mapEntityToDTO)
            .toList();
    }
    
    @Override
    protected void resetBillingFlag(String userModified, List<Integer> ids) {
        int rowsUpdated = repOrderBillingRepository.resetBillingFlagForRepOrderIds(userModified, ids);
        log.debug("Billing Flag reset for {} Rep Orders.", rowsUpdated);
    }

    @Override
    protected BillingDataFeedRecordType getBillingDataFeedRecordType() {
        return BillingDataFeedRecordType.REP_ORDER;
    }

    @Override
    protected List<ResponseEntity<String>> updateBillingRecords(List<RepOrderBillingDTO> repOrders) {
        UpdateRepOrdersRequest repOrdersRequest = UpdateRepOrdersRequest.builder()
            .repOrders(repOrders).build();

        List<ResponseEntity<String>> responses = new ArrayList<>();

        responses.add(crownCourtLitigatorFeesApiClient.updateRepOrders(repOrdersRequest));
        responses.add(crownCourtRemunerationApiClient.updateRepOrders(repOrdersRequest));
        
        return responses;
    }

    @Override
    protected String getRequestLabel() {
        return REQUEST_LABEL;
    }

    @Override
    protected void updateBillingRecordFailures(List<Integer> failedIds, String userModified) {
        List<RepOrderBillingEntity> failedRepOrders = repOrderBillingRepository.findAllById(failedIds);
        for (RepOrderBillingEntity failedRepOrder : failedRepOrders) {
            failedRepOrder.setSendToCclf(true);
            failedRepOrder.setUserModified(userModified);
        }

        repOrderBillingRepository.saveAll(failedRepOrders);
    }
}
