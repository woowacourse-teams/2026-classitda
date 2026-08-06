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

- **클래스 내부 첫 줄은 공백으로 시작**한다.
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
public ApiResponse<StudioResponse> findOne(
        @PathVariable String studioName
) {
}

public ApiResponse<StudioResponse> findAll(
        @PathVariable Long studioId,
        @RequestParam(defaultValue = "1") int page
) {
}
```

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
| Controller | 단위 테스트 — Service를 모킹 |
| Service | 단위 테스트 — Repository를 모킹 |
| Repository | Testcontainers로 운영과 동일한 환경에서 테스트 |
| 도메인 | 단위 테스트 |

---

## 7. 패키지 구조

**도메인형 → 계층형** 구조를 따른다.

```
com.classitda
├── common/            # 공통 설정, 예외, 응답 포맷 등
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