# ADR-0006: DNS와 TLS를 Cloudflare에서 처리한다

## Status

Accepted (2026-08-18)

## Context

- `api.classitda.com`으로 API 서버를 공개해야 한다.
- 도메인을 Cloudflare에서 구매했고 네임서버가 이미 Cloudflare를 가리킨다.
- 애플리케이션은 EC2 `t4g.small` 한 대 위의 Nginx 뒤에서 동작한다. AWS 관리형 종단(ALB, CloudFront)이 앞에 없다.
- ACM 공개 인증서는 프라이빗 키를 내보낼 수 없어 **EC2의 Nginx에 설치할 수 없다.** ACM을 쓰려면 ALB나 CloudFront가 필요하다.

## Decision

DNS와 TLS 종단을 Cloudflare에 둔다. 브라우저↔Cloudflare 구간은 Universal SSL, Cloudflare↔EC2 구간은 Cloudflare Origin Certificate로 암호화하고 SSL/TLS 모드를 `Full (strict)`로 설정한다. 인증서를 EC2에 직접 설치해야 하는 구조에서, 추가 비용과 갱신 부담 없이 두 구간을 모두 암호화할 수 있는 선택지가 이것이었다.

## Alternatives

- **Route53 + ACM + ALB** — ACM 인증서를 EC2에 설치할 수 없어 ALB가 강제되고, 인프라 비용이 인스턴스 값만큼 늘어난다.
- **Route53 + Let's Encrypt(origin 직접 설치)** — 90일 갱신 자동화가 필요하다. Cloudflare 프록시 뒤에서는 HTTP-01 챌린지가 통하지 않아 DNS-01로 가야 하고, 프록시를 끄면 origin IP가 노출된다.
- **Cloudflare `Flexible` 모드** — Cloudflare↔origin 구간이 평문이라 검토 단계에서 제외했다.

## Consequences

- (+) 추가 비용 없이 TLS, DDoS 완화, origin IP 은닉을 함께 얻는다.
- (+) Origin Certificate 유효기간이 15년이라 인증서 갱신 작업이 사실상 없다.
- (-) **Cloudflare 장애가 곧 서비스 장애다.** AWS 밖에 단일 실패 지점이 하나 생긴다.
- (-) 무료 플랜에는 로드밸런싱이 없다. 인스턴스를 늘리려면 이 결정을 다시 봐야 한다.
- (-) 클라이언트 IP가 `$remote_addr`가 아니라 `CF-Connecting-IP` 헤더로 온다. IP 기반 로직을 넣을 때 주의가 필요하다.
- (=) **우테코 계정은 보안 그룹을 만들거나 수정할 수 없어, 80·443을 Cloudflare IP 대역으로 제한하는 정공법을 쓸 수 없다.** 대신 Authenticated Origin Pulls를 켜고 Nginx가 `ssl_verify_client on`으로 Cloudflare의 클라이언트 인증서를 검증한다.
- (-) 그 인증서는 Cloudflare 전체가 공유하는 것이라, Nginx가 검증할 수 있는 것은 "Cloudflare를 거쳤다"까지이고 "우리 계정을 거쳤다"까지는 아니다. 다른 Cloudflare 사용자가 origin IP로 프록시를 걸면 통과한다. 전용 인증서를 쓰는 per-hostname 방식은 API 설정이 필요해 채택하지 않았다.
- (=) `Host` 헤더가 `api.classitda.com`이 아닌 요청은 Nginx의 `default_server`가 444로 끊는다.
- 재검토: 인스턴스를 2대 이상으로 늘릴 때, 또는 AWS 크레딧이 확보돼 ALB 비용 부담이 사라질 때.

## Compliance

배포 후 다음 두 가지를 확인한다.

- `https://api.classitda.com` 요청이 인증서 오류 없이 응답한다.
- `Host: api.classitda.com`을 붙여 Elastic IP로 직접 보낸 요청이 `400 No required SSL certificate was sent`로 거부된다. (EC2 내부에서 자기 Elastic IP를 호출하면 헤어핀 라우팅 때문에 결과가 무의미하므로 외부에서 확인한다.)
