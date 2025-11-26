package uk.gov.justice.laa.maat.scheduled.tasks.fdc.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.service.FinalDefenceCostService;

@WebMvcTest(FinalDefenceCostsController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinalDefenceCostsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinalDefenceCostService finalDefenceCostService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE = "/api/internal/v1/fdc";

    @Test
    @DisplayName("load-fdc: returns 200 for successful load.")
    void loadFdc_validData_returnsSuccess() throws Exception {

      String fdcDataJson = """
              [
                {
                  "maat_reference": 123456,
                  "case_no": "CASE1",
                  "supp_account_code": "SUPPLIER1",
                  "court_code": "COURT1",
                  "judicial_apportionment": 11,
                  "final_defence_cost": 456.64,
                  "item_type": "LGFS",
                  "paid_as_claimed": "Y"
                },
                {
                  "maat_reference": 234567,
                  "case_no": "CASE2",
                  "supp_account_code": "SUPPLIER2",
                  "court_code": "COURT2",
                  "judicial_apportionment": 12,
                  "final_defence_cost": 564.32,
                  "item_type": "LGFS",
                  "paid_as_claimed": "Y"
                },
                {
                  "maat_reference": 6785643,
                  "case_no": "CASE3",
                  "supp_account_code": "SUPPLIER3",
                  "court_code": "COURT3",
                  "judicial_apportionment": 13,
                  "final_defence_cost": 7365.98,
                  "item_type": "LGFS",
                  "paid_as_claimed": "N"
                }
              ]
              """;

      List<FinalDefenceCostDto> payload = createTestFDCDtos(fdcDataJson);
      when(finalDefenceCostService.processFinalDefenceCosts(payload, 1000)).thenReturn(3);

      mockMvc.perform(
          buildRequestGivenContent(HttpMethod.POST, fdcDataJson, BASE + "/load-fdc", false))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.records_inserted").value(3))
          .andExpect(jsonPath("$.message", containsString("Loaded dataset successfully.")));

      verify(finalDefenceCostService, times(1)).processFinalDefenceCosts(payload, 1000);
    }

    @Test
    @DisplayName("load-fdc: returns 400 for failed load.")
    void loadFdc_invalidData_returnsBadRequest() throws Exception {

      String fdcDataJson = """
                [
                  {
                    "maat_reference": 123456,
                    "case_no": "CASE1",
                    "supp_account_code": "",
                    "court_code": "COURT1",
                    "judicial_apportionment": 11,
                    "final_defence_cost": 456.64,
                    "item_type": "LGFS",
                    "paid_as_claimed": "Y"
                  },
                  {
                    "maat_reference": 234567,
                    "case_no": "CASE2",
                    "supp_account_code": "SUPPLIER2",
                    "court_code": "COURT2",
                    "judicial_apportionment": 12,
                    "final_defence_cost": 564.32,
                    "item_type": "HGFS",
                    "paid_as_claimed": "Y"
                  },
                  {
                    "maat_reference": 6785643,
                    "case_no": "CASE3",
                    "supp_account_code": "SUPPLIER3",
                    "court_code": "COURT3",
                    "judicial_apportionment": 13,
                    "final_defence_cost": 7365.98,
                    "item_type": "LGFS",
                    "paid_as_claimed": "G"
                  }
                ]
                """;

      List<FinalDefenceCostDto> payload = createTestFDCDtos(fdcDataJson);
      when(finalDefenceCostService.processFinalDefenceCosts(payload, 1000)).thenReturn(3);

      mockMvc.perform(
              buildRequestGivenContent(HttpMethod.POST, fdcDataJson, BASE + "/load-fdc", false))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.records_inserted").value(0))
          .andExpect(jsonPath("$.message", containsString("Invalid or missing request data.")));
    }

    @Test
    @DisplayName("load-fdc: returns 400 for failed load.")
    void loadFdc_MissingDataField_returnsBadRequest() throws Exception {

      String fdcDataJson = """
                  [
                    {
                      "maat_reference": 123456,
                      "case_no": "CASE1",
                      "supp_account_code": "SUPPLIER1",
                      "court_code": "COURT1",
                      "judicial_apportionment": 11,
                      "final_defence_cost": 456.64,
                      "paid_as_claimed": "Y"
                    },
                    {
                      "maat_reference": 234567,
                      "case_no": "CASE2",
                      "court_code": "COURT2",
                      "judicial_apportionment": 12,
                      "final_defence_cost": 564.32,
                      "item_type": "HGFS",
                      "paid_as_claimed": "Y"
                    },
                    {
                      "maat_reference": 6785643,
                      "case_no": "CASE3",
                      "supp_account_code": "SUPPLIER3",
                      "court_code": "COURT3",
                      "judicial_apportionment": 13,
                      "final_defence_cost": 7365.98,
                      "item_type": "LGFS",
                      "paid_as_claimed": "G"
                    }
                  ]
                  """;

      List<FinalDefenceCostDto> payload = createTestFDCDtos(fdcDataJson);
      when(finalDefenceCostService.processFinalDefenceCosts(payload, 1000)).thenReturn(3);

      mockMvc.perform(
              buildRequestGivenContent(HttpMethod.POST, fdcDataJson, BASE + "/load-fdc", false))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.records_inserted").value(0))
          .andExpect(jsonPath("$.message", containsString("Invalid or missing request data.")));
    }

    private List<FinalDefenceCostDto> createTestFDCDtos(String payloadJson)
        throws JsonProcessingException {

      objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());

      return objectMapper.readValue(payloadJson, new TypeReference<>() {});
    }
}
