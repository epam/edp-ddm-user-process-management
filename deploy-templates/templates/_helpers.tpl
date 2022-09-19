{{- define "keycloak.host" -}}
{{- if .Values.keycloak.customHost }}
{{- .Values.keycloak.customHost }}
{{- else }}
{{- .Values.keycloak.host }}
{{- end }}
{{- end -}}

{{- define "keycloak.urlPrefix" -}}
{{- printf "%s%s%s%s" "https://" (include "keycloak.host" .) "/auth/realms/" .Release.Namespace -}}
{{- end -}}

{{- define "issuer.officer" -}}
{{- printf "%s-%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.officer -}}
{{- end -}}

{{- define "issuer.citizen" -}}
{{- printf "%s-%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.citizen -}}
{{- end -}}

{{- define "issuer.admin" -}}
{{- printf "%s-%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.admin -}}
{{- end -}}

{{- define "jwksUri.officer" -}}
{{- printf "%s-%s%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.officer .Values.keycloak.certificatesEndpoint -}}
{{- end -}}

{{- define "jwksUri.citizen" -}}
{{- printf "%s-%s%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.citizen .Values.keycloak.certificatesEndpoint -}}
{{- end -}}

{{- define "jwksUri.admin" -}}
{{- printf "%s-%s%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.admin .Values.keycloak.certificatesEndpoint -}}
{{- end -}}

{{- define "userProcessManagement.istioResources" -}}
{{- if .Values.global.registry.userProcessManagement.istio.sidecar.resources.limits.cpu }}
sidecar.istio.io/proxyCPULimit: {{ .Values.global.registry.userProcessManagement.istio.sidecar.resources.limits.cpu | quote }}
{{- end }}
{{- if .Values.global.registry.userProcessManagement.istio.sidecar.resources.limits.memory }}
sidecar.istio.io/proxyMemoryLimit: {{ .Values.global.registry.userProcessManagement.istio.sidecar.resources.limits.memory | quote }}
{{- end }}
{{- if .Values.global.registry.userProcessManagement.istio.sidecar.resources.requests.cpu }}
sidecar.istio.io/proxyCPU: {{ .Values.global.registry.userProcessManagement.istio.sidecar.resources.requests.cpu | quote }}
{{- end }}
{{- if .Values.global.registry.userProcessManagement.istio.sidecar.resources.requests.memory }}
sidecar.istio.io/proxyMemory: {{ .Values.global.registry.userProcessManagement.istio.sidecar.resources.requests.memory | quote }}
{{- end }}
{{- end -}}

