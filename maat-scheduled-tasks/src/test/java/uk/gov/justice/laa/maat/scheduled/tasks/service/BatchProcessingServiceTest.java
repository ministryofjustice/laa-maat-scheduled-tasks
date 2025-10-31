package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantHistoryBillingDTO;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getRepOrderBillingDTO;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;

@ExtendWith(MockitoExtension.class)
public class BatchProcessingServiceTest {
    private static final int SUCCESSFUL_TEST_ID_1 = 1;
    private static final int SUCCESSFUL_TEST_ID_2 = 3;
    private static final int BATCH_SIZE = 1;
    
    @Mock
    private ApplicantBillingService applicantBillingService;
    @Mock
    private ApplicantHistoryBillingService applicantHistoryBillingService;
    @Mock
    private RepOrderBillingService repOrderBillingService;
    @Mock
    private BillingConfiguration billingConfiguration;
    @InjectMocks
    private BatchProcessingService batchProcessingService;

    @Test
    void givenNoApplicantDataExists_whenProcessApplicantBatchIsInvoked_thenNoActionsPerformed() {
        when(applicantBillingService.getBillingDTOList()).thenReturn(List.of());
        
        batchProcessingService.processApplicantBatch();

        verify(applicantBillingService, times(0)).processBatch(anyList(), anyInt());
    }

    @Test
    void givenNoApplicantHistoryDataExists_whenProcessApplicantHistoryBatchIsInvoked_thenNoActionsPerformed() {
        when(applicantHistoryBillingService.getBillingDTOList()).thenReturn(List.of());

        batchProcessingService.processApplicantHistoryBatch();

        verify(applicantHistoryBillingService, times(0)).processBatch(anyList(), anyInt());
    }

    @Test
    void givenNoRepOrderDataExists_whenProcessRepOrderBatchIsInvoked_thenNoActionsPerformed() {
        when(repOrderBillingService.getBillingDTOList()).thenReturn(List.of());

        batchProcessingService.processRepOrderBatch();

        verify(repOrderBillingService, times(0)).processBatch(anyList(), anyInt());
    }
    
    @Test
    void givenBatchSizeIs1_whenProcessApplicantBatchIsInvokedFor2Applicants_thenProcessBatchIsInvokedTwice() {
        ApplicantBillingDTO dto1 = getApplicantDTO(SUCCESSFUL_TEST_ID_1);
        ApplicantBillingDTO dto2 = getApplicantDTO(SUCCESSFUL_TEST_ID_2);
        
        when(applicantBillingService.getBillingDTOList()).thenReturn(List.of(dto1, dto2));
        when(billingConfiguration.getBatchSize()).thenReturn(BATCH_SIZE);

        batchProcessingService.processApplicantBatch();

        verify(applicantBillingService, times(2)).processBatch(anyList(), anyInt());
    }
    
    @Test
    void givenBatchSizeIs1_whenProcessApplicantHistoryBatchIsInvokedFor2ApplicantHistories_thenProcessBatchIsInvokedTwice() {
        ApplicantHistoryBillingDTO dto1 = getApplicantHistoryBillingDTO(SUCCESSFUL_TEST_ID_1);
        ApplicantHistoryBillingDTO dto2 = getApplicantHistoryBillingDTO(SUCCESSFUL_TEST_ID_2);

        when(applicantHistoryBillingService.getBillingDTOList()).thenReturn(
            List.of(dto1, dto2));
        when(billingConfiguration.getBatchSize()).thenReturn(BATCH_SIZE);

        batchProcessingService.processApplicantHistoryBatch();

        verify(applicantHistoryBillingService, times(2)).processBatch(anyList(), anyInt());
    }

    @Test
    void givenBatchSizeIs1_whenProcessRepOrderBatchIsInvokedFor2RepOrders_thenProcessBatchIsInvokedTwice() {
        RepOrderBillingDTO dto1 = getRepOrderBillingDTO(SUCCESSFUL_TEST_ID_1);
        RepOrderBillingDTO dto2 = getRepOrderBillingDTO(SUCCESSFUL_TEST_ID_2);

        when(repOrderBillingService.getBillingDTOList()).thenReturn(List.of(dto1, dto2));
        when(billingConfiguration.getBatchSize()).thenReturn(BATCH_SIZE);

        batchProcessingService.processRepOrderBatch();

        verify(repOrderBillingService, times(2)).processBatch(anyList(), anyInt());
    }
}
