{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for service containers
*/}}
{{- define "laa-maat-data-api.env-vars" }}
env:
  - name: AWS_REGION
    value: {{ .Values.aws_region }}
  - name: LOG_LEVEL
    value: {{ .Values.logging.level }}
  - name: AWS_DEFAULT_REGION
    value: {{ .Values.aws_region }}
  - name: TOGDATA_DATASOURCE_USERNAME
    valueFrom:
        secretKeyRef:
            name: maat-scheduled-tasks-env-variables
            key: TOGDATA_DATASOURCE_USERNAME
  - name: TOGDATA_DATASOURCE_PASSWORD
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
