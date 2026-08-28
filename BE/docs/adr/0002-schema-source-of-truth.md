# ADR-0002: `schema.sql`을 DDL 원본으로 사용한다

## Status

Superseded by ADR-0023 (2026-08-28)

## Context

- 활성 상태의 행에만 유일성을 적용하려면 MySQL 생성 컬럼과 유니크 제약을 함께 사용해야 해요.
- 생성 컬럼처럼 JPA 애노테이션으로 정확히 표현할 수 없는 DB 제약이 있어요.
- 엔티티에서 DDL을 생성하면 이런 제약이 누락되거나 의도와 다르게 만들어질 수 있어요.
- DDL과 엔티티 매핑이 어긋나는 문제는 별도의 검증 수단이 필요해요.

## Decision

DDL의 원본은 `src/main/resources/schema.sql`로 두고, JPA 엔티티는 해당 스키마에 대한 매핑만 담당해요. Hibernate의 `ddl-auto`는 `validate`로 사용해 스키마를 생성하거나 변경하지 않아요.

## Alternatives

- JPA 엔티티와 `ddl-auto`로 스키마 생성 — 관리 지점은 줄지만 생성 컬럼을 포함한 DB 제약을 정확히 표현할 수 없어요.

## Consequences

- (+) JPA가 표현하지 못하는 DB 제약도 의도한 형태로 유지할 수 있어요.
- (+) 테이블·컬럼·인덱스·제약의 원본이 `schema.sql` 한 곳으로 정해져요.
- (-) 스키마가 바뀌면 `schema.sql`과 엔티티 매핑을 함께 수정해야 해요.
- (-) 생성 컬럼을 포함한 일부 DDL이 MySQL에 종속돼요.
- (=) `schema.sql`만으로는 운영 환경의 점진적인 변경 이력을 관리할 수 없어요.
- 재검토: 운영 배포에서 기존 데이터와 스키마를 점진적으로 변경해야 하는 시점에 Flyway 같은 마이그레이션 도구 도입을 다시 판단해요.

## Compliance

- `EntitySchemaValidationTest`에서 MySQL 8.4에 `schema.sql`을 적용한 뒤 `ddl-auto=validate`로 엔티티 매핑을 검증해요.
