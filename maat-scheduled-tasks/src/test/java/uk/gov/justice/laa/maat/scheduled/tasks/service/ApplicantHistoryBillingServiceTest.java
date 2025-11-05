package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getApplicantHistoryBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantHistoryBillingDTO;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtRemunerationApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.FileUtils;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class ApplicantHistoryBillingServiceTest {

    private static final int SUCCESSFUL_TEST_ID_1 = 1;
    private static final int FAILING_TEST_ID = 2;
    private static final String USER_MODIFIED = "TEST";
    private ApplicantHistoryBillingEntity failingEntity;
    private ApplicantHistoryBillingDTO successDTO;
    private ApplicantHistoryBillingDTO failingDTO;
    private String multiStatusResponseBodyJson;
    ResponseEntity<String> successApiResponse;
    ResponseEntity<String> multiStatusApiResponse;

    @Mock
    private ResponseUtils responseUtils;
    @Mock
    private ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    @Mock
    private BillingConfiguration billingConfiguration;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @Mock
    private CrownCourtRemunerationApiClient crownCourtRemunerationApiClient;
    @InjectMocks
    private ApplicantHistoryBillingService applicantHistoryBillingService;

    @BeforeEach
    void setUp() throws IOException {
        failingEntity = getApplicantHistoryBillingEntity(FAILING_TEST_ID);
        successDTO = getApplicantHistoryBillingDTO(SUCCESSFUL_TEST_ID_1);
        failingDTO = getApplicantHistoryBillingDTO(FAILING_TEST_ID);
        multiStatusResponseBodyJson = FileUtils.readResourceToString(
            "billing/api-client/responses/multi-status.json");
        successApiResponse = new ResponseEntity<>(null, HttpStatus.OK);
        multiStatusApiResponse = new ResponseEntity<>(multiStatusResponseBodyJson, HttpStatus.MULTI_STATUS);
    }

    void verifications() {
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(successDTO, failingDTO));
        verify(crownCourtLitigatorFeesApiClient).updateApplicantsHistory(any(UpdateApplicantHistoriesRequest.class));
        verify(applicantHistoryBillingRepository).saveAll(List.of(failingEntity));
    }
    
    @Test
    void givenSomeFailuresFromCCLF_whenProcessBatchIsInvoked_thenFailingEntitiesAreUpdated() {
        when(crownCourtLitigatorFeesApiClient.updateApplicantsHistory(any())).thenReturn(multiStatusApiResponse);
        when(crownCourtRemunerationApiClient.updateApplicantsHistory(any())).thenReturn(successApiResponse);
        when(billingConfiguration.getUserModified()).thenReturn(USER_MODIFIED);
        
        when(applicantHistoryBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(applicantHistoryBillingRepository.resetApplicantHistory(anyList(), anyString())).thenReturn(1);
        when(responseUtils.getErroredIdsFromResponseBody(anyString(), anyString())).thenReturn(List.of(2));

        applicantHistoryBillingService.processBatch(List.of(successDTO, failingDTO), 1);

        verifications();
    }

    @Test
    void givenSomeFailuresFromCCR_whenProcessBatchIsInvoked_thenFailingEntitiesAreUpdated() {
        when(crownCourtLitigatorFeesApiClient.updateApplicantsHistory(any())).thenReturn(successApiResponse);
        when(crownCourtRemunerationApiClient.updateApplicantsHistory(any())).thenReturn(multiStatusApiResponse);
        when(billingConfiguration.getUserModified()).thenReturn(USER_MODIFIED);

        when(applicantHistoryBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(applicantHistoryBillingRepository.resetApplicantHistory(anyList(), anyString())).thenReturn(1);
        when(responseUtils.getErroredIdsFromResponseBody(anyString(), anyString())).thenReturn(List.of(2));

        applicantHistoryBillingService.processBatch(List.of(successDTO, failingDTO), 1);

        verifications();
    }

    @Test
    void givenDataAvailable_whenResendBatchIsInvoked_thenBillingRecordsAreResent() {
        applicantHistoryBillingService.resendBatch(List.of(successDTO), 1);

        verify(applicantHistoryBillingRepository, never()).resetApplicantHistory(any(), any());
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(any(), any());
        verify(crownCourtLitigatorFeesApiClient).updateApplicantsHistory(
            any(UpdateApplicantHistoriesRequest.class));
        verify(crownCourtRemunerationApiClient).updateApplicantsHistory(
            any(UpdateApplicantHistoriesRequest.class));
    }
}
