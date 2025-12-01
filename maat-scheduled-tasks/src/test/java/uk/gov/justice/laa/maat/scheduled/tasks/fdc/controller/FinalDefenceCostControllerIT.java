package uk.gov.justice.laa.maat.scheduled.tasks.fdc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.validator.FdcItemValidator;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FdcReadyRequestDTO;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FinalDefenceCostControllerIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FdcItemValidator fdcItemValidator;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE = "/api/internal/v1/fdc/save-fdc-ready";

    @Test
    @WithMockUser(authorities = "SCOPE_maat-scheduled-tasks-dev/standard")
    void saveFdcReadyReturns400WhenBodyEmpty() throws Exception {
        mockMvc.perform(post(BASE)
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

        mockMvc.perform(post(BASE)
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

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.recordsInserted").value(1))
                .andExpect(jsonPath("$.message").value("Not all FDC Ready items saved successfully."));
    }


}