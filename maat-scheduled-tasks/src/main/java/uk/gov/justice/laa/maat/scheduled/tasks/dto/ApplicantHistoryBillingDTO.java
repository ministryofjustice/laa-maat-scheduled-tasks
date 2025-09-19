package uk.gov.justice.laa.maat.scheduled.tasks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantHistoryBillingDTO extends BillingDTO {

    @JsonProperty("as_at_date")
    private LocalDate asAtDate;

    @JsonProperty("def_id")
    private Integer applId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("other_names")
    private String otherNames;

    @JsonProperty("dob")
    private LocalDate dob;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("ni_number")
    private String niNumber;

    @JsonProperty("foreign_id")
    private String foreignId;

    @NotNull
    @JsonProperty("date_created")
    private LocalDateTime dateCreated;

    @NotNull
    @JsonProperty("user_created")
    private String userCreated;

    @JsonProperty("date_modified")
    private LocalDateTime dateModified;

    @JsonProperty("user_modified")
    private String userModified;
}

