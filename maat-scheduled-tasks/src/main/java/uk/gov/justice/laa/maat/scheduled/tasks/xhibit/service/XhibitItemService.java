package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity.XhibitEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.ProcedureResult;

import java.util.List;

@Slf4j
@Service
public class XhibitItemService {

    /**
     * Persist the entity into the GTT using repository.saveAllAndFlush,
     * then invoke the stored procedure — all in a NEW transaction.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public <T extends XhibitEntity> boolean process(T entity, JpaRepository<T, Integer> repository, XhibitProcedureService<T> procedureService) {
        repository.saveAllAndFlush(List.of(entity));

        try {
            return procedureService.call(entity) == ProcedureResult.SUCCESS;
        } catch (Exception ex) {
            log.warn("Procedure threw for '{}'", entity.getFilename(), ex);
            return false;
        }
    }
}
