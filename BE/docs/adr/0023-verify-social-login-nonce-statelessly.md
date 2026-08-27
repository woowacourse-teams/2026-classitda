# ADR-0023: Google·Apple 소셜 로그인 nonce를 Stateless 방식으로 검증한다

## Status

Accepted (2026-08-28)

## Context

- ADR-0022에서 Google·Apple nonce 도입을 후속 이슈로 유예했고, 두 제공자에 공통 계약으로 적용할 시점이 됐어요.
- ID Token의 서명·issuer·audience·만료를 검증해도, 탈취된 ID Token 단독을 다른 로그인 시도에 대입하는 공격과 현재 로그인 시도의 관계는 검증할 수 없어요.
- OIDC `nonce`는 로그인 시도와 ID Token을 연결해 ID Token 단독 탈취와 다른 로그인 흐름에 대한 토큰 주입을 완화해요.
- 동일 요청의 재사용까지 차단하려면 서버가 nonce를 발급·저장·소비하거나 별도의 요청 소유 증명을 도입해야 해요.

## Decision

클라이언트가 로그인 시도마다 암호학적으로 안전한 32바이트 난수를 Base64 URL-safe, padding 없는 43자 `rawNonce`로 만들어요. 제공자에는 `SHA-256(rawNonce)`의 소문자 16진수를, 백엔드에는 `idToken + rawNonce`를 전달해요.

백엔드는 nonce를 저장하지 않고, 제공자별 ID Token 서명과 Claim을 검증한 뒤 `SHA-256(rawNonce)`와 검증된 `nonce` Claim을 안전한 바이트 비교로 확인해요. nonce가 누락되거나 일치하지 않으면 Signup Token과 Access·Refresh Token을 발급하지 않아요.

## Alternatives

- **백엔드가 nonce Challenge를 발급하고 Redis에서 일회성으로 소비** — 재사용을 감지할 수 있지만 추가 API, 저장소, 만료·소비 정합성을 관리해야 해요.
- **클라이언트 발급 nonce를 받고 사용 이력만 서버에 저장** — 동일 nonce 재사용을 감지하지만 서버 상태와 정리 정책이 필요해요.
- **nonce를 검증하지 않음** — 구현은 단순하지만 ID Token을 현재 로그인 시도와 연결할 수 없어요.

## Consequences

- (+) ID Token 단독 탈취와 다른 로그인 흐름에 대한 토큰 주입을 완화해요.
- (+) Challenge API와 nonce 저장소 없이 Google·Apple에 같은 검증 규칙을 적용해요.
- (-) `idToken + rawNonce` 전체가 함께 탈취된 경우 동일 요청의 재전송은 막지 못해요.
- (-) 클라이언트는 로그인 재시도를 포함한 매 시도마다 새로운 `rawNonce`를 생성해야 해요.
- (=) HTTPS와 제공자별 서명·issuer·audience·만료 검증은 nonce와 독립적으로 계속 필수예요.
- 재검토: `idToken + rawNonce` 전체 재전송을 차단해야 하는 위협 모델이 확정되면 Stateful nonce 소비나 기기 키 기반 요청 서명을 다시 검토해요.

## Compliance

- Google·Apple ID Token의 `nonce` Claim 누락·불일치를 기존 제공자별 ID Token 인증 오류로 거부해요.
- ID Token, `rawNonce`, 해시 결과를 로그나 영속 저장소에 남기지 않아요.
- 요청 형식, 제공자별 Claim, nonce 일치·불일치, 토큰 미발급을 자동화 테스트로 검증해요.
