# ADR-0009. 인증 토큰의 용도를 분리하고, 서버 상태는 Redis 세션으로 관리해요

- 상태: 채택 (2026-08-12)
- 후보: 서버 HTTP 세션 한 종류, 모든 인증 상태를 JWT로만 표현, 용도별 JWT와 Redis 세션을 조합
- 선택: RS256 기반 SIGNUP·ACCESS JWT + Redis 가입·휴대전화·Refresh Session
- 관련: [ADR-0003](0003-schema-source-of-truth.md), [ADR-0006](0006-layer-contract.md), [ADR-0007](0007-test-strategy.md), [Swagger Google 로그인 가이드](../guides/swagger-google-login.md)

---

## 결정 범위

이번 결정은 다음 인증 흐름을 함께 다뤄요.

- Google ID Token을 검증하고 기존 회원과 가입 필요 사용자를 구분하는 로그인
- 가입 절차에서만 사용하는 Signup Token과 Redis 가입 세션
- 휴대전화 인증번호 발송·확인과 일회성 인증 완료 상태
- Access Token과 Refresh Token 발급, Refresh Session 회전
- 현재 기기 로그아웃과 인증된 회원 ID 주입
- 로컬 Swagger에서 사용할 개발 전용 토큰 발급 경계

회원의 프로필과 서비스 활동은 `member`가 소유하고, 외부 인증 제공자의 식별 정보는
`authentication` 모듈의 `auth_account`가 소유해요. `auth_account`는 `member_id`로 회원을
참조하고, `(provider, provider_subject)`를 유일하게 유지해요.

## 🤔 선택 근거

### 가입 중인 사용자와 가입 완료 회원은 권한이 달라요

Google이 신원을 확인했다고 해서 Classitda 회원가입이 끝난 것은 아니에요. 필수 약관 동의와
휴대전화 인증이 남아 있으므로, 가입 전 사용자에게 일반 회원용 Access Token을 발급하면 안 돼요.

그래서 JWT의 `token_use` claim으로 용도를 나눠요.

| 용도 | subject | authority | TTL | 허용 범위 |
| --- | --- | --- | --- | --- |
| `SIGNUP` | 가입 세션 JTI | `SIGNUP` | 30분 | 약관 조회, 휴대전화 인증, 가입 완료 |
| `ACCESS` | 회원 ID | `MEMBER` | 15분 | 가입 완료 회원 API |

두 토큰 모두 issuer `classitda`, RS256 서명, 발급·만료 시각, JTI를 가져요. Signup Token은
`jti`와 `sub`가 같아야 하고, 같은 JTI의 Redis 가입 세션이 살아 있을 때만 유효해요.
Access Token의 subject는 회원 ID의 문자열 표현으로 고정해요.

### JWT만으로는 일회성 상태와 즉시 폐기를 표현하기 어려워요

가입 세션, 휴대전화 인증, Refresh Session에는 다음 상태 전이가 필요해요.

- 가입 완료 후 같은 Signup Token을 다시 사용하지 못하게 하기
- 인증번호 재발송 간격과 실패 횟수를 제한하기
- Refresh Token을 한 번 사용하면 이전 토큰을 즉시 무효화하기
- 현재 기기 로그아웃 시 해당 Refresh Session만 폐기하기

이 상태를 JWT에만 넣으면 서명과 만료가 유효한 동안 서버가 즉시 바꿀 수 없어요. 반대로 모든
API 요청을 서버 세션 조회로 처리하면 Access Token을 짧게 쓰는 장점이 줄어요. 자주 검증되는
Access Token은 짧은 수명의 JWT로 두고, 상태 전이가 필요한 일회성 정보만 Redis에 저장해요.

### Refresh Token 원문을 서버에 저장하지 않아요

Refresh Token은 32바이트 난수 두 개를 URL-safe Base64로 인코딩해
`{sessionId}.{secret}` 형태로 발급해요. Redis 키 조회에는 `sessionId`를 사용하고, 값에는 토큰
원문 대신 SHA-256 해시, 회원 ID, 만료 시각만 저장해요. 제출된 토큰의 해시 비교는
상수 시간 비교를 사용해요.

Redis 데이터가 노출돼도 저장된 값만으로 Refresh Token 원문을 바로 사용할 수 없고, 한 회원이
여러 기기에서 로그인하더라도 각 Refresh Token이 서로 다른 세션을 가져요.

