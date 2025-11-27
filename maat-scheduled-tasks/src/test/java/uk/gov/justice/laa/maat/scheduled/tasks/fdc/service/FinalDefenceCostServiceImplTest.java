package uk.gov.justice.laa.maat.scheduled.tasks.fdc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.dto.FinalDefenceCostDto;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.entity.FinalDefenceCostEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.fdc.repository.FinalDefenceCostsRepository;

@ExtendWith(MockitoExtension.class)
class FinalDefenceCostServiceImplTest {

    @Mock
    private FinalDefenceCostsRepository finalDefenceCostsRepository;

    @Captor
    private ArgumentCaptor<List<FinalDefenceCostEntity>> captor;

    @InjectMocks
    private FinalDefenceCostServiceImpl service;

    @Test
    void parsesAndPersistsWithBatching() throws Exception {

        String fdcDataJson = """
            [
              {
                "maat_reference": 123456,
                "case_no": "CASE1",
                "supp_account_code": "SUPPLIER1",
                "court_code": "COURT1",
                "judicial_apportionment": 11,
                "final_defence_cost": 456.64,
                "item_type": "LGFS",
                "paid_as_claimed": "Y"
              },
              {
                "maat_reference": 234567,
                "case_no": "CASE2",
                "supp_account_code": "SUPPLIER2",
                "court_code": "COURT2",
                "judicial_apportionment": 12,
                "final_defence_cost": 564.32,
                "item_type": "LGFS",
                "paid_as_claimed": "Y"
              },
              {
                "maat_reference": 6785643,
                "case_no": "CASE3",
                "supp_account_code": "SUPPLIER3",
                "court_code": "COURT3",
                "judicial_apportionment": 13,
                "final_defence_cost": 7365.98,
                "item_type": "LGFS",
                "paid_as_claimed": "N"
              },
              {
                "maat_reference": 432562,
                "case_no": "CASE4",
                "supp_account_code": "SUPPLIER4",
                "court_code": "COURT4",
                "judicial_apportionment": 14,
                "final_defence_cost": 5437.41,
                "item_type": "LGFS",
                "paid_as_claimed": "Y"
              },
              {
                "maat_reference": 253421,
                "case_no": "CASE5",
                "supp_account_code": "SUPPLIER5",
                "court_code": "COURT5",
                "judicial_apportionment": 15,
                "final_defence_cost": 8769.09,
                "item_type": "LGFS",
                "paid_as_claimed": "N"
              },
              {
                "maat_reference": 546373,
                "case_no": "CASE6",
                "supp_account_code": "SUPPLIER6",
                "court_code": "COURT6",
                "judicial_apportionment": 16,
                "final_defence_cost": 4789.67,
                "item_type": "LGFS",
                "paid_as_claimed": "Y"
              },
              {
                "maat_reference": 265435,
                "case_no": "CASE7",
                "supp_account_code": "SUPPLIER7",
                "court_code": "COURT7",
                "judicial_apportionment": 17,
                "final_defence_cost": 5684.98,
                "item_type": "LGFS",
                "paid_as_claimed": "Y"
              }
            ]
            """;

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        List<FinalDefenceCostDto> finalDefenceCosts = objectMapper.readValue(fdcDataJson, new TypeReference<>() {});

        int count = service.processFinalDefenceCosts(finalDefenceCosts, 3);

        assertThat(count).isEqualTo(7);

        verify(finalDefenceCostsRepository, times(3)).saveAll(captor.capture());

        assertThat(3).isEqualTo(captor.getAllValues().size());
    }
}
