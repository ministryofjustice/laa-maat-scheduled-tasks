package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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

@ExtendWith(MockitoExtension.class)
class RepOrderBillingServiceTest {

    private static final int TEST_ID = 1;
    private static final int FAILING_TEST_ID = 2;
    private static final String USER_MODIFIED = "TEST";
    private static final int BATCH_SIZE = 2;

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
    void givenRepOrderDataExists_whenGetRepOrdersForBillingIsInvoked_thenRepOrderDataIsReturned() {
        RepOrderBillingEntity entity = getPopulatedRepOrderForBilling(TEST_ID);
        RepOrderBillingDTO dto = getRepOrderBillingDTO(TEST_ID);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(entity));
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyString(), anyList())).thenReturn(1);
        ResponseEntity<String> apiResponse = new ResponseEntity<>("body here", HttpStatus.OK);
        when(crownCourtLitigatorFeesApiClient.updateRepOrders(any())).thenReturn(apiResponse);
        when(billingConfiguration.getBatchSize()).thenReturn(BATCH_SIZE);

        repOrderBillingService.sendToBilling(USER_MODIFIED);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(entity, entity));

        List<RepOrderBillingDTO> repOrders = repOrderBillingService.getBillingDTOList();

        assertEquals(List.of(dto, dto), repOrders);
    }

    @Test
    void givenSomeFailuresFromCCLF_whenSendRepOrdersToBillingIsInvoked_thenFailingEntitiesAreUpdated() throws Exception {
        RepOrderBillingEntity successEntity = getPopulatedRepOrderForBilling(TEST_ID);
        RepOrderBillingEntity failingEntity = getPopulatedRepOrderForBilling(FAILING_TEST_ID);
        RepOrderBillingDTO successDTO = getRepOrderBillingDTO(TEST_ID);
        RepOrderBillingDTO failingDTO = getRepOrderBillingDTO(FAILING_TEST_ID);

        String responseBodyJson = FileUtils.readResourceToString("billing/api-client/responses/mixed-status.json");

        ResponseEntity<String> apiResponse = new ResponseEntity<>(responseBodyJson, HttpStatus.MULTI_STATUS);
        when(crownCourtLitigatorFeesApiClient.updateRepOrders(any())).thenReturn(apiResponse);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(successEntity, failingEntity));
        when(repOrderBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyString(), anyList())).thenReturn(1);
        when(billingConfiguration.getBatchSize()).thenReturn(BATCH_SIZE);

        repOrderBillingService.sendToBilling(USER_MODIFIED);

        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER, List.of(successDTO, failingDTO));
        verify(crownCourtLitigatorFeesApiClient).updateRepOrders(any(UpdateRepOrdersRequest.class));
        verify(repOrderBillingRepository).saveAll(List.of(failingEntity));
    }
}
