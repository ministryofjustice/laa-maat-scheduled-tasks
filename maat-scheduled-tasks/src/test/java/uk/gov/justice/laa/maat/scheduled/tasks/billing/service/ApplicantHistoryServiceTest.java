package uk.gov.justice.laa.maat.scheduled.tasks.billing.service;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.repository.ApplicantHistoryRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ApplicantHistoryServiceTest {

    @Mock
    private ApplicantHistoryRepository repository;

    @InjectMocks
    private ApplicantHistoryService service;

    @Test
    void givenValidDataProvided_whenResetApplicantHistoryIsInvoked_thenRepositoryIsCalled() {
        String userModified = "test-u";
        List<Integer> ids = List.of(1, 2, 3);
        when(repository.resetApplicantHistory(anyString(), anyList())).thenReturn(ids.size());

        service.resetApplicantHistory(userModified, ids);

        verify(repository).resetApplicantHistory(userModified, ids);
    }

    @Test
    void givenLessRowsUpdated_whenResetApplicantHistoryIsInvoked_thenExceptionIsThrown() {
        String userModified = "test-u";
        List<Integer> ids = List.of(1, 2, 3);
        when(repository.resetApplicantHistory(anyString(), anyList())).thenReturn(ids.size() - 1);

        assertThatThrownBy(() -> {
            service.resetApplicantHistory(userModified, ids);
        }).isInstanceOf(MAATScheduledTasksException.class).hasMessageContaining(String.format(
            "Number of applicant histories reset: %d, does not equal those supplied: %d.",
            ids.size() - 1, ids.size()));
    }
}
