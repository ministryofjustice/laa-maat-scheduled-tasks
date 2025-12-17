package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.FDCType;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostReadyDTO;
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

    @InjectMocks
    private FinalDefenceCostServiceImpl service;

    @Nested
    class SaveFdcDataItems {

      @Test
      @DisplayName("Load valid FDC items, return count, and verify saveEntity() called for all records")
      void parsesAndPersistsWithBatching() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        List<FinalDefenceCostDTO> finalDefenceCosts = objectMapper.readValue(
            FdcTestDataProvider.getValidFdcData(), new TypeReference<>() {
            });

        when(fdcItemValidator.validate(finalDefenceCosts.getFirst())).thenReturn(true);
        when(fdcItemValidator.validate(finalDefenceCosts.get(1))).thenReturn(true);
        when(fdcItemValidator.validate(finalDefenceCosts.get(2))).thenReturn(true);
        List<FinalDefenceCostDTO> invalid = service.saveFDCItems(finalDefenceCosts);

        assertThat(invalid.size()).isEqualTo(0);

        verify(finalDefenceCostEntitySaver, times(3)).saveEntity(any());
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
        List<FinalDefenceCostDTO> invalid  = service.saveFDCItems(finalDefenceCosts);

        assertThat(invalid.size()).isEqualTo(finalDefenceCosts.size());

        verifyNoInteractions(finalDefenceCostEntitySaver);
      }

      @Test
      @DisplayName("Load valid part of FDC items, return invalid FDC objects, and verify saveEntity() called for valid record")
      void loadValidItemsAndLogInvalid_whenSomeValidData() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        List<FinalDefenceCostDTO> finalDefenceCosts = objectMapper.readValue(
            FdcTestDataProvider.getInvalidFdcDataWithMissingFields(), new TypeReference<>() {
            });

        when(fdcItemValidator.validate(finalDefenceCosts.getFirst())).thenReturn(false);
        when(fdcItemValidator.validate(finalDefenceCosts.get(1))).thenReturn(false);
        when(fdcItemValidator.validate(finalDefenceCosts.get(2))).thenReturn(true);
        List<FinalDefenceCostDTO> invalid = service.saveFDCItems(finalDefenceCosts);

        assertThat(invalid.size()).isEqualTo(2);

        verify(finalDefenceCostEntitySaver, times(1)).saveEntity(any());
      }
    }

    @Nested
    class SaveFdcReadyItems {

            @Test
            @DisplayName("Persists all valid items, returns no invalid record, verifies save() called")
            void persistsAllValidItemsWithBatching() {
                FinalDefenceCostReadyDTO req1 = FinalDefenceCostReadyDTO.builder()
                        .maatReference(1001)
                        .fdcReady(true)
                        .itemType(FDCType.LGFS)
                        .build();
                FinalDefenceCostReadyDTO req2 = FinalDefenceCostReadyDTO.builder()
                        .maatReference(1002)
                        .fdcReady(false)
                        .itemType(FDCType.AGFS)
                        .build();
                FinalDefenceCostReadyDTO req3 = FinalDefenceCostReadyDTO.builder()
                        .maatReference(1003)
                        .fdcReady(true)
                        .itemType(FDCType.LGFS)
                        .build();

                when(fdcItemValidator.validate(req1)).thenReturn(true);
                when(fdcItemValidator.validate(req2)).thenReturn(true);
                when(fdcItemValidator.validate(req3)).thenReturn(true);

                List<FinalDefenceCostReadyDTO> invalid = service.saveFdcReadyItems(List.of(req1, req2, req3));

                assertThat(invalid.size()).isEqualTo(0);

                verify(fdcReadyEntitySaver, times(3)).saveEntity(any());
            }

            @Test
            @DisplayName("Skips items with invalid itemType, returns invalid record, verifies saveEntity() called once")
            void skipsItemsWithInvalidItemType() {
                FinalDefenceCostReadyDTO valid = FinalDefenceCostReadyDTO.builder()
                        .maatReference(2001)
                        .fdcReady(true)
                        .itemType(FDCType.LGFS)
                        .build();
                FinalDefenceCostReadyDTO invalid = FinalDefenceCostReadyDTO.builder()
                        .maatReference(2002)
                        .fdcReady(false)
                        .itemType(null)
                        .build();
                FinalDefenceCostReadyDTO nullType = FinalDefenceCostReadyDTO.builder()
                        .maatReference(2003)
                        .fdcReady(null)
                        .itemType(FDCType.AGFS)
                        .build();
                List<FinalDefenceCostReadyDTO> requests = List.of(valid, invalid, nullType);

                when(fdcItemValidator.validate(valid)).thenReturn(true);
                when(fdcItemValidator.validate(invalid)).thenReturn(false);
                when(fdcItemValidator.validate(nullType)).thenReturn(false);

                List<FinalDefenceCostReadyDTO> response = service.saveFdcReadyItems(requests);

                assertThat(response.size()).isEqualTo(2);
                assertThat(response).containsExactlyInAnyOrder(invalid, nullType);

                verify(fdcReadyEntitySaver, times(1)).saveEntity(any());
            }

            @Test
            @DisplayName("Empty list returns zero without persisting, verify item validator not called")
            void emptyListReturnsZero() {
                List<FinalDefenceCostReadyDTO> invalid = service.saveFdcReadyItems(List.of());

                assertThat(invalid.size()).isZero();

                verifyNoInteractions(fdcItemValidator);
            }

            @Test
            @DisplayName("Single item persists, verify saveEntity() called once")
            void singleItemPersistsAtEnd() {
                FinalDefenceCostReadyDTO req = FinalDefenceCostReadyDTO.builder()
                        .maatReference(3001)
                        .fdcReady(true)
                        .itemType(FDCType.AGFS)
                        .build();

                when(fdcItemValidator.validate(req)).thenReturn(true);

                List<FinalDefenceCostReadyDTO> response = service.saveFdcReadyItems(List.of(req));

                assertThat(response.size()).isZero();

                verify(fdcReadyEntitySaver, times(1)).saveEntity(any());
            }

            @Test
            @DisplayName("All items skipped when all have invalid itemType returns zero")
            void allInvalidItemTypesReturnsZero() {
                FinalDefenceCostReadyDTO invalid1 = FinalDefenceCostReadyDTO.builder()
                        .maatReference(5001)
                        .fdcReady(true)
                        .itemType(null)
                        .build();
                FinalDefenceCostReadyDTO invalid2 = FinalDefenceCostReadyDTO.builder()
                        .maatReference(5002)
                        .fdcReady(false)
                        .itemType(null)
                        .build();
                List<FinalDefenceCostReadyDTO> invalids = List.of(invalid1, invalid2);
                when(fdcItemValidator.validate(invalid1)).thenReturn(false);
                when(fdcItemValidator.validate(invalid2)).thenReturn(false);
                List<FinalDefenceCostReadyDTO> response = service.saveFdcReadyItems(invalids);

                assertThat(response.size()).isEqualTo(invalids.size());

                verifyNoInteractions(fdcReadyEntitySaver);
            }
    }

}
