# Architecture Decision Records

ADR은 코드만으로 알 수 없는 **결정의 이유와 감수한 결과**를 팀원에게 남기는 기록이에요.

아키텍처 결정과 영향이 큰 기술 결정뿐 아니라, 단점이나 위험을 알면서도 현재 맥락에서 의식적으로 선택하거나 보류한 기술적 타협도 기록해요. 완벽한 결정만 기록하지 않지만, 막연한 걱정이나 개선 아이디어가 아니라 실제 결정이 있어야 해요.

## 언제 작성하나요?

다음 두 조건을 모두 만족하면 ADR 후보예요.

1. 실제로 선택했거나 의식적으로 보류한 결정이 있어요.
2. 다음 중 하나 이상에 해당해요.
   - 되돌리거나 교체하는 비용이 커요.
   - 구조·보안·정합성·성능·운영·공개 계약에 영향을 줘요.
   - 단점이나 위험을 알면서 감수했어요.
   - 선택 이유가 코드만 봐서는 드러나지 않아요.
   - 같은 논쟁이 다시 반복될 가능성이 높아요.

> 실제 결정이 존재하고, 그 이유나 감수한 위험을 다른 팀원이 알아야 할 때 ADR을 작성해요.

다음 내용은 원칙적으로 ADR로 작성하지 않아요.

- 막연한 개선 아이디어나 걱정
- 아직 선택하지 않은 아이디어의 단순 나열
- 네이밍·임포트·포맷 같은 코드 스타일
- 쉽게 되돌릴 수 있는 평범한 구현 방법
- 작업 내용 요약, 회의록, TODO
- 별다른 선택이 없었던 단순 버그 수정

작은 결정이라도 “왜 이렇게 구현했지?”라는 의문이 생길 만한 의식적인 타협이라면 ADR로 남길 수 있어요.

## 작성 형식

```markdown
# ADR-NNNN: 무엇을 결정한다

## Status

Accepted (YYYY-MM-DD)

## Context

- 결정이 필요해진 상황
- 당시의 중요한 제약이나 근거
- 팀원이 알아야 할 사실

## Decision

무엇을 선택했고 왜 선택했는지 1~3문장으로 쓴다.

## Alternatives

- 대안 A — 선택하지 않은 핵심 이유
- 대안 B — 선택하지 않은 핵심 이유

## Consequences

- (+) 얻는 점
- (-) 감수하는 점 또는 켕기는 점
- (=) 중립적인 변화나 추가 부담
- 재검토: 어떤 조건에서 다시 판단할지

## Compliance

- 필요한 경우에만 테스트·정적 분석·리뷰 등 검증 방법을 쓴다.
```

- `Context`에는 결론을 미리 정당화하지 않고 당시의 사실과 제약을 적어요.
- `Decision`에는 선택과 핵심 이유만 적고 상세 구현 설명을 늘어놓지 않아요.
- `Alternatives`에는 실제로 검토한 대안만 한 줄씩 적어요. 억지 대안은 만들지 않아요.
- `Consequences`에는 좋은 점뿐 아니라 켕기는 점과 새로 생기는 부담도 함께 적어요.
- 재검토 조건을 알고 있다면 구체적으로 적어요.
- `Compliance`는 검증할 가치와 방법이 있을 때만 추가해요.
- 기본 목표는 한 화면 또는 30~50줄 이내예요. 자세한 설계와 구현은 별도 문서·이슈·코드로 연결해요.

## Status

| 상태 | 의미 |
| --- | --- |
| `Proposed` | 아직 팀이 논의 중인 제안 |
| `Accepted` | 팀이 채택했고 현재 유효한 결정 |
| `Rejected` | 제안 자체를 검토했지만 채택하지 않음 |
| `Deprecated` | 더 이상 유효하지 않으며 직접 대체한 결정은 없음 |
| `Superseded by ADR-NNNN` | 새로운 ADR이 기존 결정을 대체함 |

일반적으로 선택하지 않은 대안은 채택된 ADR의 `Alternatives`에 적어요. 제안 자체가 ADR로 작성되어 검토된 경우에만 `Rejected`를 사용해요. `Implemented`, `Completed`처럼 작업 진행 상황을 나타내는 값은 사용하지 않아요.

## 제목과 번호

