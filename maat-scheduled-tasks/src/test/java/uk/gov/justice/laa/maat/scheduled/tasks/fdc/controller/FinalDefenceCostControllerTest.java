package uk.gov.justice.laa.maat.scheduled.tasks.fdc.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.justice.laa.crime.util.RequestBuilderUtils.buildRequestGivenContent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostReadyDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.service.FinalDefenceCostService;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.FdcTestDataProvider;

@WebMvcTest(FinalDefenceCostController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinalDefenceCostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinalDefenceCostService finalDefenceCostService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE = "/api/internal/v1/fdc";

    @Test
    @DisplayName("load-fdc: returns 200 and total number of records as inserted.")
    void testLoadFdc_whenValidData_thenReturnSuccess() throws Exception {

      String fdcDataJson = FdcTestDataProvider.getValidFdcData();

      List<FinalDefenceCostDTO> payload = createTestFDCDtos(fdcDataJson);
      when(finalDefenceCostService.saveFDCItems(payload)).thenReturn(List.of());

      mockMvc.perform(
          buildRequestGivenContent(HttpMethod.POST, fdcDataJson, BASE + "/load", false))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.invalid", hasSize(0)))
          .andExpect(jsonPath("$.message", containsString("Loaded dataset successfully.")));

      verify(finalDefenceCostService, times(1)).saveFDCItems(payload);
    }

    @Test
    @DisplayName("load-fdc: returns 400 for failed load.")
    void testLoadFdc_whenInvalidData_thenReturnBadRequest() throws Exception {

      String fdcDataJson = FdcTestDataProvider.getInvalidFdcData();

      List<FinalDefenceCostDTO> payload = createTestFDCDtos(fdcDataJson);
      when(finalDefenceCostService.saveFDCItems(payload)).thenReturn(payload);

      mockMvc.perform(
              buildRequestGivenContent(HttpMethod.POST, fdcDataJson, BASE + "/load", false))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(content().json("""
                                    {
                                      "success": true,
                                      "invalid": %s,
                                      "message": "Not all dataset loaded successfully."
                                    }
                                    """.formatted(fdcDataJson)))
          .andExpect(jsonPath("$.message", containsString("Not all dataset loaded successfully.")));

      verify(finalDefenceCostService).saveFDCItems(payload);
    }

  @DisplayName("load-fdc: returns 500 when service throws exception")
  @Test
  void testLoadFdc_returns500ServiceException() throws Exception {
    when(finalDefenceCostService.saveFDCItems(any()))
        .thenThrow(new RuntimeException("Internal Server Error"));

    String body = FdcTestDataProvider.getInvalidFdcData();

    mockMvc.perform(post(BASE + "/load")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.invalid", hasSize(0)))
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Exception while loading FDC data: Internal Server Error")));

    verify(finalDefenceCostService).saveFDCItems(any());
  }

    @Test
    @DisplayName("load-fdc: returns 200 and loads only valid records.")
    void testLoadFdc_Data_whenMissingDataField_thenReturnBadRequest() throws Exception {

      String fdcDataJson = FdcTestDataProvider.getInvalidFdcDataWithMissingFields();

      List<FinalDefenceCostDTO> payload = createTestFDCDtos(fdcDataJson);
      when(finalDefenceCostService.saveFDCItems(payload)).thenReturn(payload.subList(0, 2));
      String result = objectMapper.writeValueAsString(payload.subList(0, 2));

      mockMvc.perform(
              buildRequestGivenContent(HttpMethod.POST, fdcDataJson, BASE + "/load", false))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(content().json("""
                                    {
                                      "success": true,
                                      "invalid": %s,
                                      "message": "Not all dataset loaded successfully."
                                    }
                                    """.formatted(result)))
          .andExpect(jsonPath("$.message", containsString("Not all dataset loaded successfully.")));

      verify(finalDefenceCostService).saveFDCItems(payload);
    }

    @Test
    @DisplayName("load-fdc: returns 400 when payload is empty.")
    void testLoadFdc_Data_whenPayloadEmpty_thenReturnBadRequest() throws Exception {

      String fdcDataJson = "[]";

      List<FinalDefenceCostDTO> payload = createTestFDCDtos(fdcDataJson);
      when(finalDefenceCostService.saveFDCItems(payload)).thenReturn(List.of());

      mockMvc.perform(
              buildRequestGivenContent(HttpMethod.POST, fdcDataJson, BASE + "/load", false))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.invalid", hasSize(0)))
          .andExpect(jsonPath("$.message", containsString("Request body cannot be empty")));

      verifyNoInteractions(finalDefenceCostService);
    }


    @DisplayName("save-fdc-ready: returns 400 when request body is empty")
    @Test
    void saveFdcReadyReturns400WhenBodyEmpty() throws Exception {
        mockMvc.perform(post(BASE + "/ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.invalid", hasSize(0)))
                .andExpect(jsonPath("$.message").value("Request body cannot be empty"));
    }

    @DisplayName("save-fdc-ready: returns 200 with success payload when service inserts all items")
    @Test
    void saveFdcReadyReturns200WithSuccessPayload() throws Exception {

        String body = """
        [
          {"maatReference":123,"fdcReady":"Y","itemType":"AGFS"},
          {"maatReference":456,"fdcReady":"N","itemType":"AGFS"}
        ]
        """;

        when(finalDefenceCostService.saveFdcReadyItems(any())).thenReturn(List.of());

        mockMvc.perform(post(BASE + "/ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.invalid", hasSize(0)))
                .andExpect(jsonPath("$.message").value("Successfully saved 2 FDC Ready items"));

        verify(finalDefenceCostService).saveFdcReadyItems(any());
    }

    @Test
    @DisplayName("save-fdc-ready: returns 200 with success payload when service inserts only valid items")
    void saveFdcReadyReturns200WithSuccessPayloadAndFailedItems() throws Exception {

        String body = """
        [
          {"maatReference":123,"fdcReady":"Y","itemType":"AGFS"},
          {"maatReference":456,"fdcReady":"N","itemType":"AGFS"},
          {"maatReference":456,"fdcReady":"N","itemType":"InValid"}
        ]
        """;

        List<FinalDefenceCostReadyDTO> payload = createTestFDCReadyDtos(body);
        when(finalDefenceCostService.saveFdcReadyItems(payload)).thenReturn(payload.subList(1, 2));
        String result = objectMapper.writeValueAsString(payload.subList(1, 2));

        mockMvc.perform(
                        buildRequestGivenContent(HttpMethod.POST, body, BASE + "/ready", false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(content().json("""
                                    {
                                      "success": true,
                                      "invalid": %s,
                                      "message": "Not all FDC Ready items saved successfully."
                                    }
                                    """.formatted(result)))
                .andExpect(jsonPath("$.message", containsString("Not all FDC Ready items saved successfully.")));

        verify(finalDefenceCostService).saveFdcReadyItems(payload);
    }

    @DisplayName("save-fdc-ready: returns 500 when service throws exception")
    @Test
    void saveFdcReadyReturns500OnServiceException() throws Exception {

        String body = """
        [
          {"maatReference":789,"fdcReady":"Y","itemType":"LGFS"}
        ]
        """;

        when(finalDefenceCostService.saveFdcReadyItems(any()))
          .thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(post(BASE + "/ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.invalid", hasSize(0)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Failed to save FDC Ready items: DB down")));

        verify(finalDefenceCostService).saveFdcReadyItems(any());
    }

    private List<FinalDefenceCostDTO> createTestFDCDtos(String payloadJson)
        throws JsonProcessingException {

      objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());

      return objectMapper.readValue(payloadJson, new TypeReference<>() {});
    }

  private List<FinalDefenceCostReadyDTO> createTestFDCReadyDtos(String payloadJson)
      throws JsonProcessingException {

    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    return objectMapper.readValue(payloadJson, new TypeReference<>() {});
  }
}
