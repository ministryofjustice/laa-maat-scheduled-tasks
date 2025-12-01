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
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.FdcTestDataProvider;

@DataJpaTest
public class FinalDefenceCostsRepositoryTest {

  @Autowired
  private FinalDefenceCostsRepository repository;

  @Test
  void loadAndSaveFdcData() throws JsonProcessingException {

    ObjectMapper objectMapper  = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    List<FinalDefenceCostDto> dtos = objectMapper.readValue(FdcTestDataProvider.getValidFdcData(), new TypeReference<>() {});

    List<FinalDefenceCostEntity> entities = dtos.stream()
        .map(dto -> {
          FinalDefenceCostEntity entity = FinalDefenceCostEntity.builder()
              .maatReference(dto.getMaatReference())
              .caseNo(dto.getCaseNo())
              .suppAccountCode(dto.getSuppAccountCode())
              .courtCode(dto.getCourtCode())
              .judicialApportionment(dto.getJudicialApportionment())
              .finalDefenceCost(dto.getFinalDefenceCost())
              .itemType(dto.getItemType())
              .paidAsClaimed(dto.getPaidAsClaimed())
              .build();
          return entity;
        }).toList();

    repository.saveAll(entities);

    assertThat(repository.findAll().size()).isEqualTo(entities.size());

    FinalDefenceCostEntity anEntity = repository.findAll().getFirst();
    assertThat(anEntity.getHdatId()).isNotNull();
  }
}