HTTP 요청 DTO는 잘못된 형식을 서비스 호출 전에 `400 Bad Request`로 거절하고, 애플리케이션의
Refresh Token verifier도 같은 형식 제약을 검사해 HTTP 밖의 호출에서도 토큰 경계를 지켜요.
두 검사는 실패 응답을 만드는 presentation 책임과 토큰을 해석하는 application 책임이 달라요.

### 회전과 폐기는 Redis Lua로 원자적으로 처리해요

Refresh Token 갱신은 기존 세션을 읽고 새 세션을 저장하는 두 명령으로 나누면, 같은 토큰을
동시에 제출한 요청이 모두 성공할 수 있어요. Lua 스크립트 한 번으로 다음을 함께 처리해요.

1. 기존 세션이 예상한 값과 같고 만료되지 않았는지 확인해요.
2. 새 세션 ID가 아직 없는지 확인해요.
3. 기존 세션을 삭제하고 새 세션과 TTL을 저장해요.

따라서 같은 Refresh Token의 동시 갱신 요청 중 하나만 회전에 성공해요. 로그아웃도 읽은 세션과
현재 Redis 값이 같을 때만 삭제하는 compare-and-delete를 사용해, 갱신과 로그아웃이 경합할 때
새 세션이나 다른 세션을 잘못 지우지 않아요.

### 로그아웃은 현재 기기의 Refresh Session만 폐기해요

전체 기기 로그아웃은 이번 범위에서 제외해요. 로그아웃 요청의 Access Token은 요청한 회원을
식별하고, 요청 본문의 Refresh Token은 폐기할 현재 기기 세션을 식별해요.

형식 검증을 통과한 Refresh Token에 대해 세션이 없거나, 만료됐거나, 다른 회원의 세션이거나,
해시가 일치하지 않아도 다른 세션은 건드리지 않고 `204 No Content`로 처리해요. 이 응답으로
Refresh Session의 존재 여부나 소유자를 추측할 수 없게 해요. 일치하는 세션만 원자적으로
삭제하고, 이미 사라진 세션의 로그아웃은 멱등하게 완료된 것으로 봐요.

Access Token blacklist는 이번 결정에서 도입하지 않아요. 클라이언트는 서버 응답의 성공·실패와
무관하게 로컬 Access Token, Refresh Token, 기기 민감정보를 삭제해요. 서버는 Refresh Session만
폐기하고, 이미 발급된 Access Token은 최대 15분 뒤 자연 만료돼요.

### 인증된 회원 ID 해석은 presentation 경계에서 한 번만 해요

각 컨트롤러가 `Jwt.getSubject()`를 반복해서 파싱하면 타입 검사, 1 이상의 값 검사, 오류 처리가
흩어져요. `@CurrentMemberId Long memberId`와 전용 Argument Resolver가 인증된 ACCESS JWT의
subject를 양의 `Long`으로 변환해요.

이 구성은 HTTP 요청의 인증 주체를 컨트롤러 인자로 바꾸는 웹 계층 책임이므로
`authentication.presentation` 아래에 둬요. 다른 모듈은 필요할 때 애노테이션을 컨트롤러
매개변수에 선언할 수 있지만, 인증 모듈이 다른 모듈의 컨트롤러를 선제적으로 수정하지 않아요.

### 외부 호출과 Redis 흐름 때문에 DB 트랜잭션을 길게 열지 않아요

Google 검증, Redis 세션 처리, SMS 발송이 포함된 서비스에는 흐름 전체를 감싸는 DB 트랜잭션을
두지 않아요. DB 쓰기 원자성이 필요한 가입 계정 생성만 `SignupAccountCreator`의 트랜잭션으로
분리해 회원, 인증 계정, 약관 동의를 함께 저장해요.

가입 경쟁에서 DB 유니크 제약이 발생하면 이미 생성된 소셜 계정을 다시 조회해 같은 회원으로
수렴하고, 휴대전화 번호 중복은 명시적인 인증 오류로 변환해요.

## 인증 흐름 계약

### Google 로그인과 가입

