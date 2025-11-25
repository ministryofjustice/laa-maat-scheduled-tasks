package uk.gov.justice.laa.maat.scheduled.tasks.fdc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.annotation.StandardApiResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostsEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.response.LoadFDCResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.service.FinalDefenceCostServiceImpl;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.ObjectsValidator;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/fdc/")
public class FinalDefenceCostsController {

    private final FinalDefenceCostServiceImpl finalDefenceCostServiceImpl;

    private final ObjectsValidator<FinalDefenceCostDto> postValidator;

    @PostMapping("v1/load-fdc")
    @Operation(description = "Load and process FDC data into HUB and MAAT")
    @ApiResponse(responseCode = "200", description = "Request processed successfully.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Invalid or missing request data.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    public ResponseEntity<LoadFDCResponse> loadFdcV1(@RequestBody List<FinalDefenceCostDto> payload) {

      if (!validatePayload(payload)) {
        return ResponseEntity.badRequest()
            .body(new LoadFDCResponse(true, 0, "JSON payload is invalid."));
      }

      int recordsInserted = finalDefenceCostServiceImpl.processFinalDefenceCosts(payload);
      if (recordsInserted > 0) {
          return ResponseEntity.ok(
              new LoadFDCResponse(true, recordsInserted, "Loaded dataset successfully.")
          );
      } else {
          return ResponseEntity.internalServerError()
              .body(new LoadFDCResponse(false, 0, "Failed to load dataset")
          );

      }
    }

  private boolean validatePayload(List<FinalDefenceCostDto> payload) {
    Set<String> violations = new HashSet<>();

    for (FinalDefenceCostDto dto : payload) {
      violations.addAll(postValidator.validate(dto));
    }

    if (!violations.isEmpty()) {

      String errors = String.join("\n", violations);
      log.error(errors);
      return false;
    }

    return true;
  }
}
