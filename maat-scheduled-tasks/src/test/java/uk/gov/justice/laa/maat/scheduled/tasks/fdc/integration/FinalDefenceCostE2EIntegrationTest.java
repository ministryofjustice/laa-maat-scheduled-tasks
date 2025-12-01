package uk.gov.justice.laa.maat.scheduled.tasks.fdc.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.response.LoadFDCResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.FdcTestDataProvider;

@SpringBootTest
@AutoConfigureMockMvc
public class FinalDefenceCostE2EIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  void testEndToEnd_withJwtAuth() throws Exception {

    // ---- POST /api/persons (with OAuth2 JWT) ----
    String payload = FdcTestDataProvider.getValidFdcData();

    String postResponse = mockMvc.perform(
            post("/api/internal/v1/fdc/load-fdc")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload)
                .with(SecurityMockMvcRequestPostProcessors.jwt()
                    .jwt(jwt -> jwt.claim("scope", "maat-scheduled-tasks-dev/standard")))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    LoadFDCResponse created = objectMapper.readValue(postResponse, LoadFDCResponse.class);
    assertThat(created.success()).isEqualTo(true);
    assertThat(created.recordsInserted()).isEqualTo(3);
    assertThat(created.message()).isEqualTo("Loaded dataset successfully.");
  }
}
