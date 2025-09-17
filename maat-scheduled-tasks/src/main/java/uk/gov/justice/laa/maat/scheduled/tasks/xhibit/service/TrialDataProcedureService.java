package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.inputParameter;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.StoredProcedure;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter;
import uk.gov.justice.laa.maat.scheduled.tasks.service.StoredProcedureService;

@Slf4j
@Service
public class TrialDataProcedureService extends XhibitProcedureService<XhibitTrialDataEntity> {

    public TrialDataProcedureService(StoredProcedureService storedProcedureService) {
        super(storedProcedureService);
    }

    @Override
    protected Object getEntityId(XhibitTrialDataEntity e) {
        return e.getId();
    }

    @Override
    public StoredProcedure getStoredProcedure() {
        return StoredProcedure.TRIAL_DATA_TO_MAAT_PROCEDURE;
    }

    @Override
    protected List<StoredProcedureParameter<?>> getProcedureParameters(XhibitTrialDataEntity e) {
        List<StoredProcedureParameter<?>> params = new ArrayList<>(OUTPUT_PARAMETERS);
        params.add(inputParameter("id", e.getId()));
        return params;
    }
}
