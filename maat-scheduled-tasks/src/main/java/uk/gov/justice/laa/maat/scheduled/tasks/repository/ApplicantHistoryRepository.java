package uk.gov.justice.laa.maat.scheduled.tasks.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ApplicantHistoryRepository extends JpaRepository<ApplicantHistoryEntity, Integer> {

    @Modifying
    @Transactional
    @Query(value = """
                        UPDATE
                            TOGDATA.APPLICANT_HISTORY 
                        SET 
                            SEND_TO_CCLF = null, 
                            DATE_MODIFIED = CURRENT_DATE, 
                            USER_MODIFIED = :userModified
                        WHERE 
                            ID IN :ids
        """, nativeQuery = true)
    int resetApplicantHistory(@Param("userModified") String userModified,
        @Param("ids") List<Integer> ids);
}
