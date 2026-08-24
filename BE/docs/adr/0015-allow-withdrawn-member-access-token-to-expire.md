# ADR-0015: 회원 탈퇴 시 기존 Access Token을 자연 만료시킨다

## Status

Accepted (2026-08-24)

## Context

- 회원이 탈퇴하면 새로운 로그인과 Refresh Token 재발급은 즉시 차단해야 해요.
- 탈퇴 요청 후 개인정보는 7일간 보관한 뒤 정리해요.
- ADR-0012에 따라 Access Token은 일반 API 요청에서 서버 상태를 조회하지 않는 1시간 수명의 stateless JWT예요.
- 따라서 탈퇴 전에 발급한 Access Token은 별도의 폐기 장치를 추가하지 않으면 만료 전까지 사용할 수 있어요.

## Decision

회원 탈퇴 시 로그인과 Refresh Token 재발급은 즉시 차단하지만, 이미 발급한 Access Token은 blacklist를 도입하지 않고 만료 시점까지 유효하게 유지해요.
일반 API 요청의 stateless 검증을 유지하고 최대 1시간의 잔여 유효 시간을 감수해요.

## Alternatives

- Access Token blacklist 도입 — 모든 인증 요청에 상태 조회와 blacklist 수명 관리가 추가되어 현재의 stateless 검증 방식을 변경해야 해요.
- 탈퇴 회원 상태를 매 요청마다 조회 — 회원 DB 조회가 일반 인증 경로에 추가되고 Access Token의 stateless 경계가 사라져요.

## Consequences

- (+) 일반 API 요청에서 Redis나 회원 DB를 조회하지 않는 인증 경계를 유지해요.
- (=) 로그인과 Refresh Token 재발급은 회원 상태를 확인하므로 탈퇴 즉시 차단돼요.
- (-) 탈퇴 전에 발급한 Access Token은 탈퇴 후에도 최대 1시간 동안 사용할 수 있어요.
- 재검토: 탈퇴 즉시 모든 접근을 차단해야 하거나 계정 탈취 대응이 필요해지면 blacklist나 토큰 버전 전략을 다시 판단해요.

## Compliance

- 탈퇴 회원의 로그인과 Refresh Token 재발급은 거부되어야 해요.
- 탈퇴 전에 발급한 Access Token은 별도의 서버 상태 조회 없이 만료 시점까지만 유효해야 해요.
