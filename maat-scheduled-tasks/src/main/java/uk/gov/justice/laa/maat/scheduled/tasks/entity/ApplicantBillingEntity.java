package uk.gov.justice.laa.maat.scheduled.tasks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "applicants", schema = "TOGDATA")
public class ApplicantBillingEntity {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "other_names")
    private String otherNames;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "gender")
    private String gender;

    @Column(name = "ni_number")
    private String niNumber;

    @Column(name = "foreign_id")
    private String foreignId;
    
    @Column(name = "send_to_cclf")
    private Boolean sendToCclf;
    
    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "user_created")
    private String userCreated;

    @Column(name = "date_modified")
    private LocalDateTime dateModified;

    @Column(name = "user_modified")
    private String userModified;
}
