package uk.gov.justice.laa.maat.scheduled.tasks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedApplicantBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;

@ExtendWith(MockitoExtension.class)
class ApplicantBillingServiceTest {

    private static final int TEST_ID = 1;
    private static final String USER_MODIFIED = "TEST";

    @Mock
    private ApplicantBillingRepository applicantBillingRepository;
    @Mock
    private ApplicantMapper applicantMapper;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @InjectMocks
    private ApplicantBillingService applicantBillingService;


    @Test
    void givenApplicantDataExists_whenFindApplicantsForBillingIsInvoked_thenApplicantDataIsReturned() {
        ApplicantBillingEntity entity = getPopulatedApplicantBillingEntity(TEST_ID);
        ApplicantBillingDTO dto = getApplicantDTO(TEST_ID);

        when(applicantBillingRepository.findAllApplicantsForBilling()).thenReturn(
            List.of(entity, entity));
        when(applicantMapper.mapEntityToDTO(entity)).thenReturn(dto);

        List<ApplicantBillingDTO> applicants = applicantBillingService.findAllApplicantsForBilling();

        assertEquals(List.of(dto, dto), applicants);
    }

    @Test
    void givenValidData_whenSendApplicantsToBillingIsInvoked_thenDatabaseUpdatedAndCCLFCalled() {
        ApplicantBillingDTO dto = getApplicantDTO(TEST_ID);

        when(applicantBillingRepository.resetApplicantBilling(anyList(), anyString())).thenReturn(
            1);

        applicantBillingService.sendApplicantsToBilling(List.of(dto), USER_MODIFIED);

        verify(applicantBillingRepository).resetApplicantBilling(List.of(TEST_ID), USER_MODIFIED);
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT,
            List.of(dto));
        verify(crownCourtLitigatorFeesApiClient).updateApplicants(
            any(UpdateApplicantsRequest.class));
    }
}
