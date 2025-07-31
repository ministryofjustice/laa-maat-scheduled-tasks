package uk.gov.justice.laa.maat.scheduled.tasks.builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;

@Component
public class TestModelDataBuilder {
    public static ApplicantHistoryBillingDTO getApplicantHistoryBillingDTO() {
        return ApplicantHistoryBillingDTO.builder()
            .id(1)
            .asAtDate(LocalDate.parse("2006-10-06"))
            .applId(716)
            .firstName("test_first")
            .lastName("test_last")
            .otherNames("test")
            .dob(LocalDate.parse("1981-10-14"))
            .gender("Male")
            .niNumber("JM933396A")
            .foreignId(null)
            .dateCreated(LocalDateTime.parse("2021-10-09T15:01:25"))
            .userCreated("TEST")
            .dateModified(null)
            .userModified(null)
            .build();
    }
}
