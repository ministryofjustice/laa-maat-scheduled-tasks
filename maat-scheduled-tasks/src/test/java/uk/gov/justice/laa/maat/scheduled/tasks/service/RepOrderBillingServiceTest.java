package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedRepOrderForBilling;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getRepOrderBillingDTO;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
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

    @Mock
    private RepOrderBillingRepository repOrderBillingRepository;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @InjectMocks
    private RepOrderBillingService repOrderBillingService;

    @Test
    void giveCCLFDataAvailable_whenSendRepOrdersToBillingIsInvoked_thenDatabaseUpdatedAndBillingCalled() {
        RepOrderBillingEntity entity = getPopulatedRepOrderForBilling(TEST_ID);
        RepOrderBillingDTO dto = getRepOrderBillingDTO(TEST_ID);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(entity));
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyString(),
                anyList())).thenReturn(1);

        repOrderBillingService.sendRepOrdersToBilling(USER_MODIFIED);

        verify(repOrderBillingRepository).resetBillingFlagForRepOrderIds(USER_MODIFIED,
                List.of(TEST_ID));
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER,
                List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient).updateRepOrders(any(UpdateRepOrdersRequest.class));
    }

    @Test
    void givenNoCCLFDataAvailable_whenSendRepOrdersToBillingIsInvoked_thenNoActionsPerformed() {
        RepOrderBillingDTO dto = getRepOrderBillingDTO(TEST_ID);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(
                Collections.emptyList());

        repOrderBillingService.sendRepOrdersToBilling(USER_MODIFIED);

        verify(repOrderBillingRepository, never()).resetBillingFlagForRepOrderIds(USER_MODIFIED,
                List.of(TEST_ID));
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(
                BillingDataFeedRecordType.REP_ORDER, List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient, never()).updateRepOrders(
                any(UpdateRepOrdersRequest.class));
    }

    @Test
    void givenSomeFailuresFromCCLF_whenSendRepOrdersToBillingIsInvoked_thenSetCclfFlagIsCalled() throws Exception {
        RepOrderBillingEntity successEntity = getPopulatedRepOrderForBilling(TEST_ID);
        RepOrderBillingEntity failingEntity = getPopulatedRepOrderForBilling(FAILING_TEST_ID);
        RepOrderBillingDTO successDTO = getRepOrderBillingDTO(TEST_ID);
        RepOrderBillingDTO failingDTO = getRepOrderBillingDTO(FAILING_TEST_ID);

        String responseBodyJson = FileUtils.readResourceToString("billing/api-client/responses/mixed-status.json");
        byte[] responseBody = responseBodyJson.getBytes(StandardCharsets.UTF_8);

        UpdateRepOrdersRequest updateRepOrdersRequest = UpdateRepOrdersRequest.builder()
            .repOrders(List.of(successDTO, failingDTO)).build();

        RestClientResponseException expectedException = new RestClientResponseException(
            "Multi-Status", HttpStatus.MULTI_STATUS, "Multi-Status", null, responseBody, null
        );

        doThrow(expectedException).when(crownCourtLitigatorFeesApiClient).updateRepOrders(updateRepOrdersRequest);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(successEntity, failingEntity));
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyString(), anyList())).thenReturn(1);

        repOrderBillingService.sendRepOrdersToBilling(USER_MODIFIED);

        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER, List.of(successDTO, failingDTO).toString());
        verify(crownCourtLitigatorFeesApiClient).updateRepOrders(any(UpdateRepOrdersRequest.class));
        verify(repOrderBillingRepository).setCclfFlag(List.of(FAILING_TEST_ID), USER_MODIFIED, "Y");
    }
}
