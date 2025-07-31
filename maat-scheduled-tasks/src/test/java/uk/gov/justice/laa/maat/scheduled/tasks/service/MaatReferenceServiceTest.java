package uk.gov.justice.laa.maat.scheduled.tasks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.MaatReferenceRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class MaatReferenceServiceTest {

    @Mock
    private MaatReferenceRepository repository;

    @InjectMocks
    private MaatReferenceService service;

    @Test
    void givenTableIsEmpty_whenPopulateMaatReferencesIsInvoked_thenRepositoryIsCalled() {
        when(repository.count()).thenReturn(Long.valueOf(0));

        service.populateMaatReferences();

        verify(repository).populateMaatReferences();
    }

    @Test
    void givenTableIsPopulated_whenPopulateMaatReferencesIsInvoked_thenExceptionIsThrown() {
        when(repository.count()).thenReturn(Long.valueOf(666));

        assertThatThrownBy(() -> {
            service.populateMaatReferences();
        }).isInstanceOf(MAATScheduledTasksException.class)
            .hasMessageContaining("The maat references table is already populated.");
    }

    @Test
    void givenTableIsPopulated_whenDeleteMaatReferencesIsInvoked_thenRepositoryIsCalled() {
        service.deleteMaatReferences();

        verify(repository).deleteMaatReferences();
    }
}
