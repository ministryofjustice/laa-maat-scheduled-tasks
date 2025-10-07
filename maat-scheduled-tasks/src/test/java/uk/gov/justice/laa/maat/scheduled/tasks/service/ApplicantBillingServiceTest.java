package uk.gov.justice.laa.maat.scheduled.tasks.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantBillingRepository;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.FileUtils;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder.getPopulatedApplicantBillingEntity;
import static uk.gov.justice.laa.maat.scheduled.tasks.builder.TestModelDataBuilder.getApplicantDTO;

@ExtendWith(MockitoExtension.class)
class ApplicantBillingServiceTest {

    private static final int TEST_ID = 1;
    private static final int FAILING_TEST_ID = 2;
    private static final int BATCH_SIZE = 2;
    private static final String USER_MODIFIED = "TEST";

    @Mock
    private ApplicantBillingRepository applicantBillingRepository;
    @Mock
    private ApplicantMapper applicantMapper;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @Mock
    private BillingConfiguration billingConfiguration;
    @InjectMocks
    private ApplicantBillingService applicantBillingService;


    @Test
    void givenApplicantDataExists_whenFindApplicantsForBillingIsInvoked_thenApplicantDataIsReturned() {
        ApplicantBillingEntity entity = getPopulatedApplicantBillingEntity(TEST_ID);
        ApplicantBillingDTO dto = getApplicantDTO(TEST_ID);

        when(applicantBillingRepository.findAllApplicantsForBilling()).thenReturn(
            List.of(entity, entity));
        when(applicantMapper.mapEntityToDTO(entity)).thenReturn(dto);

        when(applicantBillingRepository.resetApplicantBilling(anyList(), anyString())).thenReturn(1);
        ResponseEntity<String> apiResponse = new ResponseEntity<>("body here", HttpStatus.OK);
        when(crownCourtLitigatorFeesApiClient.updateApplicants(any())).thenReturn(apiResponse);
        when(billingConfiguration.getBatchSize()).thenReturn(BATCH_SIZE);
        
        applicantBillingService.sendToBilling(USER_MODIFIED);

        List<ApplicantBillingDTO> applicants = applicantBillingService.getBillingDTOList();

        assertEquals(List.of(dto, dto), applicants);
    }

    @Test
    void givenSomeFailuresFromCCLF_whenSendApplicantsToBillingIsInvoked_thenFailingEntitiesAreUpdated() throws Exception {
        ApplicantBillingEntity successEntity = getPopulatedApplicantBillingEntity(TEST_ID);
        ApplicantBillingEntity failingEntity = getPopulatedApplicantBillingEntity(FAILING_TEST_ID);
        ApplicantBillingDTO successDTO = getApplicantDTO(TEST_ID);
        ApplicantBillingDTO failingDTO = getApplicantDTO(FAILING_TEST_ID);

        String responseBodyJson = FileUtils.readResourceToString("billing/api-client/responses/mixed-status.json");

        ResponseEntity<String> apiResponse = new ResponseEntity<>(responseBodyJson, HttpStatus.MULTI_STATUS);
        when(crownCourtLitigatorFeesApiClient.updateApplicants(any())).thenReturn(apiResponse);
        
        when(applicantBillingRepository.findAllApplicantsForBilling()).thenReturn(List.of(successEntity, failingEntity));
        when(applicantBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(applicantMapper.mapEntityToDTO(successEntity)).thenReturn(successDTO);
        when(applicantMapper.mapEntityToDTO(failingEntity)).thenReturn(failingDTO);
        when(applicantBillingRepository.resetApplicantBilling(anyList(), anyString())).thenReturn(1);
        when(billingConfiguration.getBatchSize()).thenReturn(BATCH_SIZE);

        applicantBillingService.sendToBilling(USER_MODIFIED);
        
        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT, List.of(successDTO, failingDTO));
        verify(crownCourtLitigatorFeesApiClient).updateApplicants(any(UpdateApplicantsRequest.class));
        verify(applicantBillingRepository).saveAll(List.of(failingEntity));
    }
}
