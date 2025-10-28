package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedBillingFeedLogEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedBillingFeedLogEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedRepOrderForBilling;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getRepOrderBillingDTO;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Collections;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtRemunerationApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.BillingDataFeedLogMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateRepOrdersRequest;

@ExtendWith(MockitoExtension.class)
class RepOrderBillingServiceTest {

    private static final int REP_ORDER_TEST_ID = 1;
    private static final String USER_MODIFIED = "TEST";

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .build();

    @Mock
    private RepOrderBillingRepository repOrderBillingRepository;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private BillingDataFeedLogMapper billingDataFeedLogMapper;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @Mock
    private CrownCourtRemunerationApiClient crownCourtRemunerationApiClient;
    @InjectMocks
    private RepOrderBillingService repOrderBillingService;


    @Test
    void givenRepOrderDataExists_whenGetRepOrdersForBillingIsInvoked_thenRepOrderDataIsReturned() {
        RepOrderBillingEntity entity = getPopulatedRepOrderForBilling(REP_ORDER_TEST_ID);
        RepOrderBillingDTO dto = getRepOrderBillingDTO(REP_ORDER_TEST_ID);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(entity, entity));

        List<RepOrderBillingDTO> repOrders = repOrderBillingService.getRepOrdersForBilling();

        assertEquals(List.of(dto, dto), repOrders);
    }

    @Test
    void givenNoDataAvailable_whenSendRepOrdersToBillingIsInvoked_thenNoActionsPerformed() {
        repOrderBillingService.sendRepOrdersToBilling(Collections.emptyList(), USER_MODIFIED);

        verify(repOrderBillingRepository, never()).resetBillingFlagForRepOrderIds(
            anyList(), anyString());
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(
            any(BillingDataFeedRecordType.class), anyList());
        verify(crownCourtLitigatorFeesApiClient, never()).updateRepOrders(
            any(UpdateRepOrdersRequest.class));
    }

    @Test
    void givenDataAvailable_whenSendRepOrdersToBillingIsInvoked_thenDatabaseUpdatedAndBillingCalled() {
        RepOrderBillingDTO dto = getRepOrderBillingDTO(REP_ORDER_TEST_ID);

        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyList(), anyString()))
            .thenReturn(1);

        repOrderBillingService.sendRepOrdersToBilling(List.of(dto), USER_MODIFIED);

        verify(repOrderBillingRepository).resetBillingFlagForRepOrderIds(
            List.of(REP_ORDER_TEST_ID), USER_MODIFIED);
        verify(billingDataFeedLogService).saveBillingDataFeed(
            BillingDataFeedRecordType.REP_ORDER, List.of(dto));
        verify(crownCourtLitigatorFeesApiClient).updateRepOrders(any(UpdateRepOrdersRequest.class));
    }

    @Test
    void givenNoDataAvailable_whenResendApplicantsToBillingIsInvoked_thenNoActionsPerformed() {
        when(billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.REP_ORDER))
            .thenReturn(Collections.emptyList());

        repOrderBillingService.resendRepOrdersToBilling();

        verify(repOrderBillingRepository, never()).resetBillingFlagForRepOrderIds(any(), any());
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(any(), any());
        verify(crownCourtLitigatorFeesApiClient, never()).updateRepOrders(
            any(UpdateRepOrdersRequest.class));
    }

    @Test
    void givenDataAvailable_whenResendApplicantsToBillingIsInvoked_thenDatabaseUpdatedAndBillingCalled()
        throws JsonProcessingException {
        RepOrderBillingDTO repOrderDto = getRepOrderBillingDTO(REP_ORDER_TEST_ID);
        BillingDataFeedLogEntity billingEntity = getPopulatedBillingFeedLogEntity(
            123, repOrderDto, objectMapper);

        when(billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.REP_ORDER))
            .thenReturn(List.of(billingEntity));
        when(billingDataFeedLogMapper.mapEntityToRepOrderBillingDtos(billingEntity))
            .thenReturn(List.of(repOrderDto));

        repOrderBillingService.resendRepOrdersToBilling();

        verify(repOrderBillingRepository, never()).resetBillingFlagForRepOrderIds(any(), any());
        verify(billingDataFeedLogService).saveBillingDataFeed(
            BillingDataFeedRecordType.REP_ORDER, List.of(repOrderDto));
        verify(crownCourtLitigatorFeesApiClient).updateRepOrders(any(UpdateRepOrdersRequest.class));
    }
}
