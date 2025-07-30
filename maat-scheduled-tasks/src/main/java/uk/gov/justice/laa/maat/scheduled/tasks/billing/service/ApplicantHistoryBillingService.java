package uk.gov.justice.laa.maat.scheduled.tasks.billing.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.dto.ApplicantHistoryBillingDTO;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.entity.ApplicantHistoryBillingEntity;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.mapper.ApplicantHistoryBillingMapper;
import uk.gov.justice.laa.maat.scheduled.tasks.billing.repository.ApplicantHistoryBillingRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicantHistoryBillingService {

    private final ApplicantHistoryBillingRepository applicantHistoryBillingRepository;
    private final ApplicantHistoryBillingMapper applicantHistoryBillingMapper;

    public List<ApplicantHistoryBillingDTO> extractApplicantHistory() {
        List<ApplicantHistoryBillingDTO> applicantHistoryDTOs = new ArrayList<>();

        List<ApplicantHistoryBillingEntity> applicantHistoryEntities = applicantHistoryBillingRepository.extractApplicantHistoryBilling();
        log.info("Application histories successfully extracted for billing data.");

        if (!applicantHistoryEntities.isEmpty()) {
            applicantHistoryDTOs = applicantHistoryEntities
                .stream()
                .map(applicantHistoryBillingMapper::mapEntityToDTO)
                .toList();
        }

        return applicantHistoryDTOs;
    }
}