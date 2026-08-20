# 코드 컨벤션

> Spring Boot 백엔드 팀 코드 컨벤션 문서

---

## 1. 네이밍

### 1.1 DTO / VO / Entity 접미어

| 구분 | 접미어 | 예시 |
| --- | --- | --- |
| 요청 DTO | `Request` | `StudioCreateRequest` |
| 응답 DTO | `Response` | `StudioResponse` |
| Entity | 없음 | `Studio` |

### 1.2 정적 팩토리 메서드 네이밍

| 매개변수 개수 | 메서드명 |
| --- | --- |
| 1개 | `from()` |
| 2개 이상 | `of()` |

```java
public record StudioResponse(Long id, String name) {

    public static StudioResponse from(Studio studio) {
        return new StudioResponse(studio.getId(), studio.getName());
    }

    public static StudioResponse of(Long id, String name) {
        return new StudioResponse(id, name);
    }
}
```

### 1.3 CRUD 메서드 네이밍

| 동작 | 접두어 |
| --- | --- |
| 조회 | `find` |
| 저장 | `save` |
| 수정 | `update` |
| 삭제 | `delete` |

**Controller - Service - Repository의 메서드명은 통일한다.** (단순한 흐름인 경우에만)

```java
StudioController.findAll()
    └─ StudioService.findAll()
        └─ StudioRepository.findAll()
```

---

## 2. 객체 생성

### 2.1 DTO

- DTO는 **`record` 타입**으로 정의한다.
- `record`는 **항상 정적 팩토리 메서드로 생성**한다. (생성자 직접 호출 금지)

### 2.2 Entity / 도메인 객체

- 생성은 **모두 빌더를 사용**한다.
- 모든 Entity는 `BaseEntity`를 상속해 `createdAt`, `updatedAt`을 공통으로 관리한다.
- Entity에서 감사 필드를 다시 선언하거나 빌더·Setter로 직접 주입하지 않는다.

```java
Studio studio = Studio.builder()
        .name("스튜디오")
        .address("서울시 강남구")
        .build();
```

---

## 3. 애노테이션
**중요한 애노테이션일수록 맨 아래에 배치한다.**

```java
// Controller
@RequiredArgsConstructor
@RequestMapping("/")
@RestController
public class StudioController {
}
```

```java
// Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StudioService {
}
```

```java
// Entity
@Getter
@Builder
@NoArgsConstructor
@Table(name = "studio")
@Entity
public class Studio {
}
```

---

## 4. 포맷팅

### 4.1 공백 규칙

- **`Class`, `interface` 내부 첫 줄은 공백으로 시작**한다.
    - `enum`, `record`는 제외한다.
- **메서드 내부 첫 줄은 공백 없이 시작**한다.

```java
class Studio {

    void test() {
        System.out.println("시작");
    }
}
```

### 4.2 인터페이스

인터페이스의 계약(메서드 선언)은 **모두 한 줄씩 띄운다.**

```java
public interface StudioRepository extends JpaRepository<Studio, Long> {

    Optional<Studio> findByName(String name);

    List<Studio> findAllByOwnerId(Long ownerId);

    void deleteByName(String name);
}
```

### 4.3 컨트롤러 메서드 매개변수

**매개변수 개수에 관계없이** 항상 줄바꿈하여 작성한다.

```java
public StudioResponse findOne(
        @PathVariable String studioName
) {
}

public CursorResponse<StudioResponse> findAll(
        @PathVariable Long studioId,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "20") int limit
) {
}
```

### 4.4 API 응답

- 성공 응답은 응답 DTO를 직접 반환한다.
- 상태 코드나 헤더를 직접 제어할 때만 `ResponseEntity<T>`를 사용한다.
- 에러 응답은 `code`, `message`만 가지는 공통 `ErrorResponse`를 사용한다.
- 목록 응답은 공통 `CursorResponse<T>`를 사용하고 `items`, `hasNext`, `nextCursor`를 반환한다.
- 성공 응답을 `ApiResponse<T>` 같은 공통 래퍼로 감싸지 않는다.

---

## 5. 코드 정렬 순서

클래스 내부 멤버는 다음 순서로 배치한다.
1. **private 메서드** — public 메서드 전체 아래에 일괄 배치, **public에서 호출된 순서대로**

```java
public class StudioService {

    public void save(...) { validateName(...); }

    public StudioResponse findOne(...) { }

    public void update(...) { validateOwner(...); }

    public void delete(...) { }

    private void validateName(...) { }

    private void validateOwner(...) { }

    @Override
    public String toString() { }
}
```

---

## 6. 테스트

### 6.1 작성 규칙

- **given / when / then** 구조로 작성한다.
- 테스트 메서드명은 **한글**로 작성한다.
- 모든 도메인은 **fixture를 만들어 재사용**한다.

```java
@Test
void 스튜디오_이름으로_조회할_수_있다() {
    // given
    Studio studio = StudioFixture.기본_스튜디오();

    // when
    StudioResponse response = studioService.findByName(studio.getName());

    // then
    assertThat(response.name()).isEqualTo(studio.getName());
}
```

### 6.2 계층별 테스트 전략

