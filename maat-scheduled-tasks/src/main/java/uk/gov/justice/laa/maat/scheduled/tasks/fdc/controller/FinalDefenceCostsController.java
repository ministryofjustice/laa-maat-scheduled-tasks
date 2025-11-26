package uk.gov.justice.laa.maat.scheduled.tasks.fdc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.response.LoadFDCResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.service.FinalDefenceCostService;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/internal/v1/fdc")
public class FinalDefenceCostsController {

    private final FinalDefenceCostService finalDefenceCostService;

    @PostMapping("load-fdc")
    @Operation(description = "Load and process FDC data into HUB and MAAT")
    @ApiResponse(responseCode = "200", description = "Request processed successfully.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Invalid or missing request data.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    public ResponseEntity<LoadFDCResponse> loadFdc(@Valid @RequestBody List<FinalDefenceCostDto> payload) {

      int recordsInserted = finalDefenceCostService.processFinalDefenceCosts(payload, 1000);
      if (recordsInserted > 0) {
        return ResponseEntity.ok(
            new LoadFDCResponse(true, recordsInserted, "Loaded dataset successfully.")
        );
      } else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new LoadFDCResponse(false, 0, "No data loaded.")
        );
      }
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<LoadFDCResponse> handleConstraintViolationException(ConstraintViolationException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
          new LoadFDCResponse(false, 0, "Invalid or missing request data.")
      );
    }
}
