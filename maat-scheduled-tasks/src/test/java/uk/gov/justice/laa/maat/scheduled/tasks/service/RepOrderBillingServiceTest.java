package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedRepOrderForBilling;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getRepOrderBillingDTO;


import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateRepOrdersRequest;

@ExtendWith(MockitoExtension.class)
class RepOrderBillingServiceTest {

    private static final int TEST_ID = 1;
    private static final String USER_MODIFIED = "TEST";

    @Mock
    private RepOrderBillingRepository repOrderBillingRepository;
    @Mock
    private BillingConfiguration billingConfiguration;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @InjectMocks
    private RepOrderBillingService repOrderBillingService;

    @Test
    void givenDataDoesNotRequireBatchedRequests_whenSendRepOrdersToBillingIsInvoked_thenBillingIsCalledOnce() {
        RepOrderBillingEntity entity = getPopulatedRepOrderForBilling(TEST_ID);
        RepOrderBillingDTO dto = getRepOrderBillingDTO(TEST_ID);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(entity, entity));
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyString())).thenReturn(2);
        when(billingConfiguration.getRequestBatchSize()).thenReturn(5);

        repOrderBillingService.extractRepOrderBillingData(USER_MODIFIED);

        verify(repOrderBillingRepository).resetBillingFlagForRepOrderIds(USER_MODIFIED);
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER, List.of(dto, dto).toString());
        verify(crownCourtLitigatorFeesApiClient, times(1)).updateRepOrders(any(UpdateRepOrdersRequest.class));
    }

    @Test
    void givenDataRequiresBatchedRequests_whenSendRepOrdersToBillingIsInvoked_thenBillingIsCalledMultipleTimes() {
        RepOrderBillingEntity entity = getPopulatedRepOrderForBilling(TEST_ID);
        RepOrderBillingDTO dto = getRepOrderBillingDTO(TEST_ID);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(entity, entity));
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyString())).thenReturn(2);
        when(billingConfiguration.getRequestBatchSize()).thenReturn(1);

        repOrderBillingService.extractRepOrderBillingData(USER_MODIFIED);

        verify(repOrderBillingRepository).resetBillingFlagForRepOrderIds(USER_MODIFIED);
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER, List.of(dto, dto).toString());
        verify(crownCourtLitigatorFeesApiClient, times(2)).updateRepOrders(any(UpdateRepOrdersRequest.class));
    }

    @Test
    void givenNoCCLFDataAvailable_whenSendRepOrdersToBillingIsInvoked_thenNoActionsPerformed() {
        RepOrderBillingDTO dto = getRepOrderBillingDTO(TEST_ID);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(Collections.emptyList());

        repOrderBillingService.extractRepOrderBillingData(USER_MODIFIED);

        verify(repOrderBillingRepository, never()).resetBillingFlagForRepOrderIds(USER_MODIFIED);
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER, List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient, never()).updateRepOrders(any(UpdateRepOrdersRequest.class));
    }
}
