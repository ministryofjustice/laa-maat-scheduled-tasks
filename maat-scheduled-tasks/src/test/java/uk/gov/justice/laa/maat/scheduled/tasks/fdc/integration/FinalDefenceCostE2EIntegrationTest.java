package uk.gov.justice.laa.maat.scheduled.tasks.fdc.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FdcReadyRequestDTO;
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
            post(BASE + "/load-fdc")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.recordsInserted").value(3))
        .andExpect(jsonPath("$.message").value("Loaded dataset successfully."));
  }

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void testFdcLoad_whenAllInvalidData_returns200() throws Exception {

    String payload = FdcTestDataProvider.getInvalidFdcData();

    mockMvc.perform(
            post(BASE + "/load-fdc")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload)
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                    .jwt(jwt -> jwt.claim("scope", "maat-scheduled-tasks-dev/standard")))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.recordsInserted").value(0))
        .andExpect(jsonPath("$.message").value("Not all dataset loaded successfully."));
  }

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void testFdcLoad_whenSomeInvalidData_returns200() throws Exception {

    String payload = FdcTestDataProvider.getInvalidFdcDataWithMissingFields();

    mockMvc.perform(
            post(BASE + "/load-fdc")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.recordsInserted").value(1))
        .andExpect(jsonPath("$.message").value("Not all dataset loaded successfully."));
  }

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void testFdcLoad_whenEmpty_returns400() throws Exception {

    String payload = "[]";

    mockMvc.perform(
            post(BASE + "/load-fdc")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.recordsInserted").value(0))
        .andExpect(jsonPath("$.message").value("Request body cannot be empty"));
  }

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void saveFdcReadyReturns400WhenBodyEmpty() throws Exception {
    mockMvc.perform(post(BASE+"/save-fdc-ready")
            .contentType(MediaType.APPLICATION_JSON)
            .content("[]"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.recordsInserted").value(0))
        .andExpect(jsonPath("$.message").value("Request body cannot be empty"));
  }

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void saveFdcReadyReturns200WithSuccessPayload() throws Exception {

    List<FdcReadyRequestDTO> requests = List.of(
        new FdcReadyRequestDTO(123, "Y", "AGFS"),
        new FdcReadyRequestDTO(456, "N", "AGFS")
    );
    String body = objectMapper.writeValueAsString(requests);

    mockMvc.perform(post(BASE + "/save-fdc-ready")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.recordsInserted").value(2))
        .andExpect(jsonPath("$.message").value("Successfully saved 2 FDC Ready items"));

  }

  @Test
  @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
  void saveFdcReadyReturnsZeroForInvalidRequestDto() throws Exception {
    List<FdcReadyRequestDTO> requests = List.of(
        new FdcReadyRequestDTO(123, "Y", "AGFS"),
        new FdcReadyRequestDTO(456, "Y1", "IN-VALID")
    );
    String body = objectMapper.writeValueAsString(requests);

    mockMvc.perform(post(BASE + "/save-fdc-ready")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.recordsInserted").value(1))
        .andExpect(jsonPath("$.message").value("Not all FDC Ready items saved successfully."));
  }
}
