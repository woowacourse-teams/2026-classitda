# ADR-0009: 배포 경로로 Cloudflare Tunnel을 사용한다

## Status

Accepted (2026-08-19)

## Context

- 우아한테크코스 AWS 계정은 **IAM 액세스 키 발급이 차단**되어 있다. AWS API를 호출할 수단이 없어 SSM Send Command, ECR, Terraform 경로가 모두 막힌다.
- EC2로의 SSH는 사내망에서만 열려 있고, 보안 그룹을 만들거나 수정할 권한이 없어 접근 범위를 조정할 수 없다.
- 저장소가 public이다.
- 운영 인스턴스는 `t4g.small`(2 vCPU 버스터블 / 2 GiB)로, 상주 프로세스를 늘릴 여유가 크지 않다.
- 인프라 구축과 별개로, 팀원이 외부망에서 서버에 접속할 수단도 필요하다.

## Decision

EC2에 `cloudflared` 터널을 세워 `ssh.classitda.com`으로 SSH를 노출하고, GitHub Actions는 Cloudflare Access Service Token으로 그 터널을 통과해 배포 명령을 실행한다. 터널은 EC2가 아웃바운드로 연결을 유지하므로 인바운드 포트를 열지 않으며, 사람의 접속 경로와 배포 경로를 하나로 통합한다.

## Alternatives

- **SSM Send Command** — 우테코가 `ec2-project` IAM 역할로 상정한 경로지만, GitHub Actions가 AWS API를 호출하려면 액세스 키가 필요해 쓸 수 없다.
- **self-hosted runner** — AWS 권한 없이 동작하지만, **public 저장소에서 포크 PR이 러너 위에서 임의 코드를 실행할 수 있다.** `push: main`으로만 제한하면 안전하나, 그 규칙을 영구히 지켜야 하는 부담이 남는다. 2 GiB 인스턴스에 상주 프로세스가 하나 늘어나는 것도 부담이다.
- **CodeDeploy + CodePipeline** — 서비스 역할이 제공되어 자격증명 없이 가능하지만 설정량이 크고 CodeBuild 비용이 발생한다. 예산 여유가 크지 않다.
- **이미지를 `docker save`로 SSH 전송** — 레지스트리를 쓰지 않아도 되지만 배포마다 수백 MB를 전송하고 레이어 재사용이 없다.

## Consequences

- (+) 인바운드 포트를 하나도 열지 않는다. 보안 그룹을 수정할 수 없는 제약과 잘 맞는다.
- (+) 외부망에서도 서버에 접속할 수 있다. 배포와 운영 접근이 같은 경로를 쓴다.
- (+) Cloudflare Access 정책으로 사람(이메일)과 자동화(Service Token)를 분리해 통제한다.
- (-) **Cloudflare 장애가 배포와 서버 접근을 동시에 막는다.** 이미 TLS 종단([ADR-0006](0006-terminate-tls-at-cloudflare.md))도 Cloudflare에 의존하므로 단일 실패 지점의 영향 범위가 넓어졌다.
- (-) Access Service Token이 유출되면 서버 접근 경로가 열린다. 배포 전용 SSH 키와 함께 두 개의 자격증명을 관리해야 한다.
- (=) GitHub Actions 러너에 `cloudflared`(amd64)를, EC2에 `cloudflared`(arm64)를 각각 설치해야 한다.
- (=) Zero Trust에서 One-time PIN 로그인 방법을 켜지 않으면 팀원이 이메일 인증으로 접속할 수 없다. Cloudflare 계정 로그인은 계정 멤버로 제한된다.
- 재검토: IAM 액세스 키가 확보되면 SSM Session Manager로 전환을 검토한다. 그 경우 Cloudflare 의존이 TLS 종단만 남는다.

## Compliance

배포 워크플로는 `.env`를 갱신하기 전에 터널 SSH 연결을 먼저 확인한다. 인증이 깨진 경우 서버 상태를 건드리지 않고 실패한다.
