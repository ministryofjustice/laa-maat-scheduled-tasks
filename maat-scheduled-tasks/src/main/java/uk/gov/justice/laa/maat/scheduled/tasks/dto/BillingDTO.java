package uk.gov.justice.laa.maat.scheduled.tasks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BillingDTO {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("date_created")
    private LocalDate dateCreated;

    @JsonProperty("user_created")
    private String userCreated;

    @JsonProperty("date_modified")
    private LocalDateTime dateModified;

    @JsonProperty("user_modified")
    private String userModified;
}

