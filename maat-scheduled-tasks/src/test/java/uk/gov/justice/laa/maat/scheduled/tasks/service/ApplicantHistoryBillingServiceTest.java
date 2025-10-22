package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtLitigatorFeesApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.client.CrownCourtRemunerationApiClient;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;

@ExtendWith(MockitoExtension.class)
class ApplicantHistoryBillingServiceTest {

    private static final int TEST_ID = 1;
    private static final String USER_MODIFIED = "TEST";

    @Mock
    private ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    @Mock
    private ApplicantHistoryBillingMapper applicantHistoryBillingMapper;
    @Mock
    private BillingDataFeedLogService billingDataFeedLogService;
    @Mock
    private CrownCourtLitigatorFeesApiClient crownCourtLitigatorFeesApiClient;
    @Mock
    private CrownCourtRemunerationApiClient crownCourtRemunerationApiClient;
    @InjectMocks
    private ApplicantHistoryBillingService applicantHistoryBillingService;

    @Test
    void givenApplicantHistoryDataExists_whenExtractApplicantHistoryIsInvoked_thenApplicantHistoryDataIsReturned() {
        ApplicantHistoryBillingEntity entity = getApplicantHistoryBillingEntity(TEST_ID);
        ApplicantHistoryBillingDTO dto = getApplicantHistoryBillingDTO(TEST_ID);

        when(applicantHistoryBillingRepository.extractApplicantHistoryForBilling()).thenReturn(
            List.of(entity, entity));
        when(applicantHistoryBillingMapper.mapEntityToDTO(entity)).thenReturn(dto);

        List<ApplicantHistoryBillingDTO> applicantHistories = applicantHistoryBillingService.extractApplicantHistory();

        assertEquals(List.of(dto, dto), applicantHistories);
    }

    @Test
    void givenValidData_whenSendApplicantHistoryToBillingIsInvoked_thenDatabaseUpdatedAndCCLFCalled() {
        ApplicantHistoryBillingDTO dto = getApplicantHistoryBillingDTO(TEST_ID);

        when(applicantHistoryBillingRepository.resetApplicantHistory(anyString(),
            anyList())).thenReturn(1);

        applicantHistoryBillingService.sendApplicantHistoryToBilling(List.of(dto), USER_MODIFIED);

        verify(applicantHistoryBillingRepository).resetApplicantHistory(USER_MODIFIED,
            List.of(TEST_ID));
        verify(billingDataFeedLogService).saveBillingDataFeed(
            BillingDataFeedRecordType.APPLICANT_HISTORY, List.of(dto));
        verify(crownCourtLitigatorFeesApiClient).updateApplicantsHistory(
            any(UpdateApplicantHistoriesRequest.class));
        verify(crownCourtRemunerationApiClient).updateApplicantsHistory(
            any(UpdateApplicantHistoriesRequest.class));
    }
}

