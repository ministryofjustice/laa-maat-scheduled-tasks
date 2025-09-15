package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getApplicantHistoryBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantHistoryBillingDTO;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedBillingFeedLogEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.BillingDataFeedLogMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;

@ExtendWith(MockitoExtension.class)
class ApplicantHistoryBillingServiceTest {

    private static final int APPLICANT_HISTORY_TEST_ID = 1;
    private static final String USER_MODIFIED = "TEST";

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .build();

    @Mock
    private ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    @Mock
    private ApplicantHistoryBillingMapper applicantHistoryBillingMapper;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private BillingDataFeedLogMapper billingDataFeedLogMapper;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @InjectMocks
    private ApplicantHistoryBillingService applicantHistoryBillingService;

    @Test
    void givenNoDataAvailable_whenSendApplicantHistoryToBillingIsInvoked_thenNoActionsPerformed() {
        ApplicantHistoryBillingDTO dto = getApplicantHistoryBillingDTO(APPLICANT_HISTORY_TEST_ID);

        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(Collections.emptyList());

        applicantHistoryBillingService.sendApplicantHistoryToBilling(USER_MODIFIED);

        verify(applicantHistoryBillingRepository, never()).resetApplicantHistory(USER_MODIFIED, List.of(
            APPLICANT_HISTORY_TEST_ID));
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient, never()).updateApplicantsHistory(any(
            UpdateApplicantHistoriesRequest.class));
    }

    @Test
    void giveDataAvailable_whenSendApplicantHistoryToBillingIsInvoked_thenDatabaseUpdatedAndBillingCalled() {
        ApplicantHistoryBillingEntity entity = getApplicantHistoryBillingEntity(
            APPLICANT_HISTORY_TEST_ID);
        ApplicantHistoryBillingDTO dto = getApplicantHistoryBillingDTO(APPLICANT_HISTORY_TEST_ID);

        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(
                List.of(entity));
        when(applicantHistoryBillingMapper.mapEntityToDTO(entity)).thenReturn(dto);
        when(applicantHistoryBillingRepository.resetApplicantHistory(anyString(),
                anyList())).thenReturn(1);

        applicantHistoryBillingService.sendApplicantHistoryToBilling(USER_MODIFIED);

        verify(applicantHistoryBillingRepository).resetApplicantHistory(USER_MODIFIED, List.of(
            APPLICANT_HISTORY_TEST_ID));
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(dto).toString());
        verify(applicantHistoryBillingRepository).resetApplicantHistory(USER_MODIFIED,
                List.of(APPLICANT_HISTORY_TEST_ID));
        verify(billingDataFeedLogService).saveBillingDataFeed(
                BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient).updateApplicantsHistory(any(
                UpdateApplicantHistoriesRequest.class));
    }

    @Test
    void givenNoDataAvailable_whenResendApplicantHistoryToBillingIsInvoked_thenNoActionsPerformed() {
        when(billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.APPLICANT_HISTORY))
            .thenReturn(Collections.emptyList());

        applicantHistoryBillingService.resendApplicantHistoryToBilling(USER_MODIFIED);

        verify(applicantHistoryBillingRepository, never()).resetApplicantHistory(USER_MODIFIED, List.of(
            APPLICANT_HISTORY_TEST_ID));
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(any(), any());
        verify(crownCourtLitigatorFeesApiClient, never()).updateApplicantsHistory(any(UpdateApplicantHistoriesRequest.class));
    }

    @Test
    void givenDataAvailable_whenResendApplicantHistoryToBillingIsInvoked_thenDatabaseUpdatedAndBillingCalled()
        throws JsonProcessingException {
        ApplicantHistoryBillingDTO applicantHistoryDto = getApplicantHistoryBillingDTO(APPLICANT_HISTORY_TEST_ID);
        BillingDataFeedLogEntity billingEntity = getPopulatedBillingFeedLogEntity(123, applicantHistoryDto, objectMapper);

        when(billingDataFeedLogService.getBillingDataFeedLogs(BillingDataFeedRecordType.APPLICANT_HISTORY))
            .thenReturn(List.of(billingEntity));
        when(billingDataFeedLogMapper.mapEntityToDTO(billingEntity)).thenReturn(applicantHistoryDto);
        when(applicantHistoryBillingRepository.resetApplicantHistory(anyString(), anyList())).thenReturn(1);

        applicantHistoryBillingService.resendApplicantHistoryToBilling(USER_MODIFIED);

        verify(applicantHistoryBillingRepository).resetApplicantHistory(USER_MODIFIED, List.of(
            APPLICANT_HISTORY_TEST_ID));
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(applicantHistoryDto).toString());
        verify(crownCourtLitigatorFeesApiClient).updateApplicantsHistory(any(UpdateApplicantHistoriesRequest.class));
    }
}
