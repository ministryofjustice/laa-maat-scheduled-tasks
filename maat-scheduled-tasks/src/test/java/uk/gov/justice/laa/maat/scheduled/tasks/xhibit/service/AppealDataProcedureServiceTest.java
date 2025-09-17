package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.StoredProcedure;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.StoredProcedureException;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.service.StoredProcedureService;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.entity.XhibitAppealDataEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.ProcedureResult;

@ExtendWith(MockitoExtension.class)
class AppealDataProcedureServiceTest {

    @Mock
    private StoredProcedureService storedProcedureService;

    private AppealDataProcedureService procedureService;

    @BeforeEach
    void setUp() {
        procedureService = new AppealDataProcedureService(storedProcedureService);
    }

    @Test
    void call_returnsSuccess_whenProcedureHasNoErrors() {
        XhibitAppealDataEntity entity = getTrialDataEntity(234, "appeal1.txt");

        StoredProcedureResponse response = new StoredProcedureResponse(List.of());
        when(storedProcedureService.callStoredProcedure(
                eq(StoredProcedure.APPEAL_DATA_TO_MAAT_PROCEDURE), anyList()))
                .thenReturn(response);

        ProcedureResult result = procedureService.call(entity);

        assertThat(result).isEqualTo(ProcedureResult.SUCCESS);
    }

    @Test
    void call_returnsFailure_whenProcedureHasErrorCodeAndMessage() {

        XhibitAppealDataEntity entity = getTrialDataEntity(456, "appeal2.txt");

        List<StoredProcedureParameter<?>> outputs = List.of(
                StoredProcedureParameter.safePopulate(
                        StoredProcedureParameter.outputParameter("p_err_msg", String.class),
                        "error"),
                StoredProcedureParameter.safePopulate(
                        StoredProcedureParameter.outputParameter("p_error_code", String.class),
                        "ER123")
        );

        StoredProcedureResponse response = new StoredProcedureResponse(outputs);
        when(storedProcedureService.callStoredProcedure(
                eq(StoredProcedure.APPEAL_DATA_TO_MAAT_PROCEDURE), anyList()))
                .thenReturn(response);
        ProcedureResult result = procedureService.call(entity);

        assertThat(result).isEqualTo(ProcedureResult.FAILURE);
    }

    @Test
    void call_returnsFailure_whenStoredProcedureThrowsException() {

        XhibitAppealDataEntity entity = getTrialDataEntity(789, "appeal3.txt");

        when(storedProcedureService.callStoredProcedure(
                eq(StoredProcedure.APPEAL_DATA_TO_MAAT_PROCEDURE), anyList()))
                .thenThrow(new StoredProcedureException("Stored procedure execution failed",
                        new RuntimeException("Oops!")));

        ProcedureResult result = procedureService.call(entity);

        assertThat(result).isEqualTo(ProcedureResult.FAILURE);
    }

    @Test
    void getProcedureParameters_buildsExpectedInputsAndOutputs() {
        XhibitAppealDataEntity entity = XhibitAppealDataEntity.builder().id(42).build();

        List<StoredProcedureParameter<?>> params = procedureService.getProcedureParameters(entity);

        assertThat(params).extracting(StoredProcedureParameter::getName)
                .contains("id", "p_error_code", "p_err_msg");
    }

    private static XhibitAppealDataEntity getTrialDataEntity(int id, String filename) {
        return XhibitAppealDataEntity.builder()
                .id(id)
                .filename(filename)
                .build();
    }
}
