package uk.gov.justice.laa.maat.scheduled.tasks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantBillingEntity;

import java.util.List;

@Repository
public interface ApplicantBillingRepository extends JpaRepository<ApplicantBillingEntity, Integer> {

    @Query(value = """
            SELECT a1.id, a1.first_name, a1.last_name, a1.other_names, a1.dob, a1.gender,
            a1.ni_number, a1.foreign_id, a1.date_created, a1.user_created,
            a1.date_modified, a1.user_modified, a1.send_to_cclf
            FROM TOGDATA.applicants a1
            JOIN TOGDATA.maat_refs_to_extract m ON a1.id = m.appl_id
            UNION
            SELECT a2.id, a2.first_name, a2.last_name, a2.other_names, a2.dob, a2.gender,
            a2.ni_number, a2.foreign_id, a2.date_created, a2.user_created,
            a2.date_modified, a2.user_modified, a2.send_to_cclf
            FROM TOGDATA.applicants a2
            WHERE a2.send_to_cclf = 'Y'
            """,
            nativeQuery = true)
    List<ApplicantBillingEntity> findAllApplicantsForBilling();

    @Modifying
    @Query(value = """
        UPDATE TOGDATA.applicants
        SET     send_to_cclf = NULL,
                date_modified = SYSDATE,
                user_modified = :username
        WHERE id IN (:ids)
        """, nativeQuery = true)
    int resetApplicantBilling(@Param("ids") List<Integer> ids, 
        @Param("username") String username);

    @Modifying
    @Query(value = """
        UPDATE TOGDATA.applicants
        SET     send_to_cclf = :sendToCclf,
                date_modified = SYSDATE,
                user_modified = :username
        WHERE id IN (:ids)
        """, nativeQuery = true)
    int setCclfFlag(@Param("ids") List<Integer> ids, 
        @Param("username") String username, @Param("sendToCclf") String sendToCclf);
}
