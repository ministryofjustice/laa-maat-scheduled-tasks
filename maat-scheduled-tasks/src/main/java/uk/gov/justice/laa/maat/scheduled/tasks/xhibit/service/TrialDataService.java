package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.XhibitRecordSheet;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity.XhibitTrialDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.repository.XhibitTrialDataRepository;

@Slf4j
@Service
public class TrialDataService extends XhibitDataServiceBase<XhibitTrialDataEntity> {

    public TrialDataService(
            XhibitDataService xhibitDataService,
            XhibitTrialDataRepository repository,
            TrialDataProcedureService procedureService) {
        super(xhibitDataService, repository, procedureService);
    }

    @Override
    protected RecordSheetType getRecordSheetType() {
        return RecordSheetType.TRIAL;
    }

    @Override
    protected XhibitTrialDataEntity fromDto(XhibitRecordSheet dto) {
        return XhibitTrialDataEntity.fromDto(dto);
    }

    @Override
    protected Integer getEntityId(XhibitTrialDataEntity entity) {
        return entity.getId();
    }

    @Override
    protected String getFilename(XhibitTrialDataEntity entity) {
        return entity.getFilename();
    }
}
