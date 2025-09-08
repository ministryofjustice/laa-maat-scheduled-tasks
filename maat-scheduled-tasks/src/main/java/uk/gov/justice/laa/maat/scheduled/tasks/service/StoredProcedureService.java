package uk.gov.justice.laa.maat.scheduled.tasks.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uk.gov.justice.laa.maat.scheduled.tasks.exception.StoredProcedureException;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter;
import uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.justice.laa.maat.scheduled.tasks.helper.StoredProcedureParameter.populatedOutParameter;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoredProcedureService {

    public static final String EMPTY_PROCEDURE_NAME_MESSAGE = "Stored procedure name cannot be null or empty";
    public static final String STORED_PROCEDURE_FAILURE_MESSAGE = "Failed to execute stored procedure: ";

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void callStoredProcedure(String storedProcedureName) {
        createAndExecuteStoredProcedure(storedProcedureName, Collections.emptyList());
    }

    @Transactional
    public StoredProcedureResponse callStoredProcedure(String storedProcedureName, List<StoredProcedureParameter<?>> parameters) {
        return createAndExecuteStoredProcedure(storedProcedureName, parameters);
    }

    private StoredProcedureResponse createAndExecuteStoredProcedure(String name, List<StoredProcedureParameter<?>> parameters) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException(EMPTY_PROCEDURE_NAME_MESSAGE);
        }

        StoredProcedureQuery storedProcedureQuery = getStoredProcedureQuery(name, parameters);

        try {
            storedProcedureQuery.execute();
            log.info("Completed stored procedure: { procedure: {} }", name);
            return getStoredProcedureResponse(storedProcedureQuery, parameters);
        } catch (Exception e) {
            log.error("Error executing stored procedure { procedure: {} }", name, e);
            throw new StoredProcedureException(STORED_PROCEDURE_FAILURE_MESSAGE + name, e);
        }
    }

    private StoredProcedureQuery getStoredProcedureQuery(String name, List<StoredProcedureParameter<?>> parameters) {
        StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery(name);
        for (StoredProcedureParameter<?> parameter : parameters) {
            String parameterName = parameter.getName();
            ParameterMode parameterMode = parameter.getMode();

            storedProcedureQuery.registerStoredProcedureParameter(parameterName, parameter.getType(), parameterMode);
            if (parameterMode == ParameterMode.IN || parameterMode == ParameterMode.INOUT) {
                storedProcedureQuery.setParameter(parameterName, parameter.getValue());
            }
        }
        return storedProcedureQuery;
    }

    private StoredProcedureResponse getStoredProcedureResponse(StoredProcedureQuery storedProcedureQuery, List<StoredProcedureParameter<?>> parameters) {
        List<StoredProcedureParameter<?>> outputParametersWithValues = parameters.stream()
            .filter(p -> p.getMode() == ParameterMode.OUT || p.getMode() == ParameterMode.INOUT)
            .map(p -> {
                @SuppressWarnings("unchecked")
                StoredProcedureParameter<Object> param = (StoredProcedureParameter<Object>) p;
                Object value = storedProcedureQuery.getOutputParameterValue(p.getName());
                return populatedOutParameter(param, value);
            })
            .collect(Collectors.toList());
        return new StoredProcedureResponse(outputParametersWithValues);
    }
}