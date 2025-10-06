package uk.gov.justice.laa.maat.scheduled.tasks.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseUtils {
    public static List<Integer> getErroredIdsFromResponseBody(String responseBody, String requestLabel) {
        List<Integer> failedIds = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode body = objectMapper.readTree(responseBody);

            if (body == null) {
                return failedIds;
            }
            
            JsonNode errorsArray = body.get("errors");

            if (errorsArray == null || !errorsArray.isArray()) {
                return failedIds;
            }
            
            for (JsonNode error : errorsArray) {
                JsonNode id = error.get("id");
                if (id != null) {
                    failedIds.add(id.asInt());
                }
            }
        } catch (IOException exception) {
            log.error("Error retrieving the failed {} IDs: {}", requestLabel, exception.getMessage());
        }

        return failedIds;
    }
}
