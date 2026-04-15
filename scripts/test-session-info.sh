#!/usr/bin/env bash
set -euo pipefail

# ----------------------------------------
# 사용법:
# BASE_URL=http://localhost:8080 ./scripts/test-session-info.sh <user_id> <password>
# ----------------------------------------

BASE_URL="${BASE_URL:-http://localhost:8080}"
USER_ID="${1:-}"
PASSWORD="${2:-}"

if [[ -z "$USER_ID" || -z "$PASSWORD" ]]; then
  echo "Usage: BASE_URL=http://localhost:8080 $0 <user_id> <password>"
  exit 1
fi

# 쿠키 파일: 로그인 세션 유지용
COOKIE_JAR="$(mktemp)"
# 로그인 페이지 HTML 저장용(여기서 CSRF 토큰 추출)
LOGIN_HTML="$(mktemp)"
# 로그인 응답 헤더 저장용
HEADERS="$(mktemp)"

cleanup() {
  rm -f "$COOKIE_JAR" "$LOGIN_HTML" "$HEADERS"
}
trap cleanup EXIT

echo "[1] GET /login (session + csrf)"
curl -fsS -c "$COOKIE_JAR" "$BASE_URL/login" -o "$LOGIN_HTML"

# login.html 안 hidden input(name=\"_csrf\") 값 추출
CSRF_TOKEN="$(sed -n 's/.*name=\"_csrf\" value=\"\([^\"]*\)\".*/\1/p' "$LOGIN_HTML" | sed -n '1p')"

echo "[2] POST /login"
if [[ -n "$CSRF_TOKEN" ]]; then
  # CSRF 활성화 환경
  curl -sS -D "$HEADERS" -o /dev/null \
    -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
    -X POST "$BASE_URL/login" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    --data-urlencode "username=$USER_ID" \
    --data-urlencode "password=$PASSWORD" \
    --data-urlencode "_csrf=$CSRF_TOKEN"
else
  # CSRF 비활성화 환경
  curl -sS -D "$HEADERS" -o /dev/null \
    -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
    -X POST "$BASE_URL/login" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    --data-urlencode "username=$USER_ID" \
    --data-urlencode "password=$PASSWORD"
fi

echo "[3] GET /getSessionInfo"
# 같은 쿠키(세션)로 호출해서 로그인 사용자 정보 확인
curl -fsS -b "$COOKIE_JAR" "$BASE_URL/getSessionInfo"
echo
