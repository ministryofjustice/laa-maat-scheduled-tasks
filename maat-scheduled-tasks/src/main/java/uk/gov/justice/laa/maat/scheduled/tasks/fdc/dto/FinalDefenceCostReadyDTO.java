package uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalDefenceCostReadyDTO {
    @NotNull(message = "maat_reference is mandatory")
    @Min(value = 1, message = "maat_reference cannot be less than 1.")
    @JsonProperty("maat_reference")
    private int maatReference;

    @NotNull(message = "fdc_ready is mandatory")
    @JsonProperty("fdc_ready")
    private Boolean fdcReady;

    @NotNull(message = "item_type is mandatory")
    @JsonProperty("item_type")
    private FDCType itemType;
}
