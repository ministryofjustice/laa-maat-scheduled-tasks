package uk.gov.justice.laa.maat.scheduled.tasks.repository;

import java.util.List;

public interface TrialDataStatusRepository {

    List<Integer> findUnprocessedIds();
}
