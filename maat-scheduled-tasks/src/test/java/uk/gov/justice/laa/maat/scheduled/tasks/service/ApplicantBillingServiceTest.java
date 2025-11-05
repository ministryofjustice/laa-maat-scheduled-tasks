package uk.gov.justice.laa.maat.scheduled.tasks.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
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
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.BillingDataFeedLogMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.FileUtils;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.ResponseUtils;

import static org.mockito.Mockito.*;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedApplicantBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedBillingFeedLogEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;

@ExtendWith(MockitoExtension.class)
class ApplicantBillingServiceTest {

    private static final int SUCCESSFUL_TEST_ID_1 = 1;
    private static final int FAILING_TEST_ID = 2;
    private static final String USER_MODIFIED = "TEST";

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .build();

    private ApplicantBillingEntity failingEntity;
    private ApplicantBillingDTO successDTO;
    private ApplicantBillingDTO failingDTO;
    private String multiStatusResponseBodyJson;
    ResponseEntity<String> successApiResponse;
    ResponseEntity<String> multiStatusApiResponse;
    
    @Mock
    private ResponseUtils responseUtils;
    @Mock
    private ApplicantBillingRepository applicantBillingRepository;
    @Mock
    private BillingConfiguration billingConfiguration;
    @Mock
    private BillingDataFeedLogMapper billingDataFeedLogMapper;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @Mock
    private CrownCourtRemunerationApiClient crownCourtRemunerationApiClient;
    @InjectMocks
    private ApplicantBillingService applicantBillingService;

    @BeforeEach
    void setUp() throws IOException {
        failingEntity = getPopulatedApplicantBillingEntity(FAILING_TEST_ID);
        successDTO = getApplicantDTO(SUCCESSFUL_TEST_ID_1);
        failingDTO = getApplicantDTO(FAILING_TEST_ID);
        multiStatusResponseBodyJson = FileUtils.readResourceToString(
            "billing/api-client/responses/multi-status.json");
        successApiResponse = new ResponseEntity<>(null, HttpStatus.OK);
        multiStatusApiResponse = new ResponseEntity<>(multiStatusResponseBodyJson, HttpStatus.MULTI_STATUS);
    }
    
    void verifications() {
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT, List.of(successDTO, failingDTO));
        verify(crownCourtLitigatorFeesApiClient).updateApplicants(any(UpdateApplicantsRequest.class));
        verify(crownCourtRemunerationApiClient).updateApplicants(any(UpdateApplicantsRequest.class));
        verify(applicantBillingRepository).saveAll(List.of(failingEntity));
    }
    
    @Test
    void givenSomeFailuresFromCCLF_whenProcessBatchIsInvoked_thenFailingEntitiesAreUpdated() {
        when(crownCourtLitigatorFeesApiClient.updateApplicants(any())).thenReturn(multiStatusApiResponse);
        when(crownCourtRemunerationApiClient.updateApplicants(any())).thenReturn(successApiResponse);
        when(billingConfiguration.getUserModified()).thenReturn(USER_MODIFIED);
        
        when(applicantBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(applicantBillingRepository.resetApplicantBilling(anyList(), anyString())).thenReturn(1);
        when(responseUtils.getErroredIdsFromResponseBody(anyString(), anyString())).thenReturn(List.of(2));
        
        applicantBillingService.processBatch(List.of(successDTO, failingDTO), 1);
        
        verifications();
    }

    @Test
    void givenSomeFailuresFromCCR_whenProcessBatchIsInvoked_thenFailingEntitiesAreUpdated() {
        when(crownCourtLitigatorFeesApiClient.updateApplicants(any())).thenReturn(successApiResponse);
        when(crownCourtRemunerationApiClient.updateApplicants(any())).thenReturn(multiStatusApiResponse);
        when(billingConfiguration.getUserModified()).thenReturn(USER_MODIFIED);

        when(applicantBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(applicantBillingRepository.resetApplicantBilling(anyList(), anyString())).thenReturn(1);
        when(responseUtils.getErroredIdsFromResponseBody(anyString(), anyString())).thenReturn(List.of(2));

        applicantBillingService.processBatch(List.of(successDTO, failingDTO), 1);

        verifications();
    }

    @Test
    void givenDataAvailable_whenResendBatchIsInvoked_thenBillingRecordsAreResent() {
        applicantBillingService.resendBatch(List.of(successDTO), 1);

        verify(applicantBillingRepository, never()).resetApplicantBilling(any(), any());
        verify(billingDataFeedLogService, never()).saveBillingDataFeed(any(), any());
        verify(crownCourtLitigatorFeesApiClient).updateApplicants(
            any(UpdateApplicantsRequest.class));
        verify(crownCourtRemunerationApiClient).updateApplicants(
            any(UpdateApplicantsRequest.class));
    }
}
