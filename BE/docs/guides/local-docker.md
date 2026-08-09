# Docker로 로컬 서버 실행하기

Docker 명령어를 몰라도 저장소에 포함된 스크립트로 클래스잇다 백엔드 서버와 MySQL, Redis를 실행하고 중지할 수 있습니다.

## 사전 준비

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)을 설치합니다.
- Docker Desktop을 실행합니다.
- 터미널에서 이 저장소의 `BE` 디렉터리로 이동합니다.

## 실행

```bash
./docker/scripts/start.sh
```

스크립트가 서버 이미지를 빌드하고 다음 컨테이너를 실행합니다.

| 컨테이너 | 주소 | 용도 |
| --- | --- | --- |
| `be-app` | `http://localhost:8080` | Spring Boot 서버 |
| `be-mysql` | `localhost:3306` | 로컬 MySQL 8.4 |
| `be-redis` | `localhost:6379` | 로컬 Redis 7.4 |

MySQL과 Redis가 정상 상태가 된 후 서버가 자동으로 시작됩니다. 로컬 프로필에서는 서버를 시작할 때 기존 테이블을 삭제하고 `schema.sql`로 다시 생성합니다.

## 중지

```bash
./docker/scripts/stop.sh
```

서버와 MySQL, Redis 컨테이너를 삭제하지 않고 중지합니다. 전용 네트워크와 데이터 볼륨도 유지됩니다.

## 문제가 생겼을 때

### Docker가 실행 중이 아니라는 메시지가 나오는 경우

Docker Desktop을 실행한 뒤 `./docker/scripts/start.sh`를 다시 실행합니다.

### 컨테이너 상태 확인

```bash
docker compose -f docker/compose.local.yml ps
```

### 서버 로그 확인

```bash
docker compose -f docker/compose.local.yml logs -f app
```

### MySQL 로그 확인

```bash
docker compose -f docker/compose.local.yml logs -f mysql
```

### Redis 로그 확인

```bash
docker compose -f docker/compose.local.yml logs -f redis
```

로그 화면은 `Ctrl+C`로 종료할 수 있으며, 컨테이너는 계속 실행됩니다.
