package uk.gov.justice.laa.maat.scheduled.tasks.fdc.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.enums.FDCType;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.service.FDCManualDataLoadService;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.service.FinalDefenceCostService;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.FdcTestDataProvider;

@WebMvcTest(FinalDefenceCostController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinalDefenceCostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinalDefenceCostService finalDefenceCostService;

    @MockitoBean
    private FDCManualDataLoadService fdcManualDataLoadService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE = "/api/internal/v1/fdc";

    @Test
    @DisplayName("load: returns 200 and total number of records as inserted.")
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
    @DisplayName("load: returns 400 for failed load.")
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

  @DisplayName("load: returns 500 when service throws exception")
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
  @DisplayName("load: returns 200 and loads only valid records.")
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
  @DisplayName("load: returns 400 when payload is empty.")
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


  @DisplayName("ready: returns 400 when request body is empty")
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

  @DisplayName("ready: returns 200 with success payload when service inserts all items")
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
  @DisplayName("ready: returns 200 with success payload when service inserts only valid items")
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

  @DisplayName("ready: returns 500 when service throws exception")
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

  @Test
  @DisplayName("load-fdc: returns 400 for invalid filename")
  void loadFdc_invalidFilename_returnsBadRequest() throws Exception {
    mockMvc.perform(post(BASE + "/load-fdc")
            .param("fileName", "foo.csv"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.recordsInserted").value(0))
        .andExpect(jsonPath("$.message", containsString("Invalid filename foo.csv")));

    // Service must NOT be called
    verifyNoInteractions(fdcManualDataLoadService);
  }

  @Test
  @DisplayName("load-fdc: CCR* maps to AGFS and returns 200 with body when recordsInserted > 0")
  void loadFdc_ccr_success() throws Exception {
    when(fdcManualDataLoadService.loadFinalDefenceCosts(eq("CCR_202501.csv"), eq(FDCType.AGFS), eq(1000)))
        .thenReturn(5);

    mockMvc.perform(post(BASE + "/load-fdc")
            .param("fileName", "CCR_202501.csv"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.recordsInserted").value(5))
        .andExpect(jsonPath("$.message", containsString("CCR_202501.csv")));

    verify(fdcManualDataLoadService, times(1))
        .loadFinalDefenceCosts("CCR_202501.csv", FDCType.AGFS, 1000);
  }

  @Test
  @DisplayName("load-fdc: CCLF* maps to LGFS and returns 500 when recordsInserted == 0")
  void loadFdc_cclf_failure500_whenZeroInserted() throws Exception {
    when(fdcManualDataLoadService.loadFinalDefenceCosts(eq("CCLF_dump.csv"), eq(FDCType.LGFS), eq(1000)))
        .thenReturn(0);

    mockMvc.perform(post(BASE + "/load-fdc")
            .param("fileName", "CCLF_dump.csv"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.recordsInserted").value(0))
        .andExpect(jsonPath("$.message", containsString("Failed to load dataset from file: CCLF_dump.csv")));

    verify(fdcManualDataLoadService).loadFinalDefenceCosts("CCLF_dump.csv", FDCType.LGFS, 1000);
  }

  @Test
  @DisplayName("load-fdc: mapping is case-insensitive (ccr* -> AGFS)")
  void loadFdc_caseInsensitivePrefix() throws Exception {
    when(fdcManualDataLoadService.loadFinalDefenceCosts(eq("ccr_small.csv"), eq(FDCType.AGFS), eq(1000)))
        .thenReturn(1);

    mockMvc.perform(post(BASE + "/load-fdc")
            .param("fileName", "ccr_small.csv"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.recordsInserted").value(1));

    verify(fdcManualDataLoadService).loadFinalDefenceCosts("ccr_small.csv", FDCType.AGFS, 1000);
  }

  @Test
  @DisplayName("load-fdc-ready: returns 400 for invalid filename")
  void loadFdcReady_invalidFilename_returnsBadRequest() throws Exception {
    mockMvc.perform(post(BASE + "/load-fdc-ready")
            .param("fileName", "random.txt"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.recordsInserted").value(0))
        .andExpect(jsonPath("$.message", containsString("Invalid filename random.txt")));

    verifyNoInteractions(fdcManualDataLoadService);
  }

  @Test
  @DisplayName("load-fdc-ready: CCR* -> AGFS, 200 OK when > 0")
  void loadFdcReady_success_agfs() throws Exception {
    when(fdcManualDataLoadService.loadFdcReady(eq("CCR_ready.csv"), eq(FDCType.AGFS), eq(1000)))
        .thenReturn(7);

    mockMvc.perform(post(BASE + "/load-fdc-ready")
            .param("fileName", "CCR_ready.csv"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.recordsInserted").value(7))
        .andExpect(jsonPath("$.message", containsString("CCR_ready.csv")));

    verify(fdcManualDataLoadService).loadFdcReady("CCR_ready.csv", FDCType.AGFS, 1000);
  }

  @Test
  @DisplayName("load-fdc-ready: CCLF* -> LGFS, 500 when 0")
  void loadFdcReady_failure500_whenZero() throws Exception {
    when(fdcManualDataLoadService.loadFdcReady(eq("CCLF_ready.csv"), eq(FDCType.LGFS), eq(1000)))
        .thenReturn(0);

    mockMvc.perform(post(BASE + "/load-fdc-ready")
            .param("fileName", "CCLF_ready.csv"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.recordsInserted").value(0))
        .andExpect(jsonPath("$.message", containsString("Failed to load dataset from file: CCLF_ready.csv")));

    verify(fdcManualDataLoadService).loadFdcReady("CCLF_ready.csv", FDCType.LGFS, 1000);
  }

  private List<FinalDefenceCostReadyDTO> createTestFDCReadyDtos(String payloadJson)
      throws JsonProcessingException {

    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    return objectMapper.readValue(payloadJson, new TypeReference<>() {});
  }

  private List<FinalDefenceCostDTO> createTestFDCDtos(String payloadJson)
      throws JsonProcessingException {

    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());

    return objectMapper.readValue(payloadJson, new TypeReference<>() {});
  }
}
