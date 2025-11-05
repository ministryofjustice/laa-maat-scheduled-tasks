package uk.gov.justice.laa.maat.scheduled.tasks.controller;

import static org.mockito.Mockito.doThrow;
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
import uk.gov.justice.laa.maat.scheduled.tasks.service.BatchProcessingService;

@WebMvcTest(BillingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BillingControllerTest {
    private static final String BASE_URL = "/api/internal/v1/billing/";
    private static final String RESEND_URL = BASE_URL + "resend";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BatchProcessingService batchProcessingService;

    @MockitoBean
    private BillingScheduler billingScheduler;

    @Test
    void givenExceptionThrown_whenResendBillingDataIsInvoked_thenReturnFailure() throws Exception {
        MAATScheduledTasksException expectedException =
            new MAATScheduledTasksException("Something went wrong.");

        doThrow(expectedException)
            .when(batchProcessingService).resendBillingRecords();

        mockMvc.perform(
                buildRequestGivenContent(HttpMethod.POST, "", RESEND_URL, false))
            .andExpect(status().is5xxServerError());

        verify(batchProcessingService, times(1)).resendBillingRecords();
    }

    @Test
    void givenValidRequest_whenResendBillingDataIsInvoked_thenReturnSuccess() throws Exception {
        mockMvc.perform(buildRequestGivenContent(HttpMethod.POST, "", RESEND_URL, false))
            .andExpect(status().isAccepted());

        verify(batchProcessingService, times(1)).resendBillingRecords();
    }
}