#!/usr/bin/env bash
set -euo pipefail
: "${APIM_URL:?Set APIM_URL}"
BASE="${APIM_URL%/}"

assert_status() {
  local name="$1" expected="$2"; shift 2
  local status
  status=$(curl -sS -o /tmp/valle-smoke-body -w '%{http_code}' "$@")
  if [[ "/$expected/" != *"/$status/"* ]]; then echo "FAIL $name: $status (expected $expected)" >&2; exit 1; fi
  echo "PASS $name -> $status"
}

assert_status 'missing token' '401' "$BASE/api/reportes"
assert_status 'invalid token' '401' -H 'Authorization: Bearer invalid.jwt.value' "$BASE/api/reportes"
assert_status 'CORS preflight' '200/204' -X OPTIONS -H "Origin: ${ALLOWED_ORIGIN:-http://localhost:5173}" -H 'Access-Control-Request-Method: GET' -H 'Access-Control-Request-Headers: authorization,content-type' "$BASE/api/reportes"
if [[ -n "${ACCESS_TOKEN:-}" ]]; then assert_status 'valid token' '200' -H "Authorization: Bearer $ACCESS_TOKEN" "$BASE/api/reportes"; else echo 'SKIP valid token: ACCESS_TOKEN missing'; fi
if [[ -n "${TOKEN_WITHOUT_ROLE:-}" ]]; then assert_status 'valid token without role' '403' -X DELETE -H "Authorization: Bearer $TOKEN_WITHOUT_ROLE" "$BASE/api/reportes/eliminar/0"; else echo 'SKIP 403: TOKEN_WITHOUT_ROLE missing'; fi