1. 서버가 Web Client ID, Google 서명과 claim, 이메일 인증 여부를 검증해요.
2. `(GOOGLE, providerSubject)`에 대응하는 `auth_account`가 있으면 Access Token과 Refresh Token을 발급해요.
3. 계정이 없으면 Signup Token과 Redis 가입 세션을 발급해요.
4. Signup Token으로 약관 조회, 휴대전화 인증, 가입 완료를 수행해요.
5. 가입 완료 시 회원·인증 계정·약관 동의를 하나의 DB 트랜잭션으로 저장해요.
6. 로그인 토큰 발급을 시도한 뒤 가입 세션과 인증 완료 휴대전화 상태를 정리해요.

### 휴대전화 인증

| 상태 | 값 |
| --- | --- |
| 인증번호 유효 시간 | 3분 |
| 재발송 대기 시간 | 1분 |
| 최대 실패 횟수 | 5회 |
| 인증 완료 휴대전화 유효 시간 | 30분 |

- 인증번호와 휴대전화 키는 HMAC-SHA256으로 파생하고, Redis에 인증번호 원문을 저장하지 않아요.
- 상태 키에는 Signup JTI를 포함하거나 값으로 검증해 다른 가입 세션이 인증을 가져가지 못하게 해요.
- 재발송 상태 저장, 실패 횟수 증가, 성공 처리와 기존 상태 삭제는 Lua로 원자적으로 처리해요.
- SMS 발송이 실패하면 방금 만든 활성 인증 상태를 compare-and-delete로 정리해요.
- local 프로필에서는 숫자 6자리 고정 인증번호와 no-op sender를 사용할 수 있어요.
- non-local 프로필에 고정 인증번호가 설정되면 애플리케이션 기동을 거부해요.
- 실제 SMS 연동 전 non-local sender는 성공으로 위장하지 않고 명시적인 `PHONE-007` 오류를 반환해요.

### Refresh와 로그아웃

- Refresh Token 갱신 성공 시 Access Token과 Refresh Token을 모두 새로 발급해요.
- 이전 Refresh Session은 회전과 동시에 삭제하며 재사용할 수 없어요.
- 세션 불일치와 이미 회전된 토큰은 같은 `AUTH-008` 응답으로 처리해 내부 상태를 구분해 주지 않아요.
- 로그아웃은 요청 회원과 제출된 Refresh Session이 모두 일치할 때만 현재 세션을 삭제해요.
- 로그아웃은 다른 기기의 Refresh Session을 검색하거나 삭제하지 않아요.
- 로그아웃 성공 또는 멱등 완료 응답은 본문 없는 `204 No Content`예요.
- Redis 연결·스크립트·직렬화 오류는 세션 없음으로 위장하지 않고 내부 오류로 처리해요.

## 인가 규칙

| 경로 | 요구 권한 |
| --- | --- |
| `POST /api/auth/google` | 익명 허용 |
| `POST /api/auth/tokens/refresh` | 익명 허용, Refresh Token 자체 검증 |
| `GET /api/terms` | `SIGNUP` |
| `POST /api/auth/phone-verifications` | `SIGNUP` |
| `POST /api/auth/phone-verifications/{id}/confirm` | `SIGNUP` |
| `POST /api/auth/signup` | `SIGNUP` |
| `POST /api/auth/logout` | `MEMBER` |

Spring Security는 stateless로 동작하고 form login, HTTP Basic, 기본 logout을 사용하지 않아요.
인증·인가 실패는 공통 `ErrorResponse(code, message)`로 반환하고 토큰, 개인식별값, 하위 예외
메시지를 응답에 포함하지 않아요.

현재 `SecurityConfig`의 `/api/** permitAll()`은 `be/dev` 통합 중 다른 모듈의 API를 막지 않기 위한
임시 호환 규칙이에요. 최종 인가 정책으로 채택한 것이 아니며, 각 API 작업자가 담당 경로의
권한을 명시한 뒤 제거해야 해요.

## 로컬 개발 경계

- `POST /api/auth/local/members/{memberId}/tokens`는 `local` 프로필에서만 컨트롤러가 생성돼요.
- Swagger 연동 편의를 위한 개발 도구이므로 회원 존재 여부를 조회하지 않고 입력 ID로 토큰을 발급해요.
- local SQL 초기화는 고정 테스트 회원과 Google 인증 계정을 넣어요.
- production 프로필은 로컬 컨트롤러와 seed data를 노출하지 않아요.

## 규칙

### 토큰과 세션

