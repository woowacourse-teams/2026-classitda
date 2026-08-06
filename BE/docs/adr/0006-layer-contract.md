# ADR-0006. 계층 간 계약을 고정해요 — DTO 경계·트랜잭션·예외

- 상태: 채택 (2026-08-06)
- 후보: 엔티티를 컨트롤러까지 그대로 노출, 서비스 경계에서 DTO로 변환
- 선택: 서비스 경계에서 DTO로 변환 + 트랜잭션·예외 처리 위치 고정
- 관련: [ADR-0005](0005-entity-creation-and-mutation.md), [ADR-0002](0002-integrity-enforcement-location.md)

---

## 🤔 선택 근거

### 엔티티를 컨트롤러까지 흘려보내면 지연 로딩과 직렬화가 얽혀요

[ADR-0005](0005-entity-creation-and-mutation.md)에서 연관관계를 전부 LAZY로 두기로 했어요. 엔티티를 그대로 응답으로 내보내면 직렬화 시점에 프록시를 건드리게 되고, 트랜잭션이 이미 끝난 뒤라면 예외가 나요. 트랜잭션을 웹 계층까지 늘려서 해결하는 방식은 커넥션 점유 시간을 늘려요.

DTO로 변환하면 무엇을 내보낼지가 코드에 명시돼요. 엔티티에 필드를 추가해도 API 응답이 조용히 바뀌지 않아요.

### 트랜잭션 기본값을 읽기 전용으로 두면 실수 방향이 안전해요

클래스에 `@Transactional(readOnly = true)`를 두고 쓰기 메서드에만 `@Transactional`을 얹으면, 애노테이션을 빠뜨렸을 때의 결과가 "쓰기가 안 됨"이에요. 반대로 두면 빠뜨렸을 때 "의도치 않은 쓰기가 됨"이고, 이쪽이 훨씬 늦게 발견돼요.

읽기 전용 트랜잭션은 Hibernate가 스냅샷 비교(dirty checking)를 건너뛸 수 있어서 조회 비용도 줄어요.

### 예외를 삼키면 원인 추적이 막혀요

`try-catch`로 잡아서 로그만 남기거나 null을 반환하면, 실제 실패가 한참 뒤 엉뚱한 지점에서 500으로 나타나요. 예외는 전파시키고, HTTP 상태로 바꾸는 일은 한 곳에서만 해요. 매핑이 한 곳에 있어야 "이 예외는 몇 번으로 나가는가"에 답할 수 있어요.

### 검증은 요청 DTO에서 발동시켜요

Bean Validation을 요청 DTO에 붙이고 컨트롤러에서 `@Valid`로 발동시키면, 형식 검증이 서비스에 도달하기 전에 끝나요. 서비스로 미루면 같은 검사가 계층마다 중복되고, 실패 응답 형식도 갈라져요.

---

## 규칙

### 의존 방향

```
presentation → application → domain
```

- `domain`은 다른 계층에 의존하지 않아요. DTO도, Spring 웹 타입도 몰라요.
- `presentation`의 DTO가 `domain` 객체를 받아 변환하는 건 허용해요. 반대는 금지예요.
- 방향이 한쪽이어야 도메인 로직을 웹 계층 없이 테스트할 수 있어요.

### 서비스

- **서비스는 DTO를 받아 DTO를 반환해요.** 엔티티를 컨트롤러로 내보내지 않아요.
- **클래스에 `@Transactional(readOnly = true)`, 쓰기 메서드에만 `@Transactional`이에요.**
- **의존성은 `@RequiredArgsConstructor` + `final`로 주입해요.** 필드 주입(`@Autowired`)을 쓰지 않아요.
- private 헬퍼는 public 메서드 전체 아래에, 호출된 순서대로 둬요([코드 컨벤션](../decisions/backend-code-convention.md) 5장).

### 컨트롤러

- **위임만 해요.** 분기·계산 로직을 두지 않아요.
- **반환 타입은 공통 응답 래퍼(`ApiResponse<T>`)로 통일해요.**
- **요청 DTO에 Bean Validation을 붙이고 `@Valid`로 발동시켜요.**
- 매개변수는 개수와 무관하게 줄바꿈해요([코드 컨벤션](../decisions/backend-code-convention.md) 4.3).

### 예외

- 도메인별 예외 클래스를 만들고 공통 예외를 상속시켜요.
- 예외 → HTTP 상태 매핑은 `GlobalExceptionHandler`(또는 에러 코드 enum) **한 곳**에서 결정해요.
- **컨트롤러·서비스에서 `try-catch`로 예외를 삼키지 않아요.**

### API 버저닝

버전은 **`X-API-Version` 헤더**로 구분해요. **경로에 `/v1`을 넣지 않아요.** 선택 근거는 [기술 스택 문서](../decisions/backend-tech-stack.md)에 있어요.

### 공통 요소는 `common/`에 한 번만

`ApiResponse`, `GlobalExceptionHandler`, 에러 코드, API 버전 설정은 `com.classitda.common/` 아래 한 벌만 둬요. 도메인마다 만들지 않아요.

---

## 감수하는 점

- **변환 코드가 늘어요.** 엔티티 ↔ DTO 매핑을 손으로 써야 해요. 정적 팩토리(`from`/`of`)에 모아 두는 것으로 관리해요.
- **단순 조회도 DTO를 거쳐요.** 필드를 그대로 옮기는 DTO가 생기지만, 나중에 응답만 바꿔야 할 때 엔티티를 건드리지 않아도 되는 값으로 상쇄돼요.
- **`readOnly = true` 기본값은 쓰기 메서드에 애노테이션을 빠뜨렸을 때 런타임에야 드러나요.** 리뷰 확인 항목으로 둬요.
