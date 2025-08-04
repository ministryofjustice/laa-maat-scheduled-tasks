{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for service containers
*/}}
{{- define "laa-maat-scheduled-tasks.env-vars" }}
env:
  - name: AWS_REGION
    value: {{ .Values.aws_region }}
  - name: SENTRY_DSN
    valueFrom:
      secretKeyRef:
        name: maat-scheduled-tasks-env-variables
        key: SENTRY_DSN
  - name: SENTRY_ENV
    value: {{ .Values.host_env }}
  - name: SENTRY_SAMPLE_RATE
    value: {{ .Values.sentry.sampleRate | quote }}
  - name: LMR_REPORTS
    value: {{ .Values.maat_batch.lmr_reports.cron_expression }}
  - name: EVIDENCE_REMINDER_LETTER
    value: {{ .Values.maat_batch.evidence_reminder_letter.cron_expression }}
  - name: INACTIVATE_USERS
    value: {{ .Values.maat_batch.inactive_users.cron_expression }}
  - name: FA_FIX
    value: {{ .Values.maat_batch.fa_fix.cron_expression }}
  - name: CENTRAL_PRINT
    value: {{ .Values.maat_batch.central_print.cron_expression }}
  - name: TRIAL_DATA_CRON_EXPRESSION
    value: {{ .Values.hub_batch.trial_data_population.cron_expression }}
  - name: LOG_LEVEL
    value: {{ .Values.logging.level }}
  - name: AWS_DEFAULT_REGION
    value: {{ .Values.aws_region }}
  - name: DATASOURCE_USERNAME
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: TOGDATA_DATASOURCE_USERNAME
  - name: DATASOURCE_PASSWORD
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: TOGDATA_DATASOURCE_PASSWORD
  - name: DATASOURCE_URL
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: DATASOURCE_URL
{{- end -}}
