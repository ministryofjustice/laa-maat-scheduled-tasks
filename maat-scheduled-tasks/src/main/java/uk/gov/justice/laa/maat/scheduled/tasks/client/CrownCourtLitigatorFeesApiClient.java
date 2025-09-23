package uk.gov.justice.laa.maat.scheduled.tasks.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantHistoriesRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateApplicantsRequest;
import uk.gov.justice.laa.maat.scheduled.tasks.request.UpdateRepOrdersRequest;

@HttpExchange
public interface CrownCourtLitigatorFeesApiClient {
    @PostExchange("/defendants")
    ResponseEntity<String> updateApplicants(@RequestBody UpdateApplicantsRequest applicantsRequest);
    @PostExchange("/defendant-histories")
    ResponseEntity<String> updateApplicantsHistory(@RequestBody UpdateApplicantHistoriesRequest applicantHistoriesRequest);
    @PostExchange("/rep-orders")
    ResponseEntity<String> updateRepOrders(@RequestBody UpdateRepOrdersRequest repOrdersRequest);
}
