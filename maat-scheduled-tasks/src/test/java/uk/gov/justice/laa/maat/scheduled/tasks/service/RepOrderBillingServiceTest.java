package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.mockito.ArgumentMatchers.any;
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
    private static final int FAILING_TEST_ID = 2;
    private static final String USER_MODIFIED = "TEST";

    @Mock
    private ResponseUtils responseUtils;
    @Mock
    private RepOrderBillingRepository repOrderBillingRepository;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @InjectMocks
    private RepOrderBillingService repOrderBillingService;

    @Test
    void givenSomeFailuresFromCCLF_whenProcessBatchIsInvoked_thenFailingEntitiesAreUpdated() throws Exception {
        RepOrderBillingEntity failingEntity = getPopulatedRepOrderForBilling(FAILING_TEST_ID);
        RepOrderBillingDTO successDTO = getRepOrderBillingDTO(SUCCESSFUL_TEST_ID_1);
        RepOrderBillingDTO failingDTO = getRepOrderBillingDTO(FAILING_TEST_ID);

        String responseBodyJson = FileUtils.readResourceToString("billing/api-client/responses/mixed-status.json");

        ResponseEntity<String> apiResponse = new ResponseEntity<>(responseBodyJson, HttpStatus.MULTI_STATUS);
        when(crownCourtLitigatorFeesApiClient.updateRepOrders(any())).thenReturn(apiResponse);
        
        when(repOrderBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(responseUtils.getErroredIdsFromResponseBody(anyString(), anyString())).thenReturn(List.of(2));

        repOrderBillingService.processBatch(List.of(successDTO, failingDTO), 1, USER_MODIFIED);

        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER, List.of(successDTO, failingDTO));
        verify(crownCourtLitigatorFeesApiClient).updateRepOrders(any(UpdateRepOrdersRequest.class));
        verify(repOrderBillingRepository).saveAll(List.of(failingEntity));
    }
}
