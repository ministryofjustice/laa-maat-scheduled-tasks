package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedRepOrderForBilling;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getRepOrderBillingDTO;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtRemunerationApiClient;
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
    private static final int FAILING_TEST_ID = 2;
    private static final String USER_MODIFIED = "TEST";
    private RepOrderBillingEntity failingEntity;
    private RepOrderBillingDTO successDTO;
    private RepOrderBillingDTO failingDTO;
    private String multiStatusResponseBodyJson;
    ResponseEntity<String> successApiResponse;
    ResponseEntity<String> multiStatusApiResponse;

    @Mock
    private ResponseUtils responseUtils;
    @Mock
    private RepOrderBillingRepository repOrderBillingRepository;
    @Mock
    private BillingConfiguration billingConfiguration;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @Mock
    private CrownCourtRemunerationApiClient crownCourtRemunerationApiClient;
    @InjectMocks
    private RepOrderBillingService repOrderBillingService;

    @BeforeEach
    void setUp() throws IOException {
        failingEntity = getPopulatedRepOrderForBilling(FAILING_TEST_ID);
        successDTO = getRepOrderBillingDTO(SUCCESSFUL_TEST_ID_1);
        failingDTO = getRepOrderBillingDTO(FAILING_TEST_ID);
        multiStatusResponseBodyJson = FileUtils.readResourceToString(
            "billing/api-client/responses/multi-status.json");
        successApiResponse = new ResponseEntity<>(null, HttpStatus.OK);
        multiStatusApiResponse = new ResponseEntity<>(multiStatusResponseBodyJson, HttpStatus.MULTI_STATUS);

        when(billingConfiguration.getUserModified()).thenReturn(USER_MODIFIED);
    }

    void verifications() {
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER, List.of(successDTO, failingDTO));
        verify(crownCourtLitigatorFeesApiClient).updateRepOrders(any(UpdateRepOrdersRequest.class));
        verify(repOrderBillingRepository).saveAll(List.of(failingEntity));
    }
    
    @Test
    void givenSomeFailuresFromCCLF_whenProcessBatchIsInvoked_thenFailingEntitiesAreUpdated() {
        when(crownCourtLitigatorFeesApiClient.updateRepOrders(any())).thenReturn(multiStatusApiResponse);
        when(crownCourtRemunerationApiClient.updateRepOrders(any())).thenReturn(successApiResponse);
        
        when(repOrderBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyList(), anyString())).thenReturn(1);
        when(responseUtils.getErroredIdsFromResponseBody(anyString(), anyString())).thenReturn(List.of(2));

        repOrderBillingService.processBatch(List.of(successDTO, failingDTO), 1);
        
        verifications();
    }

    @Test
    void givenSomeFailuresFromCCR_whenProcessBatchIsInvoked_thenFailingEntitiesAreUpdated() {
        when(crownCourtLitigatorFeesApiClient.updateRepOrders(any())).thenReturn(successApiResponse);
        when(crownCourtRemunerationApiClient.updateRepOrders(any())).thenReturn(multiStatusApiResponse);

        when(repOrderBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyList(), anyString())).thenReturn(1);
        when(responseUtils.getErroredIdsFromResponseBody(anyString(), anyString())).thenReturn(List.of(2));

        repOrderBillingService.processBatch(List.of(successDTO, failingDTO), 1);

        verifications();
    }
}
