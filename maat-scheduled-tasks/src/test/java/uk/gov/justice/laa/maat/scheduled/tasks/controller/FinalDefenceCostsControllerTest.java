package uk.gov.justice.laa.maat.scheduled.tasks.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;
import uk.gov.justice.laa.maat.scheduled.tasks.service.FDCDataLoadService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FinalDefenceCostsController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinalDefenceCostsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private FDCDataLoadService fdcDataLoadService;

  private static final String BASE = "/api/internal/v1/fdc";

  @Test
  @DisplayName("load-fdc: returns 400 for invalid filename")
  void loadFdc_invalidFilename_returnsBadRequest() throws Exception {
    mockMvc.perform(post(BASE + "/load-fdc-1")
            .param("fileName", "foo.csv"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.recordsInserted").value(0))
        .andExpect(jsonPath("$.message", containsString("Invalid filename foo.csv")));

    // Service must NOT be called
    verifyNoInteractions(fdcDataLoadService);
  }

  @Test
  @DisplayName("load-fdc: CCR* maps to AGFS and returns 200 with body when recordsInserted > 0")
  void loadFdc_ccr_success() throws Exception {
    when(fdcDataLoadService.loadFinalDefenceCosts(eq("CCR_202501.csv"), eq(FDCType.AGFS), eq(1000)))
        .thenReturn(5);

    mockMvc.perform(post(BASE + "/load-fdc-1")
            .param("fileName", "CCR_202501.csv"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.recordsInserted").value(5))
        .andExpect(jsonPath("$.message", containsString("CCR_202501.csv")));

    verify(fdcDataLoadService, times(1))
        .loadFinalDefenceCosts("CCR_202501.csv", FDCType.AGFS, 1000);
  }

  @Test
  @DisplayName("load-fdc: CCLF* maps to LGFS and returns 500 when recordsInserted == 0")
  void loadFdc_cclf_failure500_whenZeroInserted() throws Exception {
    when(fdcDataLoadService.loadFinalDefenceCosts(eq("CCLF_dump.csv"), eq(FDCType.LGFS), eq(1000)))
        .thenReturn(0);

    mockMvc.perform(post(BASE + "/load-fdc-1")
            .param("fileName", "CCLF_dump.csv"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.recordsInserted").value(0))
        .andExpect(jsonPath("$.message", containsString("Failed to load dataset from file: CCLF_dump.csv")));

    verify(fdcDataLoadService).loadFinalDefenceCosts("CCLF_dump.csv", FDCType.LGFS, 1000);
  }

  @Test
  @DisplayName("load-fdc: mapping is case-insensitive (ccr* -> AGFS)")
  void loadFdc_caseInsensitivePrefix() throws Exception {
    when(fdcDataLoadService.loadFinalDefenceCosts(eq("ccr_small.csv"), eq(FDCType.AGFS), eq(1000)))
        .thenReturn(1);

    mockMvc.perform(post(BASE + "/load-fdc-1")
            .param("fileName", "ccr_small.csv"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.recordsInserted").value(1));

    verify(fdcDataLoadService).loadFinalDefenceCosts("ccr_small.csv", FDCType.AGFS, 1000);
  }

  @Test
  @DisplayName("load-fdc-ready: returns 400 for invalid filename")
  void loadFdcReady_invalidFilename_returnsBadRequest() throws Exception {
    mockMvc.perform(post(BASE + "/load-fdc-ready-1")
            .param("fileName", "random.txt"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.recordsInserted").value(0))
        .andExpect(jsonPath("$.message", containsString("Invalid filename random.txt")));

    verifyNoInteractions(fdcDataLoadService);
  }

  @Test
  @DisplayName("load-fdc-ready: CCR* -> AGFS, 200 OK when > 0")
  void loadFdcReady_success_agfs() throws Exception {
    when(fdcDataLoadService.loadFdcReady(eq("CCR_ready.csv"), eq(FDCType.AGFS), eq(1000)))
        .thenReturn(7);

    mockMvc.perform(post(BASE + "/load-fdc-ready-1")
            .param("fileName", "CCR_ready.csv"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.recordsInserted").value(7))
        .andExpect(jsonPath("$.message", containsString("CCR_ready.csv")));

    verify(fdcDataLoadService).loadFdcReady("CCR_ready.csv", FDCType.AGFS, 1000);
  }

  @Test
  @DisplayName("load-fdc-ready: CCLF* -> LGFS, 500 when 0")
  void loadFdcReady_failure500_whenZero() throws Exception {
    when(fdcDataLoadService.loadFdcReady(eq("CCLF_ready.csv"), eq(FDCType.LGFS), eq(1000)))
        .thenReturn(0);

    mockMvc.perform(post(BASE + "/load-fdc-ready-1")
            .param("fileName", "CCLF_ready.csv"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.recordsInserted").value(0))
        .andExpect(jsonPath("$.message", containsString("Failed to load dataset from file: CCLF_ready.csv")));

    verify(fdcDataLoadService).loadFdcReady("CCLF_ready.csv", FDCType.LGFS, 1000);
  }
}
