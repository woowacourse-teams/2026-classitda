# ADR-0005. 엔티티는 빌더로만 만들고, 변경은 의도를 드러내는 메서드로만 해요

- 상태: 채택 (2026-08-06)
- 후보: `@Setter` + 공개 생성자로 자유롭게 조립, 빌더 단일 생성 경로 + 명시적 변경 메서드
- 선택: 빌더 단일 생성 경로 + 명시적 변경 메서드
- 관련: [ADR-0002](0002-integrity-enforcement-location.md), [ADR-0006](0006-layer-contract.md)

---

## 🤔 선택 근거

### 생성 경로가 하나여야 불변식을 한 곳에서 검사할 수 있어요

객체를 만드는 길이 여러 개면, 검증을 그 길마다 붙여야 해요. 하나라도 빠지면 그 경로로만 잘못된 객체가 만들어져요. [ADR-0002](0002-integrity-enforcement-location.md)에서 DB 대신 애플리케이션이 무결성을 책임지기로 했기 때문에, 생성 지점을 좁히는 게 더 중요해졌어요.

### JPA는 기본 생성자를 요구하지만, 그게 공개일 필요는 없어요

Hibernate가 프록시와 리플렉션을 위해 인자 없는 생성자를 필요로 해요. 하지만 필요한 건 `protected`까지예요. `public`으로 열면 애플리케이션 코드 어디서든 빈 엔티티를 만들 수 있게 되고, 그 객체는 어떤 검증도 거치지 않은 상태예요.

`@AllArgsConstructor`도 마찬가지로 빌더가 쓰려고 있는 것이라 `private`이면 충분해요.

### `@Setter`는 "무엇이 왜 바뀌었는지"를 지워요

`session.setStatus(CANCELED)`는 상태를 바꾸지만, 취소할 때 함께 해야 할 일(취소 시각 기록, 대기자 승계)을 강제하지 않아요. 호출하는 쪽마다 잊을 수 있고, 잊었는지 여부가 코드에 드러나지 않아요.

`session.cancel()`이면 그 안에 취소가 무엇을 뜻하는지 모여 있어요. 허용되지 않는 전이(이미 끝난 수업을 취소)도 그 안에서 막을 수 있어요.

### `@ManyToOne`은 기본이 EAGER예요

JPA 스펙상 `@ManyToOne`과 `@OneToOne`의 기본 페치 전략은 EAGER예요. 명시하지 않으면 엔티티 하나 조회할 때 연관 엔티티가 딸려 오고, 목록 조회에서는 그게 N+1로 이어져요. 기본값에 기대지 않고 항상 적어요.

---

## 규칙

### 엔티티 선언

```java
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "class_session")
@Entity
public class ClassSession extends BaseEntity {
```

- `@NoArgsConstructor`는 `PROTECTED`, `@AllArgsConstructor`는 `PRIVATE`이에요.
- **클래스에 `@Setter`를 붙이지 않아요.** 상태 변경은 `updateName`, `cancel`처럼 의도를 드러내는 메서드로 표현해요.
- 애노테이션 순서는 아래로 갈수록 중요한 것이에요(`@Entity`가 마지막). 근거는 [코드 컨벤션 문서](../decisions/backend-code-convention.md) 3장에 있어요.

### 검증 로직은 엔티티 안에 둬요

길이·범위·상태 전이 검증은 엔티티가 가져요. 서비스에 흩어 놓으면 같은 규칙이 여러 서비스에 복제되고, 나중에 한 곳만 고치게 돼요.

단, 여러 애그리거트를 봐야 하는 검증(스튜디오 경계 확인 등)은 엔티티 하나로 판단할 수 없으니 서비스 책임이에요. [ADR-0002](0002-integrity-enforcement-location.md)를 따라요.

### 연관관계

- **`@ManyToOne`에는 항상 `fetch = FetchType.LAZY`를 적어요.** 기본값이 EAGER라 생략은 곧 EAGER예요.
- **필요한 방향만 매핑해요.** 양방향은 실제로 양쪽에서 탐색할 때만 써요.
- **다른 애그리거트를 참조할 때는 ID 참조(`Long ownerId`)를 먼저 검토해요.** 경계를 넘는 객체 그래프는 조회 범위를 예측하기 어렵게 만들어요.
- 삭제 정책(하드/소프트)과 생성·수정 시각 필드 유무를 설계 문서에 적어요.

---

## 감수하는 점

- **테스트에서 객체 만들기가 조금 번거로워져요.** 빌더로만 만들 수 있으니까요. 도메인마다 Fixture를 두는 것으로 상쇄해요([ADR-0007](0007-test-strategy.md)).
- **`@Builder`는 필수 필드를 강제하지 않아요.** 빌더로 만들면서 필수 값을 빼도 컴파일은 통과해요. 이건 빌더 안 검증이나 정적 팩토리로 보완해야 하는 지점이라, 필수 조합이 복잡해지는 엔티티가 나오면 그때 다시 판단해요.
