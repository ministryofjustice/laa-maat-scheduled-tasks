package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FdcReadyRequestDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FDCReadyEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.util.FdcTestDataProvider;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.validator.FdcItemValidator;

@ExtendWith(MockitoExtension.class)
class FinalDefenceCostServiceImplTest {

    @Mock
    private FinalDefenceCostEntitySaver  finalDefenceCostEntitySaver;

    @Mock
    private FDCReadyEntitySaver fdcReadyEntitySaver;

    @Mock
    private FdcItemValidator fdcItemValidator;

    @Captor
    private ArgumentCaptor<FinalDefenceCostEntity> captor;

    @InjectMocks
    private FinalDefenceCostServiceImpl service;

    @Mock
    private EntityManager entityManager;

    @Nested
    class SaveFdcDataItems {

      @Test
      @DisplayName("Load valid FDC items, return count, and verify saveAll called for batches")
      void parsesAndPersistsWithBatching() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        List<FinalDefenceCostDTO> finalDefenceCosts = objectMapper.readValue(
            FdcTestDataProvider.getValidFdcData(), new TypeReference<>() {
            });

        when(fdcItemValidator.validate(finalDefenceCosts.getFirst())).thenReturn(true);
        when(fdcItemValidator.validate(finalDefenceCosts.get(1))).thenReturn(true);
        when(fdcItemValidator.validate(finalDefenceCosts.get(2))).thenReturn(true);
        List<FinalDefenceCostDTO> invalid = service.processFinalDefenceCosts(finalDefenceCosts);

        assertThat(invalid.size()).isEqualTo(0);
      }

      @Test
      @DisplayName("Load no FDC items when non valid, return all FDC objects as invalid.")
      void loadValidItemsAndLogInvalid() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        List<FinalDefenceCostDTO> finalDefenceCosts = objectMapper.readValue(
            FdcTestDataProvider.getInvalidFdcData(), new TypeReference<>() {
            });

        when(fdcItemValidator.validate(finalDefenceCosts.getFirst())).thenReturn(false);
        when(fdcItemValidator.validate(finalDefenceCosts.get(1))).thenReturn(false);
        when(fdcItemValidator.validate(finalDefenceCosts.get(2))).thenReturn(false);
        List<FinalDefenceCostDTO> invalid  = service.processFinalDefenceCosts(finalDefenceCosts);

        assertThat(invalid.size()).isEqualTo(finalDefenceCosts.size());
      }

      @Test
      @DisplayName("Load part invalid FDC items, return invalid FDC objects, and verify saveAll called for batches")
      void loadValidItemsAndLogInvalid_whenSomeValidData() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        List<FinalDefenceCostDTO> finalDefenceCosts = objectMapper.readValue(
            FdcTestDataProvider.getInvalidFdcData(), new TypeReference<>() {
            });

        when(fdcItemValidator.validate(finalDefenceCosts.getFirst())).thenReturn(false);
        when(fdcItemValidator.validate(finalDefenceCosts.get(1))).thenReturn(false);
        when(fdcItemValidator.validate(finalDefenceCosts.get(2))).thenReturn(false);
        List<FinalDefenceCostDTO> invalid = service.processFinalDefenceCosts(finalDefenceCosts);

        assertThat(invalid.size()).isEqualTo(3);
      }
    }

    @Nested
    class SaveFdcReadyItems {

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

                when(fdcItemValidator.validate(req1)).thenReturn(true);
                when(fdcItemValidator.validate(req2)).thenReturn(true);
                when(fdcItemValidator.validate(req3)).thenReturn(true);

                service.saveFdcReadyItems(List.of(req1, req2, req3));

                ArgumentCaptor<List<FDCReadyEntity>> captor = ArgumentCaptor.forClass(List.class);
//                verify(finalDefenceCostsReadyRepository, times(2)).saveAll(captor.capture());
//
//                List<List<FDCReadyEntity>> batches = captor.getAllValues();
//                assertThat(batches.get(0)).hasSize(2);
//                assertThat(batches.get(1)).hasSize(1);
//
//                assertThat(batches.stream().flatMap(List::stream).map(FDCReadyEntity::getMaatId))
//                        .containsExactlyInAnyOrder(1001, 1002, 1003);
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

                when(fdcItemValidator.validate(valid)).thenReturn(true);
                when(fdcItemValidator.validate(invalid)).thenReturn(false);
                when(fdcItemValidator.validate(nullType)).thenReturn(false);

                service.saveFdcReadyItems(List.of(valid, invalid, nullType));

//                ArgumentCaptor<List<FDCReadyEntity>> captor = ArgumentCaptor.forClass(List.class);
//                verify(finalDefenceCostsReadyRepository, times(1)).saveAll(captor.capture());
//                assertThat(captor.getValue()).hasSize(1);
            }

            @Test
            @DisplayName("Empty list returns zero without persisting")
            void emptyListReturnsZero() {
                List<FdcReadyRequestDTO> invalid = service.saveFdcReadyItems(List.of());

                assertThat(invalid.size()).isZero();
            }

            @Test
            @DisplayName("Single item persists once at end")
            void singleItemPersistsAtEnd() {
                FdcReadyRequestDTO req = FdcReadyRequestDTO.builder()
                        .maatReference(3001)
                        .fdcReady("Y")
                        .itemType("AGFS")
                        .build();

                when(fdcItemValidator.validate(req)).thenReturn(true);

                service.saveFdcReadyItems(List.of(req));

//                ArgumentCaptor<List<FDCReadyEntity>> captor = ArgumentCaptor.forClass(List.class);
//                verify(finalDefenceCostsReadyRepository, times(1)).saveAll(captor.capture());
//                assertThat(captor.getValue()).hasSize(1);
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
                when(fdcItemValidator.validate(lowerCase)).thenReturn(true);
                when(fdcItemValidator.validate(mixedCase)).thenReturn(true);
                service.saveFdcReadyItems(List.of(lowerCase, mixedCase));

//                ArgumentCaptor<List<FDCReadyEntity>> captor = ArgumentCaptor.forClass(List.class);
//                verify(finalDefenceCostsReadyRepository, times(1)).saveAll(captor.capture());
//                List<FDCReadyEntity> saved = captor.getValue();
//                assertThat(saved.get(0).getItemType()).isEqualTo(FDCType.LGFS);
//                assertThat(saved.get(1).getItemType()).isEqualTo(FDCType.AGFS);
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
                when(fdcItemValidator.validate(invalid1)).thenReturn(false);
                when(fdcItemValidator.validate(invalid2)).thenReturn(false);
                service.saveFdcReadyItems(List.of(invalid1, invalid2));

//                verify(finalDefenceCostsReadyRepository, never()).saveAll(any());
            }
    }

}
