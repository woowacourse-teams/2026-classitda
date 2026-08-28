# ADR-0015: MVP 기간 동안 예약의 수강권 필수 제약을 일시 해제한다

## Status

Accepted (2026-08-24)

## Context

- [ADR-0014](0014-unify-class-session-enrollment.md)는 `RESERVED` 상태의 신청이 반드시 수강권을 참조하도록 정했고, `schema.sql`의 `chk_enrollment_pass`가 이를 DB에서 강제했어요.
- 수강권 발급과 차감 기능이 아직 없어요. `MemberPassProduct`에는 `isUsable()`과 `isValidOn()`만 있고 남은 횟수를 차감하는 경로가 어디에도 없어요.
- MVP에서 필요한 것은 시설이 등록된 회원을 수업 회차에 예약하고 취소하는 흐름 하나예요. 이 흐름은 수강권을 소비하지 않아요.
- 제약을 만족시키려고 임의의 `PassProduct`와 `MemberPassProduct` 행을 만드는 방법도 있지만, 그 수강권이 `StudentOwnedPasses.coveredClassTypeIdsOn`을 거쳐 학생 일별 조회의 수업 종류 필터로 쓰여요. 회원에게 보이는 수업 목록이 달라지고, 신청 상세 응답에도 실재하지 않는 수강권 이름과 잔여 횟수가 노출돼요.
- 아직 운영 데이터가 없어 제약을 되돌릴 때 이관할 행이 없어요.

## Decision

`chk_enrollment_pass`에서 `RESERVED`의 수강권 필수 조건을 **일시적으로** 제거해요.

```sql
-- 원래 형태 (수강권 도입 시 복원 대상)
OR (enrollment_status = 'RESERVED'
    AND member_pass_product_id IS NOT NULL)
OR enrollment_status = 'CANCELED'

-- MVP 기간 형태
OR enrollment_status IN ('RESERVED', 'CANCELED')
```

**이 제약은 최종 모델에서 필수예요.** 수강권 도입 전 MVP 기간에만 해제하며, 수강권 발급·차감 기능이 들어오는 시점에 원래 형태로 되돌려요. 지금 빼는 이유는 규칙이 틀려서가 아니라, 규칙을 만족시킬 수단이 아직 없기 때문이에요.

MVP 기간의 예약 생성은 `ClassSessionEnrollment.reservedWithoutPassProduct()` 하나로만 만들어요. 수강권을 요구하는 기존 `reserved()`는 그대로 남겨 두어, 되돌릴 때 수강권 없이 만든 지점을 정적 검색 한 번으로 전부 찾을 수 있게 해요.

## Alternatives

- **임의의 수강권을 자동 생성해 제약을 만족** — `member_pass_product.pass_product_id`가 `NOT NULL`이라 가짜 `PassProduct`까지 만들어야 하고, 그 수강권이 학생의 수업 목록 필터와 신청 상세 응답을 오염시켜요. 되돌릴 때 진짜 데이터와 섞인 가짜 행을 골라내야 해요.
- **`RESERVED` 대신 다른 상태로 저장** — 정원 점유와 취소 전이의 의미가 달라져 조회와 정책이 모두 갈라져요.
- **예약 기능을 수강권 도입 이후로 미룸** — 안드로이드 연동에 필요한 최소 예약 흐름이 그때까지 막혀요.
- **제약을 영구히 제거** — 수강권 도입 후 "예약에는 수강권이 있다"를 코드로만 지켜야 해서 누락이 조용히 통과해요.

## Consequences

- (+) 가짜 수강권 데이터 없이 시설 대리 예약 흐름을 만들 수 있어요.
- (+) 되돌릴 때 고칠 곳이 `schema.sql`의 CHECK 한 곳과 `reservedWithoutPassProduct()` 호출 지점으로 좁아요.
- (-) **DB가 더 이상 "예약에는 수강권이 있다"를 보장하지 않아요.** 수강권 도입 이후 이 제약을 복원하지 않으면 수강권 없는 예약이 검증 없이 저장돼요.
- (=) `WAITING`, `OFFERED`, `EXPIRED`가 수강권을 가지지 않는다는 규칙은 그대로 유지해요.
- (=) [ADR-0014](0014-unify-class-session-enrollment.md)의 "`RESERVED`는 반드시 수강권 FK가 있어요" 항목은 이 ADR이 유효한 동안 적용을 미뤄요. ADR-0014의 나머지 결정은 그대로예요.
- 재검토: **수강권 발급 또는 차감 기능이 들어오는 시점에 반드시 되돌려요.** 그전에 수강권 없는 `RESERVED` 행이 쌓이면, 복원과 함께 그 행들의 처리 방침도 같이 정해야 해요.

## Compliance

- `schema.sql`의 `chk_enrollment_pass`에 이 ADR을 가리키는 주석을 남겨, 제약을 읽는 사람이 일시 해제 상태임을 알 수 있게 해요.
- 복원할 때는 위 "원래 형태" SQL로 되돌리고 `EntitySchemaValidationTest`로 MySQL 8.4에서 검증해요([ADR-0002](0002-schema-source-of-truth.md), [ADR-0003](0003-use-testcontainers-for-mysql-tests.md)).
- 복원 전에 `reservedWithoutPassProduct()` 호출 지점을 모두 제거하거나 수강권을 연결하는 경로로 바꿔요. 남아 있으면 복원 후 런타임에 제약 위반이 나요.
- 복원 전에 `SELECT COUNT(*) FROM class_session_enrollment WHERE enrollment_status = 'RESERVED' AND member_pass_product_id IS NULL` 이 0인지 확인해요.
