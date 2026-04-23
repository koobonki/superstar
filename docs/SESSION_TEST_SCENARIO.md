# Swagger OAuth2 로그인 후 세션 정보 테스트

## 브라우저 테스트
1. `http://localhost:8080/swagger-ui.html` 접속
2. 우측 상단 `Authorize` 클릭
3. `oauth2` 스킴에서 로그인 진행
4. IdP 로그인 후 Swagger로 리다이렉트 확인
5. `/api/session/me` 엔드포인트 실행

예상 응답:
```json
{
  "sessionId": "A1B2C3...",
  "empno": "user1",
  "name": "user1",
  "roles": [
    "ROLE_USER"
  ]
}
```

## curl 자동 테스트
```bash
chmod +x scripts/test-session-info.sh
BASE_URL=http://localhost:8080 ./scripts/test-session-info.sh <access_token>
```
