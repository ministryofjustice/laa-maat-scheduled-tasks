package uk.gov.justice.laa.maat.scheduled.tasks.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class UpdateApplicantHistoriesRequest implements Serializable {
    @JsonProperty("defendant_histories")
    List<ApplicantHistoryBillingDTO> defendantHistories;
}
