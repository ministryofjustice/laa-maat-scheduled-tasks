package uk.gov.justice.laa.maat.scheduled.tasks.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.service.ApplicantHistoryBillingService;
import uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder;
import uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder;

@ExtendWith(MockitoExtension.class)
class ApplicantHistoryBillingServiceTest {

    @Mock
    private ApplicantHistoryBillingRepository repository;
    @Mock
    private ApplicantHistoryBillingMapper mapper;
    @InjectMocks
    private ApplicantHistoryBillingService service;

    @Test
    void givenApplicantHistoriesToExtract_whenExtractApplicantHistoryForBillingIsInvoked_thenApplicantHistoriesReturned() {
        when(repository.extractApplicantHistoryForBilling())
            .thenReturn(List.of(TestEntityDataBuilder.getApplicantHistoryBillingEntity()));
        when(mapper.mapEntityToDTO(any(ApplicantHistoryBillingEntity.class)))
            .thenReturn(TestModelDataBuilder.getApplicantHistoryBillingDTO());

        List<ApplicantHistoryBillingDTO> dtos = service.extractApplicantHistory();

        assertEquals(List.of(TestModelDataBuilder.getApplicantHistoryBillingDTO()), dtos);
    }

    @Test
    void givenNoApplicantHistoriesToExtract_whenExtractApplicantForHistoryBillingIsInvoked_thenEmptyListReturned() {
        when(repository.extractApplicantHistoryForBilling()).thenReturn(new ArrayList<>());

        List<ApplicantHistoryBillingDTO> dtos = service.extractApplicantHistory();

        assertTrue(dtos.isEmpty(), "Applicant history billing data returned when none expected.");
    }
}

