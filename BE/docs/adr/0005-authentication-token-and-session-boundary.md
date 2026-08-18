# ADR-0005: Access Token은 stateless로, Refresh Session은 Redis에서 관리한다

## Status

Accepted (2026-08-12)

## Context

- Access Token은 대부분의 API 요청에서 반복해서 검증돼요.
- 모든 요청에서 서버 상태를 조회하면 인증이 Redis 가용성과 조회 비용에 의존하게 돼요.
- Refresh Token은 재사용 방지, 회전, 로그아웃처럼 서버가 즉시 바꿔야 하는 상태가 필요해요.
- stateless JWT는 만료되기 전까지 서버에서 즉시 폐기할 수 없어요.

## Decision

Access Token은 15분 수명의 stateless JWT로 발급하고, 일반 API 요청에서는 Redis를 조회하지 않아요. Access Token blacklist도 도입하지 않아요.

Refresh Token은 Redis의 Refresh Session과 연결하고, 갱신할 때 기존 Session을 폐기한 뒤 새 Session으로 회전해요. 로그아웃할 때는 현재 기기의 Refresh Session만 폐기해요.

## Alternatives

- Access Token blacklist 도입 — 로그아웃 즉시 폐기할 수 있지만 모든 인증 요청에 상태 조회와 blacklist 수명 관리가 추가돼요.
- Access Token과 Refresh Token을 모두 stateless로 관리 — 서버 상태 조회는 없지만 Refresh Token 재사용을 즉시 차단하기 어려워요.

## Consequences

- (+) 일반 API 요청은 Redis에 의존하지 않고 Access Token을 검증할 수 있어요.
- (+) Refresh Token은 서버에서 폐기하고 재사용을 차단할 수 있어요.
- (=) 클라이언트는 로그아웃할 때 로컬에 저장된 Access Token과 Refresh Token을 삭제해야 해요.
- (-) 서버는 기존 Access Token을 즉시 폐기하지 않으므로, 클라이언트의 로컬 삭제가 실패하거나 토큰 사본이 탈취된 경우 로그아웃 후에도 최대 15분 동안 인증에 사용할 수 있어요.
- (-) Redis 장애가 토큰 갱신과 로그아웃에 영향을 줘요.
- 재검토: 즉시 로그아웃 보장, 계정 탈취 대응 또는 전체 기기 로그아웃이 필요해지면 blacklist나 토큰 버전 전략을 다시 판단해요.

## Compliance

- 폐기되거나 이미 회전된 Refresh Token으로 새 Access Token을 발급할 수 없어야 해요.
- 같은 Refresh Token의 동시 갱신 요청은 하나만 성공해야 해요.
