package uk.gov.justice.laa.maat.scheduled.tasks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.XhibitAppealDataRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.service.TrialDataService.APPEAL_DATA_TO_MAAT_PROCEDURE;

@ExtendWith(MockitoExtension.class)
public class AppealDataServiceTest {

    @Mock
    private XhibitAppealDataRepository appealDataRepository;

    @Mock
    private StoredProcedureService storedProcedureService;

    @InjectMocks
    private TrialDataService trialDataService;
    
    @Test
    void processAppealDataInToMaat() {
        when(appealDataRepository.findAllUnprocessedIds()).thenReturn(List.of(1, 2, 3));

        trialDataService.processAppealDataInToMaat();

        verify(appealDataRepository, times(1)).findAllUnprocessedIds();
        verify(storedProcedureService, times(3)).callStoredProcedure(eq(APPEAL_DATA_TO_MAAT_PROCEDURE), anyMap());
    }
}
