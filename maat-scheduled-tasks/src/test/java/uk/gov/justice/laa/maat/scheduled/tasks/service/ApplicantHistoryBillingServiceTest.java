package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getApplicantHistoryBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantHistoryBillingDTO;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;

@ExtendWith(MockitoExtension.class)
class ApplicantHistoryBillingServiceTest {

    private static final int TEST_ID = 1;
    private static final String USER_MODIFIED = "TEST";

    @Mock
    private ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    @Mock
    private ApplicantHistoryBillingMapper applicantHistoryBillingMapper;
    @Mock
    private BillingConfiguration billingConfiguration;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @InjectMocks
    private ApplicantHistoryBillingService applicantHistoryBillingService;

    @Test
    void givenDataDoesNotRequireBatchedRequests_whenSendApplicantHistoryToBillingIsInvoked_thenBillingIsCalledOnce() {
        ApplicantHistoryBillingEntity entity = getApplicantHistoryBillingEntity(TEST_ID);
        ApplicantHistoryBillingDTO dto = getApplicantHistoryBillingDTO(TEST_ID);

        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(List.of(entity, entity));
        when(applicantHistoryBillingMapper.mapEntityToDTO(entity)).thenReturn(dto);
        when(applicantHistoryBillingRepository.resetApplicantHistory(anyString(), anyList())).thenReturn(1);
        when(billingConfiguration.getRequestBatchSize()).thenReturn("5");
        when(billingConfiguration.getResetBatchSize()).thenReturn("1000");

        applicantHistoryBillingService.sendApplicantHistoryToBilling(USER_MODIFIED);

        verify(applicantHistoryBillingRepository, times(1)).resetApplicantHistory(USER_MODIFIED, List.of(TEST_ID, TEST_ID));
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(dto, dto).toString());
        verify(crownCourtLitigatorFeesApiClient, times(1)).updateApplicantsHistory(any(
            UpdateApplicantHistoriesRequest.class));
    }

    @Test
    void givenDataRequiresBatchedRequests_whenSendApplicantHistoryToBillingIsInvoked_thenBillingIsCalledMultipleTimes() {
        ApplicantHistoryBillingEntity entity = getApplicantHistoryBillingEntity(TEST_ID);
        ApplicantHistoryBillingDTO dto = getApplicantHistoryBillingDTO(TEST_ID);

        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(List.of(entity, entity));
        when(applicantHistoryBillingMapper.mapEntityToDTO(entity)).thenReturn(dto);
        when(applicantHistoryBillingRepository.resetApplicantHistory(anyString(), anyList())).thenReturn(1);
        when(billingConfiguration.getRequestBatchSize()).thenReturn("1");
        when(billingConfiguration.getResetBatchSize()).thenReturn("1");

        applicantHistoryBillingService.sendApplicantHistoryToBilling(USER_MODIFIED);

        verify(applicantHistoryBillingRepository, times(2)).resetApplicantHistory(USER_MODIFIED, List.of(TEST_ID));
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(dto, dto).toString());
        verify(crownCourtLitigatorFeesApiClient, times(2)).updateApplicantsHistory(any(
            UpdateApplicantHistoriesRequest.class));
    }

    @Test
    void givenNoCCLFDataAvailable_whenSendApplicantHistoryToBillingIsInvoked_thenNoActionsPerformed() {
        ApplicantHistoryBillingDTO dto = getApplicantHistoryBillingDTO(TEST_ID);

        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(Collections.emptyList());

        applicantHistoryBillingService.sendApplicantHistoryToBilling(USER_MODIFIED);

        verify(applicantHistoryBillingRepository, never()).resetApplicantHistory(USER_MODIFIED, List.of(TEST_ID));
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient, never()).updateApplicantsHistory(any(
            UpdateApplicantHistoriesRequest.class));
    }
}

