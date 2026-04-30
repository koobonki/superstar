#!/usr/bin/env bash
set -euo pipefail

# ------------------------------------------------------------
# OAuth2 Bearer 토큰으로 세션 정보 API를 호출하는 스크립트
# 사용법:
#   BASE_URL=http://localhost:8080 ./scripts/test-session-info.sh <access_token>
# 예시:
#   BASE_URL=http://localhost:8080 ./scripts/test-session-info.sh eyJraWQiOi...
# ------------------------------------------------------------

BASE_URL="${BASE_URL:-http://localhost:8080}"
ACCESS_TOKEN="${1:-}"

if [[ -z "$ACCESS_TOKEN" ]]; then
  echo "Usage: BASE_URL=http://localhost:8080 $0 <access_token>"
  exit 1
fi

echo "[1] GET ${BASE_URL}/api/session/me with Bearer token"
curl -fsS \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  "${BASE_URL}/api/session/me"
echo
