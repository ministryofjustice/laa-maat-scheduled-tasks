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
  - name: SCOPE_API
    value: {{ .Values.scope }}
  - name: LMR_REPORTS
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: LMR_REPORTS
  - name: EVIDENCE_REMINDER_LETTER
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: EVIDENCE_REMINDER_LETTER
  - name: INACTIVATE_USERS
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: INACTIVATE_USERS
  - name: FA_FIX
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: FA_FIX
  - name: CENTRAL_PRINT
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: CENTRAL_PRINT
  - name: APPEAL_DATA_CRON_EXPRESSION
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: APPEAL_DATA_CRON_EXPRESSION
  - name: TRIAL_DATA_CRON_EXPRESSION
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: TRIAL_DATA_CRON_EXPRESSION
  - name: BILLING_LOG_CLEANUP_CRON_EXPRESSION
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: BILLING_LOG_CLEANUP_CRON_EXPRESSION
  - name: JWT_ISSUER_URI
    value: {{ .Values.jwt.issuerUri }}
  - name: CCLF_API_BASE_URL
    value: {{ .Values.cclfApi.baseUrl }}
  - name: CCLF_API_OAUTH_URL
    value: {{ .Values.cclfApi.oauthUrl }}
  - name: CCLF_API_OAUTH_CLIENT_ID
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: CCLF_API_OAUTH_CLIENT_ID
  - name: CCLF_API_OAUTH_CLIENT_SECRET
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: CCLF_API_OAUTH_CLIENT_SECRET
  - name: CCR_API_BASE_URL
    value: {{ .Values.ccrApi.baseUrl }}
  - name: CCR_API_OAUTH_URL
    value: {{ .Values.ccrApi.oauthUrl }}
  - name: CCR_API_OAUTH_CLIENT_ID
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: CCR_API_OAUTH_CLIENT_ID
  - name: CCR_API_OAUTH_CLIENT_SECRET
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: CCR_API_OAUTH_CLIENT_SECRET
  - name: LOG_LEVEL
    value: {{ .Values.logging.level }}
  - name: AWS_DEFAULT_REGION
    value: {{ .Values.aws_region }}
  - name: XHIBIT_BATCH_FETCH_SIZE
    value: {{ .Values.xhibitBatch.fetchSize }}
  - name: AWS_S3_XHIBIT_DATA_BUCKET_NAME
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: AWS_S3_XHIBIT_DATA_BUCKET_NAME
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
