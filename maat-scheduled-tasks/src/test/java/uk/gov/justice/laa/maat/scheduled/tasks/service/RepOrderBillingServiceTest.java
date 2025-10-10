package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedRepOrderForBilling;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getRepOrderBillingDTO;


import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateRepOrdersRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.FileUtils;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class RepOrderBillingServiceTest {

    private static final int SUCCESSFUL_TEST_ID_1 = 1;
    private static final int SUCCESSFUL_TEST_ID_2 = 3;
    private static final int FAILING_TEST_ID = 2;
    private static final String USER_MODIFIED = "TEST";
    private static final int BATCH_SIZE = 2;

    @Mock
    private ResponseUtils responseUtils;
    @Mock
    private RepOrderBillingRepository repOrderBillingRepository;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @Mock
    private BillingConfiguration billingConfiguration;
    @InjectMocks
    private RepOrderBillingService repOrderBillingService;

    @Test
    void givenNoRepOrderDataExists_whenSendToBillingIsInvoked_thenNoActionsPerformed() {
        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of());

        repOrderBillingService.sendToBilling(USER_MODIFIED);

        verify(crownCourtLitigatorFeesApiClient, times(0)).updateRepOrders(any());
    }

    @Test
    void givenBatchSizeIs1_whenSendToBillingIsInvokedFor2RepOrders_thenProcessBatchIsInvokedTwice() {
        RepOrderBillingEntity entity1 = getPopulatedRepOrderForBilling(SUCCESSFUL_TEST_ID_1);
        RepOrderBillingEntity entity2 = getPopulatedRepOrderForBilling(SUCCESSFUL_TEST_ID_2);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(entity1, entity2));
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyString(), anyList())).thenReturn(1);
        ResponseEntity<String> apiResponse = new ResponseEntity<>("body here", HttpStatus.OK);
        when(crownCourtLitigatorFeesApiClient.updateRepOrders(any())).thenReturn(apiResponse);
        when(billingConfiguration.getBatchSize()).thenReturn(1);

        repOrderBillingService.sendToBilling(USER_MODIFIED);

        verify(crownCourtLitigatorFeesApiClient, times(2)).updateRepOrders(any());
    }

    @Test
    void givenSomeFailuresFromCCLF_whenSendToBillingIsInvoked_thenFailingEntitiesAreUpdated() throws Exception {
        RepOrderBillingEntity successEntity = getPopulatedRepOrderForBilling(SUCCESSFUL_TEST_ID_1);
        RepOrderBillingEntity failingEntity = getPopulatedRepOrderForBilling(FAILING_TEST_ID);
        RepOrderBillingDTO successDTO = getRepOrderBillingDTO(SUCCESSFUL_TEST_ID_1);
        RepOrderBillingDTO failingDTO = getRepOrderBillingDTO(FAILING_TEST_ID);

        String responseBodyJson = FileUtils.readResourceToString("billing/api-client/responses/mixed-status.json");

        ResponseEntity<String> apiResponse = new ResponseEntity<>(responseBodyJson, HttpStatus.MULTI_STATUS);
        when(crownCourtLitigatorFeesApiClient.updateRepOrders(any())).thenReturn(apiResponse);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(successEntity, failingEntity));
        when(repOrderBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyString(), anyList())).thenReturn(1);
        when(billingConfiguration.getBatchSize()).thenReturn(BATCH_SIZE);
        when(responseUtils.getErroredIdsFromResponseBody(anyString(), anyString())).thenReturn(List.of(2));

        repOrderBillingService.sendToBilling(USER_MODIFIED);

        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER, List.of(successDTO, failingDTO));
        verify(crownCourtLitigatorFeesApiClient).updateRepOrders(any(UpdateRepOrdersRequest.class));
        verify(repOrderBillingRepository).saveAll(List.of(failingEntity));
    }
}
