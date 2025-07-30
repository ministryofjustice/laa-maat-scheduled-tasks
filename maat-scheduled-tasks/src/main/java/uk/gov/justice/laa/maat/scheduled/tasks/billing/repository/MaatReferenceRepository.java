package uk.gov.justice.laa.maat.scheduled.tasks.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.entity.MaatReferenceEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.entity.MaatReferenceId;

@Repository
public interface MaatReferenceRepository extends JpaRepository<MaatReferenceEntity, MaatReferenceId> {

    @Modifying
    @Query(value = "INSERT INTO TOGDATA.maat_refs_to_extract (MAAT_ID, APPL_ID, APHI_ID) SELECT id, appl_id, aphi_id FROM TOGDATA.rep_orders WHERE send_to_cclf = 'Y'", nativeQuery = true)
    void populateMaatReferences();
}
