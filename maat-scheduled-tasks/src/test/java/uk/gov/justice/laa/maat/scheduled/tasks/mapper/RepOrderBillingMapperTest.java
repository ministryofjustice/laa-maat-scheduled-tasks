package uk.gov.justice.laa.maat.scheduled.tasks.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.builder.TestEntityDataBuilder;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.RepOrderBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;

@ExtendWith(MockitoExtension.class)
class RepOrderBillingMapperTest {

    @Test
    void givenARepOrderEntity_whenMapEntityToDtoIsInvoked_thenDtoIsReturned() {
        RepOrderBillingDTO expectedRepOrder = RepOrderBillingDTO.builder()
            .id(123)
            .applicantId(123)
            .arrestSummonsNo("ARREST-5678")
            .evidenceFeeLevel("LEVEL1")
            .supplierAccountCode("AB123C")
            .magsCourtId(34)
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
            .dateCreated(LocalDate.of(2025, 6, 20).atStartOfDay())
            .userCreated("test-u")
            .dateModified(LocalDate.of(2025, 6, 21).atStartOfDay())
            .userModified("test-u")
            .caseType("EITHER WAY")
            .build();

        RepOrderBillingEntity entity = TestEntityDataBuilder.getPopulatedRepOrderForBilling(123);

        RepOrderBillingDTO actualRepOrder = RepOrderBillingMapper.mapEntityToDTO(entity);

        assertThat(actualRepOrder).isEqualTo(expectedRepOrder);
    }

}
