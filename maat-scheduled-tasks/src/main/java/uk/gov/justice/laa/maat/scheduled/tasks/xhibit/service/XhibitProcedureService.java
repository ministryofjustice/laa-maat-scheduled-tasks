package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.service;

import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.outputParameter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.maat.scheduled.tasks.xhibit.enums.ProcedureResult;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.StoredProcedure;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.StoredProcedureException;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureResponse;
import uk.gov.justice.laa.maat.scheduled.tasks.service.StoredProcedureService;

@Slf4j
@RequiredArgsConstructor
public abstract class XhibitProcedureService<T> {

    protected final StoredProcedureService storedProcedureService;

    static final List<StoredProcedureParameter<?>> OUTPUT_PARAMETERS =
            List.of(outputParameter("p_error_code", String.class),
                    outputParameter("p_err_msg", String.class)
            );

    protected boolean isErrored(StoredProcedureResponse res) {
        return res.hasValue("p_err_msg") && res.hasValue("p_error_code");
    }

    public ProcedureResult call(T entity) {
        try {
            List<StoredProcedureParameter<?>> parameters = getProcedureParameters(entity);
            StoredProcedureResponse res = storedProcedureService.callStoredProcedure(
                    getStoredProcedure(), parameters);

            if (isErrored(res)) {
                log.error(
                        "Stored procedure returned an error: { procedure: {}, recordId: {}, errorCode: {}, errorMessage: {} }",
                        getStoredProcedure().getQualifiedName(),
                        getEntityId(entity),
                        res.getValue("p_error_code"),
                        res.getValue("p_err_msg")
                );
                return ProcedureResult.FAILURE;
            } else {
                return ProcedureResult.SUCCESS;
            }

        } catch (StoredProcedureException e) {
            log.error("Stored procedure invocation failed for recordId={}", getEntityId(entity), e);
            return ProcedureResult.FAILURE;
        }
    }

    protected abstract int getEntityId(T entity);

    protected abstract StoredProcedure getStoredProcedure();

    protected abstract List<StoredProcedureParameter<?>> getProcedureParameters(T entity);
}
