package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtRemunerationApiClient;
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
    @Mock
    private CrownCourtRemunerationApiClient crownCourtRemunerationApiClient;
    @InjectMocks
    private ApplicantHistoryBillingService applicantHistoryBillingService;

    @Test
    void givenApplicantHistoryDataExists_whenExtractApplicantHistoryIsInvoked_thenApplicantHistoryDataIsReturned() {
        ApplicantHistoryBillingEntity entity = getApplicantHistoryBillingEntity(APPLICANT_HISTORY_TEST_ID);
        ApplicantHistoryBillingDTO dto = getApplicantHistoryBillingDTO(APPLICANT_HISTORY_TEST_ID);

        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(
            List.of(entity, entity));
        when(applicantHistoryBillingMapper.mapEntityToDTO(entity)).thenReturn(dto);

        List<ApplicantHistoryBillingDTO> applicantHistories = applicantHistoryBillingService.extractApplicantHistory();

        assertEquals(List.of(dto, dto), applicantHistories);
    }

    @Test
    void givenNoDataAvailable_whenSendApplicantHistoryToBillingIsInvoked_thenNoActionsPerformed() {
        applicantHistoryBillingService.sendApplicantHistoryToBilling(Collections.emptyList(), USER_MODIFIED);

        verify(applicantHistoryBillingRepository, never()).resetApplicantHistory(
            List.of(APPLICANT_HISTORY_TEST_ID), USER_MODIFIED);
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(
            any(BillingDataFeedRecordType.class), anyList());
        verify(crownCourtLitigatorFeesApiClient, never()).updateApplicantsHistory(
            any(UpdateApplicantHistoriesRequest.class));
    }

    @Test
    void giveDataAvailable_whenSendApplicantHistoryToBillingIsInvoked_thenDatabaseUpdatedAndBillingCalled() {
        ApplicantHistoryBillingDTO dto = getApplicantHistoryBillingDTO(APPLICANT_HISTORY_TEST_ID);

        when(applicantHistoryBillingRepository.resetApplicantHistory(
            anyList(), anyString())).thenReturn(1);

        applicantHistoryBillingService.sendApplicantHistoryToBilling(List.of(dto), USER_MODIFIED);

        verify(applicantHistoryBillingRepository).resetApplicantHistory(
            List.of(APPLICANT_HISTORY_TEST_ID), USER_MODIFIED);
        verify(billingDataFeedLogService).saveBillingDataFeed(
            BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(dto));
        verify(crownCourtLitigatorFeesApiClient).updateApplicantsHistory(
            any(UpdateApplicantHistoriesRequest.class));
        verify(crownCourtRemunerationApiClient).updateApplicantsHistory(
            any(UpdateApplicantHistoriesRequest.class));
    }

    @Test
    void givenNoDataAvailable_whenResendApplicantHistoryToBillingIsInvoked_thenNoActionsPerformed() {
        when(billingDataFeedLogService.getBillingDataFeedLogs(
            BillingDataFeedRecordType.APPLICANT_HISTORY))
            .thenReturn(Collections.emptyList());

        applicantHistoryBillingService.resendApplicantHistoryToBilling();

        verify(applicantHistoryBillingRepository, never()).resetApplicantHistory(any(), any());
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(any(), any());
        verify(crownCourtLitigatorFeesApiClient, never()).updateApplicantsHistory(
            any(UpdateApplicantHistoriesRequest.class));
    }

    @Test
    void givenDataAvailable_whenResendApplicantHistoryToBillingIsInvoked_thenDatabaseUpdatedAndBillingCalled()
        throws JsonProcessingException {
        ApplicantHistoryBillingDTO applicantHistoryDto = getApplicantHistoryBillingDTO(
            APPLICANT_HISTORY_TEST_ID);
        BillingDataFeedLogEntity billingEntity = getPopulatedBillingFeedLogEntity(123,
            applicantHistoryDto, objectMapper);

        when(billingDataFeedLogService.getBillingDataFeedLogs(
            BillingDataFeedRecordType.APPLICANT_HISTORY))
            .thenReturn(List.of(billingEntity));
        when(billingDataFeedLogMapper.mapEntityToApplicationHistoryBillingDtos(billingEntity))
            .thenReturn(List.of(applicantHistoryDto));

        applicantHistoryBillingService.resendApplicantHistoryToBilling();

        verify(applicantHistoryBillingRepository, never()).resetApplicantHistory(any(), any());
        verify(billingDataFeedLogService).saveBillingDataFeed(
            BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(applicantHistoryDto));
        verify(crownCourtLitigatorFeesApiClient).updateApplicantsHistory(
            any(UpdateApplicantHistoriesRequest.class));
    }
}
