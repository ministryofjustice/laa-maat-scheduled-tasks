package uk.gov.justice.laa.maat.scheduled.tasks.fdc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostReadyDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.response.LoadFDCResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.service.FinalDefenceCostService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/internal/v1/fdc")
public class FinalDefenceCostController {

    private final FinalDefenceCostService finalDefenceCostService;

    @PostMapping("/load")
    @Operation(description = "Load and process FDC data into HUB")
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request processed successfully.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
      @ApiResponse(responseCode = "400", description = "Invalid or missing request data.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
      @ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<LoadFDCResponse<FinalDefenceCostDTO>> loadFdc(@RequestBody List<FinalDefenceCostDTO> payload) {

      if (payload == null || payload.isEmpty()) {
        return createResponse(HttpStatus.BAD_REQUEST, false, List.of(), "Request body cannot be empty");
      }

      try {
        List<FinalDefenceCostDTO> failed = finalDefenceCostService.saveFDCItems(payload);
        if (failed.isEmpty()) {
          return createResponse(HttpStatus.OK, true, List.of(), "Loaded dataset successfully.");
        } else {
          return createResponse(HttpStatus.OK, true, failed, "Not all dataset loaded successfully.");
        }
      } catch (Exception e) {
        log.error("Failed to save FDC items", e);
        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, false, List.of(), String.format("Exception while loading FDC data: %s", e.getMessage()));
      }
    }

    @Operation(description = "Save FDC Ready items")
    @PostMapping("/ready")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Request processed successfully.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "400", description = "Invalid or missing request data.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<LoadFDCResponse<FinalDefenceCostReadyDTO>> saveFdcReadyItems(
             @RequestBody List<FinalDefenceCostReadyDTO> requests ) {
        if (requests == null || requests.isEmpty()) {
            return createResponse(HttpStatus.BAD_REQUEST, false, List.of(), "Request body cannot be empty");
        }

        try {
          List<FinalDefenceCostReadyDTO> failed = finalDefenceCostService.saveFdcReadyItems(requests);
            if (failed.isEmpty()) {
                return createResponse(HttpStatus.OK, true, List.of(),
                        String.format("Successfully saved %d FDC Ready items", requests.size()));
            } else {
                return createResponse(HttpStatus.OK, true, failed,
                        "Not all FDC Ready items saved successfully.");
            }
        } catch (Exception e) {
            log.error("Failed to save FDC Ready items", e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, false, List.of(),
                    String.format("Failed to save FDC Ready items: %s", e.getMessage()));
        }
    }


    private <T> ResponseEntity<LoadFDCResponse<T>> createResponse(HttpStatus status, boolean success, List<T> records, String message) {
      return ResponseEntity.status(status).body(
          new LoadFDCResponse<>(success, records, message)
      );
    }
}
