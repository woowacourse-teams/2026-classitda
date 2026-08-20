# ADR-0014: 수업 신청 생명주기, 예약과 대기를 하나의 모델로 통합한다

## Status

Accepted (2026-08-20)

## Context

- 한 회원이 한 수업에 참여권을 얻어 가는 관계를 `Reservation`과 `Waiting`이 나누어 저장하고 있어요.
- 각 테이블의 유일성 제약은 자기 테이블 안의 중복만 막으므로, 같은 회원과 수업에 활성 예약과 활성 대기가 함께 존재할 수 있어요.
- 대기 제안을 수락할 때 `Waiting`을 종료하고 `Reservation`을 새로 만들면 하나의 생명주기를 여러 Aggregate가 나누어 변경해야 해요.
- 출석과 결석은 신청의 다음 단계가 아니라 예약이 확정된 뒤 기록되는 별도 결과인데, 현재는 예약 상태와 같은 축에 섞여 있어요.
- 아직 배포와 운영 데이터가 없고 기존 API를 사용하는 외부 클라이언트도 없어 구·신 모델의 호환 계층이나 데이터 이관이 필요하지 않아요.

## Decision

`Reservation`과 `Waiting`을 `ClassSessionEnrollment` Aggregate와 `class_session_enrollment` 테이블 하나로 통합해요. 신청 상태는 `WAITING`, `OFFERED`, `RESERVED`, `CANCELED`, `EXPIRED`로 관리하고, 한 회원과 한 수업에는 활성 신청이 하나만 존재하도록 DB에서도 보장해요.

출결은 `ClassSessionEnrollment`에 포함되는 `Attendance` 값 객체로 분리하고 `NOT_RECORDED`, `ATTENDED`, `ABSENT` 결과를 관리해요. 제안 수락은 같은 신청 행의 `OFFERED`에서 `RESERVED`로 전이하며, 출결이 기록되어도 신청 상태는 `RESERVED`로 유지해요.

## Alternatives

- `Reservation`과 `Waiting`을 유지하고 Application Service에서 함께 검증 — 두 테이블을 가로지르는 모순과 제안 수락의 원자성을 모든 쓰기 경로가 반복해서 책임져야 해요.
- 통합 상태에 `ATTENDED`, `ABSENT`까지 포함 — 신청 생명주기와 출결 결과가 다시 한 축에 섞여 화면과 조회가 상태 의미를 해석해야 해요.
- `Attendance`를 독립 Aggregate와 테이블로 분리 — 예약 확정 관계 없이 존재할 수 없고 항상 신청과 같은 트랜잭션에서 변경되어 독립 경계의 이점이 없어요.

## Consequences

- (+) 활성 신청의 유일성을 한 테이블의 DB 제약으로 보장할 수 있어요.
- (+) 제안 수락이 새 예약 생성 없이 같은 Aggregate의 한 상태 전이로 끝나요.
- (+) 일별·달력 조회가 두 Repository 결과를 조합하지 않고 하나의 신청 모델을 사용해요.
- (-) 제안 기한과 출결 시각처럼 상태에 따라 nullable인 필드의 조합을 도메인과 DB 제약으로 함께 검증해야 해요.
- (=) 수강권 nullability는 이 ADR에서 확정하지 않아요. 통합 모델의 예약 생성 경로에서는 도메인이 수강권을 요구하고, DB 제약은 무료·관리자 예약 규칙을 확정한 뒤 결정해요.
- (=) `CANCELED`, `EXPIRED` 행은 이력으로 보존하고 같은 수업에 다시 신청할 때 새 행을 만들어요.
- (=) 정원, 최신 정책, 수강권, 대기 순서와 잠금은 여전히 Application Service의 트랜잭션 경계에서 검증해야 해요.
- 재검토: 처리 주체와 정정 전후를 포함한 출결 감사 이력이 필요해지면 현재 결과 필드를 늘리지 않고 별도 이력 모델을 검토해요.

## Compliance

- `schema.sql`과 JPA 매핑을 함께 변경하고 MySQL 8.4의 `EntitySchemaValidationTest`로 검증해요.
- 활성 신청 중복 거부, terminal 행 이후 재신청 허용, 제안·출결 필드 제약을 MySQL 8.4 통합 테스트로 검증해요.
- 학생·강사 일별 조회와 학생 달력 조회 동작은 통합 테스트로, 레거시 Repository 참조 제거는 코드 리뷰와 정적 검색으로 검증해요.
