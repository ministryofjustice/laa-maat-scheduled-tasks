package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto.XhibitRecordSheet;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity.XhibitAppealDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.RecordSheetType;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.repository.XhibitAppealDataRepository;

@Slf4j
@Service
public class AppealDataService extends XhibitDataServiceBase<XhibitAppealDataEntity> {

    public AppealDataService(
            XhibitDataService xhibitDataService,
            XhibitAppealDataRepository repository,
            AppealDataProcedureService procedureService) {
        super(xhibitDataService, repository, procedureService);
    }

    @Override
    protected RecordSheetType getRecordSheetType() {
        return RecordSheetType.APPEAL;
    }

    @Override
    protected XhibitAppealDataEntity fromDto(XhibitRecordSheet dto) {
        return XhibitAppealDataEntity.fromDto(dto);
    }

    @Override
    protected Integer getEntityId(XhibitAppealDataEntity entity) {
        return entity.getId();
    }

    @Override
    protected String getFilename(XhibitAppealDataEntity entity) {
        return entity.getFilename();
    }
}
