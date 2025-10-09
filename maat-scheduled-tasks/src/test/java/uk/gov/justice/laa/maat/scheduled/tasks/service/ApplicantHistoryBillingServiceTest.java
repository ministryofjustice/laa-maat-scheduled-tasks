package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
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
import uk.gov.justice.laa.maat.scheduled.tasks.config.BillingConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.utils.FileUtils;

@ExtendWith(MockitoExtension.class)
class ApplicantHistoryBillingServiceTest {

    private static final int SUCCESSFUL_TEST_ID_1 = 1;
    private static final int SUCCESSFUL_TEST_ID_2 = 3;
    private static final int FAILING_TEST_ID = 2;
    private static final String USER_MODIFIED = "TEST";
    private static final int BATCH_SIZE = 2;

    @Mock
    private ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    @Mock
    private ApplicantHistoryBillingMapper applicantHistoryBillingMapper;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @Mock
    private BillingConfiguration billingConfiguration;
    @InjectMocks
    private ApplicantHistoryBillingService applicantHistoryBillingService;

    @Test
    void givenNoApplicantHistoryDataExists_whenSendToBillingIsInvoked_thenNoActionsPerformed() {
        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(List.of());

        applicantHistoryBillingService.sendToBilling(USER_MODIFIED);

        verify(crownCourtLitigatorFeesApiClient, times(0)).updateApplicantsHistory(any());
    }

    @Test
    void givenBatchSizeIs1_whenSendToBillingIsInvokedFor2ApplicantHistories_thenProcessBatchIsInvokedTwice() {
        ApplicantHistoryBillingEntity entity1 = getApplicantHistoryBillingEntity(SUCCESSFUL_TEST_ID_1);
        ApplicantHistoryBillingEntity entity2 = getApplicantHistoryBillingEntity(SUCCESSFUL_TEST_ID_2);
        ApplicantHistoryBillingDTO dto1 = getApplicantHistoryBillingDTO(SUCCESSFUL_TEST_ID_1);
        ApplicantHistoryBillingDTO dto2 = getApplicantHistoryBillingDTO(SUCCESSFUL_TEST_ID_2);

        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(List.of(entity1, entity2));
        when(applicantHistoryBillingMapper.mapEntityToDTO(entity1)).thenReturn(dto1);
        when(applicantHistoryBillingMapper.mapEntityToDTO(entity2)).thenReturn(dto2);
        when(applicantHistoryBillingRepository.resetApplicantHistory(anyString(), anyList())).thenReturn(1);
        ResponseEntity<String> apiResponse = new ResponseEntity<>("body here", HttpStatus.OK);
        when(crownCourtLitigatorFeesApiClient.updateApplicantsHistory(any())).thenReturn(apiResponse);
        when(billingConfiguration.getBatchSize()).thenReturn(1);

        applicantHistoryBillingService.sendToBilling(USER_MODIFIED);

        verify(crownCourtLitigatorFeesApiClient, times(2)).updateApplicantsHistory(any());
    }

    @Test
    void givenSomeFailuresFromCCLF_whenSendToBillingIsInvoked_thenFailingEntitiesAreUpdated() throws Exception {
        ApplicantHistoryBillingEntity successEntity = getApplicantHistoryBillingEntity(SUCCESSFUL_TEST_ID_1);
        ApplicantHistoryBillingEntity failingEntity = getApplicantHistoryBillingEntity(FAILING_TEST_ID);
        ApplicantHistoryBillingDTO successDTO = getApplicantHistoryBillingDTO(SUCCESSFUL_TEST_ID_1);
        ApplicantHistoryBillingDTO failingDTO = getApplicantHistoryBillingDTO(FAILING_TEST_ID);

        String responseBodyJson = FileUtils.readResourceToString("billing/api-client/responses/mixed-status.json");
        
        ResponseEntity<String> apiResponse = new ResponseEntity<>(responseBodyJson, HttpStatus.MULTI_STATUS);
        when(crownCourtLitigatorFeesApiClient.updateApplicantsHistory(any())).thenReturn(apiResponse);

        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(List.of(successEntity, failingEntity));
        when(applicantHistoryBillingRepository.findAllById(any())).thenReturn(List.of(failingEntity));
        when(applicantHistoryBillingMapper.mapEntityToDTO(successEntity)).thenReturn(successDTO);
        when(applicantHistoryBillingMapper.mapEntityToDTO(failingEntity)).thenReturn(failingDTO);
        when(applicantHistoryBillingRepository.resetApplicantHistory(anyString(), anyList())).thenReturn(1);
        when(billingConfiguration.getBatchSize()).thenReturn(BATCH_SIZE);

        applicantHistoryBillingService.sendToBilling(USER_MODIFIED);

        verify(billingDataFeedLogService).saveBillingDataFeed(BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(successDTO, failingDTO));
        verify(crownCourtLitigatorFeesApiClient).updateApplicantsHistory(any(UpdateApplicantHistoriesRequest.class));
        verify(applicantHistoryBillingRepository).saveAll(List.of(failingEntity));
    }
}

