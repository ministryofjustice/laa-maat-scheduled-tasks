package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder;
import uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetRepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;

@ExtendWith(MockitoExtension.class)
class RepOrderBillingServiceTest {

    @Mock
    private RepOrderBillingRepository repOrderBillingRepository;

    @InjectMocks
    private RepOrderBillingService repOrderBillingService;

    @Test
    void givenNoRepOrders_whenGetRepOrdersForBillingIsInvoked_thenEmptyListIsReturned() {
        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(Collections.emptyList());

        List<RepOrderBillingDTO> repOrders = repOrderBillingService.getRepOrdersForBilling();

        assertEquals(Collections.emptyList(), repOrders);

    }

    @Test
    void givenRepOrdersExist_whenGetRepOrdersForBillingIsInvoked_thenRepOrdersAreReturned() {
        when(repOrderBillingRepository.getRepOrdersForBilling()).thenReturn(List.of(
                TestEntityDataBuilder.getPopulatedRepOrderForBilling(123),
                TestEntityDataBuilder.getPopulatedRepOrderForBilling(124)
        ));

        List<RepOrderBillingDTO> expectedRepOrders = List.of(
                TestModelDataBuilder.getRepOrderBillingDTO(123),
                TestModelDataBuilder.getRepOrderBillingDTO(124)
        );

        List<RepOrderBillingDTO> repOrders = repOrderBillingService.getRepOrdersForBilling();

        assertEquals(2, repOrders.size());
        assertEquals(expectedRepOrders, repOrders);
    }

    @Test
    void givenRepOrdersNotSuccessfullyUpdated_whenResetRepOrdersSentForBillingIsInvoked_thenReturnsFalse() {
        ResetRepOrderBillingDTO resetRepOrderBillingDTO = TestModelDataBuilder.getResetRepOrderBillingDTO();
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(resetRepOrderBillingDTO.getUserModified(), resetRepOrderBillingDTO.getIds()))
                .thenReturn(1);

        MAATScheduledTasksException exception = assertThrows(MAATScheduledTasksException.class,
                () -> repOrderBillingService.resetRepOrdersSentForBilling(resetRepOrderBillingDTO));

        assertEquals(
                "Unable to reset rep orders sent for billing as only 1 rep order(s) could be processed (from a total of 2 rep order(s))"
                , exception.getMessage());
    }

    @Test
    void givenRepOrdersSuccessfullyUpdated_whenResetRepOrdersSentForBillingIsInvoked_thenReturnsTrue() {
        ResetRepOrderBillingDTO resetRepOrderBillingDTO = TestModelDataBuilder.getResetRepOrderBillingDTO();
        when(repOrderBillingRepository.resetBillingFlagForRepOrderIds(
            resetRepOrderBillingDTO.getUserModified(), resetRepOrderBillingDTO.getIds()))
                .thenReturn(resetRepOrderBillingDTO.getIds().size());

        assertDoesNotThrow(() -> repOrderBillingService.resetRepOrdersSentForBilling(resetRepOrderBillingDTO));
    }
}
