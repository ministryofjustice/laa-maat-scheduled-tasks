package uk.gov.justice.laa.maat.scheduled.tasks.service;

import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.safePopulate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.StoredProcedure;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.StoredProcedureException;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoredProcedureService {

    private static final String STORED_PROCEDURE_FAILURE_MESSAGE = "Failed to execute stored procedure: ";

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void callStoredProcedure(StoredProcedure storedProcedure) {
        createAndExecuteStoredProcedure(storedProcedure, Collections.emptyList());
    }

    @Transactional
    public StoredProcedureResponse callStoredProcedure(StoredProcedure storedProcedure, List<StoredProcedureParameter<?>> parameters) {
        return createAndExecuteStoredProcedure(storedProcedure, parameters);
    }

    private StoredProcedureResponse createAndExecuteStoredProcedure(StoredProcedure storedProcedure, List<StoredProcedureParameter<?>> parameters) {
        Objects.requireNonNull(storedProcedure, "Stored procedure must not be null");

        StoredProcedureQuery storedProcedureQuery = getStoredProcedureQuery(storedProcedure.getQualifiedName(), parameters);

        try {
            storedProcedureQuery.execute();
            log.info("Completed stored procedure: { procedure: {} }", storedProcedure.getQualifiedName());
            return getStoredProcedureResponse(storedProcedureQuery, parameters);
        } catch (Exception e) {
            log.error("Error executing stored procedure { procedure: {} }", storedProcedure.getQualifiedName(), e);
            throw new StoredProcedureException(STORED_PROCEDURE_FAILURE_MESSAGE + storedProcedure.getQualifiedName(), e);
        }
    }

    private StoredProcedureQuery getStoredProcedureQuery(String name, List<StoredProcedureParameter<?>> parameters) {
        StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery(name);
        for (StoredProcedureParameter<?> parameter : parameters) {
            String parameterName = parameter.getName();
            storedProcedureQuery.registerStoredProcedureParameter(parameterName, parameter.getType(), parameter.getMode());
            if (parameter.isInputParameter()) {
                storedProcedureQuery.setParameter(parameterName, parameter.getValue());
            }
        }
        return storedProcedureQuery;
    }

    private StoredProcedureResponse getStoredProcedureResponse(StoredProcedureQuery storedProcedureQuery, List<StoredProcedureParameter<?>> parameters) {
        List<StoredProcedureParameter<?>> outputParametersWithValues = parameters.stream()
            .filter(StoredProcedureParameter::isOutputParameter)
            .map(p -> safePopulate(p, storedProcedureQuery.getOutputParameterValue(p.getName())))
            .collect(Collectors.toList());
        return new StoredProcedureResponse(outputParametersWithValues);
    }
}