| 계층 | 방식 |
| --- | --- |
| 통합 테스트 | `XXXIntegrationTest`를 만들어서 테스트 |
| Controller | 슬라이스 테스트 — `@WebMvcTest`에서 Service를 모킹 |
| Service | 통합 테스트 — `@MySqlRepositoryTest`로 실제 Repository와 MySQL Testcontainers를 사용 |
| Repository | 단독 테스트를 작성하지 않음 |
| 도메인 | 단위 테스트 |

- Repository 동작과 영속성 매핑은 Service 통합 테스트에서 함께 검증한다.
- Service 테스트는 대상 Service를 import하고 Repository를 모킹하지 않는다.
- Controller 테스트는 HTTP 요청·응답 계약과 Service 위임을 검증한다.

---

## 7. 패키지 구조

**도메인형 → 계층형** 구조를 따른다.

```
com.classitda
├── common/            # 공통 설정, 예외, 에러 응답, 페이지네이션 등
└── studio/
    ├── presentation/
    │   ├── dto/
    │   └── StudioController
    ├── application/
    │   └── StudioService
    ├── domain/
    │   ├── repository/
    │   ├       └──StudioRepository
    │   └── Studio
    └── infra/
```

---

## 8. OpenAPI 문서화

OpenAPI 문서는 클라이언트 개발자가 별도의 코드 확인 없이 API의 요청 조건과 동작을 이해할 수 있도록 작성한다.

### 8.1 API 설명

- 모든 API에 `@Operation`을 사용하여 `summary`와 `description`을 작성한다.
- `summary`에는 API의 목적을 간결하게 작성한다.
- `description`에는 다음과 같이 요청 처리에 필요한 핵심 규칙을 작성한다.
  - 요청 형태에 따른 동작 차이
  - 필수 조건과 제약 조건
  - 권한에 따른 처리 범위
  - 서버가 자동으로 결정하거나 저장하는 값
- 설명할 내용이 여러 개라면 Markdown 문단이나 목록으로 구분한다.
- 서로 다른 주제는 빈 줄로 나누고, 한 문장에 모든 규칙을 나열하지 않는다.
- 구현 방식이나 내부 클래스 구조처럼 클라이언트가 알 필요 없는 내용은 작성하지 않는다.

예시:

```java
@Operation(
        summary = "수업 생성",
        description = """
                ### 생성 방식

                - `recurring`이 `false`이면 `classDate`에 수업 한 건을 생성합니다.
                - `recurring`이 `true`이면 반복 기간 중 `recurringDays`에 해당하는 날짜마다 수업을 생성합니다.

                ### 담당 강사

                - `instructorMembershipId`로 같은 시설의 활성 강사 또는 관리자를 지정합니다.
                - 본인 관리 권한은 자신만 지정할 수 있습니다.
                - 대표 또는 전체 관리 권한은 같은 시설의 다른 강사도 지정할 수 있습니다.

                ### 수업 시간

                - 수업 진행 시간은 1분 이상 1,440분 이하입니다.
                - 담당 강사의 기존 수업과 시간이 겹치면 생성할 수 없습니다.
                """
)
```

### 8.2 요청 및 응답 필드 설명

- 모든 필드에 설명을 반복해서 작성하지 않는다.
- 이름만으로 의미와 사용 방법을 명확히 알기 어려운 필드에 `@Schema` 설명을 작성한다.
- 다음에 해당하는 필드는 반드시 설명한다.
  - 특정 리소스나 소속 관계를 가리키는 ID
  - 단위나 허용 범위가 있는 값
  - 다른 필드의 값에 따라 필수 여부가 달라지는 값
  - 생략과 `null`의 처리 방식이 중요한 값
  - 서버가 자동으로 계산하거나 변환하는 값
- 조건을 설명할 때는 관련된 실제 필드명과 값을 명시한다.
- “반복 일정 필드”, “관련 값”처럼 어떤 필드를 가리키는지 불분명한 표현은 사용하지 않는다.
- Bean Validation으로 표현할 수 있는 제약 조건은 검증 애너테이션과 `@Schema`에 동일하게 반영한다.

예시:

```java
@Schema(
        description = """
                반복하지 않는 수업의 날짜입니다.

                `recurring`이 `false`일 때 필수입니다.
                이 경우 `recurringDays`, `repeatStartDate`, `repeatEndDate`는
                생략하거나 `null`로 전달해야 합니다.
                """,
        example = "2026-08-20"
)
LocalDate classDate;
```

### 8.3 요청 예시

- 조건에 따라 요청 구조가 달라지는 API는 주요 요청 형태별 예시를 제공한다.
- 예시는 실제 요청 가능한 값과 필드 조합으로 작성한다.
- 단건 수업과 반복 수업처럼 서로 배타적인 요청 형태는 각각 구분하여 보여준다.

### 8.4 Swagger UI 표시

- 재사용 가능한 요청 및 응답 모델은 `components/schemas`로 정의한다.
- Swagger UI 하단의 전체 `Schemas` 영역은 기본 설정대로 표시한다.
- Swagger UI 전역 설정을 변경할 때는 다른 API 문서의 가독성에 미치는 영향도 확인한다.
