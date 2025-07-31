package uk.gov.justice.laa.maat.scheduled.tasks.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantHistoryBillingDTO {
    @NotNull
    private Integer id;
    private LocalDate asAtDate;
    private Integer applId;
    private String firstName;
    private String lastName;
    private String otherNames;
    private LocalDate dob;
    private String gender;
    private String niNumber;
    private String foreignId;
    @NotNull
    private LocalDateTime dateCreated;
    @NotNull
    private String userCreated;
    private LocalDateTime dateModified;
    private String userModified;
}

