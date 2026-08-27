# ADR-0021: 테스트 컨테이너는 JVM 단위로 공유하고 DB 상태는 Context 단위로 격리한다

## Status

Accepted (2026-08-27)

## Context

- [ADR-0003](0003-use-testcontainers-for-mysql-tests.md)에 따라 Repository와 스키마 테스트는 MySQL 8.4 Testcontainer에서 실행해요.
- 기존에는 Spring Context마다 MySQL과 Redis 컨테이너를 생성했어요.
- 이전 전체 테스트 실행에서는 6분이 지나도 테스트가 끝나지 않았고 MySQL Testcontainer 21개와 Redis Testcontainer 5개까지 증가했어요. 결국 일부 테스트용 MySQL과 로컬 `be-mysql`이 exit code 137로 종료됐으며, 컨테이너 증가와 메모리 사용량을 근거로 메모리 압박에 의한 종료로 판단했어요.
- 같은 문제가 반복되는 것을 막기 위한 기준 측정에서도 64초 만에 MySQL 9개와 Redis 4개가 동시에 실행되고 Testcontainer 메모리가 3,704.4MiB까지 증가해 테스트를 중단해야만 했어요.
- 개선 전 CI의 `test` job은 922개 테스트를 실행하는 데 7분 31초가 걸렸어요.
- 컨테이너 수와 실행 시간을 줄이더라도 MySQL 8.4·Redis 기반 검증과 Spring Context 간 상태 격리를 유지해야 해요.

## Decision

MySQL 8.4와 Redis 컨테이너 프로세스는 테스트 JVM에서 각각 하나만 생성해 공유해요.

MySQL 상태는 컨테이너와 별도로 다뤄요. Spring Context마다 같은 MySQL 프로세스 안에 별도의 논리 데이터베이스를 생성해 스키마와 테스트 데이터를 격리해요.

동일한 Data JPA Bean 구성과 고정 Clock은 공통 설정으로 통합해 Spring Context를 재사용하되, 서로 다른 테스트 범위를 억지로 하나의 전체 Context로 합치지 않아요. DDL을 다시 실행하는 파괴적인 테스트는 전용 Context와 논리 데이터베이스를 사용해요.

Context별 연결 풀이 하나의 MySQL 연결 한도를 함께 사용하므로 테스트용 Hikari Pool은 최대 4개 연결, 최소 유휴 연결 0개로 제한해요.

## Alternatives

- **Spring Context마다 Testcontainer 생성** — 테스트 간 격리는 단순하지만 컨테이너와 메모리가 누적되고 전체 테스트와 CI가 느려져요.
- **모든 Context가 하나의 논리 DB까지 공유** — 데이터베이스 생성 비용은 줄지만 DDL과 시드 변경이 다른 테스트에 전파돼 실행 순서에 따라 실패할 수 있어요.
- **모든 테스트를 하나의 `@SpringBootTest` Context로 통합** — Context 수가 줄어들 것으로 예상했지만 Mockito Bean과 Clock 구성이 캐시 키를 나눴고 A/B 측정에서도 성능이 개선되지 않았어요.
- **Listener로 매 테스트 전 DB 초기화** — 하나의 DB를 공유할 수 있지만 DDL과 시드를 반복해서 복원해야 해 실행 비용과 초기화 책임이 커져요.

## Consequences

- (+) 메모리 압박으로 완료되지 않던 로컬 전체 테스트 864개가 3회 모두 42~53초에 통과했어요.
- (+) MySQL과 Redis 컨테이너가 각각 1개로 유지되고, Testcontainer 최대 메모리가 3,704.4MiB에서 중앙값 587.1MiB로 84.2% 감소했어요.
- (+) [PR #157](https://github.com/woowacourse-teams/2026-classitda/pull/157)의 CI `test` job이 7분 31초에서 3분 1초로 59.9% 단축됐어요.
- (+) MySQL 8.4 기반 검증과 Spring Context 간 DB 상태 격리를 유지해요.
- (-) 테스트 JVM을 병렬로 늘리면 JVM마다 MySQL과 Redis 컨테이너가 하나씩 추가돼요.
- (-) Spring Context마다 논리 DB와 스키마를 초기화하는 비용은 남아요.
- (-) 공유 MySQL의 전체 연결 수와 공유 Redis의 키 정리를 관리해야 해요.
- 재검토: 테스트 JVM 병렬화로 컨테이너나 MySQL 연결이 다시 누적되면 공유 범위를 다시 판단해요.

## Compliance

- MySQL 테스트는 MySQL 8.4를 사용하고 `EntitySchemaValidationTest`를 유지해요.
- 전체 테스트에서 MySQL과 Redis Testcontainer가 테스트 JVM당 각각 하나만 생성되는지 확인해요.
- 전체 테스트가 실행 순서와 관계없이 통과하는지 확인해요.
- 테스트 종료 후 Testcontainer와 Ryuk이 남지 않는지 확인해요.
- 테스트 실행이 로컬 `be-mysql`, `be-redis`에 영향을 주지 않는지 확인해요.
