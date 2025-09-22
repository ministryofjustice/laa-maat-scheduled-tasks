package uk.gov.justice.laa.maat.scheduled.tasks.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.maat.scheduled.tasks.enums.StoredProcedure;
import uk.gov.justice.laa.maat.scheduled.tasks.service.LocalManagementReportsService;
import uk.gov.justice.laa.maat.scheduled.tasks.service.StoredProcedureService;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class MaatBatchesScheduler {


    private final LocalManagementReportsService localManagementReportsService;
    private final StoredProcedureService storedProcedureService;

    @Scheduled(cron = "${maat_batch.lmr_reports.cron_expression}")
    public void executeLocalManagementReports() {
        localManagementReportsService.processReportsBatches();
    }

    @Scheduled(cron = "${maat_batch.evidence_reminder_letter.cron_expression}")
    public void generateEvidenceReminderLetter() {
        storedProcedureService.callStoredProcedure(
                StoredProcedure.MAAT_BATCH_PROCESS_CORRESPONDENCE);
    }

    @Scheduled(cron = "${maat_batch.inactive_users.cron_expression}")
    public void deactivateInactiveUsers() {
        storedProcedureService.callStoredProcedure(StoredProcedure.MAAT_BATCH_INACTIVE_USERS);
    }

    @Scheduled(cron = "${maat_batch.fa_fix.cron_expression}")
    public void executeFinancialAssessmentFix() {
        storedProcedureService.callStoredProcedure(StoredProcedure.MAAT_BATCH_FA_FIX);
    }

}