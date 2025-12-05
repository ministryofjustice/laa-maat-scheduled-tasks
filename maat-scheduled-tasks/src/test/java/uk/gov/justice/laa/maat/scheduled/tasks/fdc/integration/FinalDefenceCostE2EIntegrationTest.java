package uk.gov.justice.laa.maat.scheduled.tasks.fdc.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.YesNoFlag;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostReadyDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.FdcTestDataProvider;

@SpringBootTest
@AutoConfigureMockMvc
public class FinalDefenceCostE2EIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  private static final String BASE = "/api/internal/v1/fdc";

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void testFdcLoad_whenAllValidData_returns200() throws Exception {

    String payload = FdcTestDataProvider.getValidFdcData();

    mockMvc.perform(
            post(BASE + "/load")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.invalid", hasSize(0)))
        .andExpect(jsonPath("$.message").value("Loaded dataset successfully."));
  }

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void testFdcLoad_whenAllInvalidData_returns200() throws Exception {

    String payload = FdcTestDataProvider.getInvalidFdcData();
    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    List<FinalDefenceCostDTO> invalid = objectMapper.readValue(
        payload, new TypeReference<>() {
        });

    mockMvc.perform(
            post(BASE + "/load")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(content().json("""
                                    {
                                      "success": true,
                                      "invalid": %s,
                                      "message": "Not all dataset loaded successfully."
                                    }
                                    """.formatted(payload)))
        .andExpect(jsonPath("$.message").value("Not all dataset loaded successfully."));
  }

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void testFdcLoad_whenSomeInvalidData_returns200() throws Exception {

    String payload = FdcTestDataProvider.getInvalidFdcDataWithMissingFields();

    String retJson = """
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
              "paid_as_claimed": "Y"
            }
          ]
        """;

    mockMvc.perform(
            post(BASE + "/load")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(content().json("""
                                    {
                                      "success": true,
                                      "invalid": %s,
                                      "message": "Not all dataset loaded successfully."
                                    }
                                    """.formatted(retJson)))
        .andExpect(jsonPath("$.message").value("Not all dataset loaded successfully."));
  }

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void testFdcLoad_whenEmpty_returns400() throws Exception {

    String payload = "[]";

    mockMvc.perform(
            post(BASE + "/load")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.invalid", hasSize(0)))
        .andExpect(jsonPath("$.message").value("Request body cannot be empty"));
  }

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void saveFdcReadyReturns400WhenBodyEmpty() throws Exception {
    mockMvc.perform(post(BASE+"/ready")
            .contentType(MediaType.APPLICATION_JSON)
            .content("[]"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.invalid", hasSize(0)))
        .andExpect(jsonPath("$.message").value("Request body cannot be empty"));
  }

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void saveFdcReadyReturns200WithSuccessPayload() throws Exception {

    List<FinalDefenceCostReadyDTO> requests = List.of(
        new FinalDefenceCostReadyDTO(123, YesNoFlag.Y, FDCType.AGFS),
        new FinalDefenceCostReadyDTO(456, YesNoFlag.N, FDCType.LGFS)
    );
    String body = objectMapper.writeValueAsString(requests);

    mockMvc.perform(post(BASE + "/ready")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.invalid", hasSize(0)))
        .andExpect(jsonPath("$.message").value("Successfully saved 2 FDC Ready items"));

  }

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void saveFdcReadyReturnsZeroForInvalidRequestDto() throws Exception {
    List<FinalDefenceCostReadyDTO> requests = List.of(
        new FinalDefenceCostReadyDTO(123, YesNoFlag.Y, FDCType.AGFS),
        new FinalDefenceCostReadyDTO(456, YesNoFlag.Y, null)
    );
    String body = objectMapper.writeValueAsString(requests);
    String invalid = objectMapper.writeValueAsString(List.of(new FinalDefenceCostReadyDTO(456, YesNoFlag.Y, null)));

    mockMvc.perform(post(BASE + "/ready")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(content().json("""
                                    {
                                      "success": true,
                                      "invalid": %s,
                                      "message": "Not all FDC Ready items saved successfully."
                                    }
                                    """.formatted(invalid)))
        .andExpect(jsonPath("$.message").value("Not all FDC Ready items saved successfully."));
  }
}
