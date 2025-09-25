package uk.gov.justice.laa.maat.scheduled.tasks.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getApplicantHistoryBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedApplicantBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedRepOrderForBilling;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.RepOrderBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.BillingScheduler;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.FileUtils;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BillingSchedulerIntegrationTest {

    @Autowired
    private ApplicantBillingRepository applicantBillingRepository;

    @Autowired
    private ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    
    @Autowired
    private RepOrderBillingRepository repOrderBillingRepository;

    @MockitoBean
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    
    @Autowired
    @InjectMocks
    private BillingScheduler scheduler;
    
    private static final Integer TEST_ID = 1;
    
    // This ID matches the ID in the mixed-status.json file
    private static final Integer FAILING_TEST_ID = 2;
    
    @Test
    void givenSomeFailuresFromCCLF_whenExtractCCLFBillingDataIsInvoked_thenSendToCclfFlagIsSetToY() throws Exception {
        ApplicantBillingEntity applicantSuccessEntity = getPopulatedApplicantBillingEntity(TEST_ID);
        ApplicantBillingEntity applicantFailingEntity = getPopulatedApplicantBillingEntity(FAILING_TEST_ID);
        applicantBillingRepository.saveAll(List.of(applicantSuccessEntity, applicantFailingEntity));
        
        ApplicantHistoryBillingEntity applicantHistorySuccessEntity = getApplicantHistoryBillingEntity(TEST_ID);
        ApplicantHistoryBillingEntity applicantHistoryFailingEntity = getApplicantHistoryBillingEntity(FAILING_TEST_ID);
        applicantHistoryBillingRepository.saveAll(List.of(applicantHistorySuccessEntity, applicantHistoryFailingEntity));

        RepOrderBillingEntity repOrderSuccessEntity = getPopulatedRepOrderForBilling(TEST_ID);
        RepOrderBillingEntity repOrderFailingEntity = getPopulatedRepOrderForBilling(FAILING_TEST_ID);
        repOrderBillingRepository.saveAll(List.of(repOrderSuccessEntity, repOrderFailingEntity));
        
        String responseBodyJson = FileUtils.readResourceToString("billing/api-client/responses/mixed-status.json");

        ResponseEntity<String> apiResponse = new ResponseEntity<>(responseBodyJson, HttpStatus.MULTI_STATUS);
        
        when(crownCourtLitigatorFeesApiClient.updateApplicants(any())).thenReturn(apiResponse);
        when(crownCourtLitigatorFeesApiClient.updateApplicantsHistory(any())).thenReturn(apiResponse);
        when(crownCourtLitigatorFeesApiClient.updateRepOrders(any())).thenReturn(apiResponse);

        scheduler.extractCCLFBillingData();

        Optional<ApplicantBillingEntity> successfulApplicant = applicantBillingRepository.findById(TEST_ID);
        Optional<ApplicantBillingEntity> failedApplicant = applicantBillingRepository.findById(FAILING_TEST_ID);
        assertThat(successfulApplicant.isPresent()).isTrue();
        assertThat(failedApplicant.isPresent()).isTrue();
        // If successful, send_to_cclf is reset to null
        assertThat(successfulApplicant.get().getSendToCclf()).isEqualTo(null);
        assertThat(failedApplicant.get().getSendToCclf()).isEqualTo("Y");

        Optional<ApplicantHistoryBillingEntity> successfulApplicantHistory = applicantHistoryBillingRepository.findById(TEST_ID);
        Optional<ApplicantHistoryBillingEntity> failedApplicantHistory = applicantHistoryBillingRepository.findById(FAILING_TEST_ID);
        assertThat(successfulApplicantHistory.isPresent()).isTrue();
        assertThat(failedApplicantHistory.isPresent()).isTrue();
        // If successful, send_to_cclf is reset to null
        assertThat(successfulApplicantHistory.get().getSendToCclf()).isEqualTo(null);
        assertThat(failedApplicantHistory.get().getSendToCclf()).isEqualTo("Y");
        
        Optional<RepOrderBillingEntity> successfulRepOrders = repOrderBillingRepository.findById(TEST_ID);
        Optional<RepOrderBillingEntity> failedRepOrders = repOrderBillingRepository.findById(FAILING_TEST_ID);
        assertThat(successfulRepOrders.isPresent()).isTrue();
        assertThat(failedRepOrders.isPresent()).isTrue();
        // If successful, send_to_cclf is reset to null
        assertThat(successfulRepOrders.get().getSendToCclf()).isEqualTo(null);
        assertThat(failedRepOrders.get().getSendToCclf()).isEqualTo("Y");
    }
}
