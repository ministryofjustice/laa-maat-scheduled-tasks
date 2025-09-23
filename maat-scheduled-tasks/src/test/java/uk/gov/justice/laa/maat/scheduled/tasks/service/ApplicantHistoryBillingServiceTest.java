package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.FileUtils;

@ExtendWith(MockitoExtension.class)
class ApplicantHistoryBillingServiceTest {

    private static final int TEST_ID = 1;
    private static final int FAILING_TEST_ID = 2;
    private static final String USER_MODIFIED = "TEST";

    @Mock
    private ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    @Mock
    private ApplicantHistoryBillingMapper applicantHistoryBillingMapper;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @InjectMocks
    private ApplicantHistoryBillingService applicantHistoryBillingService;

    @Test
    void givenCCLFDataAvailable_whenSendApplicantHistoryToBillingIsInvoked_thenDatabaseUpdatedAndBillingCalled() {
        ApplicantHistoryBillingEntity entity = getApplicantHistoryBillingEntity(TEST_ID);
        ApplicantHistoryBillingDTO dto = getApplicantHistoryBillingDTO(TEST_ID);

        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(
                List.of(entity));
        when(applicantHistoryBillingMapper.mapEntityToDTO(entity)).thenReturn(dto);
        when(applicantHistoryBillingRepository.resetApplicantHistory(anyString(), anyList())).thenReturn(1);
        ResponseEntity<String> apiResponse = new ResponseEntity<>("body here", HttpStatus.OK);
        when(crownCourtLitigatorFeesApiClient.updateApplicantsHistory(any())).thenReturn(apiResponse);
        
        applicantHistoryBillingService.sendApplicantHistoryToBilling(USER_MODIFIED);

        verify(applicantHistoryBillingRepository).resetApplicantHistory(USER_MODIFIED,
                List.of(TEST_ID));
        verify(billingDataFeedLogService).saveBillingDataFeed(
                BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient).updateApplicantsHistory(any(
                UpdateApplicantHistoriesRequest.class));
    }

    @Test
    void givenNoCCLFDataAvailable_whenSendApplicantHistoryToBillingIsInvoked_thenNoActionsPerformed() {
        ApplicantHistoryBillingDTO dto = getApplicantHistoryBillingDTO(TEST_ID);

        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(
                Collections.emptyList());

        applicantHistoryBillingService.sendApplicantHistoryToBilling(USER_MODIFIED);

        verify(applicantHistoryBillingRepository, never()).resetApplicantHistory(USER_MODIFIED,
                List.of(TEST_ID));
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(
                BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(dto).toString());
        verify(crownCourtLitigatorFeesApiClient, never()).updateApplicantsHistory(any(
                UpdateApplicantHistoriesRequest.class));
    }

    @Test
    void givenSomeFailuresFromCCLF_whenSendApplicantHistoryToBillingIsInvoked_thenSetCclfFlagIsCalled() throws Exception {
        ApplicantHistoryBillingEntity successEntity = getApplicantHistoryBillingEntity(TEST_ID);
        ApplicantHistoryBillingEntity failingEntity = getApplicantHistoryBillingEntity(FAILING_TEST_ID);
        ApplicantHistoryBillingDTO successDTO = getApplicantHistoryBillingDTO(TEST_ID);
        ApplicantHistoryBillingDTO failingDTO = getApplicantHistoryBillingDTO(FAILING_TEST_ID);

        String responseBodyJson = FileUtils.readResourceToString("billing/api-client/responses/mixed-status.json");
        
        ResponseEntity<String> apiResponse = new ResponseEntity<>(responseBodyJson, HttpStatus.MULTI_STATUS);
        when(crownCourtLitigatorFeesApiClient.updateApplicantsHistory(any())).thenReturn(apiResponse);

        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(List.of(successEntity, failingEntity));
        when(applicantHistoryBillingMapper.mapEntityToDTO(successEntity)).thenReturn(successDTO);
        when(applicantHistoryBillingMapper.mapEntityToDTO(failingEntity)).thenReturn(failingDTO);
        when(applicantHistoryBillingRepository.resetApplicantHistory(anyString(), anyList())).thenReturn(1);

        applicantHistoryBillingService.sendApplicantHistoryToBilling(USER_MODIFIED);

        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(successDTO, failingDTO).toString());
        verify(crownCourtLitigatorFeesApiClient).updateApplicantsHistory(any(UpdateApplicantHistoriesRequest.class));
        verify(applicantHistoryBillingRepository).setCclfFlag(List.of(FAILING_TEST_ID), USER_MODIFIED, "Y");
    }
}

