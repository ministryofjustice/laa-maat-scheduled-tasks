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
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
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
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @InjectMocks
    private RepOrderBillingService repOrderBillingService;


    @Test
    void givenRepOrderDataExists_whenGetRepOrdersForBillingIsInvoked_thenRepOrderDataIsReturned() {
        RepOrderBillingEntity entity = getPopulatedRepOrderForBilling(TEST_ID);
        RepOrderBillingDTO dto = getRepOrderBillingDTO(TEST_ID);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(entity, entity));

        List<RepOrderBillingDTO> repOrders = repOrderBillingService.getRepOrdersForBilling();

        assertEquals(List.of(dto, dto), repOrders);
    }

    @Test
    void givenValidData_whenSendRepOrdersToBillingIsInvoked_thenDatabaseUpdatedAndCCLFCalled() {
        RepOrderBillingDTO dto = getRepOrderBillingDTO(TEST_ID);

        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyString(), anyList())).thenReturn(1);

        repOrderBillingService.sendRepOrdersToBilling(List.of(dto), USER_MODIFIED);

        verify(repOrderBillingRepository).resetBillingFlagForRepOrderIds(USER_MODIFIED, List.of(TEST_ID));
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER, List.of(dto));
        verify(crownCourtLitigatorFeesApiClient).updateRepOrders(any(UpdateRepOrdersRequest.class));
    }
}