- 파일명은 `NNNN-short-english-title.md`로 작성해요.
- 제목은 `ADR-NNNN: 무엇을 결정한다`처럼 결정문으로 작성해요.
- 번호는 4자리로 순차 증가시키며 중요도나 분류로 사용하지 않아요.
- 한 번 사용한 번호는 삭제되거나 대체돼도 재사용하지 않아요.
- ADR 하나에는 하나의 결정만 기록해요.

결정이 바뀌면 기존 내용을 현재 관점으로 덮어쓰지 않아요. 새 ADR을 작성하고 기존 ADR을 `Superseded by ADR-NNNN`으로 변경해 서로 연결해요.

아직 고민만 하는 중이면 ADR을 쓰지 않아요. 결정했다면 구현 전이나 구현하면서 작성하고, 중요한 결정을 뒤늦게 깨달았다면 당시 기억과 확인 가능한 근거만 사용해 소급 작성할 수 있어요.

## AI 사용

> 작성자가 먼저 상황, 선택, 켕기는 점, 버린 대안을 직접 적어요. AI는 문장을 다듬거나 빠진 질문을 제안할 수 있지만 근거·대안·수치·트레이드오프를 만들어 내서는 안 돼요.

작성자는 ADR의 모든 문장을 직접 설명할 수 있어야 해요.

## 현재 ADR 목록

| 번호 | 결정 | 한 줄 요약 |
| --- | --- | --- |
| [0001](0001-integrity-enforcement-location.md) | 스튜디오 경계 무결성 검증 | 스튜디오 경계를 넘는 참조는 저장 전에 애플리케이션이 검증해요 |
| [0002](0002-schema-source-of-truth.md) | DDL 원본 | `schema.sql`을 DDL 원본으로 사용하고 JPA는 매핑을 검증해요 |
| [0003](0003-use-testcontainers-for-mysql-tests.md) | Testcontainers 기반 MySQL 검증 | Repository와 스키마 검증 테스트를 MySQL 8.4에서 실행해요 |
| [0004](0004-keep-external-io-outside-db-transaction.md) | 외부 I/O와 DB 트랜잭션 경계 | Redis·외부 API 호출은 DB 트랜잭션 밖에서 수행하고 DB 쓰기만 짧게 묶어요 |
| [0005](0005-authentication-token-and-session-boundary.md) | Access·Refresh 상태 경계 (`Superseded`) | ADR-0012가 토큰 상태 경계와 Access Token 수명 결정을 대체해요 |
| [0006](0006-terminate-tls-at-cloudflare.md) | DNS·TLS 종단 위치 | DNS와 TLS를 Cloudflare에서 처리하고 Origin Certificate로 origin 구간까지 암호화해요 |
| [0007](0007-manual-production-schema-application.md) | 프로덕션 스키마 반영 | 마이그레이션 도구를 보류하고 `schema.sql`을 운영 DB에 직접 적용해요 |
| [0008](0008-run-rds-without-storage-encryption.md) | RDS 저장 시 암호화 | KMS 권한 제약으로 저장 시 암호화 없이 운영하고 전송 구간을 강제해요 |
| [0009](0009-deploy-through-cloudflare-tunnel.md) | 배포 경로 | 인바운드 포트 없이 Cloudflare Tunnel로 배포하고 서버에 접속해요 |
| [0010](0010-use-managed-database-and-cache.md) | 데이터 계층 위치 | MySQL과 Redis를 RDS·ElastiCache로 분리하고 EC2에는 앱만 둬요 |
| [0011](0011-include-nginx-config-in-deploy-pipeline.md) | nginx 운영 방식 (`Proposed`) | `api.conf`를 저장소에 두고 배포가 nginx까지 다루자는 제안이에요 |
| [0012](0012-extend-access-token-lifetime.md) | Access Token 수명과 상태 경계 | Access Token은 1시간 stateless JWT로, Refresh Session은 Redis로 관리해요 |

## 다른 문서와의 관계

| 문서 | 역할 |
| --- | --- |
| [`../decisions/backend-tech-stack.md`](../decisions/backend-tech-stack.md) | 기술 스택과 선정 근거 |
| [`../decisions/backend-code-convention.md`](../decisions/backend-code-convention.md) | 현재 구현·테스트·리뷰 규칙 |
| `docs/adr/**` | 특정 시점에 내린 결정과 그 이유 |

현재 지켜야 할 규칙은 코드·설정·컨벤션 문서에서 확인해요. ADR은 결정 당시의 맥락과 트레이드오프를 설명해요.

## 참고

- [Michael Nygard, Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
- [MADR ADR Template](https://adr.github.io/madr/decisions/adr-template.html)
