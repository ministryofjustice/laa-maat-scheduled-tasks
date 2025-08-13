package uk.gov.justice.laa.maat.scheduled.tasks.factory;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.service.XhibitDataService;

@Service
public class PrototypeBeanFactory {

    @Lookup
    public XhibitDataService getXhibitDataService() {
        return null;
    }
}
