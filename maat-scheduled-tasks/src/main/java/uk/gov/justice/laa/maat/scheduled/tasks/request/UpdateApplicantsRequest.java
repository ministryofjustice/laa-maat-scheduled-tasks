package uk.gov.justice.laa.maat.scheduled.tasks.request;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class UpdateApplicantsRequest implements Serializable {
    List<ApplicantBillingDTO> defendants;
}
