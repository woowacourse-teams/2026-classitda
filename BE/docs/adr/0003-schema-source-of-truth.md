# ADR-0003. DDL 원본은 `schema.sql`이고 엔티티는 매핑만 해요

- 상태: 채택 (2026-08-06)
- 후보: `ddl-auto`로 엔티티에서 스키마 생성, `schema.sql`을 원본으로 두고 엔티티는 검증만
- 선택: `schema.sql` 원본 + `ddl-auto=validate`
- 관련: [ADR-0002](0002-integrity-enforcement-location.md)

---

## 🤔 선택 근거

### 취소 이력을 남기면서 활성 행만 유일하게 하고 싶었어요

예약·대기·수업 회차는 취소해도 행을 지우지 않고 `status = 'CANCELED'`로 남겨요. 이력이 필요하니까요.

그런데 "한 회원이 한 세션에 활성 예약을 하나만 가진다"는 제약은 그대로 필요해요. 단순히 `UNIQUE (class_session_id, membership_id)`를 걸면 **취소하고 다시 예약하는 게 불가능**해져요. 취소된 행이 자리를 차지하고 있으니까요.

필요한 건 "취소되지 않은 행에만 적용되는 유니크 제약", 즉 부분 유니크 인덱스예요.

### MySQL에는 부분 인덱스 문법이 없어서 생성 컬럼으로 우회해요

PostgreSQL이면 `CREATE UNIQUE INDEX ... WHERE status <> 'CANCELED'` 한 줄이고, SQL Server면 필터 인덱스로 끝나요. MySQL 8.4에는 그 문법이 없어요.

대신 유니크 제약이 **NULL을 중복으로 보지 않는다**는 성질을 이용해요.

```sql
active_flag TINYINT GENERATED ALWAYS AS (IF(status = 'CANCELED', NULL, 1)) STORED,
UNIQUE KEY uk_reservation_active (class_session_id, membership_id, active_flag)
```

취소된 행은 `active_flag`가 NULL이 되어 유니크 검사에서 통째로 빠져요. 살아있는 행끼리만 유일성이 걸려요. 취소 이력은 몇 개든 쌓이고, 활성 예약은 하나만 남아요.

### 이 제약은 JPA로 표현할 수 없어요

`@Table(uniqueConstraints = ...)`은 **컬럼 이름 목록만** 받아요. 생성 컬럼도, 표현식도 쓸 수 없어요. 즉 위 제약을 엔티티 애노테이션으로 옮길 방법이 없어요.

`ddl-auto`로 스키마를 만들면 이 제약이 통째로 사라지거나, 취소 이력을 막는 잘못된 유니크 제약으로 바뀌어요. 그래서 DDL 원본을 `schema.sql`에 두고, 엔티티는 그 스키마에 맞춰 매핑만 하기로 했어요.

### 대신 정합성을 테스트가 보증해요

원본을 둘로 나누지 않았지만, 엔티티 매핑이 스키마와 어긋날 위험은 남아요. `EntitySchemaValidationTest`가 그걸 막아요.

Testcontainers로 MySQL 8.4를 띄우고 `schema.sql`을 적용한 뒤, `ddl-auto=validate`로 Hibernate가 전체 매핑을 대조해요. 어긋나면 컨텍스트 로딩 자체가 실패해서 테스트가 깨져요.

---

## 규칙

- **DDL은 `src/main/resources/schema.sql`에만 써요.** 테이블·컬럼·인덱스·제약을 엔티티 애노테이션으로 만들지 않아요.
- **엔티티는 매핑만 해요.** `@Column`의 `nullable`·`length`는 스키마와 맞추기 위해 적고, `@Table`에 `uniqueConstraints`나 `indexes`를 두지 않아요.
- **생성 컬럼(`active_flag`)은 엔티티에 매핑하지 않아요.** 애플리케이션이 읽거나 쓸 값이 아니에요. DB가 계산해서 제약에만 쓰는 컬럼이라, 엔티티에 없는 게 정상이에요.
- **`ddl-auto`는 `validate`를 넘지 않아요.** `update`·`create`는 쓰지 않아요.
- **스키마를 바꾸면 `EntitySchemaValidationTest`를 돌려요.** 엔티티와 `schema.sql` 중 한쪽만 고치는 건 이 테스트가 잡아요.

### 엔티티에 유니크 제약이 안 보이는 건 누락이 아니에요

`ClassSession`, `Reservation`, `Waiting`에는 유니크 제약이 애노테이션으로 없어요. 위 이유 때문이에요. 리뷰에서 "제약 누락"으로 지적하지 않아요. 확인하려면 `schema.sql`의 `uk_session_room_active`, `uk_session_instructor_active`, `uk_reservation_active`, `uk_waiting_active`를 보면 돼요.

---

## 감수하는 점

- **스키마를 바꿀 때 두 파일을 같이 고쳐야 해요.** `schema.sql`과 엔티티예요. 빠뜨리면 `EntitySchemaValidationTest`가 잡지만, 한 번에 안 끝나는 건 비용이에요.
- **`schema.sql`은 전체 재생성 스크립트라 변경 이력이 남지 않아요.** 운영 배포가 시작되면 Flyway 같은 마이그레이션 도구가 필요해요. 그때 다시 판단해요.
- **`active_flag`는 MySQL 전용이에요.** DB를 바꾸면 이 표현도 바뀌어야 해요. 다만 MySQL 선택은 이미 [기술 스택 문서](../decisions/backend-tech-stack.md)에서 내린 결정이라, 여기서는 그 전제를 그대로 따라요.
