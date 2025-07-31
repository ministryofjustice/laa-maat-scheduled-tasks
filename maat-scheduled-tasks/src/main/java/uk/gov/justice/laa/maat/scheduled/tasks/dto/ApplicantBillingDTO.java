package uk.gov.justice.laa.maat.scheduled.tasks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class ApplicantBillingDTO implements Serializable {

    @JsonValue
    private Integer id;
    private String firstName;
    private String lastName;
    private String otherNames;
    private LocalDate dob;
    private String gender;
    private String niNumber;
    private String foreignId;
    private LocalDateTime dateCreated;
    private String userCreated;
    private LocalDateTime dateModified;
    private String userModified;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ApplicantBillingDTO(int id) {
        this.id = id;
    }
}
