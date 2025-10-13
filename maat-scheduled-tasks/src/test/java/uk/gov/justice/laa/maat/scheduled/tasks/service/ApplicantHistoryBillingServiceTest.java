package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getApplicantHistoryBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantHistoryBillingDTO;

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
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.FileUtils;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class ApplicantHistoryBillingServiceTest {

    private static final int SUCCESSFUL_TEST_ID_1 = 1;
    private static final int FAILING_TEST_ID = 2;
    private static final String USER_MODIFIED = "TEST";

    @Mock
    private ResponseUtils responseUtils;
    @Mock
    private ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @InjectMocks
    private ApplicantHistoryBillingService applicantHistoryBillingService;

    @Test
    void givenSomeFailuresFromCCLF_whenSendToBillingIsInvoked_thenFailingEntitiesAreUpdated() throws Exception {
        ApplicantHistoryBillingEntity failingEntity = getApplicantHistoryBillingEntity(FAILING_TEST_ID);
        ApplicantHistoryBillingDTO successDTO = getApplicantHistoryBillingDTO(SUCCESSFUL_TEST_ID_1);
        ApplicantHistoryBillingDTO failingDTO = getApplicantHistoryBillingDTO(FAILING_TEST_ID);

        String responseBodyJson = FileUtils.readResourceToString("billing/api-client/responses/mixed-status.json");
        
        ResponseEntity<String> apiResponse = new ResponseEntity<>(responseBodyJson, HttpStatus.MULTI_STATUS);
        when(crownCourtLitigatorFeesApiClient.updateApplicantsHistory(any())).thenReturn(apiResponse);
        
        when(applicantHistoryBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(applicantHistoryBillingRepository.resetApplicantHistory(anyString(), anyList())).thenReturn(1);
        when(responseUtils.getErroredIdsFromResponseBody(anyString(), anyString())).thenReturn(List.of(2));

        applicantHistoryBillingService.processBatch(List.of(successDTO, failingDTO), 1, USER_MODIFIED);

        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(successDTO, failingDTO));
        verify(crownCourtLitigatorFeesApiClient).updateApplicantsHistory(any(UpdateApplicantHistoriesRequest.class));
        verify(applicantHistoryBillingRepository).saveAll(List.of(failingEntity));
    }
}

