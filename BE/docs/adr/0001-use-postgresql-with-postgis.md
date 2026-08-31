# ADR-0001: PostgreSQL과 PostGIS를 사용한다

## Status

Accepted (2026-08-31)

## Context

- Pheeeew는 한숨이 등록된 근사 위치를 저장해 지도에 표시해야 해요.
- 지도 조회는 현재 화면의 사각 영역 안에 있는 한숨만 빠르게 찾을 수 있어야 해요.
- 위치를 단순한 위도·경도 숫자가 아니라 좌표계가 명확한 공간 데이터로 관리할 필요가 있어요.
- 팀은 PostgreSQL과 PostGIS 경험이 없지만, MVP의 핵심 위치 기능을 실제 공간 데이터베이스로 검증하기로 했어요.

## Decision

관계형 데이터베이스로 PostgreSQL을 사용하고 공간 기능은 PostGIS로 처리해요. 근사 위치는 `geometry(Point, 4326)`로 저장하고, 지도 영역 조회에는 PostGIS 공간 함수와 GiST 인덱스를 사용해요.

## Alternatives

- MySQL 공간 타입과 `SPATIAL` 인덱스 — `POINT`, SRID와 사각 영역 조회를 지원하므로 구현 자체는 가능해요. 다만 인덱스 기반 영역 조회를 MySQL의 MBR 함수와 R-tree 인덱스 방식에 맞춰 설계해야 하므로, Pheeeew의 핵심 조회를 `ST_Intersects`와 GiST 인덱스로 직접 표현할 수 있는 PostGIS를 선택했어요.
- 위도와 경도를 일반 숫자 컬럼으로 저장 — 시작은 단순하지만 좌표계와 공간 조회 의도가 스키마에 드러나지 않고 공간 인덱스를 활용하기 어려워요.
- 별도의 위치 검색 시스템 도입 — MVP 규모에 비해 운영 대상과 데이터 동기화 부담이 커져요.

## Consequences

- (+) 위치의 좌표계와 타입을 데이터베이스에서 명확하게 보장할 수 있어요.
- (+) `ST_MakeEnvelope`, `ST_Intersects`와 GiST 인덱스로 지도 영역 조회를 구현할 수 있어요.
- (+) 일반 데이터와 위치 데이터를 하나의 데이터베이스에서 함께 관리할 수 있어요.
- (-) 팀이 PostgreSQL, PostGIS와 공간 SQL을 새로 학습해야 해요.
- (-) 지도 조회 쿼리와 스키마 일부가 PostGIS에 종속돼요.
- (=) 일반 저장은 Spring Data JPA를 사용하고 지도 영역 조회만 PostGIS 네이티브 SQL로 작성해요.
- 재검토: 위치 기반 조회가 핵심 기능에서 빠지거나 PostGIS 운영 비용이 MVP의 이점보다 커질 때 다시 판단해요.

## Compliance

- 위치 컬럼과 GiST 인덱스는 Flyway 마이그레이션으로 관리해요.
- 공간 조회는 실제 PostgreSQL + PostGIS 환경에서 검증하고 H2로 대체하지 않아요.
