package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;

import static org.mockito.Mockito.*;
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
    private BillingConfiguration billingConfiguration;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @InjectMocks
    private ApplicantBillingService applicantBillingService;

    @Test
    void givenDataDoesNotRequireBatchedRequests_whenSendApplicantsToBillingIsInvoked_thenBillingIsCalledOnce() {
        ApplicantBillingEntity entity = getPopulatedApplicantBillingEntity(TEST_ID);
        ApplicantBillingDTO dto = getApplicantDTO(TEST_ID);

        when(applicantBillingRepository.findAllApplicantsForBilling()).thenReturn(List.of(entity, entity));
        when(applicantMapper.mapEntityToDTO(entity)).thenReturn(dto);
        when(applicantBillingRepository.resetApplicantBilling(anyList(), anyString())).thenReturn(1);
        when(billingConfiguration.getRequestBatchSize()).thenReturn(5);
        when(billingConfiguration.getResetBatchSize()).thenReturn(1000);

        applicantBillingService.sendApplicantsToBilling(USER_MODIFIED);

        verify(applicantBillingRepository, times(1)).resetApplicantBilling(List.of(TEST_ID, TEST_ID), USER_MODIFIED);
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT, List.of(dto, dto).toString());
        verify(crownCourtLitigatorFeesApiClient, times(1)).updateApplicants(any(UpdateApplicantsRequest.class));
    }

    @Test
    void givenDataRequiresBatchedRequests_whenSendApplicantsToBillingIsInvoked_thenBillingIsCalledMultipleTimes() {
        ApplicantBillingEntity entity = getPopulatedApplicantBillingEntity(TEST_ID);
        ApplicantBillingDTO dto = getApplicantDTO(TEST_ID);

        when(applicantBillingRepository.findAllApplicantsForBilling()).thenReturn(List.of(entity, entity));
        when(applicantMapper.mapEntityToDTO(entity)).thenReturn(dto);
        when(applicantBillingRepository.resetApplicantBilling(anyList(), anyString())).thenReturn(1);
        when(billingConfiguration.getRequestBatchSize()).thenReturn(1);
        when(billingConfiguration.getResetBatchSize()).thenReturn(1);

        applicantBillingService.sendApplicantsToBilling(USER_MODIFIED);

        verify(applicantBillingRepository, times(2)).resetApplicantBilling(List.of(TEST_ID), USER_MODIFIED);
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT, List.of(dto, dto).toString());
        verify(crownCourtLitigatorFeesApiClient, times(2)).updateApplicants(any(UpdateApplicantsRequest.class));
    }

    @Test
    void givenNoCCLFDataAvailable_whenSendApplicantsToBillingIsInvoked_thenNoActionsPerformed() {
        ApplicantBillingDTO dto = getApplicantDTO(TEST_ID);

        when(applicantBillingRepository.findAllApplicantsForBilling()).thenReturn(Collections.emptyList());

        applicantBillingService.sendApplicantsToBilling(USER_MODIFIED);

        verify(applicantBillingRepository, never()).resetApplicantBilling(List.of(TEST_ID), USER_MODIFIED);
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT, List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient, never()).updateApplicants(any(UpdateApplicantsRequest.class));
    }
}
