package uk.gov.justice.laa.maat.scheduled.tasks.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.repository.ApplicantHistoryBillingRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantHistoryBillingService {

    private final ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    private final ApplicantHistoryBillingMapper applicantHistoryBillingMapper;

    public List<ApplicantHistoryBillingDTO> extractApplicantHistory() {
        List<ApplicantHistoryBillingEntity> applicantHistoryEntities = applicantHistoryBillingRepository.extractApplicantHistoryForBilling();
        log.info("Application histories successfully extracted for billing data.");

        if (applicantHistoryEntities.isEmpty()) {
            return new ArrayList<>();
        }

        return applicantHistoryEntities
            .stream()
            .map(applicantHistoryBillingMapper::mapEntityToDTO)
            .toList();
    }
}