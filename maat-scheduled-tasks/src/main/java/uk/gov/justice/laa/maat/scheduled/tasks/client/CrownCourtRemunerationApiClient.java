package uk.gov.justice.laa.maat.scheduled.tasks.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;

import java.util.List;

public interface CrownCourtRemunerationApiClient {
    @PostExchange("/defendants")
    void updateApplicants(@RequestBody List<ApplicantBillingDTO> applicants);
    @PostExchange("/defendant-histories")
    void updateApplicantsHistory(@RequestBody List<ApplicantHistoryBillingDTO> applicantHistories);
    @PostExchange("/rep-orders")
    void updateRepOrders(@RequestBody List<RepOrderBillingDTO> repOrders);
}
