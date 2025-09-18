package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.ProcedureResult;

@ExtendWith(MockitoExtension.class)
class XhibitProcedureServiceTest {

    @Mock
    private StoredProcedureService storedProcedureService;

    private TestProcedureService procedureService;

    @BeforeEach
    void setUp() {
        procedureService = new TestProcedureService(storedProcedureService);
    }

    @Test
    void call_returnsSuccess_whenNoErrorOutputs() {
        TestEntity entity = new TestEntity(1, "file1.txt");

        StoredProcedureResponse response = new StoredProcedureResponse(List.of());
        when(storedProcedureService.callStoredProcedure(eq(StoredProcedure.TEST_PROCEDURE), anyList()))
                .thenReturn(response);

        ProcedureResult result = procedureService.call(entity);

        assertThat(result).isEqualTo(ProcedureResult.SUCCESS);
    }

    @Test
    void call_returnsFailure_whenErrorOutputsPresent() {
        TestEntity entity = new TestEntity(2, "file2.txt");

        List<StoredProcedureParameter<?>> outputs = List.of(
                StoredProcedureParameter.safePopulate(
                        StoredProcedureParameter.outputParameter("p_err_msg", String.class),
                        "error"),
                StoredProcedureParameter.safePopulate(
                        StoredProcedureParameter.outputParameter("p_error_code", String.class),
                        "ER123")
        );
        StoredProcedureResponse response = new StoredProcedureResponse(outputs);

        when(storedProcedureService.callStoredProcedure(eq(StoredProcedure.TEST_PROCEDURE), anyList()))
                .thenReturn(response);

        ProcedureResult result = procedureService.call(entity);

        assertThat(result).isEqualTo(ProcedureResult.FAILURE);
    }

    @Test
    void call_returnsFailure_whenStoredProcedureThrows() {
        TestEntity entity = new TestEntity(3, "file3.txt");
        when(storedProcedureService.callStoredProcedure(eq(StoredProcedure.TEST_PROCEDURE), anyList()))
                .thenThrow(new StoredProcedureException("Stored procedure execution failed",
                        new RuntimeException("Oops!")));

        ProcedureResult result = procedureService.call(entity);

        assertThat(result).isEqualTo(ProcedureResult.FAILURE);
    }

    @Test
    void getProcedureParameters_includesOutputAndEntityInputs() {
        TestEntity entity = new TestEntity(42, "file42.txt");

        List<StoredProcedureParameter<?>> params = procedureService.getProcedureParameters(entity);

        assertThat(params).extracting(StoredProcedureParameter::getName)
                .contains("id", "p_error_code", "p_err_msg");
    }

    private static class TestProcedureService extends XhibitProcedureService<TestEntity> {
        protected TestProcedureService(StoredProcedureService storedProcedureService) {
            super(storedProcedureService);
        }

        @Override
        protected int getEntityId(TestEntity e) {
            return e.id();
        }

        @Override
        public StoredProcedure getStoredProcedure() {
            return StoredProcedure.TEST_PROCEDURE;
        }

        @Override
        protected List<StoredProcedureParameter<?>> getProcedureParameters(TestEntity e) {
            List<StoredProcedureParameter<?>> params = new ArrayList<>(OUTPUT_PARAMETERS);
            params.add(StoredProcedureParameter.inputParameter("id", e.id()));
            return params;
        }
    }

    private record TestEntity(int id, String filename) {}
}
