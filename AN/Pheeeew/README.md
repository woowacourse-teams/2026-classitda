This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across the Android and iOS apps.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - [androidMain](./shared/src/androidMain/kotlin) and [iosMain](./shared/src/iosMain/kotlin) are for code
    that's compiled only for the platform indicated in the folder name.

### 패키지 구조 (`shared/src/commonMain/kotlin/com/pheeeew`)

- `core/database` — 로컬 데이터베이스 관련 코드 (DB 스키마, DAO 등 로컬 영속성 계층)
- `core/designsystem` — 앱 전반에서 사용하는 디자인 시스템
  - `component` — 재사용 가능한 공통 UI 컴포넌트
  - `theme` — 컬러, 타이포그래피, 셰이프 등 디자인 토큰 및 테마 정의
- `core/network` — 네트워크 통신 관련 공통 설정 (HTTP 클라이언트, 인터셉터 등)
- `core/navigation` — 앱 내 화면 전환(네비게이션) 관련 공통 로직
- `core/utils` — 여러 모듈에서 공통으로 사용하는 유틸리티/확장 함수
- `core/permission` — 권한 요청 및 처리 관련 공통 로직
- `data/repository` — `domain`의 Repository 인터페이스에 대한 구현체
- `data/local` — 로컬 데이터소스(DB, DataStore 등) 구현
- `data/remote` — 원격 데이터소스(API 통신, DTO 등) 구현
- `domain/repository` — 비즈니스 로직에서 사용하는 Repository 인터페이스 정의
- `domain/model` — 앱의 핵심 비즈니스 모델(도메인 모델) 정의
- `feature` — 기능(화면) 단위 모듈 (현재 비어 있으며, 추후 기능별 하위 패키지 추가 예정)

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…