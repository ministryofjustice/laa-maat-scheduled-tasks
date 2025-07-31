package uk.gov.justice.laa.maat.scheduled.tasks.builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;

@Component
public class TestEntityDataBuilder {
    public static ApplicantHistoryBillingEntity getApplicantHistoryBillingEntity() {
        return ApplicantHistoryBillingEntity.builder()
            .id(666)
            .applId(666)
            .asAtDate(LocalDate.parse("2006-10-06"))
            .firstName("test_first")
            .lastName("test_last")
            .otherNames("test")
            .dob(LocalDate.parse("1981-10-14"))
            .gender("Male")
            .niNumber("JM933396A")
            .foreignId("T35T")
            .dateCreated(LocalDateTime.parse("2021-10-09T15:01:25"))
            .userCreated("TEST")
            .dateModified(null)
            .userModified(null)
            .build();
    }
}
