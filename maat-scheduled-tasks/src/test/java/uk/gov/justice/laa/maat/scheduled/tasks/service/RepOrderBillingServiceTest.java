package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getApplicantHistoryBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedRepOrderForBilling;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantHistoryBillingDTO;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getRepOrderBillingDTO;


import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder;
import uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetRepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.RepOrderBillingMapper;
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
    void giveCCLFDataAvailable_whenSendRepOrdersToBillingIsInvoked_thenDatabaseUpdatedAndBillingCalled() {
        RepOrderBillingEntity entity = getPopulatedRepOrderForBilling(TEST_ID);
        RepOrderBillingDTO dto = getRepOrderBillingDTO(TEST_ID);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(entity));
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(anyString(), anyList())).thenReturn(1);

        repOrderBillingService.sendRepOrdersToBilling(USER_MODIFIED);

        verify(repOrderBillingRepository).resetBillingFlagForRepOrderIds(USER_MODIFIED, List.of(TEST_ID));
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER, List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient).updateRepOrders(any(UpdateRepOrdersRequest.class));
    }

    @Test
    void givenNoCCLFDataAvailable_whenSendRepOrdersToBillingIsInvoked_thenNoActionsPerformed() {
        RepOrderBillingDTO dto = getRepOrderBillingDTO(TEST_ID);

        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(Collections.emptyList());

        repOrderBillingService.sendRepOrdersToBilling(USER_MODIFIED);

        verify(repOrderBillingRepository, never()).resetBillingFlagForRepOrderIds(USER_MODIFIED, List.of(TEST_ID));
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(BillingDataFeedRecordType.REP_ORDER, List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient, never()).updateRepOrders(any(UpdateRepOrdersRequest.class));
    }
}
