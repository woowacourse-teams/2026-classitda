# ADR-0003: Repository 테스트에 Testcontainers로 MySQL 8.4를 사용한다

## Status

Accepted (2026-08-08)

## Context

- Repository 테스트는 쿼리, 엔티티 매핑, DB 제약이 의도대로 동작하는지 검증해야 해요.
- 프로젝트는 생성 컬럼과 유니크 제약 등 MySQL 전용 동작을 사용해요.
- H2는 이런 동작을 MySQL과 동일하게 지원하지 않으므로 테스트가 통과해도 실제 DB에서는 실패할 수 있어요.
- 개발자와 CI가 같은 DB 엔진과 버전에서 반복해서 테스트할 수 있는 환경이 필요해요.

## Decision

Repository와 스키마 검증 테스트는 Testcontainers가 제공하는 MySQL 8.4에서 실행해요.

실제 MySQL을 격리된 테스트 환경에서 실행함으로써 운영 DB와 다른 동작을 검증하는 문제를 줄여요.

## Alternatives

- H2 기반 테스트 — 실행이 빠르고 간단하지만 MySQL 전용 DDL과 제약을 동일하게 검증할 수 없어요.

## Consequences

- (+) 운영과 같은 DB 엔진과 버전에서 쿼리·매핑·제약을 검증할 수 있어요.
- (+) 개발자와 CI가 동일한 DB 환경을 재현할 수 있어요.
- (-) 컨테이너를 시작해야 하므로 테스트 실행 시간이 늘어나요.
- (-) 로컬과 CI 환경에서 Docker를 사용할 수 있어야 해요.
- (=) Docker를 사용할 수 없는 환경에서는 해당 테스트를 실행할 수 없어요.
- 재검토: 운영 DB 또는 테스트 실행 환경이 바뀌어 현재 방식의 비용이 신뢰성보다 커지면 다시 판단해요.

## Compliance

- Repository와 스키마 검증 테스트를 H2로 대체하지 않아요.
- `EntitySchemaValidationTest`도 MySQL 8.4에서 실행해요.
