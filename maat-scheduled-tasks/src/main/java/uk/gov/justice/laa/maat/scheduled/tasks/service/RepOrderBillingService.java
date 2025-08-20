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
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.RepOrderBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.BillingDataFeedLogRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;

import java.util.Collections;
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

            int rowsReset = resetRepOrdersSentForBilling(
                ResetRepOrderBillingDTO.builder().userModified(userModified).ids(ids).build());

            if (rowsReset != ids.size()) {
                throw new MAATScheduledTasksException(String.format(
                    "Number of rep order rows reset - %s does not equal the number of rows retrieved - %s.",
                    rowsReset, ids));
            }

            // TODO: Don't think we can get the request body as declaritive web client, would this be good enough???
            billingDataFeedLogService.saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER,
                repOrders.toString());

            // TODO: Transactional should rollback as the declaritive web client throws a WebClientResponseException get reviewed!!!
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

    private int resetRepOrdersSentForBilling(ResetRepOrderBillingDTO resetRepOrderBillingDTO) {
        return repOrderBillingRepository.resetBillingFlagForRepOrderIds(
            resetRepOrderBillingDTO.getUserModified(), resetRepOrderBillingDTO.getIds());
    }

}
