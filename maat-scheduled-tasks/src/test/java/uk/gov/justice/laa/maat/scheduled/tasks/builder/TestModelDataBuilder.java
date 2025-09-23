package uk.gov.justice.laa.maat.scheduled.tasks.builder;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.enums.AppealType;
import uk.gov.justice.laa.crime.enums.EvidenceFeeLevel;
import uk.gov.justice.laa.crime.enums.MagCourtOutcome;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetApplicantBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetRepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.CrownCourtCaseType;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.CrownCourtTrialOutcome;

import java.time.LocalDate;
import java.time.LocalDateTime;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ResetBillingDTO;

@Component
public class TestModelDataBuilder {

    private static final String USER_NAME = "test-u";

    public static ApplicantHistoryBillingDTO getApplicantHistoryBillingDTO(Integer id) {
        return ApplicantHistoryBillingDTO.builder()
            .id(id)
            .asAtDate(LocalDate.parse("2006-10-06"))
            .applId(id)
            .firstName("test_first")
            .lastName("test_last")
            .otherNames("test")
            .dob(LocalDate.parse("1981-10-14"))
            .gender("Male")
            .niNumber("JM933396A")
            .foreignId(null)
            .dateCreated(LocalDateTime.parse("2021-10-09T15:01:25"))
            .userCreated(USER_NAME)
            .dateModified(null)
            .userModified(null)
            .build();
    }

    public static ApplicantBillingDTO getApplicantDTO(Integer id) {
        return ApplicantBillingDTO.builder()
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

    public static ResetBillingDTO getResetBillingDTO() {
        return ResetBillingDTO.builder()
            .userModified(USER_NAME)
            .ids(List.of(1, 2, 3))
            .build();
    }


    public static RepOrderBillingDTO getRepOrderBillingDTO(Integer id) {
        return RepOrderBillingDTO.builder()
                .id(id)
                .applicantId(123)
                .arrestSummonsNo("ARREST-5678")
                .evidenceFeeLevel(EvidenceFeeLevel.LEVEL1.getFeeLevel())
                .supplierAccountCode("AB123C")
                .magsCourtId(34)
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
                .dateCreated(LocalDate.of(2025, 6, 20).atStartOfDay())
                .userCreated(USER_NAME)
                .dateModified(LocalDate.of(2025, 6, 21).atStartOfDay())
                .userModified(USER_NAME)
                .caseType(CrownCourtCaseType.EITHER_WAY.getValue())
                .build();
    }

    public static ResetApplicantBillingDTO getResetApplicantBillingDTO() {
        return ResetApplicantBillingDTO.builder()
                .userModified("joe-bloggs")
                .ids(List.of(1003456, 1003457))
                .build();
    }

    public static ResetRepOrderBillingDTO getResetRepOrderBillingDTO() {
        return ResetRepOrderBillingDTO.builder()
            .userModified("joe-bloggs")
            .ids(List.of(1003456, 1003457))
            .build();
    }

}
