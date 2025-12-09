package uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.FdcTestDataProvider;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.FinalDefenceCostsHelper;

@DataJpaTest
public class FinalDefenceCostsRepositoryTest {

  @Autowired
  private FinalDefenceCostsRepository repository;

  @Test
  void loadAndSaveFdcData() throws JsonProcessingException {

    ObjectMapper objectMapper  = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    List<FinalDefenceCostDTO> dtos = objectMapper.readValue(FdcTestDataProvider.getValidFdcData(), new TypeReference<>() {});

    List<FinalDefenceCostEntity> entities = dtos.stream()
        .map(FinalDefenceCostsHelper::toFinalDefenceCostEntity)
        .toList();

    repository.saveAll(entities);

    assertThat(repository.findAll().size()).isEqualTo(entities.size());

    FinalDefenceCostEntity anEntity = repository.findAll().getFirst();
    assertThat(anEntity.getHdatId()).isNotNull();
  }
}
