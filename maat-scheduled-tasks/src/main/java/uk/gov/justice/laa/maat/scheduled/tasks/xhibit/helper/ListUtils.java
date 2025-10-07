package uk.gov.justice.laa.maat.scheduled.tasks.util;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ListUtils {
    public static <T> List<List<T>> batchList(List<T> list, int batchSize) {
        List<List<T>> batchedList = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batchedList.add(list.subList(i, Math.min(list.size(), i + batchSize)));
        }
        return batchedList;
    }
}
