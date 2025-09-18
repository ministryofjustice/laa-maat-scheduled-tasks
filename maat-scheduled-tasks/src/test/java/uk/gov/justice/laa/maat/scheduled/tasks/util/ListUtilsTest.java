package uk.gov.justice.laa.maat.scheduled.tasks.util;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ListUtilsTest {

    @Test
    void givenListLongerThanBatchSize_whenBatchListIsInvoked_thenListIsSplitIntoBatches() {
        List<Integer> numList = List.of(1, 2, 3, 4, 5);
        int batchSize = 2;

        List<List<Integer>> numBatches = ListUtils.batchList(numList, batchSize);

        assertEquals(3, numBatches.size());
        assertEquals(List.of(1, 2), numBatches.get(0));
        assertEquals(List.of(3, 4), numBatches.get(1));
        assertEquals(List.of(5), numBatches.get(2));
    }
}
