package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.FdcReadyRequestDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.FDCReadyEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.FdcItemValidator;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.config.FinalDefenceCostConfiguration;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository.FinalDefenceCostsReadyRepository;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository.FinalDefenceCostsRepository;

import java.util.List;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.FdcTestDataProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinalDefenceCostServiceImplTest {

    @Mock
    private FinalDefenceCostsRepository finalDefenceCostsRepository;
    @Mock
    private FinalDefenceCostsReadyRepository finalDefenceCostsReadyRepository;

    @Mock
    private FdcItemValidator fdcItemValidator;

    @Captor
    private ArgumentCaptor<List<FinalDefenceCostEntity>> captor;

    @InjectMocks
    private FinalDefenceCostServiceImpl service;

    @Mock
    private FinalDefenceCostConfiguration fdcConfiguration;

    @Mock
    private EntityManager entityManager;

    @Test
    @DisplayName("Load valid FDC items, return count, and verify saveAll called for batches")
    void parsesAndPersistsWithBatching() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        List<FinalDefenceCostDto> finalDefenceCosts = objectMapper.readValue(FdcTestDataProvider.getValidFdcData(), new TypeReference<>() {});

        when(fdcConfiguration.getFetchSize()).thenReturn(1);
        when(fdcItemValidator.validate(finalDefenceCosts.getFirst())).thenReturn(true);
        when(fdcItemValidator.validate(finalDefenceCosts.get(1))).thenReturn(true);
        when(fdcItemValidator.validate(finalDefenceCosts.get(2))).thenReturn(true);
        int count = service.processFinalDefenceCosts(finalDefenceCosts);

        assertThat(count).isEqualTo(3);

        verify(finalDefenceCostsRepository, times(3)).saveAll(captor.capture());

        assertThat(3).isEqualTo(captor.getAllValues().size());
    }

    @Test
    @DisplayName("Load no FDC items when non valid, return zero count loaded, and verify saveAll called for batches")
    void loadValidItemsAndLogInvalid() throws Exception {

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
      List<FinalDefenceCostDto> finalDefenceCosts = objectMapper.readValue(FdcTestDataProvider.getInvalidFdcData(), new TypeReference<>() {});

      when(fdcConfiguration.getFetchSize()).thenReturn(1);
      when(fdcItemValidator.validate(finalDefenceCosts.getFirst())).thenReturn(false);
      when(fdcItemValidator.validate(finalDefenceCosts.get(1))).thenReturn(false);
      when(fdcItemValidator.validate(finalDefenceCosts.get(2))).thenReturn(false);
      int count = service.processFinalDefenceCosts(finalDefenceCosts);

      assertThat(count).isEqualTo(0);

      verify(finalDefenceCostsRepository, times(0)).saveAll(captor.capture());

      assertThat(0).isEqualTo(captor.getAllValues().size());
    }

  @Test
  @DisplayName("Load part invalid FDC items, return count loaded, and verify saveAll called for batches")
  void loadValidItemsAndLogInvalid_whenSomeValidData() throws Exception {

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    List<FinalDefenceCostDto> finalDefenceCosts = objectMapper.readValue(FdcTestDataProvider.getInvalidFdcData(), new TypeReference<>() {});

    when(fdcConfiguration.getFetchSize()).thenReturn(1);
    when(fdcItemValidator.validate(finalDefenceCosts.getFirst())).thenReturn(false);
    when(fdcItemValidator.validate(finalDefenceCosts.get(1))).thenReturn(false);
    when(fdcItemValidator.validate(finalDefenceCosts.get(2))).thenReturn(true);
    int count = service.processFinalDefenceCosts(finalDefenceCosts);

    assertThat(count).isEqualTo(1);

    verify(finalDefenceCostsRepository, times(1)).saveAll(captor.capture());

    assertThat(1).isEqualTo(captor.getAllValues().size());
  }

    @Nested
    class SaveFdcReadyItems {

            @BeforeEach
            void setUp() {
                when(fdcConfiguration.getFetchSize()).thenReturn(2);
            }

            @Test
            @DisplayName("Persists all valid items, returns count, and batches saveAll")
            void persistsAllValidItemsWithBatching() {
                FdcReadyRequestDTO req1 = FdcReadyRequestDTO.builder()
                        .maatReference(1001)
                        .fdcReady("Y")
                        .itemType("LGFS")
                        .build();
                FdcReadyRequestDTO req2 = FdcReadyRequestDTO.builder()
                        .maatReference(1002)
                        .fdcReady("N")
                        .itemType("AGFS")
                        .build();
                FdcReadyRequestDTO req3 = FdcReadyRequestDTO.builder()
                        .maatReference(1003)
                        .fdcReady("Y")
                        .itemType("LGFS")
                        .build();

                int count = service.processFdcReadyItems(List.of(req1, req2, req3));

                assertThat(count).isEqualTo(3);

                ArgumentCaptor<List<FDCReadyEntity>> captor = ArgumentCaptor.forClass(List.class);
                verify(finalDefenceCostsReadyRepository, times(2)).saveAll(captor.capture());

                List<List<FDCReadyEntity>> batches = captor.getAllValues();
                assertThat(batches.get(0)).hasSize(2);
                assertThat(batches.get(1)).hasSize(1);

                assertThat(batches.stream().flatMap(List::stream).map(FDCReadyEntity::getMaatId))
                        .containsExactlyInAnyOrder(1001, 1002, 1003);
            }
            @Test
            @DisplayName("Skips items with invalid itemType and logs warning")
            void skipsItemsWithInvalidItemType() {
                FdcReadyRequestDTO valid = FdcReadyRequestDTO.builder()
                        .maatReference(2001)
                        .fdcReady("Y")
                        .itemType("LGFS")
                        .build();
                FdcReadyRequestDTO invalid = FdcReadyRequestDTO.builder()
                        .maatReference(2002)
                        .fdcReady("N")
                        .itemType("INVALID")
                        .build();
                FdcReadyRequestDTO nullType = FdcReadyRequestDTO.builder()
                        .maatReference(2003)
                        .fdcReady("Y")
                        .itemType(null)
                        .build();

                int count = service.processFdcReadyItems(List.of(valid, invalid, nullType));

                assertThat(count).isEqualTo(1);
                ArgumentCaptor<List<FDCReadyEntity>> captor = ArgumentCaptor.forClass(List.class);
                verify(finalDefenceCostsReadyRepository, times(1)).saveAll(captor.capture());
                assertThat(captor.getValue()).hasSize(1);
            }

            @Test
            @DisplayName("Empty list returns zero without persisting")
            void emptyListReturnsZero() {
                int count = service.processFdcReadyItems(List.of());

                assertThat(count).isZero();
                verify(finalDefenceCostsReadyRepository, never()).saveAll(any());
            }

            @Test
            @DisplayName("Single item persists once at end")
            void singleItemPersistsAtEnd() {
                FdcReadyRequestDTO req = FdcReadyRequestDTO.builder()
                        .maatReference(3001)
                        .fdcReady("Y")
                        .itemType("AGFS")
                        .build();

                int count = service.processFdcReadyItems(List.of(req));

                assertThat(count).isEqualTo(1);
                ArgumentCaptor<List<FDCReadyEntity>> captor = ArgumentCaptor.forClass(List.class);
                verify(finalDefenceCostsReadyRepository, times(1)).saveAll(captor.capture());
                assertThat(captor.getValue()).hasSize(1);
            }

            @Test
            @DisplayName("Handles mixed case itemType by converting to uppercase")
            void handlesMixedCaseItemType() {
                FdcReadyRequestDTO lowerCase = FdcReadyRequestDTO.builder()
                        .maatReference(4001)
                        .fdcReady("Y")
                        .itemType("lgfs")
                        .build();
                FdcReadyRequestDTO mixedCase = FdcReadyRequestDTO.builder()
                        .maatReference(4002)
                        .fdcReady("N")
                        .itemType("AgFs")
                        .build();

                int count = service.processFdcReadyItems(List.of(lowerCase, mixedCase));

                assertThat(count).isEqualTo(2);

                ArgumentCaptor<List<FDCReadyEntity>> captor = ArgumentCaptor.forClass(List.class);
                verify(finalDefenceCostsReadyRepository, times(1)).saveAll(captor.capture());
                List<FDCReadyEntity> saved = captor.getValue();
                assertThat(saved.get(0).getItemType()).isEqualTo(FDCType.LGFS);
                assertThat(saved.get(1).getItemType()).isEqualTo(FDCType.AGFS);
            }

            @Test
            @DisplayName("All items skipped when all have invalid itemType returns zero")
            void allInvalidItemTypesReturnsZero() {
                FdcReadyRequestDTO invalid1 = FdcReadyRequestDTO.builder()
                        .maatReference(5001)
                        .fdcReady("Y")
                        .itemType("WRONG")
                        .build();
                FdcReadyRequestDTO invalid2 = FdcReadyRequestDTO.builder()
                        .maatReference(5002)
                        .fdcReady("N")
                        .itemType("")
                        .build();

                int count = service.processFdcReadyItems(List.of(invalid1, invalid2));

                assertThat(count).isZero();
                verify(finalDefenceCostsReadyRepository, never()).saveAll(any());
            }

    }

}
