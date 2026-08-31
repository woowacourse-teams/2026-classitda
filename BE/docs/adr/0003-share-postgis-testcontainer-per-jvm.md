# ADR-0003: PostGIS 테스트 컨테이너와 Data JPA Context를 JVM 단위로 공유한다

## Status

Accepted (2026-08-31)

## Context

- Service와 공간 쿼리는 PostgreSQL 17·PostGIS 3.5 Testcontainer에서 검증해야 해요.
- 기존 PostGIS 통합 테스트 2개는 각각 컨테이너와 Spring Data JPA Context를 생성했어요.
- 동일한 전체 테스트 명령을 실행하면 PostGIS 컨테이너와 Hikari Pool이 각각 2개 생성됐고 25.87초가 걸렸어요.
- 실제 PostGIS 검증과 테스트 간 데이터 정리는 유지하면서 반복되는 시작 비용을 줄여야 해요.

## Decision

PostGIS 컨테이너 프로세스는 정적 홀더를 사용해 테스트 JVM에서 하나만 생성하고 공유해요.

PostGIS Data JPA 테스트는 공통 `@PostgisDataJpaTest`를 사용해 동일한 Bean 구성과 Spring Context를 재사용해요. 현재는 하나의 논리 데이터베이스도 공유하므로 각 테스트가 자신이 변경한 데이터와 DDL을 원래 상태로 복구해요.

서로 다른 테스트 범위를 하나의 `@SpringBootTest`로 합치지는 않아요.

## Alternatives

- **테스트 클래스마다 컨테이너 생성** — 테스트 격리는 단순하지만 같은 이미지와 스키마를 반복해서 시작해요.
- **컨테이너만 공유하고 Context는 분리** — 컨테이너 시작 비용은 줄지만 같은 Data JPA 구성을 반복해서 초기화해요.
- **Context마다 논리 데이터베이스 생성** — DDL 격리는 강화되지만 현재처럼 같은 Context를 사용하는 소수 테스트에는 데이터베이스 생성과 마이그레이션 비용이 더 커요.
- **모든 테스트를 하나의 `@SpringBootTest`로 통합** — 슬라이스 격리를 잃고 불필요한 Bean 초기화 범위가 커져요.

## Consequences

- (+) 같은 전체 테스트 명령 1회 기준 실행 시간이 25.87초에서 15.90초로 38.5% 단축됐어요.
- (+) PostGIS 컨테이너와 Hikari Pool이 각각 2개에서 1개로 줄었어요.
- (+) PostgreSQL 17·PostGIS 3.5 기반 검증과 Data JPA 슬라이스 테스트를 유지해요.
- (-) 공유 데이터베이스의 상태를 정리하지 않으면 다른 테스트에 영향을 줄 수 있어요.
- (-) 테스트 JVM을 여러 개 실행하면 JVM마다 컨테이너가 하나씩 생성돼요.
- 재검토: 테스트 병렬 실행, 파괴적인 DDL 테스트 또는 서로 다른 Data JPA Bean 구성이 늘어나면 Context별 논리 데이터베이스 격리를 검토해요.

## Compliance

- 전체 테스트에서 PostGIS Testcontainer와 Hikari Pool이 각각 하나만 생성되는지 확인해요.
- 전체 테스트가 공유 상태를 정리하며 통과하는지 확인해요.
- 데이터나 DDL을 변경하는 테스트는 종료 전에 원래 상태로 복구해요.
