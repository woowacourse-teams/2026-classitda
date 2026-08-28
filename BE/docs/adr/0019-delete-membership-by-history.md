# ADR-0019: 소속 삭제를 이력 유무에 따라 하드와 소프트로 가른다

## Status

Accepted (2026-08-26)

## Context

- 강사가 시설 회원을 목록에서 삭제하는 기능이 필요해요.
- `studio_membership.id`를 참조하는 FK가 4개인데 `ON DELETE CASCADE`가 하나도 없어요. `class_session.instructor_membership_id`, `class_session_enrollment.membership_id`, `member_pass_product.membership_id`, `notice.author_membership_id`예요.
- 그래서 수업을 한 번이라도 예약했거나 수강권을 가진 회원은 `DELETE`가 FK 위반으로 실패해요.
- 반대로 강사가 전화번호를 잘못 입력했거나 등록만 하고 지우는 경우가 있어요. **이런 소속은 네 테이블 어디에도 없어서 지워도 안전해요.**
- 전부 소프트 삭제하면 보존할 이력이 없는 행까지 쌓여요.
- `MembershipStatus`에 `INACTIVE`와 `WITHDRAWN`이 있지만 아무도 설정하지 않아요. 그런데 `status != ACTIVE`를 검사하는 곳은 8군데예요.
- `notice`는 엔티티만 있고 리포지터리·서비스·API가 없어서 항상 비어 있어요.

## Decision

삭제 요청이 오면 참조 이력이 있는지 확인해서, **없으면 하드 삭제하고 있으면 `WITHDRAWN`으로 바꿔요.** 강사가 보는 결과는 둘 다 `204`로 같아요.

소프트 삭제할 때는 아직 시작하지 않은 수업의 예약을 함께 취소하고, 이미 지난 수강 이력은 남겨요.

이력 판정은 `class_session_enrollment`, `member_pass_product`, `class_session` 세 곳을 확인해요. **이 검사가 유일한 판정 수단이에요.** 예외를 잡아 소프트 삭제로 넘기는 폴백은 두지 않아요 — Hibernate가 삭제된 소속을 참조하는 엔티티를 발견하면 DB 제약에 닿기 전에 `TransientPropertyValueException`을 던져서, 제약 위반 예외로는 잡히지 않아요.

## Alternatives

- **전부 소프트 삭제** — 오타로 등록한 소속까지 영구히 쌓여요.
- **FK에 `ON DELETE CASCADE` 추가** — 정산과 출결 이력이 사라지고, 공지는 작성자가 지워지면 글까지 없어져요.
- **이력이 있으면 삭제를 거부** — 강사가 왜 안 되는지 알 수 없어요.

## Consequences

- (+) 실수로 등록한 소속이 흔적 없이 사라져요.
- (+) 이력이 있는 소속은 상태만 바뀌어 정산과 출결이 보존돼요.
- (+) `status != ACTIVE` 검사 8곳이 이미 있어서 예약, 조회, 권한이 함께 차단돼요.
- (-) **같은 API가 안에서 두 가지로 갈려요.** 삭제된 소속이 되살아나는지 아닌지가 이력 유무에 따라 달라져요.
- (-) **`membership_id`를 참조하는 테이블이 늘면 판정에 검사를 반드시 추가해야 해요.** 빠뜨리면 하드 삭제가 `500`으로 실패해요. 받아 주는 폴백이 없어요.
- (-) `notice`를 이번 판정에서 뺐어요. 리포지터리와 API가 없어 행이 만들어지지 않기 때문인데, **공지 기능이 생기면 `existsByAuthorMembershipId` 검사를 함께 추가해야 해요.**
- (=) 목록 조회가 `WITHDRAWN` 소속을 걸러야 해요. 커서 페이지네이션이라 커서 동작을 함께 확인해야 해요.
- 재검토: 소프트 삭제된 소속이 쌓여 조회 성능에 영향을 주거나, 하드 삭제와 소프트 삭제의 차이가 사용자에게 드러나면 다시 판단해요.

## Compliance

- 이력이 없는 소속을 삭제하면 행이 남지 않아요.
- 이력이 있는 소속을 삭제하면 `WITHDRAWN`으로 바뀌고 지난 수강 이력이 남아요.
- 삭제 시 아직 시작하지 않은 예약이 취소돼요.
- 대표 강사와 자기 자신은 삭제할 수 없어요.
