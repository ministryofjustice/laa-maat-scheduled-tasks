package uk.gov.justice.laa.maat.scheduled.tasks.billing.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.entity.ApplicantHistoryBillingEntity;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface ApplicantHistoryBillingMapper {
    ApplicantHistoryBillingDTO mapEntityToDTO(ApplicantHistoryBillingEntity applicantHistoryEntity);
}

