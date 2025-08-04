package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetRepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.RepOrderBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepOrderBillingService {

    private final RepOrderBillingRepository repOrderBillingRepository;

    public List<RepOrderBillingDTO> getRepOrdersForBilling() {
        List<RepOrderBillingEntity> extractedRepOrders = repOrderBillingRepository.getRepOrdersForBilling();

        if (extractedRepOrders.isEmpty()) {
            return Collections.emptyList();
        }

        return extractedRepOrders.stream()
                .map(RepOrderBillingMapper::mapEntityToDTO)
                .toList();
    }

    @Transactional(rollbackFor = MAATScheduledTasksException.class)
    public void resetRepOrdersSentForBilling(ResetRepOrderBillingDTO resetRepOrderBillingDTO) {
        int updatedRows = repOrderBillingRepository.resetBillingFlagForRepOrderIds(
            resetRepOrderBillingDTO.getUserModified(), resetRepOrderBillingDTO.getIds());

        if (updatedRows != resetRepOrderBillingDTO.getIds().size()) {
            String message = MessageFormat.format(
                "Unable to reset rep orders sent for billing as only {0} rep order(s) could be processed (from a total of {1} rep order(s))", 
                updatedRows, resetRepOrderBillingDTO.getIds().size());
            log.error(message);
            throw new MAATScheduledTasksException(message);
        }
    }

}
