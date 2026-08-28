# ADR-0023: Flyway로 스키마를 관리한다

## Status

Accepted (2026-08-28)

## Context

- [ADR-0002](0002-schema-source-of-truth.md)는 `schema.sql`을 DDL 원본으로 두고 JPA는 `ddl-auto: validate`로 매핑만 검증하도록 정했어요.
- [ADR-0007](0007-manual-production-schema-application.md)은 마이그레이션 도구를 보류하고 `schema.sql`을 사람이 운영 DB에 직접 적용하도록 정했어요. 당시에는 운영 데이터가 없고 스키마가 자주 흔들리는 단계였어요.
- 개발 배포는 매번 `reset-schema.sql` → `schema.sql` → `dev-data.sql`을 실행해 DB를 통째로 다시 만들었어요. 개발 서버가 운영 DB를 공유하고 있어서, 이 초기화가 운영 DB에도 적용됐어요.
- 프로덕션과 개발 환경을 분리하면서 개발 서버가 자체 MySQL을 갖게 됐어요. 앱을 새로 띄울 때마다 DB가 초기화되지 않기를 원해요.
- Spring Boot 4는 자동설정이 모듈로 나뉘어 있어, `flyway-core`만 넣으면 자동설정이 따라오지 않아요.

## Decision

스키마를 Flyway로 관리해요. `db/migration`을 전 환경 공통으로 두고, 개발·로컬 시드는 프로필별 `locations`로 분리해 운영에는 적용되지 않게 해요. 마이그레이션 버전은 `VYYMMDD_XX` 형식으로 붙여 작성한 날짜순으로 정렬되게 해요.

DDL을 사람이 쓰고 JPA가 `validate`로 검증하는 ADR-0002의 원칙은 그대로 유지하고, 원본의 위치만 `schema.sql`에서 마이그레이션 파일로 옮겨요.

## Alternatives

- **수동 적용 유지(ADR-0007)** — 환경이 둘로 늘어나면 사람이 두 DB에 같은 DDL을 순서대로 반영해야 하고, 무엇이 적용됐는지 DB에 남지 않아요.
- **배포마다 리셋 유지** — 개발 DB가 매번 비워져 확인하던 데이터가 남지 않고, 운영에는 애초에 쓸 수 없어요.

## Consequences

- (+) 환경마다 같은 마이그레이션이 같은 순서로 적용되고, 적용 이력이 `flyway_schema_history`에 남아요.
- (+) 개발 DB가 배포마다 초기화되지 않아요.
- (+) 운영 스키마를 사람이 손으로 반영하지 않아요.
- (-) 한 번 적용한 마이그레이션은 고칠 수 없어서, 잘못 넣으면 되돌리는 마이그레이션을 새로 써야 해요.
- (-) 시드가 한 번만 실행되므로 상대 날짜(`DATE_ADD(CURRENT_DATE, ...)`)를 쓴 개발 시드는 최초 적용 시점에 고정돼요.
- (-) `@DataJpaTest`는 Flyway 자동설정을 포함하지 않아, 테스트는 `SharedTestContainers`에서 마이그레이션을 직접 실행해요. 스키마를 만드는 경로가 애플리케이션과 테스트로 나뉘어요.
- (=) `spring-boot-flyway` 모듈을 함께 넣어야 자동설정이 등록돼요.
- 재검토: 운영 데이터가 쌓여 되돌리는 마이그레이션의 비용이 커질 때. 마이그레이션이 많아져 테스트마다 전부 실행하는 시간이 문제될 때.

## Compliance

- 빈 데이터베이스에 Flyway 자동설정만으로 스키마가 만들어지는지 `FlywayAutoConfigurationTest`가 검증해요.
- 적용된 버전이 `VYYMMDD_XX` 형식을 따르는지 같은 테스트가 검증해요.
- 개발·로컬 시드가 현재 스키마에서 실행되는지 `SeedScriptValidationTest`가 검증해요.
