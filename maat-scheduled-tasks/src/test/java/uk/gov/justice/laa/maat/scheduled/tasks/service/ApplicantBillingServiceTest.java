package uk.gov.justice.laa.maat.scheduled.tasks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateBillingRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedApplicantBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;

@ExtendWith(MockitoExtension.class)
class ApplicantBillingServiceTest {

    private static final int TEST_ID_1 = 1;
    private static final int TEST_ID_2 = 2;
    private static final String USERNAME = "test_user";

    @Mock
    private ApplicantBillingRepository applicantBillingRepository;
    @Mock
    private ApplicantMapper applicantMapper;
    @InjectMocks
    private ApplicantBillingService applicantBillingService;

    @Test
    void givenApplicantData_WhenExtracted_ThenMappedCorrectlyToDTOList() {
        ApplicantBillingEntity entity1 = getPopulatedApplicantBillingEntity(TEST_ID_1);
        ApplicantBillingEntity entity2 = getPopulatedApplicantBillingEntity(TEST_ID_2);

        when(applicantBillingRepository.findAllApplicantsForBilling()).thenReturn(List.of(entity1, entity2));

        ApplicantBillingDTO dto1 = getApplicantDTO(TEST_ID_1);
        ApplicantBillingDTO dto2 = getApplicantDTO(TEST_ID_2);

        when(applicantMapper.mapEntityToDTO(entity1)).thenReturn(dto1);
        when(applicantMapper.mapEntityToDTO(entity2)).thenReturn(dto2);

        assertEquals(List.of(dto1, dto2), applicantBillingService.findAllApplicantsForBilling());
        verify(applicantBillingRepository, times(1)).findAllApplicantsForBilling();
    }

    @Test
    void givenAnUpdateBillingRequest_whenResetApplicantBillingCalled_shouldCallResetApplicantBillingOnRepository() {
        UpdateBillingRequest request = new UpdateBillingRequest();
        request.setUserModified(USERNAME);
        request.setIds(List.of(TEST_ID_1, TEST_ID_2));

        applicantBillingService.resetApplicantBilling(request);

        verify(applicantBillingRepository).resetApplicantBilling(request.getIds(), request.getUserModified());
    }
}
