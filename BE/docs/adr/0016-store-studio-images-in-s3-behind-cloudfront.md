# ADR-0016: 시설 이미지를 공유 S3 버킷에 저장하고 CloudFront로 서빙한다

## Status

Accepted (2026-08-24)

## Context

- 시설마다 대표 이미지를 한 장 저장해야 해요. 상세 이미지 여러 장은 필요해질 때 별도 테이블로 추가해요.
- 파일이 서버를 거치면 EC2 한 대에 부담이 몰려요. 앱 컨테이너는 메모리 상한 1400MB로 동작해요.
- **우테코 인프라 규약이 스토리지를 지정해요.** 팀이 버킷을 만들지 않고, 이미지 파일은 공용 버킷 `techcourse-project-2026`에 **팀별 폴더**를 만들어 써요. 빌드 산출물용 `techcourse-project-2026-artifacts`와는 용도가 달라요.
- **CloudFront용 OAC와 `ec2-project` IAM 역할도 규약이 제공해요.** 역할의 용도에 "S3, CloudWatch, CloudFront를 EC2에서 접근하고 싶은 경우"가 명시돼 있어요. `ec2-project`는 전 팀이 공유하므로 팀이 정책을 덧붙이지 않아요.
- **S3 단독으로는 우리 도메인에 HTTPS를 붙일 수 없어요.** S3가 제시하는 인증서는 `*.s3.{region}.amazonaws.com`용이고, 정적 웹사이트 호스팅 엔드포인트는 HTTP만 지원해요.
- 버킷을 공개하지 않으려면 앞단이 필요해요. 조회마다 presigned GET을 발급하면 서명 비용이 들고 캐시가 되지 않아요.
- KMS 권한이 없어요([ADR-0008](0008-run-rds-without-storage-encryption.md)). 객체 암호화는 버킷 기본값인 SSE-S3를 그대로 써요.

## Decision

시설 이미지는 `techcourse-project-2026` 버킷의 `classitda/` 폴더에 저장하고, 조회는 CloudFront 배포를 통해 서빙해요. 버킷은 규약이 제공하는 OAC로 접근하고 공개하지 않아요.

CloudFront의 **Origin path를 `/classitda`로 두어 팀 폴더를 URL에서 감춰요.** 공개 경로에 팀 폴더가 드러나지 않고, 다른 팀 폴더로는 이 배포를 통해 접근할 수 없어요.

업로드는 presigned URL로 클라이언트가 S3에 직접 올려요. 서버는 URL을 발급할 때 권한을 검증하고, Content-Type과 확장자를 서명 조건으로 제한해요.

**커스텀 도메인은 동작을 확인한 뒤에 붙여요.** 우선 CloudFront 기본 도메인으로 서빙하고, 이미지 base URL을 설정값으로 분리해 둬요.

## Alternatives

- **Cloudflare R2 + 커스텀 도메인** — 엣지가 하나로 유지되고 egress가 영구 무료지만, 데이터 계층이 이미 AWS에 있고 액세스 키도 확보돼 AWS로 모으는 쪽을 택했어요.
- **S3를 Cloudflare가 직접 프록시** — S3가 제시하는 인증서가 도메인과 맞지 않아 SSL 모드를 낮춰야 하고, Cloudflare는 AWS 오리진이 아니라 S3 egress가 과금돼요.
- **처음부터 커스텀 도메인 붙이기** — 검증해야 할 항목이 이미 많아 변수를 줄였어요. objectKey만 저장하므로 나중에 붙이는 비용이 낮아요.

## Consequences

- (+) 커스텀 도메인 HTTPS와 비공개 버킷을 함께 얻을 수 있고, 지금은 기본 도메인으로 바로 서빙돼요.
- (+) S3에서 CloudFront로 나가는 전송이 무료라 조회 비용이 CloudFront 무료 등급 안에 들어와요.
- (+) 버킷과 OAC를 규약이 제공해 만들 것이 CloudFront 배포 하나예요.
- (-) **공유 버킷이라 팀 간 격리를 IAM으로 할 수 없어요.** `ec2-project` 역할이 버킷 전체 권한을 가지면 다른 팀 폴더에도 쓸 수 있어요. **애플리케이션이 서명할 때 `classitda/` 접두사를 강제하는 것이 유일한 방어선이에요.**
- (-) 버킷의 CORS와 버킷 정책을 팀이 바꿀 수 없어요. presigned 업로드가 막히면 우테코에 문의해야 해요.
- (-) **서버가 presigned URL로 실제 무엇이 올라갔는지 확인할 수 없어요.** 서명 조건이 유일한 방어선이에요.
- (-) 엣지 벤더가 둘이 돼요. `api.classitda.com`은 Cloudflare가, 이미지는 CloudFront가 종단해요.
- (=) 커스텀 도메인을 붙일 때는 ACM 인증서를 us-east-1에 발급하고 검증 레코드를 Cloudflare에 추가해야 해요. 그전까지 이미지 URL에 AWS 도메인이 노출돼요.
- (=) [ADR-0006](0006-terminate-tls-at-cloudflare.md)은 `api.classitda.com`에 대한 결정이라 그대로 유효해요.
- 재검토: 커스텀 도메인을 붙일 때. 저장 용량이나 조회량이 무료 등급을 넘길 때. 우테코 과정이 끝나 공유 버킷을 쓸 수 없게 될 때.

## Compliance

- CloudFront 기본 도메인으로 `/{objectKey}`를 요청하면 응답한다.
- S3 버킷 URL로 직접 접근한 요청이 거부된다.
- 만료된 presigned URL로 업로드하면 거부된다.
- 서명 조건과 다른 Content-Type으로 업로드하면 거부된다.
- **`classitda/` 밖의 키로는 presigned URL이 발급되지 않는다.**
