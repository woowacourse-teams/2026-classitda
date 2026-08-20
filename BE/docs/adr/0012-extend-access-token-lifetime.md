# ADR-0012: Access Token을 1시간 stateless JWT로 발급한다

## Status

Accepted (2026-08-20)

## Context

- ADR-0005는 Access Token을 15분 수명의 stateless JWT로 정했어요.
- 비즈니스 정책이 Access Token 수명을 1시간으로 변경했어요.
- stateless Access Token은 만료되기 전까지 서버에서 즉시 폐기할 수 없어요.

## Decision

Access Token은 1시간 수명의 stateless JWT로 발급하고 일반 API 요청에서는 Redis를 조회하지 않아요.
Refresh Token은 기존과 같이 Redis의 Refresh Session과 연결하고 회전·폐기해요.

## Alternatives

- Access Token 15분 유지 — 변경된 비즈니스 정책과 맞지 않아요.
- Access Token blacklist 도입 — 모든 인증 요청에 상태 조회와 blacklist 수명 관리가 추가돼요.

## Consequences

- (+) Access Token 수명이 변경된 비즈니스 정책과 일치해요.
- (=) 일반 API의 stateless 검증과 Refresh Session의 Redis 관리 방식은 유지돼요.
- (-) 로그아웃 후 남은 토큰이나 탈취된 토큰을 사용할 수 있는 최대 시간이 15분에서 1시간으로 늘어나요.
- 재검토: 즉시 로그아웃 보장이나 계정 탈취 대응이 필요해지면 blacklist나 토큰 버전 전략을 다시 판단해요.

## Compliance

- 발급된 Access Token의 `exp - iat`는 1시간이어야 해요.
- API 응답의 `accessTokenExpiresIn`은 3,600초여야 해요.
