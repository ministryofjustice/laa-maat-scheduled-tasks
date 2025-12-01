package uk.gov.justice.laa.maat.scheduled.tasks.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FDCReadyEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.FinalDefenceCostsEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;

import jakarta.persistence.EntityManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FDCDataLoadServiceTest {

  @Mock
  private ResourceLoader resourceLoader;
  @Mock
  private EntityManager entityManager;
  @Mock
  private Resource resource;

  @InjectMocks
  private FDCDataLoadService service;

  private void mockCsv(String csvPath, String csv) throws IOException {
    when(resourceLoader.getResource("classpath:" + csvPath)).thenReturn(resource);
    when(resource.getInputStream()).thenReturn(
        new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))
    );
  }

  @Nested
  class LoadFinalDefenceCosts {

    @Test
    @DisplayName("Parses rows, maps fields, sets itemType, returns count, and batches flush/clear")
    void parsesAndPersistsWithBatching() throws Exception {
      // batchSize = 2 to exercise batching with 3 data rows
      String csv = String.join("\n",
          "maat_reference,case_no,supp_account_code,court_code,judicial_apportionment,final_defence_cost,paid_as_claimed",
          "1001,CASE1,SUP1,CRT1,1,123.45,YES",
          "1002,CASE2,SUP2,CRT2,0,0.00,NO",
          "1003,CASE3,SUP3,CRT3,2,98765.43,YES"
      );
      mockCsv("CCR_data.csv", csv);

      int count = service.loadFinalDefenceCosts("CCR_data.csv", FDCType.AGFS, 2);

      assertThat(count).isEqualTo(3);

      // capture entities persisted
      ArgumentCaptor<FinalDefenceCostsEntity> captor = ArgumentCaptor.forClass(FinalDefenceCostsEntity.class);
      verify(entityManager, times(3)).persist(captor.capture());

      List<FinalDefenceCostsEntity> all = captor.getAllValues();
      FinalDefenceCostsEntity first = all.getFirst();
      assertThat(first.getMaatId()).isEqualTo(1001);
      assertThat(first.getCaseNo()).isEqualTo("CASE1");
      assertThat(first.getSuppAccountCode()).isEqualTo("SUP1");
      assertThat(first.getCourCourtCode()).isEqualTo("CRT1");
      assertThat(first.getJudicialApportionment()).isEqualTo(1);
      assertThat(first.getTotalCaseCosts()).isEqualByComparingTo(new BigDecimal("123.45"));
      assertThat(first.getItemType()).isEqualTo(FDCType.AGFS);
      assertThat(first.getPaidAsClaimed()).isEqualTo("YES");

      FinalDefenceCostsEntity third = all.get(2);
      assertThat(third.getMaatId()).isEqualTo(1003);
      assertThat(third.getTotalCaseCosts()).isEqualByComparingTo(new BigDecimal("98765.43"));

      // batching: expect flush/clear after row 2, and again at end for remaining row
      verify(entityManager, times(2)).flush();
      verify(entityManager, times(2)).clear();
    }

    @Test
    @DisplayName("Blank lines are skipped")
    void skipsBlankLines() throws Exception {
      String csv = String.join("\n",
          "maat_reference,case_no,supp_account_code,court_code,judicial_apportionment,final_defence_cost,paid_as_claimed",
          "", // blank line
          "2001,CASEX,SUPX,CRTX,1,10.00,YES",
          "   ", // whitespace-only line
          "2002,CASEY,SUPY,CRTY,0,20.00,NO"
      );
      mockCsv("CCLF_data.csv", csv);

      int count = service.loadFinalDefenceCosts("CCLF_data.csv", FDCType.LGFS, 10);

      assertThat(count).isEqualTo(2);
      verify(entityManager, times(2)).persist(any(FinalDefenceCostsEntity.class));
    }

    @Test
    @DisplayName("Header-only (no data rows) returns 0 and does not persist")
    void headerOnlyReturnsZero() throws Exception {
      String csv = "maat_reference,case_no,supp_account_code,court_code,judicial_apportionment,final_defence_cost,paid_as_claimed\n";
      mockCsv("empty.csv", csv);

      int count = service.loadFinalDefenceCosts("empty.csv", FDCType.AGFS, 1000);

      assertThat(count).isZero();
      verify(entityManager, never()).persist(any());
      verify(entityManager, never()).flush();
      verify(entityManager, never()).clear();
    }

    @Test
    @DisplayName("IOException while reading returns 0 and logs, without persisting")
    void ioExceptionReturnsZero() throws Exception {
      when(resourceLoader.getResource("classpath:bad.csv")).thenReturn(resource);
      when(resource.getInputStream()).thenThrow(new IOException("boom"));

      int count = service.loadFinalDefenceCosts("bad.csv", FDCType.AGFS, 1000);

      assertThat(count).isZero();
      verify(entityManager, never()).persist(any());
    }
  }

  @Nested
  class LoadFdcReady {

    @Test
    @DisplayName("Parses rows, maps fields, sets itemType, returns count, and batches")
    void parsesAndPersistsReady() throws Exception {
      String csv = String.join("\n",
          "maat_reference,FDC_READY",
          "3001,YES",
          "3002,NO",
          "3003,YES"
      );
      mockCsv("CCR_ready.csv", csv);

      int count = service.loadFdcReady("CCR_ready.csv", FDCType.AGFS, 2);

      assertThat(count).isEqualTo(3);

      ArgumentCaptor<FDCReadyEntity> captor = ArgumentCaptor.forClass(FDCReadyEntity.class);
      verify(entityManager, times(3)).persist(captor.capture());

      List<FDCReadyEntity> all = captor.getAllValues();
      assertThat(all.getFirst().getMaatId()).isEqualTo(3001);
      assertThat(all.getFirst().getFdcReady()).isEqualTo("YES");
      assertThat(all.getFirst().getItemType()).isEqualTo(FDCType.AGFS);

      assertThat(all.get(1).getMaatId()).isEqualTo(3002);
      assertThat(all.get(1).getFdcReady()).isEqualTo("NO");

      verify(entityManager, times(2)).flush();
      verify(entityManager, times(2)).clear();
    }

    @Test
    @DisplayName("Header-only returns 0 (no persist)")
    void readyHeaderOnly() throws Exception {
      String csv = "maat_reference,FDC_READY\n";
      mockCsv("ready_empty.csv", csv);

      int count = service.loadFdcReady("ready_empty.csv", FDCType.LGFS, 1000);

      assertThat(count).isZero();
      verify(entityManager, never()).persist(any());
    }

    @Test
    @DisplayName("IOException while reading returns 0")
    void readyIOException() throws Exception {
      when(resourceLoader.getResource("classpath:oops.csv")).thenReturn(resource);
      when(resource.getInputStream()).thenThrow(new IOException("nope"));

      int count = service.loadFdcReady("oops.csv", FDCType.LGFS, 1000);

      assertThat(count).isZero();
      verify(entityManager, never()).persist(any());
    }
  }
}
