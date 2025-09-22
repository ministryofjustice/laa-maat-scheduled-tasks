package uk.gov.justice.laa.maat.scheduled.tasks.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.RepOrderBillingEntity;

import java.util.List;

@Repository
public interface RepOrderBillingRepository extends
    JpaRepository<RepOrderBillingEntity, Integer> {

    @Query(value = """
        SELECT  r.id
              , r.appl_id
              , r.arrest_summons_no
              , r.efel_fee_level
              , r.supp_account_code
              , r.maco_court
              , r.mcoo_outcome
              , r.date_received
              , r.cc_reporder_date
              , r.ofty_offence_type
              , r.cc_withdrawal_date
              , r.aphi_id
              , r.case_id
              , r.committal_date
              , r.rors_status
              , r.apty_code
              , r.ccoo_outcome
              , r.date_created
              , r.user_created
              , r.date_modified
              , r.user_modified
              , r.caty_case_type
        FROM    TOGDATA.REP_ORDERS r
        JOIN    TOGDATA.MAAT_REFS_TO_EXTRACT ex
        ON      r.ID = ex.MAAT_ID
    """, nativeQuery = true)
    List<RepOrderBillingEntity> getRepOrdersForBilling();


    @Modifying
    @Query(value = """
        UPDATE  TOGDATA.REP_ORDERS r
        SET     r.SEND_TO_CCLF = null,
                r.DATE_MODIFIED = SYSDATE,
                r.USER_MODIFIED = :userModified
        WHERE   r.ID IN (
            SELECT  MAAT_ID 
            FROM    TOGDATA.MAAT_REFS_TO_EXTRACT
        )
    """, nativeQuery = true)
    int resetBillingFlagForRepOrderIds(@Param("userModified") String userModified);


}