- SIGNUP JWT는 가입 절차에만, ACCESS JWT는 가입 완료 회원 API에만 사용해요.
- JWT 알고리즘은 RS256, issuer는 `classitda`로 고정해요.
- Signup Token은 30분, Access Token은 15분, Refresh Token과 Session은 30일로 고정해요.
- Refresh Token 원문은 서버 저장소에 저장하지 않아요.
- Refresh Session 회전과 조건부 삭제는 Redis Lua 한 번으로 원자적으로 처리해요.
- Redis가 반환한 결과 코드가 계약에 없는 값이거나 `null`이면 성공으로 간주하지 않아요.

### 가입과 휴대전화 인증

- 외부 인증 제공자의 계정 식별 정보는 `authentication` 모듈의 `auth_account`가 소유해요.
- Google ID Token의 audience, 서명·표준 claim, `email_verified`를 서버에서 검증해요.
- 회원·인증 계정·약관 동의의 DB 저장만 하나의 쓰기 트랜잭션으로 묶어요.
- OTP 원문을 Redis에 저장하지 않고 HMAC digest만 저장해요.
- 인증번호 확인 성공과 실패 횟수 증가는 원자적으로 처리해요.

### 로그아웃과 회원 식별

- 로그아웃은 현재 기기의 일치하는 Refresh Session 하나만 폐기해요.
- 전체 기기 로그아웃과 Access Token blacklist는 별도 결정 전까지 구현하지 않아요.
- 세션 없음·만료·소유자 불일치·해시 불일치는 다른 세션을 변경하지 않고 동일하게 `204` 처리해요.
- 클라이언트는 로그아웃 서버 응답과 무관하게 로컬 인증정보를 삭제해요.
- 컨트롤러에서 인증된 회원 ID가 필요하면 `@CurrentMemberId Long`을 사용해요.
- 인증 모듈은 다른 모듈의 컨트롤러에 `@CurrentMemberId`를 임의로 부착하지 않아요.

## 검토했지만 선택하지 않은 대안

| 대안 | 선택하지 않은 이유 |
| --- | --- |
| 모든 요청을 서버 HTTP 세션으로 인증 | API 요청마다 상태 저장소 조회가 필요하고 모바일 Bearer Token 흐름과 맞지 않아요 |
| Signup·Access·Refresh를 모두 JWT로 발급 | 일회성 가입 상태와 Refresh Token 재사용을 즉시 막기 어려워요 |
| Refresh Token 원문을 Redis에 저장 | Redis 노출 시 저장된 토큰을 그대로 사용할 수 있어요 |
| Refresh Session을 조회 후 별도 명령으로 교체 | 동시 갱신 요청이 모두 성공하는 경쟁 조건이 생겨요 |
| 로그아웃 시 회원의 모든 Refresh Session 삭제 | 현재 기기 로그아웃 요구를 넘고 다른 정상 기기까지 로그아웃시켜요 |
| Access Token blacklist를 즉시 도입 | 모든 인증 요청에 blacklist 조회가 추가되고 이번 현재 기기 로그아웃 범위를 키워요 |
| JWT subject를 컨트롤러마다 직접 파싱 | 형식·범위 검사와 오류 처리가 여러 모듈에 중복돼요 |

## 감수하는 점

- **로그아웃 직후에도 탈취된 Access Token은 최대 15분 동안 사용할 수 있어요.** 현재는 짧은 TTL로
  위험을 제한해요. 즉시 차단이나 전체 기기 로그아웃이 필요해지면 blacklist 또는 토큰 버전 전략을
  별도 ADR로 결정해요.
- **Redis 장애는 가입, 휴대전화 인증, 토큰 갱신, 로그아웃에 영향을 줘요.** 데이터베이스 연결을
  길게 점유하지 않는 대신 Redis를 인증 가용성의 핵심 의존성으로 받아들여요.
- **Refresh Token을 잃어버리면 해당 세션은 TTL까지 Redis에 남을 수 있어요.** 원문 없이는 사용할
  수 없고 30일 후 만료되지만, 운영상 강제 정리가 필요하면 별도 세션 관리 기능이 필요해요.
- **항상 같은 `204`를 반환하면 클라이언트는 서버 세션이 실제 삭제됐는지 구분할 수 없어요.**
  세션 존재 여부를 노출하지 않고 로그아웃을 멱등하게 만드는 쪽을 선택했어요.
- **로컬 토큰 발급 API는 실제 회원 존재를 검증하지 않아요.** `local` 프로필로만 제한하고 운영에서는
  컨트롤러와 seed data가 생성되지 않게 해 위험을 격리해요.
