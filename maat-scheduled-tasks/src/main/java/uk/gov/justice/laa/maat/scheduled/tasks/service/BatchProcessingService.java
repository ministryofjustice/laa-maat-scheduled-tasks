package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static uk.gov.justice.laa.maat.scheduled.tasks.util.ListUtils.batchList;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.BillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchProcessingService<T extends BillingDTO> {
    private final ApplicantBillingService applicantBillingService;
    private final ApplicantHistoryBillingService applicantHistoryBillingService;
    private final RepOrderBillingService repOrderBillingService;
    protected final BillingConfiguration billingConfiguration;
    
    public void processApplicantBatch(String userModified) {
        List<ApplicantBillingDTO> billingDTOList = applicantBillingService.getBillingDTOList();

        if (billingDTOList.isEmpty()) {
            return;
        }

        List<List<ApplicantBillingDTO>> billingBatches = batchList(billingDTOList,
            billingConfiguration.getBatchSize());

        Integer batchNumber = 0;
        for (List<ApplicantBillingDTO> currentBatch : billingBatches) {
            batchNumber++;
            applicantBillingService.processBatch(currentBatch, batchNumber, userModified);
        }
    }
    
    public void processApplicantHistoryBatch(String userModified) {
        List<ApplicantHistoryBillingDTO> billingDTOList = applicantHistoryBillingService.getBillingDTOList();

        if (billingDTOList.isEmpty()) {
            return;
        }
        
        List<List<ApplicantHistoryBillingDTO>> billingBatches = batchList(billingDTOList,
            billingConfiguration.getBatchSize());
        
        Integer batchNumber = 0;
        for (List<ApplicantHistoryBillingDTO> currentBatch : billingBatches) {
            batchNumber++;
            applicantHistoryBillingService.processBatch(currentBatch, batchNumber, userModified);
        }
    }
    
    public void processRepOrderBatch(String userModified) {
        List<RepOrderBillingDTO> billingDTOList = repOrderBillingService.getBillingDTOList();

        if (billingDTOList.isEmpty()) {
            return;
        }

        List<List<RepOrderBillingDTO>> billingBatches = batchList(billingDTOList,
            billingConfiguration.getBatchSize());

        Integer batchNumber = 0;
        for (List<RepOrderBillingDTO> currentBatch : billingBatches) {
            batchNumber++;
            repOrderBillingService.processBatch(currentBatch, batchNumber, userModified);
        }
    }
}
