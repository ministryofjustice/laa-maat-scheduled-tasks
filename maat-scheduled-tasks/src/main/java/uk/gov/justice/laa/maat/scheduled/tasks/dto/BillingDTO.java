package uk.gov.justice.laa.maat.scheduled.tasks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BillingDTO {
    @JsonProperty("id")
    private Integer id;
}

