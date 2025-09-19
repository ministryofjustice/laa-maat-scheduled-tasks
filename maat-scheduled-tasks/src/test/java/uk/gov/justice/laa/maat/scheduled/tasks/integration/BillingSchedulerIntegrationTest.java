package uk.gov.justice.laa.maat.scheduled.tasks.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getApplicantHistoryBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedApplicantBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedRepOrderForBilling;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClientResponseException;
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
        byte[] responseBody = responseBodyJson.getBytes(StandardCharsets.UTF_8);

        RestClientResponseException expectedException = new RestClientResponseException(
            "Multi-Status", HttpStatus.MULTI_STATUS, "Multi-Status", null, responseBody, null
        );

        doThrow(expectedException).when(crownCourtLitigatorFeesApiClient).updateApplicants(any());
        doThrow(expectedException).when(crownCourtLitigatorFeesApiClient).updateApplicantsHistory(any());
        doThrow(expectedException).when(crownCourtLitigatorFeesApiClient).updateRepOrders(any());

        scheduler.extractCCLFBillingData();

        Optional<ApplicantBillingEntity> successfulApplicant = applicantBillingRepository.findById(TEST_ID);
        Optional<ApplicantBillingEntity> failedApplicant = applicantBillingRepository.findById(FAILING_TEST_ID);
        assertTrue(successfulApplicant.isPresent());
        assertTrue(failedApplicant.isPresent());
        // If successful, send_to_cclf is reset to null
        assertEquals(null, successfulApplicant.get().getSendToCclf());
        assertEquals("Y", failedApplicant.get().getSendToCclf());

        Optional<ApplicantHistoryBillingEntity> successfulApplicantHistory = applicantHistoryBillingRepository.findById(TEST_ID);
        Optional<ApplicantHistoryBillingEntity> failedApplicantHistory = applicantHistoryBillingRepository.findById(FAILING_TEST_ID);
        assertTrue(successfulApplicantHistory.isPresent());
        assertTrue(failedApplicantHistory.isPresent());
        // If successful, send_to_cclf is reset to null
        assertEquals(null, successfulApplicantHistory.get().getSendToCclf());
        assertEquals("Y", failedApplicantHistory.get().getSendToCclf());
        
        Optional<RepOrderBillingEntity> successfulRepOrders = repOrderBillingRepository.findById(TEST_ID);
        Optional<RepOrderBillingEntity> failedRepOrders = repOrderBillingRepository.findById(FAILING_TEST_ID);
        assertTrue(successfulRepOrders.isPresent());
        assertTrue(failedRepOrders.isPresent());
        // If successful, send_to_cclf is reset to null
        assertEquals(null, successfulRepOrders.get().getSendToCclf());
        assertEquals("Y", failedRepOrders.get().getSendToCclf());
    }
}
