package uk.gov.justice.laa.maat.scheduled.tasks.util;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ListUtilsTest {

    @Test
    void givenListLongerThanBatchSize_whenBatchListIsInvoked_thenListIsSplitIntoBatches() {
        List<Integer> numList = List.of(1, 2, 3, 4, 5);
        int batchSize = 2;

        List<List<Integer>> numBatches = ListUtils.batchList(numList, batchSize);

        assertThat(numBatches.size()).isEqualTo(3);
        assertThat(numBatches.get(0)).isEqualTo(List.of(1, 2));
        assertThat(numBatches.get(1)).isEqualTo(List.of(3, 4));
        assertThat(numBatches.get(2)).isEqualTo(List.of(5));
    }
}
