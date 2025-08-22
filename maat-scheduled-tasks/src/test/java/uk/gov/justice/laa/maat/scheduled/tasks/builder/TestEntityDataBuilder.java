package uk.gov.justice.laa.maat.scheduled.tasks.builder;

import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.enums.AppealType;
import uk.gov.justice.laa.crime.enums.EvidenceFeeLevel;
import uk.gov.justice.laa.crime.enums.MagCourtOutcome;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.BillingDataFeedLogEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.BillingDataFeedRecordType;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.CrownCourtCaseType;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.CrownCourtTrialOutcome;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class TestEntityDataBuilder {

    private static final String USER_NAME = "test-u";

    public static ApplicantHistoryBillingEntity getApplicantHistoryBillingEntity(Integer id) {
        return ApplicantHistoryBillingEntity.builder()
            .id(id)
            .applId(id)
            .asAtDate(LocalDate.parse("2006-10-06"))
            .firstName("test_first")
            .lastName("test_last")
            .otherNames("test")
            .dob(LocalDate.parse("1981-10-14"))
            .gender("Male")
            .niNumber("JM933396A")
            .foreignId("T35T")
            .dateCreated(LocalDateTime.parse("2021-10-09T15:01:25"))
            .userCreated(USER_NAME)
            .dateModified(null)
            .userModified(null)
            .build();
    }

    public static ApplicantBillingEntity getPopulatedApplicantBillingEntity(Integer id) {
        return ApplicantBillingEntity.builder()
                .id(id)
                .firstName("test-first-name")
                .lastName("test-last-name")
                .otherNames("test-other-names")
                .dob(LocalDate.parse("2025-07-31"))
                .gender("male")
                .niNumber("AB123456C")
                .foreignId("foreign-ID")
                .dateCreated(LocalDateTime.parse("2024-08-29T11:38:12"))
                .userCreated(USER_NAME)
                .dateModified(LocalDateTime.parse("2024-04-01T10:45:09"))
                .userModified(USER_NAME)
                .build();
    }

    public static RepOrderBillingEntity getPopulatedRepOrderForBilling(Integer id) {
        return RepOrderBillingEntity.builder()
                .id(id)
                .applicantId(123)
                .arrestSummonsNo("ARREST-5678")
                .evidenceFeeLevel(EvidenceFeeLevel.LEVEL1.getFeeLevel())
                .supplierAccountCode("AB123C")
                .magsCourtId("34")
                .magsCourtOutcome(MagCourtOutcome.COMMITTED.getOutcome())
                .dateReceived(LocalDate.of(2025, 6, 10))
                .crownCourtRepOrderDate(LocalDate.of(2025, 6, 12))
                .offenceType("BURGLARY")
                .crownCourtWithdrawalDate(LocalDate.of(2025, 6, 30))
                .applicantHistoryId(96)
                .caseId("CASE-123-C")
                .committalDate(LocalDate.of(2025, 6, 11))
                .repOrderStatus("CURR")
                .appealTypeCode(AppealType.ACN.getCode())
                .crownCourtOutcome(CrownCourtTrialOutcome.CONVICTED.getValue())
                .dateCreated(LocalDate.of(2025, 6, 20))
                .userCreated(USER_NAME)
                .dateModified(LocalDate.of(2025, 6, 21).atStartOfDay())
                .userModified(USER_NAME)
                .caseType(CrownCourtCaseType.EITHER_WAY.getValue())
                .build();
    }
    
    public static BillingDataFeedLogEntity getPopulatedBillingLogEntity(Integer id, LocalDateTime dateCreated) {
        return BillingDataFeedLogEntity.builder()
            .id(id)
            .recordType(BillingDataFeedRecordType.APPLICANT.getValue())
            .dateCreated((dateCreated != null) ? dateCreated : LocalDateTime.parse("2021-10-09T15:01:25"))
            .payload("{}").build();
    }
}
