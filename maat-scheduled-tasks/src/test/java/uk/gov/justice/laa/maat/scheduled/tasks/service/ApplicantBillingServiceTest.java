package uk.gov.justice.laa.maat.scheduled.tasks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.FileUtils;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.ResponseUtils;

import static org.mockito.Mockito.*;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedApplicantBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;

@ExtendWith(MockitoExtension.class)
class ApplicantBillingServiceTest {

    private static final int SUCCESSFUL_TEST_ID_1 = 1;
    private static final int FAILING_TEST_ID = 2;
    private static final String USER_MODIFIED = "TEST";

    @Mock
    private ResponseUtils responseUtils;
    @Mock
    private ApplicantBillingRepository applicantBillingRepository;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @InjectMocks
    private ApplicantBillingService applicantBillingService;

    @Test
    void givenSomeFailuresFromCCLF_whenSendToBillingIsInvoked_thenFailingEntitiesAreUpdated() throws Exception {
        ApplicantBillingEntity failingEntity = getPopulatedApplicantBillingEntity(FAILING_TEST_ID);
        ApplicantBillingDTO successDTO = getApplicantDTO(SUCCESSFUL_TEST_ID_1);
        ApplicantBillingDTO failingDTO = getApplicantDTO(FAILING_TEST_ID);

        String responseBodyJson = FileUtils.readResourceToString("billing/api-client/responses/mixed-status.json");

        ResponseEntity<String> apiResponse = new ResponseEntity<>(responseBodyJson, HttpStatus.MULTI_STATUS);
        when(crownCourtLitigatorFeesApiClient.updateApplicants(any())).thenReturn(apiResponse);
        
        when(applicantBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(applicantBillingRepository.resetApplicantBilling(anyList(), anyString())).thenReturn(1);
        when(responseUtils.getErroredIdsFromResponseBody(anyString(), anyString())).thenReturn(List.of(2));
        
        applicantBillingService.processBatch(List.of(successDTO, failingDTO), 1, USER_MODIFIED);
        
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT, List.of(successDTO, failingDTO));
        verify(crownCourtLitigatorFeesApiClient).updateApplicants(any(UpdateApplicantsRequest.class));
        verify(applicantBillingRepository).saveAll(List.of(failingEntity));
    }
}
