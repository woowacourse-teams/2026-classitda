# Docker로 로컬 서버 실행하기

Docker 명령어를 몰라도 저장소에 포함된 스크립트로 클래스잇다 백엔드 서버와 MySQL, Redis를 실행하고 중지할 수 있습니다.

## 사전 준비

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)을 설치합니다.
- Docker Desktop을 실행합니다.
- 터미널에서 이 저장소의 `BE` 디렉터리로 이동합니다.

환경변수는 따로 준비하지 않아도 됩니다. 실행 스크립트가 `docker/.env.example`을 복사해 `docker/.env`를 만들고, 비어 있는 JWT 키와 HMAC 키를 자동으로 생성합니다. `docker/.env`는 git에 올라가지 않습니다.

## 실행

```bash
./docker/scripts/start.sh
```

스크립트가 서버 이미지를 빌드하고 다음 컨테이너를 실행합니다.

| 컨테이너 | 주소 | 용도 |
| --- | --- | --- |
| `be-app` | `http://localhost:8080` | Spring Boot 서버 |
| `be-mysql` | `localhost:3306` | 로컬 MySQL 8.4 |
| `be-redis` | `localhost:6379` | 로컬 Redis 7.4 |

MySQL과 Redis가 정상 상태가 된 후 서버가 자동으로 시작됩니다. 로컬 프로필에서는 서버를 시작할 때 기존 테이블을 삭제하고 `schema.sql`로 다시 생성합니다.
실행 스크립트는 애플리케이션의 `http://localhost:8080/actuator/health`가 정상 상태가 될 때까지 기다립니다.

API 문서는 `http://localhost:8080/swagger-ui/index.html`에서 볼 수 있습니다.

## 인증 토큰 받기

대부분의 API는 `Authorization: Bearer <accessToken>` 헤더를 요구합니다. 로컬에서는 구글 로그인 없이 토큰을 받을 수 있습니다.

```bash
curl -X POST http://localhost:8080/api/auth/local/members/1/tokens \
  -H "X-API-Version: 1"
```

```json
{
  "accessToken": "eyJhbGciOi...",
  "accessTokenExpiresIn": 3600,
  "refreshToken": "eyJhbGciOi...",
  "refreshTokenExpiresIn": 2592000
}
```

`1`은 `local-data.sql`이 넣어 두는 테스트 회원의 id입니다. 받은 `accessToken`을 그대로 헤더에 넣어 호출합니다.

```bash
curl http://localhost:8080/api/studios/me \
  -H "X-API-Version: 1" \
  -H "Authorization: Bearer <accessToken>"
```

> 이 엔드포인트는 `local` 프로필에서만 동작하며 배포 환경에는 존재하지 않습니다.

## 요청할 때 주의할 점

- **모든 API는 `X-API-Version: 1` 헤더가 필요합니다.** 빠뜨리면 `API-001` 오류가 납니다.
- 휴대전화 인증은 실제 SMS를 보내지 않습니다. 인증번호는 `docker/.env`의 `AUTH_SMS_LOCAL_FIXED_OTP` 값(기본 `123456`)으로 고정되어 있습니다.
- 전화번호는 `010XXXXXXXX` 형식이어야 합니다.

## 회원가입 플로우를 테스트하려면

위의 로컬 토큰 발급은 **이미 가입된 회원**의 로그인 토큰만 내려줍니다. 신규 가입 흐름은 구글 인증에서 시작하므로 준비가 더 필요합니다.

**1. 실제 클라이언트 ID 설정**

`docker/.env`의 `GOOGLE_OAUTH_WEB_CLIENT_ID`와 `GOOGLE_OAUTH_IOS_CLIENT_ID`를 팀 채널에서 받은 값으로 바꾸고 서버를 다시 시작합니다.

```bash
./docker/scripts/stop.sh && ./docker/scripts/start.sh
```

**2. 구글 ID 토큰 발급**

[Swagger에서 Google 로그인 테스트하기](swagger-google-login.md)의 절차대로 OAuth 2.0 Playground에서 ID 토큰을 받습니다. 유효기간이 약 1시간이라 만료되면 다시 받아야 합니다.

**3. 가입 흐름 호출**

| 순서 | 요청 | 사용하는 토큰 |
| --- | --- | --- |
| 1 | `POST /api/auth/google` | 없음 (구글 ID 토큰을 본문에) |
| 2 | `GET /api/terms` | `signupToken` |
| 3 | `POST /api/auth/phone-verifications` | `signupToken` |
| 4 | `POST /api/auth/phone-verifications/{verificationId}/confirm` | `signupToken` |
| 5 | `POST /api/auth/signup` | `signupToken` |

1번 응답의 `status`가 `REGISTRATION_REQUIRED`이면 미가입 상태이고 `signupToken`이 함께 옵니다. `REGISTERED`이면 이미 가입된 계정이라 액세스 토큰이 바로 내려옵니다.

4번의 인증번호는 `docker/.env`의 `AUTH_SMS_LOCAL_FIXED_OTP` 값(기본 `123456`)입니다.

> 이미 가입된 구글 계정으로 다시 가입 흐름을 보려면 로컬 DB에서 해당 계정을 지우거나, `./docker/scripts/start.sh`로 재시작해 스키마를 초기화합니다.

## 중지

```bash
./docker/scripts/stop.sh
```

서버와 MySQL, Redis 컨테이너를 삭제하지 않고 중지합니다. 전용 네트워크와 데이터 볼륨도 유지됩니다.

## 문제가 생겼을 때

### Docker가 실행 중이 아니라는 메시지가 나오는 경우

Docker Desktop을 실행한 뒤 `./docker/scripts/start.sh`를 다시 실행합니다.

### 환경변수를 처음부터 다시 만들고 싶은 경우

```bash
rm docker/.env
./docker/scripts/start.sh
```

`docker/.env.example`을 다시 복사하고 키를 새로 생성합니다. JWT 키가 바뀌므로 기존에 발급받은 토큰은 무효가 됩니다.

### 서버가 기동하다가 종료되는 경우

`docker/.env`에 값이 비어 있을 수 있습니다. 아래 네 값이 모두 채워져 있어야 합니다.

```bash
grep -cE '^[A-Z0-9_]+=$' docker/.env    # 0이어야 정상
```

값이 비어 있으면 `docker/.env`를 지우고 다시 실행하세요.

### 컨테이너 상태 확인

```bash
docker compose -f docker/compose.local.yml ps
```

### 서버 로그 확인

```bash
docker compose -f docker/compose.local.yml logs -f app
```

### MySQL 로그 확인

```bash
docker compose -f docker/compose.local.yml logs -f mysql
```

### Redis 로그 확인

```bash
docker compose -f docker/compose.local.yml logs -f redis
```

로그 화면은 `Ctrl+C`로 종료할 수 있으며, 컨테이너는 계속 실행됩니다.
