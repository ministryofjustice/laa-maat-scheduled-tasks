package uk.gov.justice.laa.maat.scheduled.tasks.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.justice.laa.crime.util.RequestBuilderUtils.buildRequestGivenContent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.MAATScheduledTasksException;
import uk.gov.justice.laa.maat.scheduled.tasks.scheduler.BillingScheduler;
import uk.gov.justice.laa.maat.scheduled.tasks.service.ApplicantBillingService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.ApplicantHistoryBillingService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.RepOrderBillingService;

@WebMvcTest(BillingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BillingControllerTest {
    private static final String BASE_URL = "/api/internal/v1/billing/";
    private static final String RESEND_URL = BASE_URL + "resend";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BillingScheduler billingScheduler;

    @MockitoBean
    private ApplicantBillingService applicantBillingService;

    @MockitoBean
    private ApplicantHistoryBillingService applicantHistoryBillingService;

    @MockitoBean
    private RepOrderBillingService repOrderBillingService;

    @Test
    void givenExceptionThrown_whenResendBillingDataIsInvoked_thenReturnFailure() throws Exception {
        MAATScheduledTasksException expectedException =
            new MAATScheduledTasksException("Something went wrong.");

        doThrow(expectedException)
            .when(applicantBillingService).resendApplicantsToBilling();

        mockMvc.perform(
            buildRequestGivenContent(HttpMethod.POST, "", RESEND_URL, false))
            .andExpect(status().is5xxServerError());

        verify(applicantBillingService, times(1))
            .resendApplicantsToBilling();
        verify(applicantHistoryBillingService, never()).resendApplicantHistoryToBilling();
        verify(repOrderBillingService, never()).resendRepOrdersToBilling();
    }

    @Test
    void givenValidRequest_whenResendBillingDataIsInvoked_thenReturnSuccess() throws Exception {
        mockMvc.perform(buildRequestGivenContent(HttpMethod.POST, "", RESEND_URL, false))
            .andExpect(status().isAccepted());

        verify(applicantBillingService, times(1))
            .resendApplicantsToBilling();
        verify(applicantHistoryBillingService, times(1))
            .resendApplicantHistoryToBilling();
        verify(repOrderBillingService, times(1)).resendRepOrdersToBilling();
    }
}
