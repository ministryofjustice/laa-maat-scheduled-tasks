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

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoredProcedureService {

    public static final String EMPTY_PROCEDURE_NAME_MESSAGE = "Stored procedure name cannot be null or empty";

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void callStoredProcedure(String storedProcedureName) {
        createAndExecuteStoredProcedure(storedProcedureName, Collections.emptyMap());
    }

    @Transactional
    public void callStoredProcedure(String storedProcedureName, Map<String, Object> parameterMap) {
        createAndExecuteStoredProcedure(storedProcedureName, parameterMap);
    }

    private void createAndExecuteStoredProcedure(String storedProcedureName, Map<String, Object> parameterMap) {
        if (!StringUtils.hasText(storedProcedureName)) {
            throw new IllegalArgumentException(EMPTY_PROCEDURE_NAME_MESSAGE
            );
        }

        StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery(storedProcedureName);
        for (var entry : parameterMap.entrySet()) {
            storedProcedureQuery.registerStoredProcedureParameter(entry.getKey(), entry.getValue().getClass(), ParameterMode.IN);
            storedProcedureQuery.setParameter(entry.getKey(), entry.getValue());
        }

        try {
            log.info("Executing stored procedure: {}", storedProcedureName);
            storedProcedureQuery.execute();
            log.info("Completed stored procedure: {}", storedProcedureName);
        } catch (Exception e) {
            log.error("Error executing stored procedure {}: {}", storedProcedureName, e.getMessage());
            throw new StoredProcedureException("Failed to execute stored procedure: " + storedProcedureName, e);
        }
    }
}