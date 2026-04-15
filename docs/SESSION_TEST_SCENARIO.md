# Swagger 로그인 후 getSessionInfo 테스트

## 브라우저 테스트
1. `http://localhost:8080/swagger-ui.html` 접속
2. 로그인 페이지(`/login`)로 이동 확인
3. `tb_com_user.user_id / password` 입력 후 로그인
4. Swagger UI 진입 확인
5. 같은 브라우저에서 `http://localhost:8080/getSessionInfo` 호출

예상 응답:
```json
{
  "empno": "admin",
  "name": "관리자"
}
```

## curl 자동 테스트
```bash
chmod +x scripts/test-session-info.sh
BASE_URL=http://localhost:8080 ./scripts/test-session-info.sh admin 1234
```
