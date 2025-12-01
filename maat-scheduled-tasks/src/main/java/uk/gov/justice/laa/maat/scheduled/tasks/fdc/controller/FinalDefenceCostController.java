package uk.gov.justice.laa.maat.scheduled.tasks.fdc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FdcReadyRequestDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.response.LoadFDCResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.service.FinalDefenceCostService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/internal/v1/fdc")
public class FinalDefenceCostController {

    private final FinalDefenceCostService finalDefenceCostService;

    @PostMapping("load-fdc")
    @Operation(description = "Load and process FDC data into HUB")
    @ApiResponse(responseCode = "200", description = "Request processed successfully.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Invalid or missing request data.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    public ResponseEntity<LoadFDCResponse> loadFdc(@RequestBody List<FinalDefenceCostDto> payload) {

      if (payload == null || payload.isEmpty()) {
        return createResponse(HttpStatus.BAD_REQUEST, false, 0, "Request body cannot be empty");
      }

      try {
        int recordsInserted = finalDefenceCostService.processFinalDefenceCosts(payload);
        if (recordsInserted == payload.size()) {
          return createResponse(HttpStatus.OK, true, recordsInserted, "Loaded dataset successfully.");
        } else {
          return createResponse(HttpStatus.OK, true, recordsInserted, "Not all dataset loaded successfully.");
        }
      } catch (Exception e) {
        log.error("Failed to save FDC items", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new LoadFDCResponse(false, 0,
                String.format("Failed to load FDC data: %s", e.getMessage()))
        );
      }
    }

    @Operation(description = "Save FDC Ready items")
    @PostMapping("save-fdc-ready")
    public ResponseEntity<LoadFDCResponse> saveFdcReadyItems(
             @RequestBody List<FdcReadyRequestDTO> requests ) {
        if (requests == null || requests.isEmpty()) {
            return createResponse(HttpStatus.BAD_REQUEST, false, 0, "Request body cannot be empty");
        }

        try {
            int recordsInserted = finalDefenceCostService.saveFdcReadyItems(requests);
            if (recordsInserted == requests.size()) {
                return createResponse(HttpStatus.OK, true, recordsInserted,
                        String.format("Successfully saved %d FDC Ready items", recordsInserted));
            } else {
                return createResponse(HttpStatus.OK, true, recordsInserted,
                        "Not all FDC Ready items saved successfully.");
            }
        } catch (Exception e) {
            log.error("Failed to save FDC Ready items", e);
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, false, 0,
                    String.format("Failed to save FDC Ready items: %s", e.getMessage()));
        }
    }


    private ResponseEntity<LoadFDCResponse> createResponse(HttpStatus status, boolean success, int records, String message) {
      return ResponseEntity.status(status).body(
          new LoadFDCResponse(success, records, message)
      );
    }
}
