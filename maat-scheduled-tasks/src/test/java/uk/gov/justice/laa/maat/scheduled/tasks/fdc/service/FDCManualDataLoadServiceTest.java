package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostReadyEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.enums.FDCType;

@ExtendWith(MockitoExtension.class)
class FDCManualDataLoadServiceTest {

  @Mock
  private EntityManager entityManager;
  @Mock
  private Resource resource;

  @InjectMocks
  private FDCManualDataLoadService service;

  @Nested
  class LoadFinalDefenceCosts {

    @Test
    @DisplayName("Parses rows, maps fields, sets itemType, returns count, and batches flush/clear")
    void parsesAndPersistsWithBatching() throws Exception {
      // batchSize = 2 to exercise batching with 3 data rows
      int count = service.loadFinalDefenceCosts("CCR_data.csv", FDCType.AGFS, 2);

      assertThat(count).isEqualTo(3);

      // capture entities persisted
      ArgumentCaptor<FinalDefenceCostEntity> captor = ArgumentCaptor.forClass(FinalDefenceCostEntity.class);
      verify(entityManager, times(3)).persist(captor.capture());

      List<FinalDefenceCostEntity> all = captor.getAllValues();
      FinalDefenceCostEntity first = all.getFirst();
      assertThat(first.getMaatReference()).isEqualTo(1001);
      assertThat(first.getCaseNo()).isEqualTo("CASE1");
      assertThat(first.getSuppAccountCode()).isEqualTo("SUP1");
      assertThat(first.getCourtCode()).isEqualTo("CRT1");
      assertThat(first.getJudicialApportionment()).isEqualTo(1);
      assertThat(first.getFinalDefenceCost()).isEqualByComparingTo(new BigDecimal("123.45"));
      assertThat(first.getItemType()).isEqualTo(FDCType.AGFS);
      assertThat(first.isPaidAsClaimed()).isEqualTo(true);

      FinalDefenceCostEntity third = all.get(2);
      assertThat(third.getMaatReference()).isEqualTo(1003);
      assertThat(third.getFinalDefenceCost()).isEqualByComparingTo(new BigDecimal("98765.43"));

      // batching: expect flush/clear after row 2, and again at end for remaining row
      verify(entityManager, times(2)).flush();
      verify(entityManager, times(2)).clear();
    }

    @Test
    @DisplayName("Blank lines are skipped")
    void skipsBlankLines() throws Exception {

      int count = service.loadFinalDefenceCosts("CCLF_data.csv", FDCType.LGFS, 10);

      assertThat(count).isEqualTo(2);
      verify(entityManager, times(2)).persist(any(FinalDefenceCostEntity.class));
    }

    @Test
    @DisplayName("Header-only (no data rows) returns 0 and does not persist")
    void headerOnlyReturnsZero() throws Exception {

      int count = service.loadFinalDefenceCosts("empty.csv", FDCType.AGFS, 1000);

      assertThat(count).isZero();
      verify(entityManager, never()).persist(any());
      verify(entityManager, never()).flush();
      verify(entityManager, never()).clear();
    }

    @Test
    @DisplayName("IOException while reading returns 0 and logs, without persisting")
    void ioExceptionReturnsZero() throws Exception {

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

      int count = service.loadFdcReady("CCR_ready.csv", FDCType.AGFS, 2);

      assertThat(count).isEqualTo(3);

      ArgumentCaptor<FinalDefenceCostReadyEntity> captor = ArgumentCaptor.forClass(FinalDefenceCostReadyEntity.class);
      verify(entityManager, times(3)).persist(captor.capture());

      List<FinalDefenceCostReadyEntity> all = captor.getAllValues();
      assertThat(all.getFirst().getMaatId()).isEqualTo(3001);
      assertThat(all.getFirst().isFdcReady()).isEqualTo(true);
      assertThat(all.getFirst().getItemType()).isEqualTo(FDCType.AGFS);

      assertThat(all.get(1).getMaatId()).isEqualTo(3002);
      assertThat(all.get(1).isFdcReady()).isEqualTo(false);

      verify(entityManager, times(2)).flush();
      verify(entityManager, times(2)).clear();
    }

    @Test
    @DisplayName("Header-only returns 0 (no persist)")
    void readyHeaderOnly() throws Exception {

      int count = service.loadFdcReady("ready_empty.csv", FDCType.LGFS, 1000);

      assertThat(count).isZero();
      verify(entityManager, never()).persist(any());
    }

    @Test
    @DisplayName("IOException while reading returns 0")
    void readyIOException() throws Exception {

      int count = service.loadFdcReady("oops.csv", FDCType.LGFS, 1000);

      assertThat(count).isZero();
      verify(entityManager, never()).persist(any());
    }
  }

  @Test
  void rejectsNonCsvFile() {
    Exception ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.loadCsv("users.txt")
    );

    assertTrue(ex.getMessage().contains("Invalid file type"));
  }

  @Test
  void rejectsPathTraversal() {
    Exception ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.loadCsv("../traversal.csv")
    );

    assertTrue(ex.getMessage().contains("Path traversal"));
  }

  @Test
  void rejectsHttpScheme() {
    Exception ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.loadCsv("http://evil.com/evil.csv")
    );

    assertTrue(ex.getMessage().contains("CSV"));
  }

  @Test
  void rejectsMissingFile() {
    Exception ex = assertThrows(
        IllegalArgumentException.class,
        () -> service.loadCsv("missing.csv")
    );

    assertTrue(ex.getMessage().contains("not found"));
  }
}
