package uk.gov.justice.laa.maat.scheduled.tasks.builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;

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

    public static RepOrderBillingEntity getPopulatedRepOrderForBilling(Integer id) {
        return RepOrderBillingEntity.builder()
            .id(id)
            .applicantId(123)
            .arrestSummonsNo("ARREST-5678")
            .evidenceFeeLevel("LEVEL1")
            .supplierAccountCode("AB123C")
            .magsCourtId("34")
            .magsCourtOutcome("COMMITTED")
            .dateReceived(LocalDate.of(2025, 6, 10))
            .crownCourtRepOrderDate(LocalDate.of(2025, 6, 12))
            .offenceType("BURGLARY")
            .crownCourtWithdrawalDate(LocalDate.of(2025, 6, 30))
            .applicantHistoryId(96)
            .caseId("CASE-123-C")
            .committalDate(LocalDate.of(2025, 6, 11))
            .repOrderStatus("CURR")
            .appealTypeCode("ACN")
            .crownCourtOutcome("CONVICTED")
            .dateCreated(LocalDate.of(2025, 6, 20))
            .userCreated("joe-bloggs")
            .dateModified(LocalDate.of(2025, 6, 21).atStartOfDay())
            .userModified("alice-smith")
            .caseType("EITHER WAY")
            .build();
    }
}
