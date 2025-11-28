package uk.gov.justice.laa.maat.scheduled.tasks.fdc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.config.FinalDefenceCostConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.service.FinalDefenceCostService;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.FdcTestDataProvider;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.justice.laa.crime.util.RequestBuilderUtils.buildRequestGivenContent;

@WebMvcTest(FinalDefenceCostController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinalDefenceCostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private FinalDefenceCostConfiguration fdcConfiguration;

    @MockitoBean
    private FinalDefenceCostService finalDefenceCostService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE = "/api/internal/v1/fdc";

    @Test
    @DisplayName("load-fdc: returns 200 and total number of records as inserted.")
    void testLoadFdc_whenValidData_thenReturnSuccess() throws Exception {

      String fdcDataJson = FdcTestDataProvider.getValidFdcData();

      List<FinalDefenceCostDto> payload = createTestFDCDtos(fdcDataJson);
      when(fdcConfiguration.getFetchSize()).thenReturn(1);
      when(finalDefenceCostService.processFinalDefenceCosts(payload)).thenReturn(3);

      mockMvc.perform(
          buildRequestGivenContent(HttpMethod.POST, fdcDataJson, BASE + "/load-fdc", false))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.records_inserted").value(3))
          .andExpect(jsonPath("$.message", containsString("Loaded dataset successfully.")));

      verify(finalDefenceCostService, times(1)).processFinalDefenceCosts(payload);
    }

    @Test
    @DisplayName("load-fdc: returns 400 for failed load.")
    void testLoadFdc_whenInvalidData_thenReturnBadRequest() throws Exception {

      String fdcDataJson = FdcTestDataProvider.getInvalidFdcData();

      List<FinalDefenceCostDto> payload = createTestFDCDtos(fdcDataJson);
      when(fdcConfiguration.getFetchSize()).thenReturn(1);
      when(finalDefenceCostService.processFinalDefenceCosts(payload)).thenReturn(1);

      mockMvc.perform(
              buildRequestGivenContent(HttpMethod.POST, fdcDataJson, BASE + "/load-fdc", false))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.records_inserted").value(1))
          .andExpect(jsonPath("$.message", containsString("Not all dataset loaded dataset successfully.")));

      verify(finalDefenceCostService).processFinalDefenceCosts(payload);
    }

  @DisplayName("load-fdc: returns 500 when service throws exception")
  @Test
  void loadFdcReturns500OnServiceException() throws Exception {
    when(finalDefenceCostService.processFinalDefenceCosts(any()))
        .thenThrow(new RuntimeException("Internal Server Error"));

    String body = FdcTestDataProvider.getInvalidFdcData();

    mockMvc.perform(post(BASE + "/load-fdc")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.records_inserted").value(0))
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Failed to load FDC data: Internal Server Error")));

    verify(finalDefenceCostService).processFinalDefenceCosts(any());
  }

    @Test
    @DisplayName("load-fdc: returns 200 and loads only valid records.")
    void testLoadFdc_Data_whenMissingDataField_thenReturnBadRequest() throws Exception {

      String fdcDataJson = FdcTestDataProvider.getInvalidFdcDataWithMissingFields();

      List<FinalDefenceCostDto> payload = createTestFDCDtos(fdcDataJson);
      when(fdcConfiguration.getFetchSize()).thenReturn(1);
      when(finalDefenceCostService.processFinalDefenceCosts(payload)).thenReturn(1);

      mockMvc.perform(
              buildRequestGivenContent(HttpMethod.POST, fdcDataJson, BASE + "/load-fdc", false))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.records_inserted").value(1))
          .andExpect(jsonPath("$.message", containsString("Not all dataset loaded dataset successfully.")));

      verify(finalDefenceCostService).processFinalDefenceCosts(payload);
    }

    @Test
    @DisplayName("load-fdc: returns 400 for failed load.")
    void testLoadFdc_Data_whenPayloadEmpty_thenReturnBadRequest() throws Exception {

      String fdcDataJson = "[]";

      List<FinalDefenceCostDto> payload = createTestFDCDtos(fdcDataJson);
      when(fdcConfiguration.getFetchSize()).thenReturn(1);
      when(finalDefenceCostService.processFinalDefenceCosts(payload)).thenReturn(3);

      mockMvc.perform(
              buildRequestGivenContent(HttpMethod.POST, fdcDataJson, BASE + "/load-fdc", false))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.records_inserted").value(0))
          .andExpect(jsonPath("$.message", containsString("Request body cannot be empty")));

      verifyNoInteractions(finalDefenceCostService);
    }


    @DisplayName("load-fdc-ready: returns 400 when request body is empty")
    @Test
    void loadFdcReadyReturns400WhenBodyEmpty() throws Exception {
        mockMvc.perform(post(BASE + "/load-fdc-ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.records_inserted").value(0))
                .andExpect(jsonPath("$.message").value("Request body cannot be empty"));
    }

    @DisplayName("load-fdc-ready: returns 200 with success payload when service inserts items")
    @Test
    void loadFdcReadyReturns200WithSuccessPayload() throws Exception {
        when(finalDefenceCostService.processFdcReadyItems(any())).thenReturn(2);

        String body = """
        [
          {"maatReference":123,"fdcReady":"Y","itemType":"AGFS"},
          {"maatReference":456,"fdcReady":"N","itemType":"AGFS"}
        ]
        """;

        mockMvc.perform(post(BASE + "/load-fdc-ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.records_inserted").value(2))
                .andExpect(jsonPath("$.message").value("Successfully saved 2 FDC Ready items"));

        verify(finalDefenceCostService).processFdcReadyItems(any());
    }

    @DisplayName("load-fdc-ready: returns 500 when service throws exception")
    @Test
    void loadFdcReadyReturns500OnServiceException() throws Exception {
        when(finalDefenceCostService.processFdcReadyItems(any()))
                .thenThrow(new RuntimeException("DB down"));

        String body = """
        [
          {"maatReference":789,"fdcReady":"Y","itemType":"LGFS"}
        ]
        """;

        mockMvc.perform(post(BASE + "/load-fdc-ready")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.records_inserted").value(0))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Failed to save FDC Ready items: DB down")));

        verify(finalDefenceCostService).processFdcReadyItems(any());
    }

    private List<FinalDefenceCostDto> createTestFDCDtos(String payloadJson)
        throws JsonProcessingException {

      objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());

      return objectMapper.readValue(payloadJson, new TypeReference<>() {});
    }
}
