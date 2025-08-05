package uk.gov.justice.laa.maat.scheduled.tasks.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetApplicantBillingDTO {

    private String userModified;
    private List<Integer> ids;
}
