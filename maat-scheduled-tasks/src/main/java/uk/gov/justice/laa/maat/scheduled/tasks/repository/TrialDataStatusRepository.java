package uk.gov.justice.laa.maat.scheduled.tasks.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TrialDataStatusRepository implements StatusRepository<Integer> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Integer> findUnprocessedIds() {
        String sql = "SELECT ID FROM HUB_TRIAL_DATA WHERE STATUS = 'UNPROCESSED'";
        return jdbcTemplate.queryForList(sql, Integer.class);
    }
}
