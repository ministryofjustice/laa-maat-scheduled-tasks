package uk.gov.justice.laa.maat.scheduled.tasks.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.FDCReadyEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.FinalDefenceCostsEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FDCDataLoadService {
  private final ResourceLoader resourceLoader;
  private final EntityManager entityManager;

  // this method will be replaced to invoke the Billing API to load the data into MAAT DB
  @Transactional
  public int loadFinalDefenceCosts(String csvPath, FDCType itemType, int batchSize) {
    log.info("Loading FDC Final Defence Costs");
    int count = 0;
    Resource resource = resourceLoader.getResource("classpath:" + csvPath);
    try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
      // read header
      String headerLine = br.readLine();
      if (headerLine == null) return 0;

      Map<String, Integer> idx = headerIndex(headerLine.split(",", -1));
      int inBatch = 0;
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isBlank()) continue;
        String[] cols = line.split(",", -1);
        FinalDefenceCostsEntity e = new FinalDefenceCostsEntity();
        e.setMaatId(getInt(cols, idx, "maat_reference"));
        e.setCaseNo(getStr(cols, idx, "case_no"));
        e.setSuppAccountCode(getStr(cols, idx, "supp_account_code"));
        e.setCourCourtCode(getStr(cols, idx, "court_code"));
        e.setJudicialApportionment(getInt(cols, idx, "judicial_apportionment"));
        e.setTotalCaseCosts(getDecimal(cols, idx));
        e.setItemType(itemType);
        e.setPaidAsClaimed(getStr(cols, idx, "paid_as_claimed"));

        entityManager.persist(e);
        inBatch++;
        count++;

        if (inBatch >= batchSize) {
          entityManager.flush();
          entityManager.clear();
          inBatch = 0;
        }
      }

      if (inBatch > 0) {
        entityManager.flush();
        entityManager.clear();
      }
    } catch (IOException e) {
      log.error("Failed to load FDC Final Defence Costs", e);
    }
    return count;
  }

  @Transactional
  public int loadFdcReady(String csvPath, FDCType itemType, int batchSize) {
    log.info("Loading FDC Ready");
    int count = 0;
    Resource resource = resourceLoader.getResource("classpath:" + csvPath);

    try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
      String headerLine = br.readLine();
      if (headerLine == null) return 0;
      Map<String, Integer> idx = headerIndex(headerLine.split(",", -1));
      int inBatch = 0;
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isBlank()) continue;
        String[] cols = line.split(",", -1);
        FDCReadyEntity e = new FDCReadyEntity();
        e.setMaatId(getInt(cols, idx, "maat_reference"));
        e.setFdcReady(getStr(cols, idx, "fdc_ready"));
        e.setItemType(itemType);
        entityManager.persist(e);
        inBatch++;
        count++;

        if (inBatch >= batchSize) {
          entityManager.flush();
          entityManager.clear();
          inBatch = 0;
        }
      }
      if (inBatch > 0) {
        entityManager.flush();
        entityManager.clear();
      }
    } catch (IOException e) {
      log.error("Failed to load FDC Ready", e);
    }
    return count;
  }


  private static Map<String, Integer> headerIndex(String[] headers) {
    Map<String, Integer> idx = new HashMap<>();
    for (int i = 0; i < headers.length; i++) {
      idx.put(headers[i].trim().toUpperCase(), i);
    }
    return idx;
  }

  private static Integer getInt(String[] cols, Map<String, Integer> idx, String key) {
    String v = getStr(cols, idx, key);
    return (v == null) ? null : Integer.valueOf(v);
  }

  private static String getStr(String[] cols, Map<String, Integer> idx, String key) {
    Integer i = idx.get(key.toUpperCase());
    if (i == null || i >= cols.length) return null;
    String v = cols[i].trim();
    return v.isEmpty() ? null : v;
  }


  private static BigDecimal getDecimal(String[] cols, Map<String, Integer> idx) {
    String v = getStr(cols, idx, "final_defence_cost");
    return (v == null) ? null : new BigDecimal(v);
  }
}
