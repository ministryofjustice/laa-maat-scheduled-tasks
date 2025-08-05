package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
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

    @Test
    void givenValidDataProvided_whenResetApplicantHistoryIsInvoked_thenRepositoryIsCalled() {
        ResetBillingDTO resetBillingDTO = TestModelDataBuilder.getResetBillingDTO();
        List<Integer> ids = resetBillingDTO.getIds();
        when(repository.resetApplicantHistory(anyString(), anyList())).thenReturn(ids.size());

        service.resetApplicantHistory(resetBillingDTO);

        verify(repository).resetApplicantHistory(resetBillingDTO.getUserModified(), ids);
    }
}

