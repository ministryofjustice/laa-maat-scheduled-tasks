package uk.gov.justice.laa.maat.scheduled.tasks.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;

@HttpExchange
public interface CrownCourtLitigatorFeesApiClient {
    @PostExchange("/defendants")
    void updateApplicants(@RequestBody UpdateApplicantsRequest applicantsRequest);
    @PostExchange("/defendant-histories")
    void updateApplicantsHistory(@RequestBody List<ApplicantHistoryBillingDTO> applicantHistories);
    @PostExchange("/rep-orders")
    void updateRepOrders(@RequestBody List<RepOrderBillingDTO> repOrders);
}
