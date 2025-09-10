package uk.gov.justice.laa.maat.scheduled.tasks.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;
import uk.gov.justice.laa.maat.scheduled.tasks.responses.LoadFDCResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.service.FDCDataLoadService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/v1/fdc")
public class FinalDefenceCostsController {
    private final FDCDataLoadService fdcDataLoadService;

    @PostMapping("load-fdc")
    public ResponseEntity<LoadFDCResponse> loadFdc(
            @RequestParam String fileName
    ) {
        FDCType itemType = getFdcType(fileName);

        if (itemType == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new LoadFDCResponse(false, 0,
                            String.format("Invalid filename %s", fileName))
            );
        }

        int recordsInserted = fdcDataLoadService.loadFinalDefenceCosts(fileName, itemType, 1000);
        if (recordsInserted > 0) {
            return ResponseEntity.ok(
                new LoadFDCResponse(true, recordsInserted,
                    String.format("Loaded dataset from file: %s", fileName))
            );
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new LoadFDCResponse(false, 0,
                            String.format("Failed to load dataset from file: %s", fileName))
            );

        }
    }

    @PostMapping("load-fdc-ready")
    public ResponseEntity<LoadFDCResponse> loadFdcReady(
            @RequestParam String fileName
    ) {
        FDCType itemType = getFdcType(fileName);

        if (itemType == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new LoadFDCResponse(false, 0,
                            String.format("Invalid filename %s", fileName))
            );
        }
        int recordsInserted = fdcDataLoadService.loadFdcReady(fileName, itemType, 1000);
        if (recordsInserted > 0) {
            return ResponseEntity.ok(
                    new LoadFDCResponse(true, recordsInserted,
                            String.format("Loaded dataset from file: %s", fileName))
            );
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new LoadFDCResponse(false, 0,
                            String.format("Failed to load dataset from file: %s", fileName))
            );
        }
    }

    @Nullable
    private static FDCType getFdcType(String fileName) {
        FDCType itemType;
        if (fileName.toUpperCase().startsWith("CCR")) {
            itemType = FDCType.AGFS;
        } else if (fileName.toUpperCase().startsWith("CCLF")) {
            itemType = FDCType.LGFS;
        } else {
            itemType = null;
        }
        return itemType;
    }
}
