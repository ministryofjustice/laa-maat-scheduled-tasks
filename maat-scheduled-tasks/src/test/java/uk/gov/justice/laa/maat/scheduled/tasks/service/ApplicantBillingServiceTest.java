package uk.gov.justice.laa.maat.scheduled.tasks.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.BillingDataFeedLogMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;

import static org.mockito.Mockito.*;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedApplicantBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedBillingFeedLogEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;

@ExtendWith(MockitoExtension.class)
class ApplicantBillingServiceTest {

    private static final int APPLICANT_TEST_ID = 1;
    private static final String USER_MODIFIED = "TEST";

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .build();

    @Mock
    private ApplicantBillingRepository applicantBillingRepository;
    @Mock
    private ApplicantMapper applicantMapper;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private BillingDataFeedLogMapper billingDataFeedLogMapper;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @InjectMocks
    private ApplicantBillingService applicantBillingService;

    @Test
    void givenNoDataAvailable_whenSendApplicantsToBillingIsInvoked_thenNoActionsPerformed() {
        ApplicantBillingDTO dto = getApplicantDTO(APPLICANT_TEST_ID);

        when(applicantBillingRepository.findAllApplicantsForBilling()).thenReturn(Collections.emptyList());

        applicantBillingService.sendApplicantsToBilling(USER_MODIFIED);

        verify(applicantBillingRepository, never()).resetApplicantBilling(List.of(APPLICANT_TEST_ID), USER_MODIFIED);
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT, List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient, never()).updateApplicants(any(UpdateApplicantsRequest.class));
    }

    @Test
    void givenDataAvailable_whenSendApplicantsToBillingIsInvoked_thenDatabaseUpdatedAndBillingCalled() {
        ApplicantBillingEntity entity = getPopulatedApplicantBillingEntity(APPLICANT_TEST_ID);
        ApplicantBillingDTO dto = getApplicantDTO(APPLICANT_TEST_ID);

        when(applicantBillingRepository.findAllApplicantsForBilling()).thenReturn(List.of(entity));
        when(applicantMapper.mapEntityToDTO(entity)).thenReturn(dto);
        when(applicantBillingRepository.resetApplicantBilling(anyList(), anyString())).thenReturn(1);

        applicantBillingService.sendApplicantsToBilling(USER_MODIFIED);

        verify(applicantBillingRepository).resetApplicantBilling(List.of(APPLICANT_TEST_ID), USER_MODIFIED);
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT, List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient).updateApplicants(any(UpdateApplicantsRequest.class));
    }

    @Test
    void givenNoDataAvailable_whenResendApplicantsToBillingIsInvoked_thenNoActionsPerformed() {
        when(billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.APPLICANT))
            .thenReturn(Collections.emptyList());

        applicantBillingService.resendApplicantsToBilling(USER_MODIFIED);

        verify(applicantBillingRepository, never()).resetApplicantBilling(List.of(APPLICANT_TEST_ID), USER_MODIFIED);
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(any(), any());
        verify(crownCourtLitigatorFeesApiClient, never()).updateApplicants(any(UpdateApplicantsRequest.class));
    }

    @Test
    void givenDataAvailable_whenResendApplicantsToBillingIsInvoked_thenDatabaseUpdatedAndBillingCalled()
        throws JsonProcessingException {
        ApplicantBillingDTO applicantDto = getApplicantDTO(APPLICANT_TEST_ID);
        BillingDataFeedLogEntity billingEntity = getPopulatedBillingFeedLogEntity(123, applicantDto, objectMapper);

        when(billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.APPLICANT))
            .thenReturn(List.of(billingEntity));
        when(billingDataFeedLogMapper.mapEntityToDTO(billingEntity)).thenReturn(applicantDto);
        when(applicantBillingRepository.resetApplicantBilling(anyList(), anyString())).thenReturn(1);

        applicantBillingService.resendApplicantsToBilling(USER_MODIFIED);

        verify(applicantBillingRepository).resetApplicantBilling(List.of(APPLICANT_TEST_ID), USER_MODIFIED);
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT, List.of(applicantDto).toString());
        verify(crownCourtLitigatorFeesApiClient).updateApplicants(any(UpdateApplicantsRequest.class));
    }
}
