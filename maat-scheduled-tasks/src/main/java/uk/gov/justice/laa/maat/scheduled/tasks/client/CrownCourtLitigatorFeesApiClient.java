package uk.gov.justice.laa.maat.scheduled.tasks.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateRepOrdersRequest;

@HttpExchange
public interface CrownCourtLitigatorFeesApiClient {
    @PostExchange("/defendants")
    void updateApplicants(@RequestBody UpdateApplicantsRequest applicantsRequest);
    @PostExchange("/defendant-histories")
    void updateApplicantsHistory(@RequestBody UpdateApplicantHistoriesRequest applicantHistoriesRequest);
    @PostExchange("/rep-orders")
    void updateRepOrders(@RequestBody UpdateRepOrdersRequest repOrdersRequest);
}
