package uk.gov.justice.laa.maat.scheduled.tasks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FdcReadyRequestDTO {
    @NotNull(message = "maat_reference is mandatory")
    @JsonProperty("maat_reference")
    private Integer maatReference;

    @NotBlank(message = "fdc_ready is mandatory")
    @Pattern(regexp = "Y|N", message = "fdc_ready must be Y or N")
    @JsonProperty("fdc_ready")
    private String fdcReady;

    @NotBlank(message = "item_type is mandatory")
    @Pattern(regexp = "LGFS|AGFS", message = "item_type must be LGFS or AGFS")
    @JsonProperty("item_type")
    private String itemType;
}
