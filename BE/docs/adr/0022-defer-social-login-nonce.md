# ADR-0022: Google·Apple 소셜 로그인 nonce 적용을 함께 유예한다

## Status

Superseded by ADR-0023 (2026-08-28)

## Context

- 현재 Google 로그인은 클라이언트가 ID Token만 전달하고, 서버가 서명·issuer·audience·만료와 사용자 식별 정보를 검증해요.
- Apple 로그인도 Authorization Code와 Apple Access·Refresh Token을 사용하지 않고 Identity Token만 검증하는 범위로 구현해요.
- Google과 Apple은 모두 OIDC `nonce`로 로그인 요청과 ID Token을 연결해 ID Token만 탈취하거나 다른 로그인 시도에 대입하는 공격을 완화할 수 있어요.
- Apple에만 `rawNonce`를 추가하면 두 제공자의 클라이언트 요청과 서버 검증 계약이 달라지고, 현재 Google 로그인과 공통 처리할 범위가 줄어들어요.
- 두 제공자에 nonce를 도입하려면 클라이언트의 nonce 생성·전달과 서버의 검증 계약을 함께 변경해야 해요.

## Decision

Apple 로그인 최초 구현에서는 현재 Google 로그인과 동일하게 ID Token만 받아 검증하고 `rawNonce`를 요구하지 않아요.

nonce를 영구적으로 생략하지 않고 별도 후속 이슈에서 Google과 Apple에 함께 도입해요. 이때 제공자별 JWT 검증은 분리해서 유지하고, `rawNonce` 해시와 token `nonce` claim 비교만 공통화해요.

## Alternatives

- **Apple에만 nonce를 즉시 적용** — Apple ID Token만 탈취하거나 다른 로그인 시도에 대입하는 공격은 먼저 완화하지만 제공자별 요청 계약과 검증 흐름이 달라져요.
- **현재 이슈에서 Google과 Apple에 모두 적용** — 두 제공자를 동시에 보호할 수 있지만 기존 Google 클라이언트 계약까지 변경해 Apple 로그인 이슈의 범위가 커져요.
- **두 제공자 모두 nonce를 계속 사용하지 않음** — 구현은 단순하지만 알고 있는 재전송 위험을 해소할 후속 경로가 없어져요.

## Consequences

- (+) Apple 로그인이 현재 Google 로그인과 같은 `idToken` 중심 계약을 사용해 공통 로그인 처리 흐름을 최대한 재사용해요.
- (+) nonce 계약과 공통 검증 책임을 두 제공자에 한 번에 설계할 수 있어요.
- (-) 후속 도입 전까지 탈취된 유효 ID Token이 만료 전에 재전송될 위험을 감수해요.
- (-) 모바일 앱 배포 후 `rawNonce`를 필수화하면 기존 앱과 호환되지 않으므로 클라이언트와 서버의 단계적 배포 또는 새 API 계약이 필요해요.
- (-) 후속 클라이언트 nonce 도입 후에도 `idToken + rawNonce` 전체 요청이 함께 탈취되면 서버 상태 없이 동일 요청의 재전송까지 차단할 수는 없어요.
- (=) HTTPS 전송과 제공자별 서명·issuer·audience·만료 검증은 계속 필수예요. 이는 nonce를 대체하지 않아요.
- 재검토: Google·Apple nonce 후속 이슈를 시작할 때 클라이언트 배포 상태를 확인하고 즉시 전환 또는 버전이 분리된 계약 중 하나를 선택해요.

## Compliance

- Apple과 Google ID Token은 서명·issuer·audience·만료를 검증한 뒤 사용자 식별에 사용해야 해요.
- ID Token을 로그나 영속 저장소에 남기지 않아야 해요.
- 후속 이슈에서는 로그인 시도마다 새로운 `rawNonce`를 생성하고 두 제공자 모두 nonce 일치·불일치·누락과 로그인 재시도 시 새 nonce 생성을 검증해야 해요.
