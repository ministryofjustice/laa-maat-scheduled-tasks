package uk.gov.justice.laa.maat.scheduled.tasks.fdc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.annotation.StandardApiResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.response.LoadFDCResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.service.FinalDefenceCostServiceImpl;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/fdc/")
public class FinalDefenceCostsController {

    private final FinalDefenceCostServiceImpl finalDefenceCostServiceImpl;

    @PostMapping("v1/load-fdc")
    @Operation(description = "Load and process FDC data into HUB and MAAT")
    @ApiResponse(responseCode = "200", description = "Request processed successfully.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Invalid or missing request data.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @StandardApiResponse
    public ResponseEntity<LoadFDCResponse> loadFdcV1(@RequestParam String payload) {

      if (!payloadIsValidJson(payload)) {
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

    private boolean payloadIsValidJson(String payload) {

      try {
        new ObjectMapper().readTree(payload);
      } catch (IOException ne) {
        return false;
      }

      return true;
    }
}
