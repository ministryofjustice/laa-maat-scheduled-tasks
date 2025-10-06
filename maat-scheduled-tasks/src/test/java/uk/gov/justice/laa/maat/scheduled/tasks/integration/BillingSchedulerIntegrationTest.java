package uk.gov.justice.laa.maat.scheduled.tasks.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getApplicantHistoryBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedApplicantBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedRepOrderForBilling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
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
@AutoConfigureWireMock(port = 0)
public class BillingSchedulerIntegrationTest {

    @Autowired
    private ApplicantBillingRepository applicantBillingRepository;

    @Autowired
    private ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    
    @Autowired
    private RepOrderBillingRepository repOrderBillingRepository;

    @Autowired
    private WireMockServer crownCourtLitigatorFeesApiClient;
    
    @Autowired
    private BillingScheduler scheduler;
    
    private static final Integer TEST_ID = 1;
    
    // This ID matches the ID in the mixed-status.json file
    private static final Integer FAILING_TEST_ID = 2;
    private static final String CCLF_API_BASE_URL = "/cclf/api/internal/v1";
    private static String multiStatusResponseBody = "";

    @BeforeAll()
    static void setUp() throws IOException {
        multiStatusResponseBody = FileUtils.readResourceToString("billing/api-client/responses/mixed-status.json");
    }
    
    @AfterEach
    void clean() {
        crownCourtLitigatorFeesApiClient.resetAll();
    }
    
    private void stubForOAuth() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> token = Map.of(
            "expires_in", 3600,
            "token_type", "Bearer",
            "access_token", UUID.randomUUID()
        );

        stubFor(post("/oauth2/token")
            .willReturn(WireMock.ok()
                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                .withBody(mapper.writeValueAsString(token))));
    }
    
    public void stubForDefendants() {
        stubFor(post(urlPathMatching(CCLF_API_BASE_URL + "/defendants")).willReturn(aResponse()
            .withStatus(207)
            .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
            .withBody(multiStatusResponseBody)));
    }

    public void stubForDefendantHistories() {
        stubFor(post(urlPathMatching(CCLF_API_BASE_URL+ "/defendant-histories")).willReturn(aResponse()
            .withStatus(207)
            .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
            .withBody(multiStatusResponseBody)));
    }

    public void stubForRepOrders() {
        stubFor(post(urlPathMatching(CCLF_API_BASE_URL + "/rep-orders")).willReturn(aResponse()
            .withStatus(207)
            .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
            .withBody(multiStatusResponseBody)));
    }
    
    @Test
    void givenSomeFailuresFromCCLF_whenExtractCCLFBillingDataIsInvoked_thenSendToCclfFlagIsSetToY() throws Exception {
        stubForOAuth();
        stubForDefendants();
        stubForDefendantHistories();
        stubForRepOrders();
        
        ApplicantBillingEntity applicantSuccessEntity = getPopulatedApplicantBillingEntity(TEST_ID);
        ApplicantBillingEntity applicantFailingEntity = getPopulatedApplicantBillingEntity(FAILING_TEST_ID);
        applicantBillingRepository.saveAll(List.of(applicantSuccessEntity, applicantFailingEntity));
        
        ApplicantHistoryBillingEntity applicantHistorySuccessEntity = getApplicantHistoryBillingEntity(TEST_ID);
        ApplicantHistoryBillingEntity applicantHistoryFailingEntity = getApplicantHistoryBillingEntity(FAILING_TEST_ID);
        applicantHistoryBillingRepository.saveAll(List.of(applicantHistorySuccessEntity, applicantHistoryFailingEntity));

        RepOrderBillingEntity repOrderSuccessEntity = getPopulatedRepOrderForBilling(TEST_ID);
        RepOrderBillingEntity repOrderFailingEntity = getPopulatedRepOrderForBilling(FAILING_TEST_ID);
        repOrderBillingRepository.saveAll(List.of(repOrderSuccessEntity, repOrderFailingEntity));

        scheduler.extractCCLFBillingData();

        Optional<ApplicantBillingEntity> successfulApplicant = applicantBillingRepository.findById(TEST_ID);
        Optional<ApplicantBillingEntity> failedApplicant = applicantBillingRepository.findById(FAILING_TEST_ID);
        assertThat(successfulApplicant.isPresent()).isTrue();
        assertThat(failedApplicant.isPresent()).isTrue();
        // If successful, send_to_cclf is reset to null
        assertThat(successfulApplicant.get().getSendToCclf()).isEqualTo(false);
        assertThat(failedApplicant.get().getSendToCclf()).isEqualTo(true);

        Optional<ApplicantHistoryBillingEntity> successfulApplicantHistory = applicantHistoryBillingRepository.findById(TEST_ID);
        Optional<ApplicantHistoryBillingEntity> failedApplicantHistory = applicantHistoryBillingRepository.findById(FAILING_TEST_ID);
        assertThat(successfulApplicantHistory.isPresent()).isTrue();
        assertThat(failedApplicantHistory.isPresent()).isTrue();
        // If successful, send_to_cclf is reset to null
        assertThat(successfulApplicantHistory.get().getSendToCclf()).isEqualTo(false);
        assertThat(failedApplicantHistory.get().getSendToCclf()).isEqualTo(true);
        
        Optional<RepOrderBillingEntity> successfulRepOrders = repOrderBillingRepository.findById(TEST_ID);
        Optional<RepOrderBillingEntity> failedRepOrders = repOrderBillingRepository.findById(FAILING_TEST_ID);
        assertThat(successfulRepOrders.isPresent()).isTrue();
        assertThat(failedRepOrders.isPresent()).isTrue();
        // If successful, send_to_cclf is reset to null
        assertThat(successfulRepOrders.get().getSendToCclf()).isEqualTo(false);
        assertThat(failedRepOrders.get().getSendToCclf()).isEqualTo(true);
    }
}
