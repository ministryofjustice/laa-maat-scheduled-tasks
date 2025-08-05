package uk.gov.justice.laa.maat.scheduled.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetRepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.RepOrderBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;

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
    
    public void resetRepOrdersSentForBilling(ResetRepOrderBillingDTO resetRepOrderBillingDTO) {
        repOrderBillingRepository.resetBillingFlagForRepOrderIds(
            resetRepOrderBillingDTO.getUserModified(), resetRepOrderBillingDTO.getIds());
    }

}
