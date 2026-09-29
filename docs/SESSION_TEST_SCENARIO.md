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

## ComUser 조회/저장 API Swagger JSON 예시

### 1) 사용자 목록 조회

#### GET `/api/com-users`

응답 예시 (`200 OK`)
```json
[
  {
    "id": 1,
    "userId": "admin",
    "userName": "관리자",
    "role": "ROLE_ADMIN"
  },
  {
    "id": 2,
    "userId": "user01",
    "userName": "일반사용자",
    "role": "ROLE_USER"
  }
]
```

### 2) 수정 리스트 저장

#### POST `/api/com-users/save`

요청 예시 1: 정상 수정
```json
[
  {
    "id": 1,
    "userName": "관리자(수정)",
    "role": "ROLE_ADMIN"
  },
  {
    "id": 2,
    "userName": "일반사용자A",
    "role": "ROLE_USER"
  }
]
```

응답 예시 1 (`200 OK`)
```json
{
  "requestedCount": 2,
  "updatedCount": 2,
  "skippedCount": 0,
  "errors": []
}
```

요청 예시 2: 일부 오류 포함(`id` 없음/존재하지 않는 id)
```json
[
  {
    "id": 1,
    "userName": "관리자",
    "role": "ROLE_ADMIN"
  },
  {
    "id": 9999,
    "userName": "없는사용자",
    "role": "ROLE_USER"
  },
  {
    "id": null,
    "userName": "ID누락",
    "role": "ROLE_USER"
  }
]
```

응답 예시 2 (`200 OK`)
```json
{
  "requestedCount": 2,
  "updatedCount": 1,
  "skippedCount": 1,
  "errors": [
    "id가 없는 항목은 저장할 수 없습니다.",
    "id=9999 사용자를 찾을 수 없습니다."
  ]
}
```

요청 예시 3: 변경사항 없는 경우
```json
[
  {
    "id": 1,
    "userName": "관리자",
    "role": "ROLE_ADMIN"
  }
]
```

응답 예시 3 (`200 OK`)
```json
{
  "requestedCount": 1,
  "updatedCount": 0,
  "skippedCount": 1,
  "errors": []
}
```

> 참고: `requestedCount`는 내부 로직상 유효한 `id` 기준(중복 제거 후)으로 계산됩니다.
